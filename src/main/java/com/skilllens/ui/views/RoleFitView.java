package com.skilllens.ui.views;

import com.skilllens.domain.model.AnalysisResult;
import com.skilllens.domain.model.RoleFitResult;
import com.skilllens.domain.model.VerificationInsight;
import com.skilllens.ui.NavigationCoordinator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

/**
 * Role Fit & Skill Gaps View.
 * Displays mathematical role-fit breakdown, exaggeration alerts, and insights.
 */
public class RoleFitView extends VBox {

    public RoleFitView(NavigationCoordinator coordinator) {
        setSpacing(25);
        setPadding(new Insets(30));
        setAlignment(Pos.TOP_LEFT);

        AnalysisResult result = coordinator.getCurrentAnalysisResult();
        if (result == null) {
            Label placeholder = new Label("No candidate analysis available. Please run an analysis in the Student Workspace first.");
            placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-muted;");
            Button goToBtn = new Button("Go to Student Workspace");
            goToBtn.setOnAction(e -> coordinator.navigateTo(NavigationCoordinator.Screen.STUDENT));
            getChildren().addAll(placeholder, goToBtn);
            return;
        }

        RoleFitResult fit = result.roleFit();

        // Header
        HBox topBox = new HBox(20);
        topBox.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label header = new Label("Role Readiness & Gap Intelligence");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        Label subHeader = new Label("Target: " + result.targetRole().title());
        subHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-muted;");
        titleBox.getChildren().addAll(header, subHeader);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button roadmapBtn = new Button("View 4-Month Roadmap");
        roadmapBtn.setStyle("-fx-font-weight: bold; -fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-on-emphasis;");
        roadmapBtn.setOnAction(e -> coordinator.navigateTo(NavigationCoordinator.Screen.ROADMAP));

        topBox.getChildren().addAll(titleBox, spacer, roadmapBtn);

        // Score Hero Card + Metrics Breakdown
        HBox scoreSection = new HBox(30);
        scoreSection.setAlignment(Pos.CENTER_LEFT);

        // Overall Score Hero
        VBox heroCard = new VBox(8);
        heroCard.setAlignment(Pos.CENTER);
        heroCard.setPrefWidth(220);
        heroCard.setStyle("-fx-padding: 25; -fx-background-color: -color-bg-subtle; -fx-background-radius: 10; -fx-border-color: -color-accent-emphasis; -fx-border-radius: 10; -fx-border-width: 2;");

        Label scoreNum = new Label(String.format("%.1f", fit.overallScore()));
        scoreNum.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: -color-accent-fg;");

        Label scoreLabel = new Label("Role Fit Score (/100)");
        scoreLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        heroCard.getChildren().addAll(scoreNum, scoreLabel);

        // Formula Breakdown Metrics
        VBox breakdownCard = new VBox(10);
        HBox.setHgrow(breakdownCard, Priority.ALWAYS);
        breakdownCard.setStyle("-fx-padding: 20; -fx-background-color: -color-bg-subtle; -fx-background-radius: 8;");

        breakdownCard.getChildren().addAll(
                createMetricBar("Required Skills Coverage (40% Weight)", fit.requiredSkillCoverage()),
                createMetricBar("Verified Evidence Coverage (25% Weight)", fit.verifiedSkillCoverage()),
                createMetricBar("Preferred Skills Coverage (15% Weight)", fit.preferredSkillCoverage()),
                createMetricBar("Project Alignment (10% Weight)", fit.projectAlignmentScore()),
                createMetricBar("Verification Confidence (10% Weight)", fit.evidenceConfidenceScore())
        );

        scoreSection.getChildren().addAll(heroCard, breakdownCard);

        // Verification Insights & Exaggeration Alerts
        VBox insightsBox = new VBox(12);
        Label insightsTitle = new Label("Actionable Verification Insights & Evidence Alerts");
        insightsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        insightsBox.getChildren().add(insightsTitle);

        if (result.verificationInsights().isEmpty()) {
            Label noInsights = new Label("No critical discrepancies detected.");
            insightsBox.getChildren().add(noInsights);
        } else {
            for (VerificationInsight insight : result.verificationInsights()) {
                VBox card = new VBox(4);
                card.setStyle("-fx-padding: 12 16; -fx-background-radius: 6; -fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-muted; -fx-border-radius: 6;");

                Label cardTitle = new Label(insight.title());
                cardTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                Label cardDesc = new Label(insight.description());
                cardDesc.setWrapText(true);
                cardDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-fg-muted;");

                card.getChildren().addAll(cardTitle, cardDesc);
                insightsBox.getChildren().add(card);
            }
        }

        getChildren().addAll(topBox, scoreSection, insightsBox);
    }

    private VBox createMetricBar(String labelText, double value) {
        VBox box = new VBox(3);
        HBox header = new HBox();
        Label nameLbl = new Label(labelText);
        nameLbl.setStyle("-fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valLbl = new Label(String.format("%.1f%%", value));
        valLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        header.getChildren().addAll(nameLbl, spacer, valLbl);

        ProgressBar bar = new ProgressBar(value / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().addAll(header, bar);
        return box;
    }
}
