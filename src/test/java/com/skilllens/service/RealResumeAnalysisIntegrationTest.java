package com.skilllens.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilllens.domain.model.*;
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
import com.skilllens.service.resume.DocxGeneratorService;
import com.skilllens.service.resume.PdfGeneratorService;
import com.skilllens.service.resume.ResumeRewriteService;
import com.skilllens.service.resume.ResumeTemplateService;
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
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RealResumeAnalysisIntegrationTest {

    private AnalysisOrchestrationService orchestrationService;
    private ResumeTemplateService templateService;
    private ResumeRewriteService rewriteService;

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

        LocalVectorStore vectorStore = new LocalVectorStore();
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

        PdfGeneratorService pdfGenerator = new PdfGeneratorService();
        DocxGeneratorService docxGenerator = new DocxGeneratorService();
        templateService = new ResumeTemplateService(pdfGenerator, docxGenerator);
        rewriteService = new ResumeRewriteService(null, objectMapper);
    }

    @Test
    @DisplayName("Analyzes real user DOCX resume and verifies full pipeline execution")
    void testRealResumeAnalysis(@TempDir Path tempDir) throws Exception {
        File resumeFile = new File("C:\\Users\\deepa\\OneDrive\\Desktop\\RESUME_DEEPAK_RAKSHIT.docx");
        if (!resumeFile.exists()) {
            // Skip gracefully if file is moved
            return;
        }

        AnalysisResult result = orchestrationService.analyze(
                resumeFile,
                null,
                "deepak", // GitHub handle
                "Backend Developer",
                null
        );

        assertNotNull(result);
        assertEquals("Backend Developer", result.targetRole().title());
        assertFalse(result.extractedSkills().isEmpty(), "Must extract skills from Deepak's resume");
        assertTrue(result.roleFit().overallScore() >= 0.0);
        assertEquals(4, result.careerRoadmap().size());

        // Test export of rewritten resume
        CandidateProfile profile = new CandidateProfile(result.candidate().getId());
        profile.setFullName(result.candidate().getFullName());
        profile.setEmail(result.candidate().getEmail());
        profile.setGithubUsername(result.candidate().getGithubUsername());

        RewrittenResume rewritten = rewriteService.rewriteResume(
                profile,
                result.verificationMatrix(),
                null,
                "Backend Developer",
                ResumeTemplateType.SOFTWARE_ENGINEER
        );

        File pdfOut = tempDir.resolve("Deepak_Verified_Resume.pdf").toFile();
        File docxOut = tempDir.resolve("Deepak_Verified_Resume.docx").toFile();

        templateService.exportToPdfFile(rewritten, profile, ResumeTemplateType.SOFTWARE_ENGINEER, pdfOut);
        templateService.exportToDocxFile(rewritten, profile, ResumeTemplateType.SOFTWARE_ENGINEER, docxOut);

        assertTrue(pdfOut.exists() && pdfOut.length() > 500, "Exported PDF must be valid");
        assertTrue(docxOut.exists() && docxOut.length() > 500, "Exported DOCX must be valid");
    }
}
