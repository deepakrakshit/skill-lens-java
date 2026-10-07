package com.skilllens.ui.views;

import com.skilllens.domain.model.RecruiterBatch;
import com.skilllens.domain.model.RecruiterCandidateAssessment;
import com.skilllens.service.recruiter.RecruiterService;
import com.skilllens.ui.NavigationCoordinator;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Recruiter Workspace View.
 * Enables candidate batch evaluation and ranking with fairness safeguards.
 */
public class RecruiterWorkspaceView extends VBox {

    private final NavigationCoordinator coordinator;
    private final RecruiterService recruiterService;

    private final TextField roleTitleField = new TextField();
    private final TextArea jobDescArea = new TextArea();
    private final TableView<RecruiterCandidateAssessment> candidateTable = new TableView<>();
    private final Label statusLabel = new Label();

    public RecruiterWorkspaceView(NavigationCoordinator coordinator) {
        this.coordinator = coordinator;
        this.recruiterService = coordinator.getSpringContext().getBean(RecruiterService.class);

        setSpacing(20);
        setPadding(new Insets(30));
        setAlignment(Pos.TOP_LEFT);

        Label header = new Label("Recruiter Candidate Evaluation Workspace");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Fairness Invariant Banner
        VBox banner = new VBox(6);
        banner.setStyle("-fx-padding: 12 16; -fx-background-color: -color-bg-subtle; -fx-border-color: -color-accent-emphasis; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label bannerTitle = new Label("Candidate Fairness Policy");
        bannerTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: -color-accent-fg;");

        Label bannerText = new Label("Candidates who do not provide a public GitHub profile are transparently marked as 'NOT_PROVIDED' and evaluated on available resume evidence. They are never penalized with negative evidence.");
        bannerText.setWrapText(true);
        bannerText.setStyle("-fx-font-size: 12px;");

        banner.getChildren().addAll(bannerTitle, bannerText);

        // Batch Setup Controls
        HBox setupBox = new HBox(15);
        setupBox.setAlignment(Pos.CENTER_LEFT);

        roleTitleField.setPromptText("Target Role Title (e.g. Senior Backend Engineer)");
        roleTitleField.setPrefWidth(300);

        Button createBatchBtn = new Button("Create New Batch");
        createBatchBtn.setStyle("-fx-font-weight: bold; -fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-on-emphasis;");
        createBatchBtn.setOnAction(e -> handleCreateBatch());

        setupBox.getChildren().addAll(new Label("Role:"), roleTitleField, createBatchBtn, statusLabel);

        // Candidates Table
        setupCandidateTable();
        VBox.setVgrow(candidateTable, Priority.ALWAYS);

        loadInitialBatchData();

        getChildren().addAll(header, banner, setupBox, candidateTable);
    }

    private void setupCandidateTable() {
        candidateTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RecruiterCandidateAssessment, String> nameCol = new TableColumn<>("Candidate Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().candidateName()));

        TableColumn<RecruiterCandidateAssessment, String> ghCol = new TableColumn<>("GitHub Handle");
        ghCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().githubUsername() != null ? "@" + d.getValue().githubUsername() : "Not Provided"
        ));

        TableColumn<RecruiterCandidateAssessment, String> statusCol = new TableColumn<>("GitHub Evidence Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().gitHubEvidenceStatus()));

        TableColumn<RecruiterCandidateAssessment, String> scoreCol = new TableColumn<>("Role Fit Score");
        scoreCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.1f / 100", d.getValue().roleFitScore())));

        TableColumn<RecruiterCandidateAssessment, String> verifiedCol = new TableColumn<>("Verified Skills");
        verifiedCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().verifiedSkillsCount())));

        TableColumn<RecruiterCandidateAssessment, String> recCol = new TableColumn<>("Recommendation");
        recCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().recommendation()));

        candidateTable.getColumns().addAll(nameCol, ghCol, statusCol, scoreCol, verifiedCol, recCol);
    }

    private void handleCreateBatch() {
        String title = roleTitleField.getText();
        if (title == null || title.trim().isEmpty()) {
            statusLabel.setText("Please enter a role title.");
            return;
        }

        RecruiterBatch batch = recruiterService.createBatch(title, jobDescArea.getText());
        statusLabel.setText("Created evaluation batch: " + batch.getRoleTitle());
    }

    private void loadInitialBatchData() {
        List<RecruiterBatch> batches = recruiterService.getAllBatches();
        if (!batches.isEmpty()) {
            List<RecruiterCandidateAssessment> assessments = recruiterService.getRankedCandidatesForBatch(batches.getFirst().getId());
            candidateTable.getItems().addAll(assessments);
        }
    }
}
