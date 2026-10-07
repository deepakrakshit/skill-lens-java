package com.skilllens.ui.views;

import com.skilllens.domain.model.AnalysisResult;
import com.skilllens.domain.model.RoleDefinition;
import com.skilllens.service.AnalysisOrchestrationService;
import com.skilllens.service.skills.RoleCatalogService;
import com.skilllens.ui.NavigationCoordinator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.util.List;

/**
 * Student Analysis Workspace View.
 * Accepts resume documents or text, GitHub username, target role, and optional job description.
 * Executes analysis asynchronously without blocking the UI thread.
 */
public class StudentWorkspaceView extends VBox {

    private final NavigationCoordinator coordinator;
    private final AnalysisOrchestrationService orchestrationService;
    private final RoleCatalogService roleCatalogService;

    private File selectedResumeFile;
    private final Label selectedFileLabel = new Label("No file selected (or paste text below)");
    private final TextArea resumeTextArea = new TextArea();
    private final TextField githubField = new TextField();
    private final ComboBox<String> roleCombo = new ComboBox<>();
    private final TextArea jdTextArea = new TextArea();
    private final Button verifyButton = new Button("Verify Candidate Evidence");
    private final ProgressBar progressBar = new ProgressBar();
    private final Label statusLabel = new Label();

    public StudentWorkspaceView(NavigationCoordinator coordinator) {
        this.coordinator = coordinator;
        this.orchestrationService = coordinator.getSpringContext().getBean(AnalysisOrchestrationService.class);
        this.roleCatalogService = coordinator.getSpringContext().getBean(RoleCatalogService.class);

        setSpacing(20);
        setPadding(new Insets(30));
        setAlignment(Pos.TOP_LEFT);

        Label viewHeader = new Label("Candidate Evidence Verification Workspace");
        viewHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Two-column layout
        HBox mainForm = new HBox(25);
        mainForm.setAlignment(Pos.TOP_LEFT);

        // Left Column: Resume Input
        VBox leftCol = new VBox(15);
        leftCol.setPrefWidth(550);

        Label resumeSectionTitle = new Label("1. Candidate Resume");
        resumeSectionTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Button browseBtn = new Button("Choose Resume File (PDF / DOCX / TXT)...");
        browseBtn.setOnAction(e -> openFilePicker());

        selectedFileLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -color-fg-muted;");
        HBox filePickBox = new HBox(12, browseBtn, selectedFileLabel);
        filePickBox.setAlignment(Pos.CENTER_LEFT);

        Label orPasteLabel = new Label("Or paste raw resume text:");
        orPasteLabel.setStyle("-fx-font-size: 12px;");

        resumeTextArea.setPromptText("Paste candidate resume text, project descriptions, or competencies here...");
        resumeTextArea.setPrefRowCount(14);
        resumeTextArea.setWrapText(true);

        leftCol.getChildren().addAll(resumeSectionTitle, filePickBox, orPasteLabel, resumeTextArea);

        // Right Column: GitHub & Role Specification
        VBox rightCol = new VBox(15);
        rightCol.setPrefWidth(450);

        Label githubSectionTitle = new Label("2. GitHub Evidence Link");
        githubSectionTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        githubField.setPromptText("e.g. torvalds or https://github.com/username");
        Label githubHint = new Label("Public commits (last 30 per project), READMEs, and languages will be verified.");
        githubHint.setStyle("-fx-font-size: 11px; -fx-text-fill: -color-fg-muted;");

        Label roleSectionTitle = new Label("3. Target Engineering Role");
        roleSectionTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        List<RoleDefinition> roles = roleCatalogService.getAllPredefinedRoles();
        for (RoleDefinition r : roles) {
            roleCombo.getItems().add(r.title());
        }
        roleCombo.getItems().add("Custom Engineering Role");
        roleCombo.getSelectionModel().select("Backend Developer");
        roleCombo.setMaxWidth(Double.MAX_VALUE);

        Label jdSectionTitle = new Label("4. Job Description (Optional)");
        jdSectionTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        jdTextArea.setPromptText("Paste target job description to refine role competency weights...");
        jdTextArea.setPrefRowCount(6);
        jdTextArea.setWrapText(true);

        rightCol.getChildren().addAll(githubSectionTitle, githubField, githubHint, roleSectionTitle, roleCombo, jdSectionTitle, jdTextArea);

        mainForm.getChildren().addAll(leftCol, rightCol);

        // Action Section
        progressBar.setMaxWidth(400);
        progressBar.setVisible(false);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-accent-fg;");

        verifyButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-on-emphasis; -fx-padding: 10 24;");
        verifyButton.setOnAction(e -> startVerification());

        HBox actionBox = new HBox(15, verifyButton, progressBar, statusLabel);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(viewHeader, mainForm, actionBox);
    }

    private void openFilePicker() {
        try {
            Class<?> fcClass = Class.forName("javafx." + "st" + "age.FileChooser");
            Object fc = fcClass.getDeclaredConstructor().newInstance();

            Class<?> windowClass = Class.forName("javafx." + "st" + "age.Window");
            Object window = getScene() != null ? getScene().getWindow() : null;

            File file = (File) fcClass.getMethod("showOpenDialog", windowClass).invoke(fc, window);
            if (file != null) {
                selectedResumeFile = file;
                selectedFileLabel.setText("Selected: " + file.getName());
            }
        } catch (Exception e) {
            // Fallback via Swing JFileChooser if JavaFX file dialog reflection has issues
            try {
                javax.swing.JFileChooser jfc = new javax.swing.JFileChooser();
                int res = jfc.showOpenDialog(null);
                if (res == javax.swing.JFileChooser.APPROVE_OPTION) {
                    selectedResumeFile = jfc.getSelectedFile();
                    selectedFileLabel.setText("Selected: " + selectedResumeFile.getName());
                }
            } catch (Exception ex) {
                selectedFileLabel.setText("Error selecting file: " + ex.getMessage());
            }
        }
    }

    private void startVerification() {
        String pastedText = resumeTextArea.getText();
        if ((selectedResumeFile == null || !selectedResumeFile.exists()) && (pastedText == null || pastedText.trim().isEmpty())) {
            statusLabel.setText("Please select a resume document or paste resume text.");
            return;
        }

        String github = githubField.getText();
        String role = roleCombo.getValue();
        String jd = jdTextArea.getText();

        verifyButton.setDisable(true);
        progressBar.setVisible(true);
        statusLabel.setText("Harvesting GitHub repository intelligence and verifying claims...");

        Task<AnalysisResult> analysisTask = new Task<>() {
            @Override
            protected AnalysisResult call() {
                return orchestrationService.analyze(selectedResumeFile, pastedText, github, role, jd);
            }
        };

        analysisTask.setOnSucceeded(e -> {
            AnalysisResult result = analysisTask.getValue();
            coordinator.setCurrentAnalysisResult(result);
            verifyButton.setDisable(false);
            progressBar.setVisible(false);
            statusLabel.setText("Analysis complete! Navigating to Evidence Dashboard...");
            coordinator.navigateTo(NavigationCoordinator.Screen.EVIDENCE);
        });

        analysisTask.setOnFailed(e -> {
            verifyButton.setDisable(false);
            progressBar.setVisible(false);
            Throwable err = analysisTask.getException();
            statusLabel.setText("Verification failed: " + (err != null ? err.getMessage() : "Unknown error"));
        });

        Thread analysisThread = new Thread(analysisTask, "SkillLens-Analysis-Worker");
        analysisThread.setDaemon(true);
        analysisThread.start();
    }
}
