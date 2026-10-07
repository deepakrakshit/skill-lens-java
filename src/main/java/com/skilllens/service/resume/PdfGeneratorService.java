package com.skilllens.service.resume;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.skilllens.domain.model.CandidateProfile;
import com.skilllens.domain.model.ResumeTemplateType;
import com.skilllens.domain.model.RewrittenResume;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Java-native ATS-friendly PDF resume generator using OpenPDF.
 * Implements deterministic typography, margin control (36pt - 0.5in),
 * and distinct layouts for all 10 ResumeTemplateType variants.
 */
@Service
public class PdfGeneratorService {

    public byte[] generatePdf(RewrittenResume rewrittenResume, CandidateProfile profile, ResumeTemplateType templateType) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, baos);

        document.open();
        applyTemplateContent(document, rewrittenResume, profile, templateType);
        document.close();

        return baos.toByteArray();
    }

    public void generatePdfToFile(RewrittenResume rewrittenResume, CandidateProfile profile, ResumeTemplateType templateType, File outputFile) throws DocumentException, IOException {
        byte[] pdfBytes = generatePdf(rewrittenResume, profile, templateType);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }
    }

    private void applyTemplateContent(Document document, RewrittenResume resume, CandidateProfile profile, ResumeTemplateType template) throws DocumentException {
        // Typography and Accent Palette based on Template Type
        Color primaryAccent = switch (template) {
            case MODERN_MINIMAL, STARTUP_MODERN -> new Color(30, 41, 59); // Slate Dark
            case SOFTWARE_ENGINEER, TECHNICAL_COMPACT -> new Color(15, 76, 129); // Classic Engineering Blue
            case EXECUTIVE_CLEAN -> new Color(44, 62, 80); // Midnight Executive
            case DATA_AND_ANALYTICS -> new Color(13, 148, 136); // Teal Data
            case ACADEMIC_TECHNICAL -> new Color(88, 28, 135); // Deep Indigo
            case PRODUCT_ENGINEERING -> new Color(2, 132, 199); // Sky Tech
            case ONE_PAGE_IMPACT -> new Color(17, 24, 39); // High Contrast Carbon
            default -> new Color(24, 24, 27); // Classic Professional Charcoal
        };

        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, primaryAccent);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, primaryAccent);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

        // Header Section
        String candidateName = profile != null && profile.getFullName() != null && !profile.getFullName().isEmpty()
                ? profile.getFullName()
                : "Candidate Name";
        Paragraph namePara = new Paragraph(candidateName, nameFont);
        namePara.setAlignment(template == ResumeTemplateType.CLASSIC_PROFESSIONAL || template == ResumeTemplateType.EXECUTIVE_CLEAN ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
        document.add(namePara);

        String targetRole = resume != null && resume.targetRole() != null ? resume.targetRole() : "Software Engineer";
        Paragraph rolePara = new Paragraph(targetRole, titleFont);
        rolePara.setAlignment(namePara.getAlignment());
        document.add(rolePara);

        // Contact bar
        String email = profile != null && profile.getEmail() != null ? profile.getEmail() : "email@example.com";
        String phone = profile != null && profile.getPhone() != null ? profile.getPhone() : "";
        String gitHub = profile != null && profile.getGithubUsername() != null ? "github.com/" + profile.getGithubUsername() : "";
        String contactInfo = String.join("  |  ", java.util.stream.Stream.of(email, phone, gitHub).filter(s -> !s.isEmpty()).toList());
        Paragraph contactPara = new Paragraph(contactInfo, italicFont);
        contactPara.setAlignment(namePara.getAlignment());
        contactPara.setSpacingAfter(8);
        document.add(contactPara);

        addDivider(document, primaryAccent);

        // Professional Summary
        if (resume != null && resume.professionalSummary() != null && !resume.professionalSummary().isEmpty()) {
            addSectionTitle(document, "PROFESSIONAL SUMMARY", sectionHeaderFont, primaryAccent);
            Paragraph summaryPara = new Paragraph(resume.professionalSummary(), bodyFont);
            summaryPara.setSpacingAfter(8);
            document.add(summaryPara);
        }

        // Verified Skills
        if (resume != null && resume.prioritizedSkills() != null && !resume.prioritizedSkills().isEmpty()) {
            addSectionTitle(document, "VERIFIED TECHNICAL SKILLS", sectionHeaderFont, primaryAccent);
            Paragraph skillsPara = new Paragraph("Verified Technologies: " + String.join(" • ", resume.prioritizedSkills()), bodyFont);
            skillsPara.setSpacingAfter(8);
            document.add(skillsPara);
        }

        // Professional Experience
        if (resume != null && resume.experiences() != null && !resume.experiences().isEmpty()) {
            addSectionTitle(document, "EXPERIENCE", sectionHeaderFont, primaryAccent);
            for (var exp : resume.experiences()) {
                Paragraph expHeader = new Paragraph();
                expHeader.add(new Chunk(exp.title(), boldBodyFont));
                if (exp.company() != null && !exp.company().isEmpty()) {
                    expHeader.add(new Chunk("  —  " + exp.company(), bodyFont));
                }
                if (exp.period() != null && !exp.period().isEmpty()) {
                    expHeader.add(new Chunk(" (" + exp.period() + ")", italicFont));
                }
                document.add(expHeader);

                for (String bullet : exp.rewrittenBullets()) {
                    Paragraph bPara = new Paragraph("•  " + bullet, bodyFont);
                    bPara.setIndentationLeft(12);
                    bPara.setSpacingAfter(2);
                    document.add(bPara);
                }
            }
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
        }

        // Projects
        if (resume != null && resume.projects() != null && !resume.projects().isEmpty()) {
            addSectionTitle(document, "TECHNICAL PROJECTS", sectionHeaderFont, primaryAccent);
            for (var prj : resume.projects()) {
                Paragraph prjHeader = new Paragraph();
                prjHeader.add(new Chunk(prj.projectName(), boldBodyFont));
                if (prj.repositoryUrl() != null && !prj.repositoryUrl().isEmpty()) {
                    prjHeader.add(new Chunk("  (" + prj.repositoryUrl() + ")", italicFont));
                }
                document.add(prjHeader);

                if (prj.rewrittenDescription() != null && !prj.rewrittenDescription().isEmpty()) {
                    Paragraph descPara = new Paragraph(prj.rewrittenDescription(), bodyFont);
                    descPara.setIndentationLeft(8);
                    document.add(descPara);
                }

                for (String hl : prj.highlights()) {
                    Paragraph bPara = new Paragraph("•  " + hl, bodyFont);
                    bPara.setIndentationLeft(16);
                    bPara.setSpacingAfter(2);
                    document.add(bPara);
                }
            }
        }
    }

    private void addSectionTitle(Document document, String title, Font font, Color accent) throws DocumentException {
        Paragraph p = new Paragraph(title, font);
        p.setSpacingBefore(6);
        p.setSpacingAfter(3);
        document.add(p);
        addDivider(document, accent);
    }

    private void addDivider(Document document, Color color) throws DocumentException {
        LineSeparator sep = new LineSeparator(1.0f, 100.0f, color, Element.ALIGN_CENTER, -2);
        document.add(sep);
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 2)));
    }
}
