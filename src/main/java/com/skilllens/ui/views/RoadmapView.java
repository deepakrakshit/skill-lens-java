package com.skilllens.ui.views;

import com.skilllens.domain.model.AnalysisResult;
import com.skilllens.domain.model.RoadmapMonth;
import com.skilllens.ui.NavigationCoordinator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * 4-Month Actionable Career Progression Roadmap View.
 */
public class RoadmapView extends VBox {

    public RoadmapView(NavigationCoordinator coordinator) {
        setSpacing(20);
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

        Label header = new Label("Personalized 4-Month Career Progression Roadmap");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subHeader = new Label("Grounded in verified skill gaps for " + result.targetRole().title() + " with concrete GitHub milestones.");
        subHeader.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-fg-muted;");

        VBox monthsContainer = new VBox(18);
        monthsContainer.setPadding(new Insets(10, 0, 10, 0));

        for (RoadmapMonth month : result.careerRoadmap()) {
            VBox monthCard = createMonthCard(month);
            monthsContainer.getChildren().add(monthCard);
        }

        ScrollPane scrollPane = new ScrollPane(monthsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, subHeader, scrollPane);
    }

    private VBox createMonthCard(RoadmapMonth month) {
        VBox card = new VBox(10);
        card.setStyle("-fx-padding: 18; -fx-background-color: -color-bg-subtle; -fx-background-radius: 8; -fx-border-color: -color-border-subtle; -fx-border-radius: 8;");

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label("MONTH " + month.monthNumber());
        badge.setStyle("-fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-on-emphasis; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 4; -fx-font-size: 11px;");

        Label title = new Label(month.title());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        topRow.getChildren().addAll(badge, title);

        Label summary = new Label(month.summary());
        summary.setWrapText(true);
        summary.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-fg-default;");

        // Focus skills
        Label skillsLbl = new Label("Focus Competencies: " + String.join(" • ", month.focusSkills()));
        skillsLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: -color-accent-fg;");

        // Practical tasks
        VBox tasksBox = new VBox(4);
        Label tasksTitle = new Label("Hands-On Implementation Tasks:");
        tasksTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        tasksBox.getChildren().add(tasksTitle);
        for (String task : month.practicalWork()) {
            Label taskItem = new Label("•  " + task);
            taskItem.setWrapText(true);
            taskItem.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-fg-muted;");
            tasksBox.getChildren().add(taskItem);
        }

        // GitHub Milestone
        HBox milestoneBox = new HBox(8);
        milestoneBox.setAlignment(Pos.CENTER_LEFT);
        milestoneBox.setStyle("-fx-padding: 8 12; -fx-background-color: -color-bg-default; -fx-background-radius: 6;");

        Label milestoneIcon = new Label("GitHub Milestone:");
        milestoneIcon.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label milestoneDesc = new Label(month.gitHubMilestone());
        milestoneDesc.setStyle("-fx-font-size: 12px;");

        milestoneBox.getChildren().addAll(milestoneIcon, milestoneDesc);

        card.getChildren().addAll(topRow, summary, skillsLbl, tasksBox, milestoneBox);
        return card;
    }
}
