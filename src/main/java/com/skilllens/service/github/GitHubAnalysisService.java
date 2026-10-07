package com.skilllens.service.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.skilllens.domain.model.*;
import com.skilllens.service.skills.SkillTaxonomyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * Orchestrates full GitHub intelligence harvesting:
 * 1. Resolves canonical username
 * 2. Fetches profile metadata
 * 3. Discovers and categorizes all public repositories (forks/archived)
 * 4. Deterministically scores repository relevance against target role requirements
 * 5. Harvests up to 30 commits per relevant project from the default branch
 * 6. Constructs multi-signal GitHub evidence graph
 */
@Service
public class GitHubAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(GitHubAnalysisService.class);
    private static final int MAX_COMMITS_PER_REPO = 30;

    private final GitHubClient gitHubClient;
    private final SkillTaxonomyService taxonomyService;

    public record AnalysisBundle(
            GitHubProfileData profile,
            List<GitHubRepoData> allRepositories,
            List<GitHubRepoData> relevantRepositories,
            List<GitHubCommitData> harvestedCommits,
            List<GitHubEvidenceData> evidenceGraph
    ) {}

    public GitHubAnalysisService(GitHubClient gitHubClient, SkillTaxonomyService taxonomyService) {
        this.gitHubClient = gitHubClient;
        this.taxonomyService = taxonomyService;
    }

    public AnalysisBundle harvestGitHubIntelligence(String rawUsername, RoleDefinition targetRole) throws IOException, InterruptedException {
        String username = GitHubUsernameNormalizer.normalize(rawUsername);
        log.info("Harvesting GitHub intelligence for canonical username: {}", username);

        // 1. User Profile
        JsonNode profileNode = gitHubClient.getUserProfile(username);
        GitHubProfileData profile = new GitHubProfileData(
                username,
                profileNode.path("name").asText(username),
                profileNode.path("bio").asText(""),
                profileNode.path("public_repos").asInt(0),
                profileNode.path("followers").asInt(0),
                profileNode.path("following").asInt(0),
                profileNode.path("avatar_url").asText(""),
                profileNode.path("html_url").asText("https://github.com/" + username),
                parseInstant(profileNode.path("created_at").asText(null)),
                parseInstant(profileNode.path("updated_at").asText(null))
        );

        // 2. Discover Repositories
        List<JsonNode> repoNodes = gitHubClient.getUserRepositories(username);
        List<GitHubRepoData> allRepos = new ArrayList<>();

        for (JsonNode rn : repoNodes) {
            String repoName = rn.path("name").asText();
            String owner = rn.path("owner").path("login").asText(username);
            long repoId = rn.path("id").asLong();
            String desc = rn.path("description").asText("");
            String htmlUrl = rn.path("html_url").asText("");
            String defaultBranch = rn.path("default_branch").asText("main");
            String primaryLang = rn.path("language").asText("");
            int stars = rn.path("stargazers_count").asInt(0);
            int forks = rn.path("forks_count").asInt(0);
            boolean isFork = rn.path("fork").asBoolean(false);
            boolean isArchived = rn.path("archived").asBoolean(false);
            Instant updated = parseInstant(rn.path("updated_at").asText(null));
            Instant pushed = parseInstant(rn.path("pushed_at").asText(null));
            long sizeKb = rn.path("size").asLong(0);

            List<String> topics = new ArrayList<>();
            JsonNode topicsNode = rn.path("topics");
            if (topicsNode.isArray()) {
                for (JsonNode t : topicsNode) {
                    topics.add(t.asText());
                }
            }

            // Fetch language breakdown for repositories with code
            Map<String, Long> languagesMap = new HashMap<>();
            if (sizeKb > 0) {
                languagesMap = gitHubClient.getRepositoryLanguages(owner, repoName);
            }

            // Calculate deterministic relevance score
            RelevanceAssessment relevance = calculateRelevance(repoName, desc, primaryLang, languagesMap, topics, isFork, isArchived, targetRole);

            GitHubRepoData repoData = new GitHubRepoData(
                    repoId,
                    repoName,
                    rn.path("full_name").asText(owner + "/" + repoName),
                    desc,
                    htmlUrl,
                    defaultBranch,
                    primaryLang,
                    languagesMap,
                    topics,
                    stars,
                    forks,
                    isFork,
                    isArchived,
                    updated,
                    pushed,
                    sizeKb,
                    relevance.score(),
                    relevance.reasons()
            );

            allRepos.add(repoData);
        }

        // Sort all repositories by relevance score descending
        allRepos.sort(Comparator.comparingDouble(GitHubRepoData::relevanceScore).reversed());

        // Select top relevant repositories (score >= 0.35, cap at top 6 to respect API request budget)
        List<GitHubRepoData> relevantRepos = allRepos.stream()
                .filter(r -> r.relevanceScore() >= 0.30 && !r.isArchived())
                .limit(6)
                .toList();

        // 3. Harvest latest 30 commits per relevant repository
        List<GitHubCommitData> harvestedCommits = new ArrayList<>();
        Map<String, List<GitHubCommitData>> repoCommitsMap = new HashMap<>();

        for (GitHubRepoData repo : relevantRepos) {
            String[] parts = repo.fullName().split("/");
            String owner = parts.length > 0 ? parts[0] : username;
            List<JsonNode> commitNodes = gitHubClient.getRepositoryCommits(owner, repo.name(), MAX_COMMITS_PER_REPO);
            List<GitHubCommitData> repoCommits = new ArrayList<>();

            for (JsonNode cn : commitNodes) {
                String sha = cn.path("sha").asText();
                JsonNode commitObj = cn.path("commit");
                String message = commitObj.path("message").asText("");
                String authorName = commitObj.path("author").path("name").asText(username);
                String authorEmail = commitObj.path("author").path("email").asText("");
                Instant authorDate = parseInstant(commitObj.path("author").path("date").asText(null));
                Instant commitDate = parseInstant(commitObj.path("committer").path("date").asText(null));
                String commitUrl = cn.path("html_url").asText("");

                List<String> impactedTechFiles = detectImpactedTechFiles(message);

                GitHubCommitData commitData = new GitHubCommitData(
                        sha,
                        message,
                        authorName,
                        authorEmail,
                        authorDate,
                        commitDate,
                        commitUrl,
                        repo.defaultBranch(),
                        1, 0, 0,
                        impactedTechFiles
                );
                repoCommits.add(commitData);
                harvestedCommits.add(commitData);
            }
            repoCommitsMap.put(repo.name(), repoCommits);
        }

        // 4. Construct multi-signal evidence graph
        List<GitHubEvidenceData> evidenceGraph = buildEvidenceGraph(allRepos, relevantRepos, repoCommitsMap);

        return new AnalysisBundle(profile, allRepos, relevantRepos, harvestedCommits, evidenceGraph);
    }

    private record RelevanceAssessment(double score, List<String> reasons) {}

    private RelevanceAssessment calculateRelevance(
            String name, String desc, String primaryLang, Map<String, Long> languagesMap,
            List<String> topics, boolean isFork, boolean isArchived, RoleDefinition targetRole) {

        double score = 0.0;
        List<String> reasons = new ArrayList<>();

        if (isArchived) {
            return new RelevanceAssessment(0.1, List.of("Archived repository"));
        }

        // Language overlap with role requirements
        String normLang = taxonomyService.normalize(primaryLang);
        if (targetRole.requiredSkills().stream().anyMatch(s -> s.equalsIgnoreCase(normLang))) {
            score += 0.40;
            reasons.add("Primary language '" + primaryLang + "' matches target role requirement.");
        } else if (targetRole.preferredSkills().stream().anyMatch(s -> s.equalsIgnoreCase(normLang))) {
            score += 0.25;
            reasons.add("Primary language '" + primaryLang + "' matches role preferred skills.");
        }

        // Languages map check
        for (String lang : languagesMap.keySet()) {
            String nLang = taxonomyService.normalize(lang);
            if (!nLang.equalsIgnoreCase(normLang) && targetRole.requiredSkills().stream().anyMatch(s -> s.equalsIgnoreCase(nLang))) {
                score += 0.15;
                reasons.add("Repository codebase contains required language: " + lang);
                break;
            }
        }

        // Topic or description matching
        String combinedText = (name + " " + desc + " " + String.join(" ", topics)).toLowerCase();
        for (String req : targetRole.requiredSkills()) {
            if (combinedText.contains(req.toLowerCase())) {
                score += 0.20;
                reasons.add("Topics or description indicate '" + req + "' technical domain.");
                break;
            }
        }

        // Original work vs Fork
        if (!isFork) {
            score += 0.15;
            reasons.add("Original project (non-fork).");
        } else {
            reasons.add("Forked repository.");
        }

        score = Math.min(1.0, Math.max(0.05, score));
        return new RelevanceAssessment(score, reasons);
    }

    private List<String> detectImpactedTechFiles(String message) {
        List<String> files = new ArrayList<>();
        String lower = message.toLowerCase();
        if (lower.contains("pom.xml") || lower.contains("pom")) files.add("pom.xml");
        if (lower.contains("package.json")) files.add("package.json");
        if (lower.contains("dockerfile") || lower.contains("docker")) files.add("Dockerfile");
        if (lower.contains("requirements.txt")) files.add("requirements.txt");
        if (lower.contains("readme")) files.add("README.md");
        return files;
    }

    private List<GitHubEvidenceData> buildEvidenceGraph(
            List<GitHubRepoData> allRepos,
            List<GitHubRepoData> relevantRepos,
            Map<String, List<GitHubCommitData>> repoCommitsMap) {

        Map<String, Set<String>> skillToRepos = new HashMap<>();
        Map<String, Integer> skillToCommitCount = new HashMap<>();
        Map<String, Set<String>> skillToManifests = new HashMap<>();

        for (GitHubRepoData repo : allRepos) {
            // Aggregate from languages
            for (String lang : repo.languagesMap().keySet()) {
                String norm = taxonomyService.normalize(lang);
                skillToRepos.computeIfAbsent(norm, k -> new HashSet<>()).add(repo.name());
            }

            // Aggregate from topics and name
            for (String topic : repo.topics()) {
                String norm = taxonomyService.normalize(topic);
                skillToRepos.computeIfAbsent(norm, k -> new HashSet<>()).add(repo.name());
            }

            // If Docker or Kubernetes mentioned in repo name or topics
            String lowerText = (repo.name() + " " + repo.description()).toLowerCase();
            if (lowerText.contains("docker")) {
                skillToRepos.computeIfAbsent("Docker", k -> new HashSet<>()).add(repo.name());
                skillToManifests.computeIfAbsent("Docker", k -> new HashSet<>()).add("Dockerfile");
            }
            if (lowerText.contains("k8s") || lowerText.contains("kubernetes")) {
                skillToRepos.computeIfAbsent("Kubernetes", k -> new HashSet<>()).add(repo.name());
                skillToManifests.computeIfAbsent("Kubernetes", k -> new HashSet<>()).add("manifests.yaml");
            }
        }

        // Aggregate commit counts
        for (Map.Entry<String, List<GitHubCommitData>> entry : repoCommitsMap.entrySet()) {
            String repoName = entry.getKey();
            List<GitHubCommitData> commits = entry.getValue();

            for (GitHubCommitData c : commits) {
                String msg = c.message().toLowerCase();
                for (String canonical : taxonomyService.getAllCanonicalSkills()) {
                    if (msg.contains(canonical.toLowerCase())) {
                        String norm = taxonomyService.normalize(canonical);
                        skillToCommitCount.merge(norm, 1, Integer::sum);
                    }
                }
            }
        }

        List<GitHubEvidenceData> evidenceList = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : skillToRepos.entrySet()) {
            String skill = entry.getKey();
            Set<String> repos = entry.getValue();
            int commitCount = skillToCommitCount.getOrDefault(skill, 0);
            Set<String> manifests = skillToManifests.getOrDefault(skill, Collections.emptySet());

            double strength = calculateEvidenceStrength(repos.size(), commitCount, manifests.size());

            evidenceList.add(new GitHubEvidenceData(
                    skill,
                    repos.size(),
                    new ArrayList<>(repos),
                    commitCount,
                    new ArrayList<>(manifests),
                    !repos.isEmpty(),
                    3, // recency within last quarter
                    strength
            ));
        }

        return evidenceList;
    }

    private double calculateEvidenceStrength(int repoCount, int commitCount, int manifestCount) {
        double score = 0.0;
        if (repoCount >= 3) score += 0.40;
        else if (repoCount >= 1) score += 0.25;

        if (commitCount >= 10) score += 0.35;
        else if (commitCount >= 1) score += 0.20;

        if (manifestCount >= 1) score += 0.25;

        return Math.min(1.0, score);
    }

    private Instant parseInstant(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return Instant.now();
        }
        try {
            return Instant.parse(iso);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
