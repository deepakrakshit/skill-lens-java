package com.skilllens.ui.views;

import com.skilllens.domain.model.AnalysisResult;
import com.skilllens.domain.model.CandidateProfile;
import com.skilllens.domain.model.ResumeTemplateType;
import com.skilllens.domain.model.RewrittenResume;
import com.skilllens.service.resume.ResumeRewriteService;
import com.skilllens.service.resume.ResumeTemplateService;
import com.skilllens.ui.NavigationCoordinator;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.nio.file.Paths;

/**
 * Resume Optimization & ATS Generator View.
 * Provides 10 ATS-friendly templates, XYZ formula bullet preview, and native PDF/DOCX export.
 */
public class ResumeBuilderView extends VBox {

    private final NavigationCoordinator coordinator;
    private final ResumeRewriteService rewriteService;
    private final ResumeTemplateService templateService;

    private final ComboBox<ResumeTemplateType> templateCombo = new ComboBox<>();
    private final TextArea previewTextArea = new TextArea();
    private final Button rewriteButton = new Button("Optimize Bullets (XYZ Formula)");
    private final Button exportPdfBtn = new Button("Export ATS-Friendly PDF");
    private final Button exportDocxBtn = new Button("Export ATS-Friendly DOCX");
    private final Label statusLabel = new Label();
    private final ProgressBar progressBar = new ProgressBar();

    private RewrittenResume currentRewrittenResume;

    public ResumeBuilderView(NavigationCoordinator coordinator) {
        this.coordinator = coordinator;
        this.rewriteService = coordinator.getSpringContext().getBean(ResumeRewriteService.class);
        this.templateService = coordinator.getSpringContext().getBean(ResumeTemplateService.class);

        setSpacing(20);
        setPadding(new Insets(30));
        setAlignment(Pos.TOP_LEFT);

        Label header = new Label("Resume Optimization & ATS Generator");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subHeader = new Label("Transform resume bullets using Google XYZ formula grounded strictly in verified GitHub evidence. Export across 10 ATS-friendly formats.");
        subHeader.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-fg-muted;");

        // Controls bar
        HBox controlsBar = new HBox(15);
        controlsBar.setAlignment(Pos.CENTER_LEFT);

        Label templateLabel = new Label("Template Style:");
        templateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        templateCombo.getItems().addAll(ResumeTemplateType.values());
        templateCombo.getSelectionModel().select(ResumeTemplateType.SOFTWARE_ENGINEER);
        templateCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ResumeTemplateType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName() + " — " + item.getDescription());
            }
        });
        templateCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ResumeTemplateType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });

        rewriteButton.setStyle("-fx-font-weight: bold; -fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-on-emphasis;");
        rewriteButton.setOnAction(e -> executeRewrite());

        exportPdfBtn.setStyle("-fx-font-weight: bold;");
        exportPdfBtn.setOnAction(e -> exportDocument("PDF"));

        exportDocxBtn.setStyle("-fx-font-weight: bold;");
        exportDocxBtn.setOnAction(e -> exportDocument("DOCX"));

        controlsBar.getChildren().addAll(templateLabel, templateCombo, rewriteButton, exportPdfBtn, exportDocxBtn);

        // Preview Area
        previewTextArea.setEditable(false);
        previewTextArea.setWrapText(true);
        previewTextArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");
        VBox.setVgrow(previewTextArea, Priority.ALWAYS);

        // Status bar
        progressBar.setVisible(false);
        progressBar.setMaxWidth(300);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-accent-fg;");

        HBox statusBox = new HBox(12, progressBar, statusLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(header, subHeader, controlsBar, previewTextArea, statusBox);

        // Populate initial preview if analysis exists
        AnalysisResult result = coordinator.getCurrentAnalysisResult();
        if (result != null) {
            executeRewrite();
        } else {
            previewTextArea.setText("No candidate profile available yet. Run an analysis in the Student Workspace to optimize resume bullets.");
        }
    }

    private void executeRewrite() {
        AnalysisResult result = coordinator.getCurrentAnalysisResult();
        if (result == null) {
            statusLabel.setText("Please run an analysis in the Student Workspace first.");
            return;
        }

        rewriteButton.setDisable(true);
        progressBar.setVisible(true);
        statusLabel.setText("Rewriting bullets with Google XYZ formula & non-fabrication constraints...");

        ResumeTemplateType selectedTemplate = templateCombo.getValue();
        CandidateProfile dummyProfile = new CandidateProfile(result.candidate().getId());
        dummyProfile.setFullName(result.candidate().getFullName());
        dummyProfile.setEmail(result.candidate().getEmail());
        dummyProfile.setGithubUsername(result.candidate().getGithubUsername());

        Task<RewrittenResume> task = new Task<>() {
            @Override
            protected RewrittenResume call() {
                return rewriteService.rewriteResume(
                        dummyProfile,
                        result.verificationMatrix(),
                        null,
                        result.targetRole().title(),
                        selectedTemplate
                );
            }
        };

        task.setOnSucceeded(e -> {
            currentRewrittenResume = task.getValue();
            rewriteButton.setDisable(false);
            progressBar.setVisible(false);
            statusLabel.setText("Bullets optimized successfully!");
            displayPreview(currentRewrittenResume);
        });

        task.setOnFailed(e -> {
            rewriteButton.setDisable(false);
            progressBar.setVisible(false);
            statusLabel.setText("Rewrite notice: Using deterministic layout.");
        });

        Thread t = new Thread(task, "SkillLens-ResumeRewrite-Worker");
        t.setDaemon(true);
        t.start();
    }

    private void displayPreview(RewrittenResume resume) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("TARGET ROLE: ").append(resume.targetRole()).append("\n");
        sb.append("TEMPLATE STYLE: ").append(resume.templateType().getDisplayName()).append("\n");
        sb.append("================================================================================\n\n");
        sb.append("PROFESSIONAL SUMMARY:\n").append(resume.professionalSummary()).append("\n\n");
        sb.append("VERIFIED COMPETENCIES:\n").append(String.join(" • ", resume.prioritizedSkills())).append("\n\n");
        sb.append("EXPERIENCE BULLETS (XYZ FORMULA):\n");
        for (var exp : resume.experiences()) {
            sb.append(exp.title()).append(" — ").append(exp.company()).append(" (").append(exp.period()).append(")\n");
            for (String b : exp.rewrittenBullets()) {
                sb.append("  • ").append(b).append("\n");
            }
            sb.append("\n");
        }
        sb.append("PROJECT HIGHLIGHTS:\n");
        for (var prj : resume.projects()) {
            sb.append(prj.projectName()).append(" (").append(prj.repositoryUrl()).append(")\n");
            sb.append("  ").append(prj.rewrittenDescription()).append("\n");
            for (String h : prj.highlights()) {
                sb.append("  • ").append(h).append("\n");
            }
            sb.append("\n");
        }
        previewTextArea.setText(sb.toString());
    }

    private void exportDocument(String format) {
        if (currentRewrittenResume == null) {
            statusLabel.setText("Please optimize resume first before exporting.");
            return;
        }

        AnalysisResult result = coordinator.getCurrentAnalysisResult();
        CandidateProfile dummyProfile = new CandidateProfile(result != null ? result.candidate().getId() : "1");
        if (result != null) {
            dummyProfile.setFullName(result.candidate().getFullName());
            dummyProfile.setEmail(result.candidate().getEmail());
            dummyProfile.setGithubUsername(result.candidate().getGithubUsername());
        }

        try {
            String homeDir = System.getProperty("user.home");
            String fileName = "SkillLens_Optimized_Resume." + format.toLowerCase();
            File outFile = Paths.get(homeDir, fileName).toFile();

            if ("PDF".equalsIgnoreCase(format)) {
                templateService.exportToPdfFile(currentRewrittenResume, dummyProfile, templateCombo.getValue(), outFile);
            } else {
                templateService.exportToDocxFile(currentRewrittenResume, dummyProfile, templateCombo.getValue(), outFile);
            }

            statusLabel.setText("Exported successfully to: " + outFile.getAbsolutePath());
        } catch (Exception e) {
            statusLabel.setText("Export failed: " + e.getMessage());
        }
    }
}
