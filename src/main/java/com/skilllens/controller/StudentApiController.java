package com.skilllens.controller;

import com.skilllens.domain.model.*;
import com.skilllens.domain.repository.CandidateProfileRepository;
import com.skilllens.service.AnalysisOrchestrationService;
import com.skilllens.service.mentor.MentorshipEngine;
import com.skilllens.service.resume.ResumeRewriteService;
import com.skilllens.service.resume.ResumeTemplateService;
import com.skilllens.service.skills.RoleCatalogService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Student operations:
 * Analysis, Mentorship, Roles, and Resume Engineering.
 */
@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentApiController {

    private final AnalysisOrchestrationService orchestrationService;
    private final RoleCatalogService roleCatalogService;
    private final MentorshipEngine mentorshipEngine;
    private final ResumeRewriteService rewriteService;
    private final ResumeTemplateService templateService;
    private final CandidateProfileRepository profileRepository;

    public StudentApiController(AnalysisOrchestrationService orchestrationService,
                                RoleCatalogService roleCatalogService,
                                MentorshipEngine mentorshipEngine,
                                ResumeRewriteService rewriteService,
                                ResumeTemplateService templateService,
                                CandidateProfileRepository profileRepository) {
        this.orchestrationService = orchestrationService;
        this.roleCatalogService = roleCatalogService;
        this.mentorshipEngine = mentorshipEngine;
        this.rewriteService = rewriteService;
        this.templateService = templateService;
        this.profileRepository = profileRepository;
    }

    public record AnalysisRequest(
            String resumeText,
            String githubUsername,
            String targetRole,
            String jobDescription
    ) {}

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> analyze(@RequestBody AnalysisRequest request) {
        AnalysisResult result = orchestrationService.analyze(
                null,
                request.resumeText(),
                request.githubUsername(),
                request.targetRole(),
                request.jobDescription()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/analyze-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisResult> analyzeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "githubUsername", required = false) String githubUsername,
            @RequestParam(value = "targetRole", required = false, defaultValue = "Backend Developer") String targetRole,
            @RequestParam(value = "jobDescription", required = false) String jobDescription) throws Exception {

        File tempFile = File.createTempFile("resume-upload-", "-" + file.getOriginalFilename());
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        AnalysisResult result = orchestrationService.analyze(
                tempFile,
                null,
                githubUsername,
                targetRole,
                jobDescription
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleDefinition>> getSupportedRoles() {
        return ResponseEntity.ok(roleCatalogService.getAllPredefinedRoles());
    }

    public record MentorAskRequest(
            String candidateId,
            String sessionId,
            String question,
            String targetRole
    ) {}

    @PostMapping("/mentor/ask")
    public ResponseEntity<Map<String, String>> askMentor(@RequestBody MentorAskRequest request) {
        String answer = mentorshipEngine.askMentor(
                request.candidateId(),
                request.sessionId(),
                request.question(),
                request.targetRole(),
                null,
                null
        );
        return ResponseEntity.ok(Map.of("answer", answer));
    }

    public record ResumeRewriteRequest(
            String candidateId,
            String targetRole,
            String templateType
    ) {}

    @PostMapping("/resume/rewrite")
    public ResponseEntity<RewrittenResume> rewriteResume(@RequestBody ResumeRewriteRequest request) {
        CandidateProfile profile = profileRepository.findByCandidateId(request.candidateId()).orElse(null);
        ResumeTemplateType tType = ResumeTemplateType.CLASSIC_PROFESSIONAL;
        if (request.templateType() != null) {
            try {
                tType = ResumeTemplateType.valueOf(request.templateType());
            } catch (Exception ignored) {}
        }

        RewrittenResume rewritten = rewriteService.rewriteResume(
                profile,
                List.of(),
                null,
                request.targetRole(),
                tType
        );
        return ResponseEntity.ok(rewritten);
    }

    @PostMapping("/resume/export-pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestBody RewrittenResume resume) throws Exception {
        CandidateProfile profile = profileRepository.findByCandidateId(resume.candidateId()).orElse(null);
        byte[] pdfBytes = templateService.exportToPdf(resume, profile, resume.templateType());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"verified-resume.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/resume/export-docx")
    public ResponseEntity<byte[]> exportDocx(@RequestBody RewrittenResume resume) throws Exception {
        CandidateProfile profile = profileRepository.findByCandidateId(resume.candidateId()).orElse(null);
        byte[] docxBytes = templateService.exportToDocx(resume, profile, resume.templateType());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"verified-resume.docx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docxBytes);
    }
}
