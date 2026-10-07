package com.skilllens.service.resume;

import com.skilllens.domain.model.CandidateProfile;
import com.skilllens.domain.model.ResumeTemplateType;
import com.skilllens.domain.model.RewrittenResume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentGenerationTest {

    private PdfGeneratorService pdfGenerator;
    private DocxGeneratorService docxGenerator;

    private RewrittenResume sampleResume;
    private CandidateProfile sampleProfile;

    @BeforeEach
    void setUp() {
        pdfGenerator = new PdfGeneratorService();
        docxGenerator = new DocxGeneratorService();

        sampleProfile = new CandidateProfile("test-cand-1");
        sampleProfile.setFullName("Alex Morgan");
        sampleProfile.setEmail("alex.morgan@example.com");
        sampleProfile.setPhone("+1-555-0199");
        sampleProfile.setGithubUsername("alexmorgan-dev");

        sampleResume = new RewrittenResume(
                "test-cand-1",
                "Senior Backend Engineer",
                "Results-oriented backend software engineer with verified expertise in Java and Spring Boot.",
                List.of(
                        new RewrittenResume.RewrittenExperience(
                                "Acme Cloud Corp",
                                "Backend Engineer",
                                "2022 - Present",
                                List.of("Built APIs"),
                                List.of(
                                        "Architected high-throughput ingestion pipelines using Spring Boot and PostgreSQL",
                                        "Engineered automated integration tests achieving 92% code coverage"
                                ),
                                List.of("Java", "Spring Boot", "PostgreSQL")
                        )
                ),
                List.of(
                        new RewrittenResume.RewrittenProject(
                                "Distributed Cache Engine",
                                "https://github.com/alexmorgan-dev/cache-engine",
                                "In-memory cache",
                                "Custom distributed key-value store with raft consensus.",
                                List.of(
                                        "Implemented consistent hashing and concurrent memory pools in Java 25",
                                        "Authored complete benchmark suite verifying sub-millisecond p99 latencies"
                                ),
                                List.of("Java", "Concurrency")
                        )
                ),
                List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Git"),
                ResumeTemplateType.SOFTWARE_ENGINEER,
                Instant.now()
        );
    }

    @Test
    @DisplayName("Generates valid ATS-friendly PDF document with OpenPDF")
    void testPdfGeneration(@TempDir Path tempDir) throws Exception {
        byte[] pdfBytes = pdfGenerator.generatePdf(sampleResume, sampleProfile, ResumeTemplateType.SOFTWARE_ENGINEER);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "PDF byte array must not be empty");

        File pdfFile = tempDir.resolve("test-resume.pdf").toFile();
        pdfGenerator.generatePdfToFile(sampleResume, sampleProfile, ResumeTemplateType.SOFTWARE_ENGINEER, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 500);
    }

    @Test
    @DisplayName("Generates valid ATS-friendly DOCX document with Apache POI")
    void testDocxGeneration(@TempDir Path tempDir) throws Exception {
        byte[] docxBytes = docxGenerator.generateDocx(sampleResume, sampleProfile, ResumeTemplateType.CLASSIC_PROFESSIONAL);
        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 500, "DOCX byte array must not be empty");

        File docxFile = tempDir.resolve("test-resume.docx").toFile();
        docxGenerator.generateDocxToFile(sampleResume, sampleProfile, ResumeTemplateType.CLASSIC_PROFESSIONAL, docxFile);
        assertTrue(docxFile.exists());
        assertTrue(docxFile.length() > 500);
    }
}
