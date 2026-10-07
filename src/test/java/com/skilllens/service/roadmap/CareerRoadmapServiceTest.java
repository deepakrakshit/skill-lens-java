package com.skilllens.service.roadmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilllens.domain.model.CareerRoadmap;
import com.skilllens.domain.model.RoadmapMonth;
import com.skilllens.domain.model.RoleFitResult;
import com.skilllens.domain.repository.CareerRoadmapRepository;
import com.skilllens.service.resources.LearningResourceService;
import com.skilllens.service.resources.OfflineResourceCatalog;
import com.skilllens.service.resources.YouTubeClient;
import com.skilllens.service.security.CredentialManager;
import com.skilllens.service.security.SecureCredentialStorageService;
import com.skilllens.service.skills.SkillTaxonomyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CareerRoadmapServiceTest {

    private CareerRoadmapService roadmapService;

    @BeforeEach
    void setUp() {
        CareerRoadmapRepository fakeRepository = (CareerRoadmapRepository) Proxy.newProxyInstance(
                CareerRoadmapRepository.class.getClassLoader(),
                new Class<?>[]{CareerRoadmapRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName()) && args != null && args.length > 0) {
                        return args[0];
                    }
                    return null;
                }
        );

        ObjectMapper objectMapper = new ObjectMapper();
        SkillTaxonomyService taxonomyService = new SkillTaxonomyService();
        OfflineResourceCatalog offlineCatalog = new OfflineResourceCatalog();

        SecureCredentialStorageService fakeVault = new SecureCredentialStorageService() {
            @Override public Optional<String> getCredential(String key) { return Optional.empty(); }
            @Override public void saveCredential(String key, String value) {}
            @Override public void deleteCredential(String key) {}
            @Override public boolean hasCredential(String key) { return false; }
        };

        CredentialManager credentialManager = new CredentialManager(fakeVault);
        YouTubeClient youTubeClient = new YouTubeClient(credentialManager, objectMapper);
        LearningResourceService resourceService = new LearningResourceService(offlineCatalog, youTubeClient, taxonomyService);

        roadmapService = new CareerRoadmapService(fakeRepository, resourceService, objectMapper);
    }

    @Test
    @DisplayName("Generates 4-month structured progression roadmap with GitHub milestones")
    void testGenerateRoadmapStructure() {
        RoleFitResult fitResult = new RoleFitResult(
                72.0, 75.0, 60.0, 70.0, 80.0, 75.0,
                5, 2, 1, 2, 3,
                "COMPETITIVE_FIT", "Formula breakdown", Map.of(),
                List.of("Java", "Git"),
                List.of("Docker", "Kubernetes", "AWS"),
                List.of("Microservices")
        );

        CareerRoadmap roadmap = roadmapService.generateRoadmap("cand-123", "Backend Developer", fitResult);

        assertNotNull(roadmap);
        assertEquals("cand-123", roadmap.getCandidateId());
        assertEquals("Backend Developer", roadmap.getTargetRole());
        assertEquals(4, roadmap.getDurationMonths());

        List<RoadmapMonth> months = roadmapService.deserializeMonths(roadmap);
        assertEquals(4, months.size(), "Roadmap must span 4 progressive months");

        for (int i = 0; i < 4; i++) {
            RoadmapMonth m = months.get(i);
            assertEquals(i + 1, m.monthNumber());
            assertNotNull(m.title());
            assertFalse(m.focusSkills().isEmpty());
            assertNotNull(m.gitHubMilestone(), "Must contain a concrete GitHub milestone");
            assertNotNull(m.portfolioTarget());
        }
    }
}
