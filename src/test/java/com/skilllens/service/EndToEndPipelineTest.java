package com.skilllens.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilllens.domain.model.AnalysisResult;
import com.skilllens.domain.model.Candidate;
import com.skilllens.domain.model.CandidateDocument;
import com.skilllens.domain.model.CandidateProfile;
import com.skilllens.domain.model.CareerRoadmap;
import com.skilllens.domain.repository.CandidateDocumentRepository;
import com.skilllens.domain.repository.CandidateProfileRepository;
import com.skilllens.domain.repository.CandidateRepository;
import com.skilllens.domain.repository.CareerRoadmapRepository;
import com.skilllens.service.document.DocumentParserService;
import com.skilllens.service.github.GitHubAnalysisService;
import com.skilllens.service.github.GitHubClient;
import com.skilllens.service.rag.EmbeddingService;
import com.skilllens.service.rag.LocalVectorStore;
import com.skilllens.service.resources.LearningResourceService;
import com.skilllens.service.resources.OfflineResourceCatalog;
import com.skilllens.service.resources.YouTubeClient;
import com.skilllens.service.roadmap.CareerRoadmapService;
import com.skilllens.service.security.CredentialManager;
import com.skilllens.service.security.SecureCredentialStorageService;
import com.skilllens.service.skills.RoleCatalogService;
import com.skilllens.service.skills.SkillExtractionService;
import com.skilllens.service.skills.SkillTaxonomyService;
import com.skilllens.service.verification.RoleFitScoringService;
import com.skilllens.service.verification.SkillVerificationService;
import com.skilllens.service.verification.VerificationInsightsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EndToEndPipelineTest {

    private AnalysisOrchestrationService orchestrationService;
    private LocalVectorStore vectorStore;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        SkillTaxonomyService taxonomyService = new SkillTaxonomyService();
        DocumentParserService documentParserService = new DocumentParserService();
        SkillExtractionService skillExtractionService = new SkillExtractionService(taxonomyService);
        RoleCatalogService roleCatalogService = new RoleCatalogService(taxonomyService);

        SecureCredentialStorageService fakeVault = new SecureCredentialStorageService() {
            @Override public Optional<String> getCredential(String key) { return Optional.empty(); }
            @Override public void saveCredential(String key, String value) {}
            @Override public void deleteCredential(String key) {}
            @Override public boolean hasCredential(String key) { return false; }
        };
        CredentialManager credentialManager = new CredentialManager(fakeVault);
        GitHubClient gitHubClient = new GitHubClient(credentialManager, objectMapper);
        GitHubAnalysisService gitHubAnalysisService = new GitHubAnalysisService(gitHubClient, taxonomyService);

        SkillVerificationService verificationService = new SkillVerificationService();
        RoleFitScoringService scoringService = new RoleFitScoringService();
        VerificationInsightsService insightsService = new VerificationInsightsService();

        OfflineResourceCatalog offlineCatalog = new OfflineResourceCatalog();
        YouTubeClient youTubeClient = new YouTubeClient(credentialManager, objectMapper);
        LearningResourceService resourceService = new LearningResourceService(offlineCatalog, youTubeClient, taxonomyService);

        CareerRoadmapRepository fakeRoadmapRepo = (CareerRoadmapRepository) Proxy.newProxyInstance(
                CareerRoadmapRepository.class.getClassLoader(),
                new Class<?>[]{CareerRoadmapRepository.class},
                (proxy, method, args) -> args != null && args.length > 0 ? args[0] : null
        );
        CareerRoadmapService roadmapService = new CareerRoadmapService(fakeRoadmapRepo, resourceService, objectMapper);

        vectorStore = new LocalVectorStore();
        EmbeddingService embeddingService = new EmbeddingService();

        CandidateRepository fakeCandidateRepo = (CandidateRepository) Proxy.newProxyInstance(
                CandidateRepository.class.getClassLoader(),
                new Class<?>[]{CandidateRepository.class},
                (proxy, method, args) -> args != null && args.length > 0 ? args[0] : null
        );

        CandidateDocumentRepository fakeDocRepo = (CandidateDocumentRepository) Proxy.newProxyInstance(
                CandidateDocumentRepository.class.getClassLoader(),
                new Class<?>[]{CandidateDocumentRepository.class},
                (proxy, method, args) -> args != null && args.length > 0 ? args[0] : null
        );

        CandidateProfileRepository fakeProfileRepo = (CandidateProfileRepository) Proxy.newProxyInstance(
                CandidateProfileRepository.class.getClassLoader(),
                new Class<?>[]{CandidateProfileRepository.class},
                (proxy, method, args) -> args != null && args.length > 0 ? args[0] : null
        );

        orchestrationService = new AnalysisOrchestrationService(
                documentParserService,
                skillExtractionService,
                roleCatalogService,
                gitHubAnalysisService,
                verificationService,
                scoringService,
                insightsService,
                roadmapService,
                resourceService,
                vectorStore,
                embeddingService,
                fakeCandidateRepo,
                fakeDocRepo,
                fakeProfileRepo,
                objectMapper
        );
    }

    @Test
    @DisplayName("Executes end-to-end analysis on candidate resume claims and generates verification matrix")
    void testEndToEndAnalysis() {
        String resumeText = """
                Deepak Rakshit
                deepak@example.com
                
                SKILLS:
                Languages: Java, Python, SQL
                Frameworks: Spring Boot, React
                Tools: Docker, Git, PostgreSQL
                
                EXPERIENCE:
                Software Engineering Intern
                Built scalable REST API backend microservices using Java and Spring Boot.
                Managed relational database migrations and queries with PostgreSQL.
                """;

        AnalysisResult result = orchestrationService.analyze(
                null,
                resumeText,
                null, // No GitHub provided
                "Backend Developer",
                null
        );

        assertNotNull(result);
        assertNotNull(result.analysisId());
        assertEquals("Backend Developer", result.targetRole().title());
        assertFalse(result.extractedSkills().isEmpty(), "Extracted skills should not be empty");

        // Role Fit must be deterministically calculated
        assertTrue(result.roleFit().overallScore() > 0.0);
        assertTrue(result.roleFit().overallScore() <= 100.0);

        // 4-month roadmap generated
        assertEquals(4, result.careerRoadmap().size());

        // Local RAG vector store indexed
        assertTrue(vectorStore.size() > 0, "RAG vector store must have indexed evidence chunks");
    }
}
