package com.skilllens.ui.views;

import com.skilllens.domain.model.AnalysisResult;
import com.skilllens.domain.model.GitHubRepoData;
import com.skilllens.domain.model.SkillVerificationRecord;
import com.skilllens.domain.model.VerificationStatus;
import com.skilllens.ui.NavigationCoordinator;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Evidence Matrix & Verification Dashboard View.
 * Displays 5-tier skill status, evidence confidence, matched commits, and repositories.
 */
public class EvidenceDashboardView extends VBox {

    public EvidenceDashboardView(NavigationCoordinator coordinator) {
        setSpacing(20);
        setPadding(new Insets(30));
        setAlignment(Pos.TOP_LEFT);

        AnalysisResult result = coordinator.getCurrentAnalysisResult();
        if (result == null) {
            Label placeholder = new Label("No analysis result available yet. Please run an analysis in the Student Workspace.");
            placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-muted;");
            Button goToWorkspaceBtn = new Button("Go to Student Workspace");
            goToWorkspaceBtn.setOnAction(e -> coordinator.navigateTo(NavigationCoordinator.Screen.STUDENT));
            getChildren().addAll(placeholder, goToWorkspaceBtn);
            return;
        }

        // Candidate Profile Header
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-padding: 15; -fx-background-color: -color-bg-subtle; -fx-background-radius: 8;");

        VBox candidateInfo = new VBox(4);
        Label nameLabel = new Label(result.candidate().getFullName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        String ghText = result.candidate().getGithubUsername() != null ? "GitHub: @" + result.candidate().getGithubUsername() : "GitHub: Not Provided";
        Label metaLabel = new Label(result.targetRole().title() + "  |  " + ghText);
        metaLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-fg-muted;");
        candidateInfo.getChildren().addAll(nameLabel, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewRoleFitBtn = new Button("View Role Fit (Score: " + (int) result.roleFit().overallScore() + "%)");
        viewRoleFitBtn.setStyle("-fx-font-weight: bold; -fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-on-emphasis;");
        viewRoleFitBtn.setOnAction(e -> coordinator.navigateTo(NavigationCoordinator.Screen.ROLE_FIT));

        headerBox.getChildren().addAll(candidateInfo, spacer, viewRoleFitBtn);

        // Verification Status Summary Badges
        HBox summaryBadges = new HBox(15);
        summaryBadges.getChildren().addAll(
                createStatusBadge("VERIFIED", countStatus(result.verificationMatrix(), VerificationStatus.VERIFIED), "#22c55e"),
                createStatusBadge("PARTIAL", countStatus(result.verificationMatrix(), VerificationStatus.PARTIALLY_VERIFIED), "#3b82f6"),
                createStatusBadge("WEAK EVIDENCE", countStatus(result.verificationMatrix(), VerificationStatus.WEAK_EVIDENCE), "#eab308"),
                createStatusBadge("UNVERIFIED", countStatus(result.verificationMatrix(), VerificationStatus.CLAIMED_UNVERIFIED), "#f97316"),
                createStatusBadge("MISSING", countStatus(result.verificationMatrix(), VerificationStatus.MISSING), "#ef4444")
        );

        // Tabs: Skills Verification Table vs Repositories Table
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Tab 1: Skills Verification Table
        Tab skillsTab = new Tab("Skills Verification Matrix (" + result.verificationMatrix().size() + ")");
        TableView<SkillVerificationRecord> skillsTable = createSkillsTable(result.verificationMatrix());
        skillsTab.setContent(skillsTable);

        // Tab 2: Harvested GitHub Repositories
        Tab reposTab = new Tab("Harvested Public Repositories (" + result.gitHubRepositories().size() + ")");
        TableView<GitHubRepoData> reposTable = createReposTable(result.gitHubRepositories());
        reposTab.setContent(reposTable);

        tabPane.getTabs().addAll(skillsTab, reposTab);

        getChildren().addAll(headerBox, summaryBadges, tabPane);
    }

    private int countStatus(List<SkillVerificationRecord> records, VerificationStatus status) {
        return (int) records.stream().filter(r -> r.status() == status).count();
    }

    private HBox createStatusBadge(String title, int count, String hexColor) {
        HBox badge = new HBox(8);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-padding: 8 16; -fx-background-color: -color-bg-subtle; -fx-border-color: " + hexColor + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1.5;");

        Label countLbl = new Label(String.valueOf(count));
        countLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + hexColor + ";");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        badge.getChildren().addAll(countLbl, titleLbl);
        return badge;
    }

    private TableView<SkillVerificationRecord> createSkillsTable(List<SkillVerificationRecord> records) {
        TableView<SkillVerificationRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SkillVerificationRecord, String> skillCol = new TableColumn<>("Skill");
        skillCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().canonicalSkill()));
        skillCol.setMaxWidth(160);

        TableColumn<SkillVerificationRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status().name()));
        statusCol.setMaxWidth(160);

        TableColumn<SkillVerificationRecord, String> roleNeedCol = new TableColumn<>("Role Priority");
        roleNeedCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isRoleRequired() ? "REQUIRED" : (data.getValue().isRolePreferred() ? "PREFERRED" : "EXTRA")
        ));
        roleNeedCol.setMaxWidth(120);

        TableColumn<SkillVerificationRecord, String> confidenceCol = new TableColumn<>("Confidence");
        confidenceCol.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.0f%%", data.getValue().confidenceScore() * 100)
        ));
        confidenceCol.setMaxWidth(100);

        TableColumn<SkillVerificationRecord, String> evidenceCol = new TableColumn<>("GitHub Evidence Summary");
        evidenceCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().gitHubEvidenceSummary()));

        table.getColumns().addAll(skillCol, statusCol, roleNeedCol, confidenceCol, evidenceCol);
        table.getItems().addAll(records);
        return table;
    }

    private TableView<GitHubRepoData> createReposTable(List<GitHubRepoData> repos) {
        TableView<GitHubRepoData> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<GitHubRepoData, String> nameCol = new TableColumn<>("Repository");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        nameCol.setMaxWidth(200);

        TableColumn<GitHubRepoData, String> langCol = new TableColumn<>("Primary Language");
        langCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().primaryLanguage() != null ? data.getValue().primaryLanguage() : "N/A"));
        langCol.setMaxWidth(150);

        TableColumn<GitHubRepoData, String> relCol = new TableColumn<>("Relevance");
        relCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.0f%%", data.getValue().relevanceScore() * 100)));
        relCol.setMaxWidth(110);

        TableColumn<GitHubRepoData, String> descCol = new TableColumn<>("Description / Relevance Reasons");
        descCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().description() != null ? data.getValue().description() : String.join(", ", data.getValue().relevanceReasons())
        ));

        table.getColumns().addAll(nameCol, langCol, relCol, descCol);
        table.getItems().addAll(repos);
        return table;
    }
}
