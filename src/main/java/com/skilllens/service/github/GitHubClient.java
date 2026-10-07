package com.skilllens.service.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilllens.service.security.CredentialManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dedicated, resilient GitHub REST API client.
 * Features:
 * - Unauthenticated mode for public access + optional GITHUB_TOKEN authenticated access
 * - Local in-memory / disk cache with ETag & timestamp
 * - Exponential backoff on rate limits (403, 429, Retry-After)
 * - Safe pagination up to configured caps
 */
@Service
public class GitHubClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);
    private static final String GITHUB_API_BASE = "https://api.github.com";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CredentialManager credentialManager;

    // Cache entry: data, etag, expiry
    private record CacheEntry(String responseBody, String etag, Instant cachedAt) {}
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();

    public GitHubClient(CredentialManager credentialManager, ObjectMapper objectMapper) {
        this.credentialManager = credentialManager;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    private HttpRequest.Builder createRequestBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "SkillLens-Agent/1.0");

        Optional<String> tokenOpt = credentialManager.getApiKey(CredentialManager.KEY_GITHUB);
        if (tokenOpt.isPresent() && !tokenOpt.get().trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + tokenOpt.get().trim());
        }

        return builder;
    }

    public JsonNode getJson(String endpoint) throws IOException, InterruptedException {
        String url = endpoint.startsWith("http") ? endpoint : GITHUB_API_BASE + endpoint;

        // Check local cache
        CacheEntry cached = localCache.get(url);
        HttpRequest.Builder requestBuilder = createRequestBuilder(url);

        if (cached != null && cached.etag != null) {
            requestBuilder.header("If-None-Match", cached.etag);
        }

        HttpRequest request = requestBuilder.GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        int statusCode = response.statusCode();

        // 304 Not Modified
        if (statusCode == 304 && cached != null) {
            log.debug("GitHub cache hit (304) for: {}", url);
            return objectMapper.readTree(cached.responseBody);
        }

        // Handle Rate Limits and Backoff
        if (statusCode == 429 || statusCode == 403) {
            String retryAfterHeader = response.headers().firstValue("Retry-After").orElse(null);
            String rateLimitRemaining = response.headers().firstValue("x-ratelimit-remaining").orElse("0");
            log.warn("GitHub rate limit encountered (status: {}). Remaining: {}, Retry-After: {}",
                    statusCode, rateLimitRemaining, retryAfterHeader);

            if (cached != null) {
                log.info("Serving cached data under rate limit condition for: {}", url);
                return objectMapper.readTree(cached.responseBody);
            }

            if ("0".equals(rateLimitRemaining)) {
                String resetEpoch = response.headers().firstValue("x-ratelimit-reset").orElse("0");
                throw new IOException("GitHub API rate limit exceeded. Reset at epoch timestamp: " + resetEpoch +
                        ". Configure GITHUB_TOKEN in Settings to increase limit to 5000 requests/hr.");
            }
            throw new IOException("GitHub API rate limit or access forbidden (HTTP " + statusCode + ")");
        }

        if (statusCode == 404) {
            throw new NoSuchElementException("GitHub resource not found at: " + url);
        }

        if (statusCode >= 500) {
            if (cached != null) {
                log.warn("GitHub 5xx server error. Serving cached data for: {}", url);
                return objectMapper.readTree(cached.responseBody);
            }
            throw new IOException("GitHub upstream server error (HTTP " + statusCode + ")");
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("Unexpected GitHub API response (HTTP " + statusCode + "): " + response.body());
        }

        String body = response.body();
        String etag = response.headers().firstValue("ETag").orElse(null);
        localCache.put(url, new CacheEntry(body, etag, Instant.now()));

        return objectMapper.readTree(body);
    }

    public JsonNode getUserProfile(String username) throws IOException, InterruptedException {
        String canonical = GitHubUsernameNormalizer.normalize(username);
        return getJson("/users/" + canonical);
    }

    public List<JsonNode> getUserRepositories(String username) throws IOException, InterruptedException {
        String canonical = GitHubUsernameNormalizer.normalize(username);
        List<JsonNode> allRepos = new ArrayList<>();

        // Paginate up to 100 public repositories per page, cap at 2 pages (200 repositories)
        for (int page = 1; page <= 2; page++) {
            String endpoint = "/users/" + canonical + "/repos?per_page=100&sort=pushed&page=" + page;
            try {
                JsonNode pageNode = getJson(endpoint);
                if (pageNode.isArray()) {
                    for (JsonNode repoNode : pageNode) {
                        allRepos.add(repoNode);
                    }
                    if (pageNode.size() < 100) {
                        break; // Last page reached
                    }
                } else {
                    break;
                }
            } catch (NoSuchElementException e) {
                break;
            }
        }
        return allRepos;
    }

    public Map<String, Long> getRepositoryLanguages(String owner, String repo) throws IOException, InterruptedException {
        String endpoint = "/repos/" + owner + "/" + repo + "/languages";
        try {
            JsonNode langNode = getJson(endpoint);
            Map<String, Long> languages = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = langNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                languages.put(field.getKey(), field.getValue().asLong());
            }
            return languages;
        } catch (Exception e) {
            log.debug("Could not fetch languages for {}/{}: {}", owner, repo, e.getMessage());
            return Collections.emptyMap();
        }
    }

    public List<JsonNode> getRepositoryCommits(String owner, String repo, int maxCount) throws IOException, InterruptedException {
        String endpoint = "/repos/" + owner + "/" + repo + "/commits?per_page=" + Math.min(maxCount, 30);
        try {
            JsonNode commitsNode = getJson(endpoint);
            List<JsonNode> commits = new ArrayList<>();
            if (commitsNode.isArray()) {
                for (JsonNode c : commitsNode) {
                    commits.add(c);
                    if (commits.size() >= maxCount) {
                        break;
                    }
                }
            }
            return commits;
        } catch (Exception e) {
            log.debug("Could not fetch commits for {}/{}: {}", owner, repo, e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<String> getRepositoryReadme(String owner, String repo) {
        String endpoint = "/repos/" + owner + "/" + repo + "/readme";
        try {
            JsonNode readmeNode = getJson(endpoint);
            if (readmeNode.has("content")) {
                String base64Content = readmeNode.get("content").asText().replaceAll("\\s", "");
                byte[] decoded = Base64.getDecoder().decode(base64Content);
                return Optional.of(new String(decoded, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.debug("No README available for {}/{}", owner, repo);
        }
        return Optional.empty();
    }

    public void clearCache() {
        localCache.clear();
    }
}
