package com.skilllens.service.resume;

import com.skilllens.domain.model.CandidateProfile;
import com.skilllens.domain.model.ResumeTemplateType;
import com.skilllens.domain.model.RewrittenResume;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Java-native ATS-friendly DOCX resume generator using Apache POI.
 * Creates clean Word documents with single-flow styling, explicit paragraph runs,
 * and standard section hierarchy for ATS parsing.
 */
@Service
public class DocxGeneratorService {

    public byte[] generateDocx(RewrittenResume rewrittenResume, CandidateProfile profile, ResumeTemplateType templateType) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Header Section
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun nameRun = titlePara.createRun();
            nameRun.setBold(true);
            nameRun.setFontSize(18);
            nameRun.setFontFamily("Arial");
            nameRun.setText(profile != null && profile.getFullName() != null ? profile.getFullName() : "Candidate Name");

            // Role
            XWPFParagraph rolePara = document.createParagraph();
            XWPFRun roleRun = rolePara.createRun();
            roleRun.setFontSize(12);
            roleRun.setBold(true);
            roleRun.setColor("4A5568");
            roleRun.setText(rewrittenResume != null && rewrittenResume.targetRole() != null ? rewrittenResume.targetRole() : "Software Engineer");

            // Contact
            XWPFParagraph contactPara = document.createParagraph();
            XWPFRun contactRun = contactPara.createRun();
            contactRun.setFontSize(10);
            contactRun.setColor("718096");
            String email = profile != null && profile.getEmail() != null ? profile.getEmail() : "email@example.com";
            String phone = profile != null && profile.getPhone() != null ? profile.getPhone() : "";
            String gitHub = profile != null && profile.getGithubUsername() != null ? "github.com/" + profile.getGithubUsername() : "";
            contactRun.setText(String.join("  |  ", java.util.stream.Stream.of(email, phone, gitHub).filter(s -> !s.isEmpty()).toList()));

            // Summary
            if (rewrittenResume != null && rewrittenResume.professionalSummary() != null && !rewrittenResume.professionalSummary().isEmpty()) {
                addSectionHeading(document, "PROFESSIONAL SUMMARY");
                XWPFParagraph p = document.createParagraph();
                XWPFRun r = p.createRun();
                r.setFontSize(10.5);
                r.setText(rewrittenResume.professionalSummary());
            }

            // Verified Skills
            if (rewrittenResume != null && rewrittenResume.prioritizedSkills() != null && !rewrittenResume.prioritizedSkills().isEmpty()) {
                addSectionHeading(document, "VERIFIED TECHNICAL SKILLS");
                XWPFParagraph p = document.createParagraph();
                XWPFRun r = p.createRun();
                r.setFontSize(10.5);
                r.setText("Verified Technologies: " + String.join(" • ", rewrittenResume.prioritizedSkills()));
            }

            // Experience
            if (rewrittenResume != null && rewrittenResume.experiences() != null && !rewrittenResume.experiences().isEmpty()) {
                addSectionHeading(document, "PROFESSIONAL EXPERIENCE");
                for (var exp : rewrittenResume.experiences()) {
                    XWPFParagraph jobHeader = document.createParagraph();
                    XWPFRun tRun = jobHeader.createRun();
                    tRun.setBold(true);
                    tRun.setText(exp.title());
                    if (exp.company() != null && !exp.company().isEmpty()) {
                        XWPFRun cRun = jobHeader.createRun();
                        cRun.setText("  —  " + exp.company());
                    }
                    if (exp.period() != null && !exp.period().isEmpty()) {
                        XWPFRun dRun = jobHeader.createRun();
                        dRun.setItalic(true);
                        dRun.setText(" (" + exp.period() + ")");
                    }

                    for (String bullet : exp.rewrittenBullets()) {
                        XWPFParagraph bPara = document.createParagraph();
                        bPara.setIndentationLeft(360);
                        XWPFRun bRun = bPara.createRun();
                        bRun.setText("•  " + bullet);
                    }
                }
            }

            // Projects
            if (rewrittenResume != null && rewrittenResume.projects() != null && !rewrittenResume.projects().isEmpty()) {
                addSectionHeading(document, "TECHNICAL PROJECTS");
                for (var prj : rewrittenResume.projects()) {
                    XWPFParagraph prjHeader = document.createParagraph();
                    XWPFRun pRun = prjHeader.createRun();
                    pRun.setBold(true);
                    pRun.setText(prj.projectName());
                    if (prj.repositoryUrl() != null && !prj.repositoryUrl().isEmpty()) {
                        XWPFRun uRun = prjHeader.createRun();
                        uRun.setItalic(true);
                        uRun.setText(" (" + prj.repositoryUrl() + ")");
                    }

                    if (prj.rewrittenDescription() != null && !prj.rewrittenDescription().isEmpty()) {
                        XWPFParagraph dPara = document.createParagraph();
                        XWPFRun dRun = dPara.createRun();
                        dRun.setText(prj.rewrittenDescription());
                    }

                    for (String hl : prj.highlights()) {
                        XWPFParagraph bPara = document.createParagraph();
                        bPara.setIndentationLeft(360);
                        XWPFRun bRun = bPara.createRun();
                        bRun.setText("•  " + hl);
                    }
                }
            }

            document.write(baos);
            return baos.toByteArray();
        }
    }

    public void generateDocxToFile(RewrittenResume rewrittenResume, CandidateProfile profile, ResumeTemplateType templateType, File outputFile) throws IOException {
        byte[] docxBytes = generateDocx(rewrittenResume, profile, templateType);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(docxBytes);
        }
    }

    private void addSectionHeading(XWPFDocument document, String title) {
        XWPFParagraph headingPara = document.createParagraph();
        headingPara.setSpacingBefore(200);
        headingPara.setSpacingAfter(60);
        XWPFRun headingRun = headingPara.createRun();
        headingRun.setBold(true);
        headingRun.setFontSize(11);
        headingRun.setColor("1A365D");
        headingRun.setText(title);
    }
}
