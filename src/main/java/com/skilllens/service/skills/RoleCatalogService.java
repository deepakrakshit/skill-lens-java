package com.skilllens.service.skills;

import com.skilllens.domain.model.RoleDefinition;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Role catalog service managing the 13 predefined industry roles,
 * custom role specifications, and Job Description-based role refinement.
 */
@Service
public class RoleCatalogService {

    private final Map<String, RoleDefinition> predefinedRoles = new LinkedHashMap<>();
    private final SkillTaxonomyService taxonomyService;

    public RoleCatalogService(SkillTaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
        initPredefinedRoles();
    }

    private void initPredefinedRoles() {
        // 1. Backend Developer
        predefinedRoles.put("Backend Developer", new RoleDefinition(
                "Backend Developer",
                "Designs, builds, and maintains server-side architecture, APIs, database persistence, and system integrations.",
                "Engineering",
                List.of("Java", "Spring Boot", "SQL", "PostgreSQL", "REST APIs", "Git", "Docker"),
                List.of("Redis", "Microservices", "Kubernetes", "AWS", "CI/CD", "JUnit", "Kafka", "Linux"),
                List.of("API Design & Documentation", "Data Persistence & Transaction Management", "Authentication & Authorization", "Concurrency & Performance"),
                List.of("REST API Services", "Microservice Architecture", "Database Schema & Migrations", "Event-Driven Processing"),
                List.of("Repository with database migrations", "Controller/Service/Repository layer structure", "API unit/integration tests", "Docker containerization"),
                Map.of("Java", 1.2, "Spring Boot", 1.2, "SQL", 1.0, "REST APIs", 1.0, "PostgreSQL", 0.9, "Docker", 0.8)
        ));

        // 2. Frontend Developer
        predefinedRoles.put("Frontend Developer", new RoleDefinition(
                "Frontend Developer",
                "Constructs engaging, responsive, and accessible client-side web interfaces and user interactions.",
                "Engineering",
                List.of("JavaScript", "TypeScript", "React", "HTML5", "CSS3", "Git"),
                List.of("Next.js", "Vue.js", "Tailwind", "REST APIs", "GraphQL", "CI/CD", "Jest"),
                List.of("Component Architecture", "State Management", "Responsive Design", "Client-Side Routing"),
                List.of("Interactive Single Page Applications", "Responsive Dashboards", "Design System Implementation"),
                List.of("package.json with React/Vue/Angular", "Component hierarchies", "Responsive CSS/Tailwind styles", "State management hooks"),
                Map.of("React", 1.2, "TypeScript", 1.1, "JavaScript", 1.0, "HTML5", 0.9, "CSS3", 0.9)
        ));

        // 3. Full Stack Developer
        predefinedRoles.put("Full Stack Developer", new RoleDefinition(
                "Full Stack Developer",
                "Builds end-to-end applications spanning client interfaces, backend services, databases, and deployment.",
                "Engineering",
                List.of("JavaScript", "TypeScript", "React", "Java", "Spring Boot", "SQL", "REST APIs", "Git"),
                List.of("Node.js", "Docker", "PostgreSQL", "MongoDB", "AWS", "CI/CD"),
                List.of("End-to-End Application Design", "Full Stack Data Flow", "API Contract Negotiation", "Database Integration"),
                List.of("Full Stack Web Applications", "Client-Server Management Portals", "E-Commerce or CRUD Platforms"),
                List.of("Client & server workspaces", "API controllers and frontend fetch clients", "Database schema definition", "Deploy scripts"),
                Map.of("React", 1.1, "Spring Boot", 1.1, "Java", 1.0, "TypeScript", 1.0, "SQL", 1.0)
        ));

        // 4. Software Engineer
        predefinedRoles.put("Software Engineer", new RoleDefinition(
                "Software Engineer",
                "Generalist software engineer building robust algorithmic systems, data structures, and core software applications.",
                "Engineering",
                List.of("Java", "Python", "Data Structures", "Algorithms", "Git", "SQL"),
                List.of("C++", "Docker", "REST APIs", "Linux", "CI/CD", "Testing"),
                List.of("Algorithmic Efficiency", "Modular Clean Architecture", "Object-Oriented Design", "Problem Solving"),
                List.of("Core Software Utilities", "Data Processing Tools", "Automated Workflows"),
                List.of("Clean object-oriented code", "Unit test coverage", "Algorithm implementation with benchmarks"),
                Map.of("Java", 1.1, "Data Structures", 1.1, "Algorithms", 1.1, "Git", 0.9)
        ));

        // 5. Mobile Developer
        predefinedRoles.put("Mobile Developer", new RoleDefinition(
                "Mobile Developer",
                "Creates high-performance native and cross-platform applications for Android and iOS mobile platforms.",
                "Engineering",
                List.of("Kotlin", "Java", "Android", "React Native", "Git"),
                List.of("Swift", "Flutter", "REST APIs", "SQLite", "Firebase"),
                List.of("Mobile Lifecycle Management", "Touch UX Optimization", "Offline Data Caching"),
                List.of("Native Mobile Applications", "Offline-First Mobile Apps"),
                List.of("Android manifest or React Native project", "Activity/Fragment or React component lifecycle", "Mobile layout XML/JSX"),
                Map.of("Android", 1.2, "Kotlin", 1.1, "Java", 1.0, "React Native", 1.0)
        ));

        // 6. Data Analyst
        predefinedRoles.put("Data Analyst", new RoleDefinition(
                "Data Analyst",
                "Extracts, transforms, analyzes, and visualizes structured data to derive actionable business insights.",
                "Data",
                List.of("SQL", "Python", "Pandas", "NumPy", "Data Visualization", "Git"),
                List.of("Tableau", "Power BI", "Excel", "PostgreSQL", "R", "Statistics"),
                List.of("Data Cleansing & Transformation", "Exploratory Data Analysis", "Dashboard Creation"),
                List.of("Analytical Reports", "Exploratory Notebooks", "Automated Dashboards"),
                List.of("Jupyter notebooks or SQL scripts", "Data transformation pipelines", "Statistical charts and correlation plots"),
                Map.of("SQL", 1.3, "Python", 1.1, "Pandas", 1.1, "Data Visualization", 1.0)
        ));

        // 7. Data Scientist
        predefinedRoles.put("Data Scientist", new RoleDefinition(
                "Data Scientist",
                "Designs predictive models, statistical frameworks, and data mining pipelines to solve complex problems.",
                "Data",
                List.of("Python", "SQL", "Pandas", "NumPy", "Machine Learning", "Scikit-learn", "Git"),
                List.of("Deep Learning", "TensorFlow", "PyTorch", "Statistics", "Docker", "NLP"),
                List.of("Feature Engineering", "Statistical Hypothesis Testing", "Model Evaluation & Metrics"),
                List.of("Predictive Modeling", "Classification & Regression Pipelines", "Clustering Analysis"),
                List.of("Model training scripts", "Hyperparameter tuning", "Confusion matrix/ROC evaluation metrics"),
                Map.of("Machine Learning", 1.3, "Python", 1.2, "Scikit-learn", 1.1, "SQL", 1.0)
        ));

        // 8. Machine Learning Engineer
        predefinedRoles.put("Machine Learning Engineer", new RoleDefinition(
                "Machine Learning Engineer",
                "Productionizes machine learning models into scalable, low-latency deployment pipelines and services.",
                "AI/ML",
                List.of("Python", "Machine Learning", "PyTorch", "TensorFlow", "Docker", "REST APIs", "Git"),
                List.of("FastAPI", "Kubernetes", "MLflow", "Deep Learning", "AWS", "CUDA"),
                List.of("Model Deployment & Serving", "Inference Latency Optimization", "Data Pipeline Scalability"),
                List.of("Model Serving REST APIs", "Batch Inference Pipelines", "Distributed Training Frameworks"),
                List.of("Model serialization (ONNX/TorchScript)", "FastAPI/Flask serving endpoints", "Dockerfile with dependencies"),
                Map.of("PyTorch", 1.2, "Machine Learning", 1.2, "Docker", 1.1, "Python", 1.0)
        ));

        // 9. DevOps Engineer
        predefinedRoles.put("DevOps Engineer", new RoleDefinition(
                "DevOps Engineer",
                "Automates deployment pipelines, provisions infrastructure as code, and maintains production reliability.",
                "DevOps",
                List.of("Linux", "Docker", "Kubernetes", "CI/CD", "Git", "Bash"),
                List.of("AWS", "Terraform", "Ansible", "Prometheus", "Grafana", "Python"),
                List.of("Continuous Integration & Deployment", "Infrastructure as Code", "Container Orchestration", "Monitoring"),
                List.of("Automated CI/CD Pipelines", "Kubernetes Manifests & Helm Charts", "Terraform Cloud Provisioning"),
                List.of(".github/workflows or Jenkinsfile", "Kubernetes yaml manifests", "Terraform/IaC files", "Shell automation scripts"),
                Map.of("Kubernetes", 1.3, "Docker", 1.2, "CI/CD", 1.2, "Linux", 1.0, "Bash", 1.0)
        ));

        // 10. QA Engineer
        predefinedRoles.put("QA Engineer", new RoleDefinition(
                "QA Engineer",
                "Designs test automation frameworks, regression test suites, and quality gates across software systems.",
                "Quality",
                List.of("Testing", "Java", "Python", "JUnit", "Selenium", "Git"),
                List.of("PyTest", "API Testing", "CI/CD", "Postman", "Cypress", "Performance Testing"),
                List.of("Test Case Design", "Automated End-to-End Testing", "Defect Triage & Reporting"),
                List.of("Automated Test Suites", "Regression Harnesses", "API Integration Test Frameworks"),
                List.of("Test classes and assertions", "Page Object Model implementation", "Automated test execution reports in CI"),
                Map.of("Testing", 1.3, "JUnit", 1.2, "Selenium", 1.1, "Java", 1.0)
        ));

        // 11. Cloud Engineer
        predefinedRoles.put("Cloud Engineer", new RoleDefinition(
                "Cloud Engineer",
                "Architects and maintains secure, scalable cloud infrastructure across public cloud platforms.",
                "Cloud",
                List.of("AWS", "Google Cloud", "Docker", "Linux", "Terraform", "Git"),
                List.of("Kubernetes", "Azure", "CI/CD", "Bash", "Python", "Networking"),
                List.of("Cloud Architecture", "IAM & Security Governance", "VPC Networking & Storage"),
                List.of("Multi-Tier Cloud Deployments", "Serverless Architectures", "Cloud Cost & Performance Optimization"),
                List.of("Terraform/CloudFormation templates", "Cloud architecture diagrams", "IAM policy definitions"),
                Map.of("AWS", 1.3, "Terraform", 1.2, "Docker", 1.0, "Linux", 1.0)
        ));

        // 12. AI Engineer
        predefinedRoles.put("AI Engineer", new RoleDefinition(
                "AI Engineer",
                "Builds intelligent applications powered by Foundation Models, RAG pipelines, and agentic workflows.",
                "AI/ML",
                List.of("Python", "Java", "LLM", "Spring AI", "REST APIs", "Git"),
                List.of("PyTorch", "Docker", "Vector Databases", "Prompt Engineering", "FastAPI"),
                List.of("RAG System Architecture", "Vector Embeddings & Retrieval", "Model Context Management", "Evaluation Benchmarks"),
                List.of("RAG Applications", "Autonomous Agents", "LLM-Powered Analysis Platforms"),
                List.of("Vector index integration", "Prompt templates with delimiters", "Spring AI or LangChain orchestration", "Evaluation tests"),
                Map.of("LLM", 1.3, "Spring AI", 1.2, "Python", 1.1, "Java", 1.0)
        ));

        // 13. UI Engineer
        predefinedRoles.put("UI Engineer", new RoleDefinition(
                "UI Engineer",
                "Crafts design systems, desktop and web component libraries, animations, and accessible interfaces.",
                "Engineering",
                List.of("CSS3", "HTML5", "JavaScript", "TypeScript", "React", "JavaFX", "Git"),
                List.of("Tailwind", "Figma", "Design Systems", "Animation", "Accessibility (a11y)"),
                List.of("Design Token Architectures", "Micro-Interactions", "Cross-Platform UI Rendering"),
                List.of("Design System Component Kits", "Accessible Interface Suites", "Theme Engines"),
                List.of("CSS token hierarchies", "Reusable UI widgets", "Accessible keyboard navigation", "Theme switcher implementation"),
                Map.of("CSS3", 1.2, "React", 1.1, "JavaFX", 1.1, "HTML5", 1.0, "JavaScript", 1.0)
        ));
    }

    public List<String> getPredefinedRoleTitles() {
        return new ArrayList<>(predefinedRoles.keySet());
    }

    public RoleDefinition getRole(String roleTitle) {
        if (roleTitle == null || roleTitle.trim().isEmpty()) {
            return predefinedRoles.get("Backend Developer");
        }

        // Exact match
        for (Map.Entry<String, RoleDefinition> entry : predefinedRoles.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(roleTitle.trim())) {
                return entry.getValue();
            }
        }

        // Fuzzy match
        String lower = roleTitle.toLowerCase();
        for (Map.Entry<String, RoleDefinition> entry : predefinedRoles.entrySet()) {
            if (lower.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        // Synthesize custom role
        return createCustomRole(roleTitle.trim());
    }

    public RoleDefinition resolveRole(String roleTitle, String jobDescription) {
        RoleDefinition base = getRole(roleTitle);
        if (jobDescription != null && !jobDescription.trim().isEmpty()) {
            return refineWithJobDescription(base, jobDescription);
        }
        return base;
    }

    public RoleDefinition createCustomRole(String title) {
        String normalizedTitle = title.trim();
        return new RoleDefinition(
                normalizedTitle,
                "Custom role definition: " + normalizedTitle,
                "Custom",
                List.of("Git", "Problem Solving", "Software Design"),
                List.of("Testing", "Documentation", "CI/CD"),
                List.of("Domain Knowledge", "Technical Execution"),
                List.of("Practical Applications"),
                List.of("Repository with commit activity and documentation"),
                Map.of("Git", 1.0, "Software Design", 1.0)
        );
    }

    /**
     * Refine role definition with explicit Job Description requirements.
     * Priority: explicit JD > selected role profile > generic baseline.
     */
    public RoleDefinition refineWithJobDescription(RoleDefinition baseRole, String jobDescriptionText) {
        if (jobDescriptionText == null || jobDescriptionText.trim().isEmpty()) {
            return baseRole;
        }

        // Extract skills mentioned in the JD text using taxonomy
        Set<String> jdRequired = new LinkedHashSet<>(baseRole.requiredSkills());
        Set<String> jdPreferred = new LinkedHashSet<>(baseRole.preferredSkills());

        String lowerJD = jobDescriptionText.toLowerCase();
        for (String canonicalSkill : taxonomyService.getAllCanonicalSkills()) {
            String norm = taxonomyService.normalize(canonicalSkill);
            if (lowerJD.contains(canonicalSkill)) {
                if (lowerJD.contains("must have") || lowerJD.contains("required") || lowerJD.contains("proficiency in")) {
                    jdRequired.add(norm);
                } else {
                    jdPreferred.add(norm);
                }
            }
        }

        return new RoleDefinition(
                baseRole.title(),
                baseRole.description() + " [Refined by Job Description]",
                baseRole.category(),
                new ArrayList<>(jdRequired),
                new ArrayList<>(jdPreferred),
                baseRole.expectedCompetencies(),
                baseRole.expectedProjectTypes(),
                baseRole.projectEvidenceExpectations(),
                baseRole.skillWeights()
        );
    }

    public List<RoleDefinition> getAllPredefinedRoles() {
        return new ArrayList<>(predefinedRoles.values());
    }

    public List<RoleDefinition> getAllRoles() {
        return getAllPredefinedRoles();
    }
}
