package com.skilllens.service.resources;

import com.skilllens.domain.model.LearningResource;
import com.skilllens.domain.model.LearningResource.ResourceType;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Curated offline learning resources catalog.
 * Guarantees high-quality authoritative documentation, books, and interactive tutorials
 * are always available even without internet access or YouTube API credentials.
 */
@Service
public class OfflineResourceCatalog {

    private final Map<String, List<LearningResource>> resourcesBySkill = new HashMap<>();

    public OfflineResourceCatalog() {
        populateCatalog();
    }

    public List<LearningResource> getResourcesForSkill(String normalizedSkill) {
        if (normalizedSkill == null) return Collections.emptyList();
        return resourcesBySkill.getOrDefault(normalizedSkill.toLowerCase(Locale.ROOT), Collections.emptyList());
    }

    public List<LearningResource> getAllResources() {
        return resourcesBySkill.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    private void populateCatalog() {
        // Java
        add("java", List.of(
                new LearningResource("java-docs", "Oracle Java 25 Documentation & Language Specification",
                        "Official language specification, virtual threads documentation, and API reference.",
                        "https://docs.oracle.com/en/java/", "OFFICIAL_DOCS", ResourceType.DOCUMENTATION,
                        "Java", "INTERMEDIATE", 4.9, "Ongoing", 1, true),
                new LearningResource("java-dev-guide", "Modern Java: Records, Pattern Matching, and Concurrency",
                        "Comprehensive guide to modern Java language features and clean architecture.",
                        "https://dev.java/", "OFFICIAL_DOCS", ResourceType.PROJECT_GUIDE,
                        "Java", "INTERMEDIATE", 4.8, "6 hours", 1, true)
        ));

        // Spring Boot
        add("spring boot", List.of(
                new LearningResource("spring-boot-ref", "Spring Boot 3 Reference Documentation",
                        "Official Spring Boot reference manual covering auto-configuration, actuators, and WebFlux.",
                        "https://docs.spring.io/spring-boot/docs/current/reference/html/", "OFFICIAL_DOCS", ResourceType.DOCUMENTATION,
                        "Spring Boot", "INTERMEDIATE", 5.0, "10 hours", 2, true),
                new LearningResource("spring-guides", "Spring Building a RESTful Web Service",
                        "Official step-by-step tutorial for architecting production REST APIs with Spring Boot.",
                        "https://spring.io/guides/gs/rest-service/", "OFFICIAL_DOCS", ResourceType.INTERACTIVE_TUTORIAL,
                        "Spring Boot", "BEGINNER", 4.8, "2 hours", 1, true)
        ));

        // Python
        add("python", List.of(
                new LearningResource("py-docs", "Python 3 Standard Library Documentation & Tutorial",
                        "The official Python 3 tutorial and complete language reference manual.",
                        "https://docs.python.org/3/tutorial/", "OFFICIAL_DOCS", ResourceType.DOCUMENTATION,
                        "Python", "BEGINNER", 4.9, "8 hours", 1, true)
        ));

        // TypeScript
        add("typescript", List.of(
                new LearningResource("ts-handbook", "The TypeScript Handbook",
                        "Comprehensive handbook on TypeScript type system, generics, and compiler options.",
                        "https://www.typescriptlang.org/docs/handbook/intro.html", "OFFICIAL_DOCS", ResourceType.DOCUMENTATION,
                        "TypeScript", "INTERMEDIATE", 4.9, "5 hours", 1, true)
        ));

        // React
        add("react", List.of(
                new LearningResource("react-docs", "React Official Documentation (react.dev)",
                        "Modern interactive documentation covering hooks, component lifecycle, and state management.",
                        "https://react.dev/learn", "OFFICIAL_DOCS", ResourceType.INTERACTIVE_TUTORIAL,
                        "React", "INTERMEDIATE", 4.9, "6 hours", 2, true)
        ));

        // Docker
        add("docker", List.of(
                new LearningResource("docker-guide", "Docker Getting Started Guide & Best Practices",
                        "Official guide to multi-step container builds, caching, and container orchestration.",
                        "https://docs.docker.com/get-started/", "OFFICIAL_DOCS", ResourceType.DOCUMENTATION,
                        "Docker", "BEGINNER", 4.8, "4 hours", 3, true)
        ));

        // Kubernetes
        add("kubernetes", List.of(
                new LearningResource("k8s-basics", "Kubernetes Documentation & Interactive Tutorials",
                        "Official guide covering Pods, Deployments, Services, Ingress, and ConfigMaps.",
                        "https://kubernetes.io/docs/tutorials/kubernetes-basics/", "OFFICIAL_DOCS", ResourceType.INTERACTIVE_TUTORIAL,
                        "Kubernetes", "ADVANCED", 4.8, "8 hours", 3, true)
        ));

        // SQL & PostgreSQL
        add("sql", List.of(
                new LearningResource("pg-docs", "PostgreSQL Official Documentation: Indexes & Query Optimization",
                        "In-depth guide to transaction isolation levels, indexing (B-Tree, GIN), and EXPLAIN plans.",
                        "https://www.postgresql.org/docs/current/performance-tips.html", "OFFICIAL_DOCS", ResourceType.DOCUMENTATION,
                        "SQL", "INTERMEDIATE", 4.9, "5 hours", 2, true)
        ));
        add("postgresql", resourcesBySkill.get("sql"));

        // Git & GitHub
        add("git", List.of(
                new LearningResource("pro-git-book", "Pro Git (Official Book by Scott Chacon)",
                        "The definitive guide to Git branching, rebase workflows, submodules, and internals.",
                        "https://git-scm.com/book/en/v2", "OFFICIAL_DOCS", ResourceType.DOCUMENTATION,
                        "Git", "BEGINNER", 5.0, "7 hours", 1, true)
        ));

        // AWS
        add("aws", List.of(
                new LearningResource("aws-architect", "AWS Well-Architected Framework",
                        "Architectural pillars: Reliability, Performance Efficiency, Security, and Cost Optimization.",
                        "https://aws.amazon.com/architecture/well-architected/", "OFFICIAL_DOCS", ResourceType.DOCUMENTATION,
                        "AWS", "INTERMEDIATE", 4.8, "6 hours", 3, true)
        ));

        // System Design
        add("system design", List.of(
                new LearningResource("sys-design-primer", "The System Design Primer (Donne Martin)",
                        "Open-source curriculum on scalable distributed systems, caching, replication, and CAP theorem.",
                        "https://github.com/donnemartin/system-design-primer", "GITHUB", ResourceType.PROJECT_GUIDE,
                        "System Design", "ADVANCED", 5.0, "12 hours", 4, true)
        ));
    }

    private void add(String skillKey, List<LearningResource> list) {
        resourcesBySkill.put(skillKey.toLowerCase(Locale.ROOT), list);
    }
}
