package com.skilllens.service.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilllens.domain.model.LearningResource;
import com.skilllens.domain.model.LearningResource.ResourceType;
import com.skilllens.service.security.CredentialManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * YouTube Data API v3 Client.
 * Features quota conservation with local cache, query normalization,
 * and graceful fallback when unconfigured.
 */
@Service
public class YouTubeClient {

    private static final Logger log = LoggerFactory.getLogger(YouTubeClient.class);
    private static final String YOUTUBE_SEARCH_ENDPOINT = "https://www.googleapis.com/youtube/v3/search";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CredentialManager credentialManager;

    // Cache with query key to avoid consuming YouTube search quota repeatedly
    private final Map<String, List<LearningResource>> searchCache = new ConcurrentHashMap<>();

    public YouTubeClient(CredentialManager credentialManager, ObjectMapper objectMapper) {
        this.credentialManager = credentialManager;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Searches YouTube for high-quality technical playlists and videos for a given skill.
     * Returns cached results if available, or queries YouTube API if an API key is present.
     * Returns an empty list gracefully if key is absent or quota is exceeded.
     */
    public List<LearningResource> searchResourcesForSkill(String skill, String role, int roadmapMonth) {
        if (skill == null || skill.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String cacheKey = (skill + ":" + (role != null ? role : "")).toLowerCase(Locale.ROOT).trim();
        if (searchCache.containsKey(cacheKey)) {
            return searchCache.get(cacheKey);
        }

        Optional<String> keyOpt = credentialManager.getApiKey(CredentialManager.KEY_YOUTUBE);
        if (keyOpt.isEmpty() || keyOpt.get().trim().isEmpty()) {
            log.debug("YouTube API key not configured. Gracefully skipping YouTube video search for skill: {}", skill);
            return Collections.emptyList();
        }

        String apiKey = keyOpt.get().trim();
        String searchQuery = skill + " tutorial full course architecture";

        try {
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            String url = String.format("%s?part=snippet&type=video,playlist&maxResults=3&relevanceLanguage=en&q=%s&key=%s",
                    YOUTUBE_SEARCH_ENDPOINT, encodedQuery, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                List<LearningResource> results = parseYouTubeResponse(response.body(), skill, roadmapMonth);
                searchCache.put(cacheKey, results);
                return results;
            } else {
                log.warn("YouTube API returned status {}: {}. Falling back to offline resources.", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch YouTube resources for {}: {}", skill, e.getMessage());
        }

        return Collections.emptyList();
    }

    private List<LearningResource> parseYouTubeResponse(String json, String skill, int roadmapMonth) {
        List<LearningResource> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("items");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    JsonNode idNode = item.path("id");
                    String kind = idNode.path("kind").asText();
                    String videoId = idNode.path("videoId").asText();
                    String playlistId = idNode.path("playlistId").asText();

                    String url;
                    ResourceType type;
                    if ("youtube#playlist".equalsIgnoreCase(kind) || !playlistId.isEmpty()) {
                        url = "https://www.youtube.com/playlist?list=" + playlistId;
                        type = ResourceType.PLAYLIST;
                    } else {
                        url = "https://www.youtube.com/watch?v=" + videoId;
                        type = ResourceType.VIDEO;
                    }

                    JsonNode snippet = item.path("snippet");
                    String title = snippet.path("title").asText();
                    String description = snippet.path("description").asText();
                    String channelTitle = snippet.path("channelTitle").asText();

                    String formattedDesc = description != null && !description.isEmpty()
                            ? description
                            : "Comprehensive video guide by " + channelTitle + " on " + skill;

                    list.add(new LearningResource(
                            "yt-" + (videoId.isEmpty() ? playlistId : videoId),
                            title,
                            formattedDesc,
                            url,
                            "YOUTUBE",
                            type,
                            skill,
                            "INTERMEDIATE",
                            4.7,
                            "Video",
                            roadmapMonth,
                            false
                    ));
                }
            }
        } catch (IOException e) {
            log.warn("Failed to parse YouTube JSON: {}", e.getMessage());
        }
        return list;
    }
}
