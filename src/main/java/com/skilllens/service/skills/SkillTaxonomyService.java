package com.skilllens.service.skills;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Normalizes skill aliases, acronyms, and variants into canonical technical skill names.
 * Maps skills into structured categories (Language, Framework, Cloud, DevOps, Database, AI/ML, Testing).
 */
@Service
public class SkillTaxonomyService {

    private final Map<String, String> aliasToCanonical = new HashMap<>();
    private final Map<String, String> canonicalToCategory = new HashMap<>();

    public SkillTaxonomyService() {
        initTaxonomy();
    }

    private void register(String canonical, String category, String... aliases) {
        canonicalToCategory.put(canonical.toLowerCase(), category);
        aliasToCanonical.put(canonical.toLowerCase(), canonical);
        for (String alias : aliases) {
            aliasToCanonical.put(alias.toLowerCase().trim(), canonical);
        }
    }

    private void initTaxonomy() {
        // Languages
        register("Java", "Programming Languages", "java 8", "java 11", "java 17", "java 21", "java 25", "core java", "jdk");
        register("Python", "Programming Languages", "python 3", "python3", "python programming", "py");
        register("JavaScript", "Programming Languages", "js", "javascript (es6+)", "vanilla js", "ecmascript");
        register("TypeScript", "Programming Languages", "ts", "typescript 4", "typescript 5");
        register("C++", "Programming Languages", "cpp", "c/c++");
        register("C#", "Programming Languages", "csharp", "c#.net", ".net c#");
        register("Go", "Programming Languages", "golang", "go programming");
        register("Rust", "Programming Languages", "rustlang");
        register("Kotlin", "Programming Languages", "kotlin jvm");
        register("SQL", "Programming Languages", "ansi sql", "structured query language");
        register("Bash", "Programming Languages", "shell", "bash scripting", "sh");

        // Backend Frameworks
        register("Spring Boot", "Backend Frameworks", "springboot", "spring-boot", "spring framework", "spring boot 3", "spring boot 4", "spring");
        register("Node.js", "Backend Frameworks", "node", "nodejs", "node js");
        register("Express", "Backend Frameworks", "express.js", "expressjs");
        register("FastAPI", "Backend Frameworks", "fast api", "fast-api");
        register("Django", "Backend Frameworks", "django rest framework", "drf");
        register("Flask", "Backend Frameworks", "flask py");
        register("ASP.NET", "Backend Frameworks", "asp.net core", ".net core", "dotnet");

        // Frontend Frameworks
        register("React", "Frontend Engineering", "reactjs", "react.js", "react framework");
        register("React Native", "Mobile Development", "react-native");
        register("Vue.js", "Frontend Engineering", "vue", "vuejs", "vue 3");
        register("Angular", "Frontend Engineering", "angularjs", "angular 2+");
        register("Next.js", "Frontend Engineering", "nextjs", "next js");
        register("HTML5", "Frontend Engineering", "html", "html5/css3");
        register("CSS3", "Frontend Engineering", "css", "styling", "sass", "scss", "tailwind", "tailwind css", "tailwindcss");
        register("JavaFX", "Desktop Engineering", "javafx 21", "javafx 25", "openjfx");

        // Databases & Caches
        register("PostgreSQL", "Databases & Storage", "postgres", "postgresql", "psql");
        register("MySQL", "Databases & Storage", "mysql server");
        register("SQLite", "Databases & Storage", "sqlite3");
        register("MongoDB", "Databases & Storage", "mongo", "mongodb atlas");
        register("Redis", "Databases & Storage", "redis cache");
        register("H2", "Databases & Storage", "h2 database");
        register("Hibernate", "Databases & Storage", "jpa", "spring data jpa", "orm");

        // DevOps & Cloud
        register("Docker", "DevOps & Cloud", "dockerfile", "docker compose", "containerization");
        register("Kubernetes", "DevOps & Cloud", "k8s", "k8s cluster", "helm");
        register("AWS", "DevOps & Cloud", "amazon web services", "aws ec2", "aws s3", "aws lambda", "aws cloud");
        register("Google Cloud", "DevOps & Cloud", "gcp", "google cloud platform");
        register("Azure", "DevOps & Cloud", "microsoft azure");
        register("Git", "DevOps & Cloud", "git version control", "github", "gitlab");
        register("CI/CD", "DevOps & Cloud", "continuous integration", "github actions", "jenkins", "gitlab ci");

        // AI, ML & Data
        register("Machine Learning", "AI & Machine Learning", "ml", "statistical learning");
        register("Deep Learning", "AI & Machine Learning", "neural networks", "dl");
        register("PyTorch", "AI & Machine Learning", "pytorch framework", "torch");
        register("TensorFlow", "AI & Machine Learning", "tf", "keras");
        register("Scikit-learn", "AI & Machine Learning", "sklearn");
        register("Pandas", "Data & Analytics", "pandas dataframes");
        register("NumPy", "Data & Analytics", "numpy array");
        register("OpenCV", "AI & Machine Learning", "computer vision");
        register("Spring AI", "AI & Machine Learning", "spring-ai", "rag");
        register("LLM", "AI & Machine Learning", "large language models", "prompt engineering", "genai", "generative ai");

        // Testing & Quality
        register("JUnit", "Testing & QA", "junit 5", "junit4", "unit testing");
        register("Mockito", "Testing & QA", "mockito framework", "mocking");
        register("PyTest", "Testing & QA", "pytest framework");
        register("Selenium", "Testing & QA", "selenium webdriver");
        register("TestFX", "Testing & QA", "testfx ui");

        // Protocols & Architecture
        register("REST APIs", "Software Architecture", "rest", "restful", "rest api", "http api", "web apis");
        register("GraphQL", "Software Architecture", "graphql api", "apollo");
        register("Microservices", "Software Architecture", "microservice architecture", "distributed systems");
    }

    public String normalize(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            return "Unknown";
        }
        String key = skill.toLowerCase().trim();
        return aliasToCanonical.getOrDefault(key, capitalizeWords(skill.trim()));
    }

    public String getCategory(String normalizedSkill) {
        if (normalizedSkill == null) return "General Technical";
        return canonicalToCategory.getOrDefault(normalizedSkill.toLowerCase().trim(), "General Technical");
    }

    public boolean isKnown(String skill) {
        if (skill == null) return false;
        return aliasToCanonical.containsKey(skill.toLowerCase().trim());
    }

    public Set<String> getAllCanonicalSkills() {
        return new TreeSet<>(canonicalToCategory.keySet());
    }

    private String capitalizeWords(String str) {
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) {
                    sb.append(w.substring(1));
                }
            }
        }
        return sb.toString();
    }
}
