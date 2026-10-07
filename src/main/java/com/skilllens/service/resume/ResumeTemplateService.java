package com.skilllens.service.resume;

import com.skilllens.domain.model.CandidateProfile;
import com.skilllens.domain.model.ResumeTemplateType;
import com.skilllens.domain.model.RewrittenResume;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Service coordinating ATS-friendly resume template presentation and export operations.
 */
@Service
public class ResumeTemplateService {

    private final PdfGeneratorService pdfGenerator;
    private final DocxGeneratorService docxGenerator;

    public ResumeTemplateService(PdfGeneratorService pdfGenerator, DocxGeneratorService docxGenerator) {
        this.pdfGenerator = pdfGenerator;
        this.docxGenerator = docxGenerator;
    }

    public List<ResumeTemplateType> getAvailableTemplates() {
        return Arrays.asList(ResumeTemplateType.values());
    }

    public byte[] exportToPdf(RewrittenResume rewrittenResume, CandidateProfile profile, ResumeTemplateType templateType) throws Exception {
        return pdfGenerator.generatePdf(rewrittenResume, profile, templateType);
    }

    public void exportToPdfFile(RewrittenResume rewrittenResume, CandidateProfile profile, ResumeTemplateType templateType, File targetFile) throws Exception {
        pdfGenerator.generatePdfToFile(rewrittenResume, profile, templateType, targetFile);
    }

    public byte[] exportToDocx(RewrittenResume rewrittenResume, CandidateProfile profile, ResumeTemplateType templateType) throws IOException {
        return docxGenerator.generateDocx(rewrittenResume, profile, templateType);
    }

    public void exportToDocxFile(RewrittenResume rewrittenResume, CandidateProfile profile, ResumeTemplateType templateType, File targetFile) throws IOException {
        docxGenerator.generateDocxToFile(rewrittenResume, profile, templateType, targetFile);
    }
}
