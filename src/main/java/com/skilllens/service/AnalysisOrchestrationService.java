package com.skilllens.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilllens.domain.model.*;
import com.skilllens.domain.repository.CandidateDocumentRepository;
import com.skilllens.domain.repository.CandidateProfileRepository;
import com.skilllens.domain.repository.CandidateRepository;
import com.skilllens.service.document.DocumentParserService;
import com.skilllens.service.github.GitHubAnalysisService;
import com.skilllens.service.github.GitHubUsernameNormalizer;
import com.skilllens.service.rag.EmbeddingService;
import com.skilllens.service.rag.LocalVectorStore;
import com.skilllens.service.resources.LearningResourceService;
import com.skilllens.service.roadmap.CareerRoadmapService;
import com.skilllens.service.skills.RoleCatalogService;
import com.skilllens.service.skills.SkillExtractionService;
import com.skilllens.service.verification.RoleFitScoringService;
import com.skilllens.service.verification.SkillVerificationService;
import com.skilllens.service.verification.VerificationInsightsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * Central Analysis Orchestration Service.
 * Unifies document ingestion, resume skill extraction, GitHub intelligence,
 * 5-tier verification, deterministic scoring, career roadmapping,
 * learning resource ranking, and local RAG indexing into a single workflow.
 */
@Service
public class AnalysisOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisOrchestrationService.class);

    private final DocumentParserService documentParserService;
    private final SkillExtractionService skillExtractionService;
    private final RoleCatalogService roleCatalogService;
    private final GitHubAnalysisService gitHubAnalysisService;
    private final SkillVerificationService verificationService;
    private final RoleFitScoringService scoringService;
    private final VerificationInsightsService insightsService;
    private final CareerRoadmapService roadmapService;
    private final LearningResourceService resourceService;
    private final LocalVectorStore vectorStore;
    private final EmbeddingService embeddingService;
    private final CandidateRepository candidateRepository;
    private final CandidateDocumentRepository documentRepository;
    private final CandidateProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    public AnalysisOrchestrationService(
            DocumentParserService documentParserService,
            SkillExtractionService skillExtractionService,
            RoleCatalogService roleCatalogService,
            GitHubAnalysisService gitHubAnalysisService,
            SkillVerificationService verificationService,
            RoleFitScoringService scoringService,
            VerificationInsightsService insightsService,
            CareerRoadmapService roadmapService,
            LearningResourceService resourceService,
            LocalVectorStore vectorStore,
            EmbeddingService embeddingService,
            CandidateRepository candidateRepository,
            CandidateDocumentRepository documentRepository,
            CandidateProfileRepository profileRepository,
            ObjectMapper objectMapper) {
        this.documentParserService = documentParserService;
        this.skillExtractionService = skillExtractionService;
        this.roleCatalogService = roleCatalogService;
        this.gitHubAnalysisService = gitHubAnalysisService;
        this.verificationService = verificationService;
        this.scoringService = scoringService;
        this.insightsService = insightsService;
        this.roadmapService = roadmapService;
        this.resourceService = resourceService;
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
        this.candidateRepository = candidateRepository;
        this.documentRepository = documentRepository;
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes the end-to-end SkillLens evidence verification workflow.
     */
    @Transactional
    public AnalysisResult analyze(File resumeFile,
                                  String rawPastedText,
                                  String rawGithubInput,
                                  String targetRoleName,
                                  String customJobDescription) {

        String analysisId = UUID.randomUUID().toString();
        log.info("Starting candidate analysis workflow [id={}] for role: {}", analysisId, targetRoleName);

        // 1. Ingest and extract resume text
        DocumentParserService.ParseResult docInfo;
        String fileName = "pasted-resume.txt";
        String fileType = "TXT";
        long fileSize = 0;

        if (resumeFile != null && resumeFile.exists()) {
            fileName = resumeFile.getName();
            fileSize = resumeFile.length();
            try {
                docInfo = documentParserService.parseFile(resumeFile.toPath());
                fileType = docInfo.documentType();
            } catch (IOException e) {
                log.warn("Failed to parse document {}: {}. Falling back to plain text reading.", fileName, e.getMessage());
                docInfo = new DocumentParserService.ParseResult(rawPastedText != null ? rawPastedText : "", "TXT", 0, false, fileName);
            }
        } else {
            docInfo = new DocumentParserService.ParseResult(rawPastedText != null ? rawPastedText : "", "TXT", 0, false, "pasted-resume.txt");
        }

        String resumeText = docInfo.text();

        // 2. Resolve or create Candidate
        String normalizedGithub = rawGithubInput != null && !rawGithubInput.trim().isEmpty()
                ? GitHubUsernameNormalizer.normalize(rawGithubInput)
                : null;

        String extractedName = extractNameFromText(resumeText);
        String extractedEmail = extractEmailFromText(resumeText);

        Candidate candidate = new Candidate(extractedName, extractedEmail, normalizedGithub);
        candidate = candidateRepository.save(candidate);

        // Save CandidateDocument
        CandidateDocument document = new CandidateDocument(
                candidate.getId(),
                fileType,
                resumeText,
                fileName,
                fileSize
        );
        document = documentRepository.save(document);

        // 3. Extract skills from resume
        List<SkillClaim> extractedSkills = skillExtractionService.extractSkills(resumeText);
        log.info("Extracted {} skill claims from candidate resume", extractedSkills.size());

        // Save CandidateProfile
        CandidateProfile profile = new CandidateProfile(candidate.getId());
        profile.setFullName(extractedName);
        profile.setEmail(extractedEmail);
        profile.setGithubUsername(normalizedGithub);
        try {
            profile.setSkillsJson(objectMapper.writeValueAsString(extractedSkills));
        } catch (Exception e) {
            log.error("Failed to serialize extracted skills: {}", e.getMessage());
        }
        profile = profileRepository.save(profile);

        // 4. Resolve Target Role & requirements
        RoleDefinition targetRole = roleCatalogService.resolveRole(targetRoleName, customJobDescription);

        // 5. Harvest GitHub Intelligence
        GitHubProfileData gitHubProfile = null;
        List<GitHubRepoData> allRepos = new ArrayList<>();
        List<GitHubRepoData> relevantRepos = new ArrayList<>();
        List<GitHubCommitData> commits = new ArrayList<>();
        List<GitHubEvidenceData> evidenceGraph = new ArrayList<>();

        if (normalizedGithub != null && !normalizedGithub.isEmpty() && GitHubUsernameNormalizer.isValidUsername(normalizedGithub)) {
            try {
                GitHubAnalysisService.AnalysisBundle bundle = gitHubAnalysisService.harvestGitHubIntelligence(normalizedGithub, targetRole);
                gitHubProfile = bundle.profile();
                allRepos = bundle.allRepositories();
                relevantRepos = bundle.relevantRepositories();
                commits = bundle.harvestedCommits();
                evidenceGraph = bundle.evidenceGraph();
                log.info("GitHub intelligence harvested: {} repos, {} commits, {} evidence nodes",
                        allRepos.size(), commits.size(), evidenceGraph.size());
            } catch (Exception e) {
                log.warn("GitHub harvesting encountered error for user {}: {}. Proceeding with empty GitHub evidence.",
                        normalizedGithub, e.getMessage());
            }
        } else {
            log.info("No GitHub username provided or invalid format. Proceeding with unverified GitHub status.");
        }

        // 6. Multi-Signal Evidence Verification
        List<SkillVerificationRecord> verificationMatrix = verificationService.evaluateSkills(extractedSkills, evidenceGraph, targetRole);

        List<SkillVerificationRecord> skillGaps = verificationMatrix.stream()
                .filter(r -> r.status() == VerificationStatus.MISSING
                        || r.status() == VerificationStatus.CLAIMED_UNVERIFIED
                        || r.status() == VerificationStatus.WEAK_EVIDENCE)
                .toList();

        // Calculate Project Relevance Aggregate
        double projectRelevance = relevantRepos.isEmpty() ? 0.30 :
                relevantRepos.stream().mapToDouble(GitHubRepoData::relevanceScore).average().orElse(0.5);

        // Deterministic Role Fit Calculation
        RoleFitResult roleFit = scoringService.calculateRoleFit(targetRole, verificationMatrix, projectRelevance);

        // Generate Actionable Verification Insights
        List<VerificationInsight> insights = insightsService.generateInsights(verificationMatrix, relevantRepos, targetRole);

        // 7. Generate 4-Month Actionable Career Roadmap
        CareerRoadmap roadmapEntity = roadmapService.generateRoadmap(candidate.getId(), targetRole.name(), roleFit);
        List<RoadmapMonth> roadmapMonths = roadmapService.deserializeMonths(roadmapEntity);

        // 8. Recommend Learning Resources
        List<LearningResource> learningResources = resourceService.getResourcesForGaps(roleFit.missingSkills(), targetRole.name(), 2);

        // 9. Index Chunks into Local Vector Store for RAG Grounding
        long studentNumericId = Math.abs((long) candidate.getId().hashCode());
        indexCandidateEvidenceIntoRag(studentNumericId, candidate, targetRole, extractedSkills, evidenceGraph, roadmapMonths);

        log.info("Analysis completed successfully [overallScore={}]", roleFit.overallScore());

        return new AnalysisResult(
                analysisId,
                candidate,
                targetRole,
                document,
                gitHubProfile,
                allRepos,
                commits,
                extractedSkills,
                evidenceGraph,
                verificationMatrix,
                skillGaps,
                roleFit,
                insights,
                roadmapMonths,
                learningResources,
                Map.of(
                        "analyzerVersion", "SkillLens-1.0.0-Java25",
                        "hasGitHubEvidence", gitHubProfile != null,
                        "verifiedCount", roleFit.verifiedSkills().size(),
                        "missingCount", roleFit.missingSkills().size()
                ),
                Instant.now()
        );
    }

    private void indexCandidateEvidenceIntoRag(long studentId,
                                              Candidate candidate,
                                              RoleDefinition role,
                                              List<SkillClaim> claims,
                                              List<GitHubEvidenceData> evidence,
                                              List<RoadmapMonth> months) {
        vectorStore.clearStudentChunks(studentId);

        List<KnowledgeChunk> chunksToIndex = new ArrayList<>();

        // Role requirements
        String roleContent = String.format("Target Role: %s. Description: %s. Required: %s. Preferred: %s",
                role.title(), role.description(), String.join(", ", role.requiredSkills()), String.join(", ", role.preferredSkills()));
        chunksToIndex.add(new KnowledgeChunk(
                "chunk-role-" + studentId,
                studentId,
                role.title() + " Specification",
                "ROLE_REQUIREMENT",
                roleContent,
                embeddingService.embed(roleContent),
                role.name(),
                null
        ));

        // Resume Claims
        for (int i = 0; i < claims.size(); i++) {
            SkillClaim c = claims.get(i);
            String content = "Resume claim: " + c.canonicalSkill() + " in context: " + c.contextSnippet();
            chunksToIndex.add(new KnowledgeChunk(
                    "chunk-claim-" + studentId + "-" + i,
                    studentId,
                    "Resume: " + c.canonicalSkill(),
                    "RESUME_CLAIM",
                    content,
                    embeddingService.embed(content),
                    role.name(),
                    c.normalizedSkill()
            ));
        }

        // GitHub Evidence
        for (int i = 0; i < evidence.size(); i++) {
            GitHubEvidenceData ev = evidence.get(i);
            String content = String.format("GitHub Verified Evidence for %s: Repositories: %s. Commit sample: %s. Summary: %s",
                    ev.canonicalSkill(), String.join(", ", ev.matchedRepositories()),
                    String.join("; ", ev.matchedCommitMessages().subList(0, Math.min(3, ev.matchedCommitMessages().size()))),
                    ev.evidenceSummary());
            chunksToIndex.add(new KnowledgeChunk(
                    "chunk-evidence-" + studentId + "-" + i,
                    studentId,
                    "GitHub Evidence: " + ev.canonicalSkill(),
                    "GITHUB_COMMIT",
                    content,
                    embeddingService.embed(content),
                    role.name(),
                    ev.normalizedSkill()
            ));
        }

        // Roadmap Objectives
        for (RoadmapMonth m : months) {
            String content = String.format("Month %d: %s. Focus: %s. Practical Work: %s. Milestone: %s",
                    m.monthNumber(), m.title(), String.join(", ", m.focusSkills()),
                    String.join("; ", m.practicalWork()), m.projectMilestone());
            chunksToIndex.add(new KnowledgeChunk(
                    "chunk-roadmap-" + studentId + "-" + m.monthNumber(),
                    studentId,
                    "Roadmap Month " + m.monthNumber(),
                    "ROADMAP_OBJECTIVE",
                    content,
                    embeddingService.embed(content),
                    role.name(),
                    null
            ));
        }

        vectorStore.indexChunks(chunksToIndex);
        log.info("Indexed {} contextual evidence chunks into local RAG store for studentId={}", chunksToIndex.size(), studentId);
    }

    private String extractNameFromText(String text) {
        if (text == null || text.trim().isEmpty()) return "Candidate";
        for (String line : text.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.length() >= 3 && trimmed.length() <= 40 && trimmed.matches("^[A-Za-z\\s.-]+$")
                    && !trimmed.toLowerCase().contains("curriculum")
                    && !trimmed.toLowerCase().contains("resume")) {
                return trimmed;
            }
        }
        return "Candidate";
    }

    private String extractEmailFromText(String text) {
        if (text == null) return null;
        var matcher = java.util.regex.Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
