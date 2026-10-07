package com.skilllens.service.roadmap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilllens.domain.model.CareerRoadmap;
import com.skilllens.domain.model.LearningResource;
import com.skilllens.domain.model.RoadmapMonth;
import com.skilllens.domain.model.RoleFitResult;
import com.skilllens.domain.repository.CareerRoadmapRepository;
import com.skilllens.service.resources.LearningResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Service for generating structured, actionable 4-month career progression roadmaps.
 * Maps identified skill gaps to concrete GitHub project milestones,
 * practical exercises, and measurable success criteria.
 */
@Service
public class CareerRoadmapService {

    private static final Logger log = LoggerFactory.getLogger(CareerRoadmapService.class);

    private final CareerRoadmapRepository roadmapRepository;
    private final LearningResourceService resourceService;
    private final ObjectMapper objectMapper;

    public CareerRoadmapService(CareerRoadmapRepository roadmapRepository,
                                LearningResourceService resourceService,
                                ObjectMapper objectMapper) {
        this.roadmapRepository = roadmapRepository;
        this.resourceService = resourceService;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates a 4-month actionable career roadmap for a candidate based on verified skill gaps.
     */
    public CareerRoadmap generateRoadmap(String candidateId, String targetRole, RoleFitResult fitResult) {
        List<String> missingSkills = fitResult != null ? new ArrayList<>(fitResult.missingSkills()) : new ArrayList<>();
        List<String> weakSkills = fitResult != null ? new ArrayList<>(fitResult.unverifiedClaims()) : new ArrayList<>();

        List<String> prioritizedGaps = new ArrayList<>();
        prioritizedGaps.addAll(missingSkills);
        prioritizedGaps.addAll(weakSkills);

        if (prioritizedGaps.isEmpty()) {
            prioritizedGaps.add("Advanced System Design");
            prioritizedGaps.add("Performance Optimization");
            prioritizedGaps.add("Cloud Architecture");
            prioritizedGaps.add("Security Hardening");
        }

        List<LearningResource> resources = resourceService.getResourcesForGaps(prioritizedGaps, targetRole, 2);
        Map<Integer, List<String>> resourceIdsByMonth = new HashMap<>();
        for (LearningResource r : resources) {
            int m = Math.max(1, Math.min(4, r.roadmapMonth()));
            resourceIdsByMonth.computeIfAbsent(m, k -> new ArrayList<>()).add(r.id());
        }

        // Partition gaps into 4 months
        List<String> m1Skills = extractSubset(prioritizedGaps, 0, 2);
        List<String> m2Skills = extractSubset(prioritizedGaps, 2, 4);
        List<String> m3Skills = extractSubset(prioritizedGaps, 4, 6);
        List<String> m4Skills = extractSubset(prioritizedGaps, 6, 8);

        if (m1Skills.isEmpty()) m1Skills.add("Core Language Fundamentals & Concurrency");
        if (m2Skills.isEmpty()) m2Skills.add("Service Architecture & Database Layer");
        if (m3Skills.isEmpty()) m3Skills.add("Containerization & CI/CD Automation");
        if (m4Skills.isEmpty()) m4Skills.add("System Polish & Release v1.0.0");

        List<RoadmapMonth> months = List.of(
                new RoadmapMonth(
                        1,
                        "Core Fundamentals & Immediate Gaps",
                        "Address primary foundational skill gaps required for " + targetRole + " with formal project scaffolding.",
                        m1Skills,
                        List.of("Master core data structures and protocols", "Implement initial domain entities and business rules", "Establish clean project architecture"),
                        List.of("Scaffold new repository with automated CI build", "Implement unit tests covering core domain components", "Document architectural decisions in ADR format"),
                        "Scaffold clean repository skeleton with Maven build and CI validation",
                        "Repository creation with initial commit structure and automated GitHub Actions workflow",
                        "Public GitHub repository with clear README, license, and passing build status badge",
                        resourceIdsByMonth.getOrDefault(1, List.of("java-docs", "pro-git-book")),
                        List.of("Project builds cleanly with zero errors", "100% test pass rate for domain logic", "Clean commit history on main branch")
                ),
                new RoadmapMonth(
                        2,
                        "Deep Architectural Implementation",
                        "Build out production-grade backend components, data layer, and API contracts.",
                        m2Skills,
                        List.of("Implement robust database queries and transaction boundaries", "Expose secure REST or gRPC endpoints", "Add rigorous integration test suites"),
                        List.of("Develop service business logic and query projections", "Implement error handling and input validation filters", "Write end-to-end integration tests"),
                        "Functional service handling realistic domain workloads with database persistence",
                        "Feature branches and pull requests merged with detailed review notes and squash commits",
                        "Comprehensive test suite achieving > 80% line and branch coverage",
                        resourceIdsByMonth.getOrDefault(2, List.of("spring-boot-ref", "pg-docs")),
                        List.of("All API endpoints return standard HTTP status codes", "No unhandled exceptions in integration tests", "Documented schema migrations")
                ),
                new RoadmapMonth(
                        3,
                        "Production Readiness & Tooling",
                        "Introduce containerization, caching, observability, and deployment pipelines.",
                        m3Skills,
                        List.of("Package service as production container image", "Integrate caching and connection pooling", "Configure logging and metric collection"),
                        List.of("Write multi-step Dockerfile", "Implement Docker Compose local environment", "Configure structured JSON logging"),
                        "Containerized application runnable locally with one-command orchestration",
                        "Automated container build and security scanning workflow in GitHub Actions",
                        "Multi-platform Docker image published to GitHub Container Registry",
                        resourceIdsByMonth.getOrDefault(3, List.of("docker-guide", "aws-architect")),
                        List.of("Container starts up cleanly and passes health check", "Zero high-severity CVEs in image scan", "Configuration fully externalized")
                ),
                new RoadmapMonth(
                        4,
                        "Portfolio Polish & Technical Interview Readiness",
                        "Finalize documentation, benchmark performance, and publish release v1.0.0.",
                        m4Skills,
                        List.of("Synthesize architecture diagrams and performance benchmarks", "Prepare for system design deep dives", "Polish candidate portfolio presentation"),
                        List.of("Create system architecture diagram and benchmark results", "Record demo walkthrough or live deployment", "Tag and publish release v1.0.0"),
                        "Portfolio-ready flagship project ready to demonstrate in senior technical interviews",
                        "Official GitHub Release v1.0.0 with full changelog, release notes, and binary artifacts",
                        "Flagship portfolio piece with comprehensive README, interactive demo, and verified commit log",
                        resourceIdsByMonth.getOrDefault(4, List.of("sys-design-primer")),
                        List.of("Release v1.0.0 tagged and published on GitHub", "Complete README with architecture diagram", "Ready for recruiter and interviewer review")
                )
        );

        String json;
        try {
            json = objectMapper.writeValueAsString(months);
        } catch (IOException e) {
            log.error("Failed to serialize roadmap months: {}", e.getMessage());
            json = "[]";
        }

        CareerRoadmap roadmap = new CareerRoadmap(candidateId, targetRole, 4, json);
        return roadmapRepository.save(roadmap);
    }

    public List<RoadmapMonth> deserializeMonths(CareerRoadmap roadmap) {
        if (roadmap == null || roadmap.getRoadmapMonthsJson() == null || roadmap.getRoadmapMonthsJson().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(roadmap.getRoadmapMonthsJson(), new TypeReference<List<RoadmapMonth>>() {});
        } catch (IOException e) {
            log.error("Failed to deserialize roadmap months: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> extractSubset(List<String> list, int start, int end) {
        if (start >= list.size()) return new ArrayList<>();
        int actualEnd = Math.min(end, list.size());
        return new ArrayList<>(list.subList(start, actualEnd));
    }
}
