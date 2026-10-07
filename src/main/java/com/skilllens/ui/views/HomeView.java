package com.skilllens.ui.views;

import com.skilllens.service.security.CredentialManager;
import com.skilllens.ui.NavigationCoordinator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Home & Overview Screen.
 */
public class HomeView extends VBox {

    public HomeView(NavigationCoordinator coordinator) {
        setSpacing(25);
        setPadding(new Insets(35));
        setAlignment(Pos.TOP_LEFT);

        // Header Title & Tagline
        Label titleLabel = new Label("SkillLens");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: -color-accent-fg;");

        Label subtitleLabel = new Label("Evidence-Based Student Skill Verification, Career Roadmapping & Mentorship Platform");
        subtitleLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: -color-fg-muted;");

        VBox headerBox = new VBox(5, titleLabel, subtitleLabel);

        // Core Principle Banner
        VBox bannerBox = new VBox(10);
        bannerBox.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 20; -fx-background-radius: 8; -fx-border-color: -color-border-subtle; -fx-border-radius: 8;");
        Label bannerTitle = new Label("Core Verification Architecture");
        bannerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label bannerText = new Label("SkillLens rigorously contrasts candidate resume claims against real public GitHub commit history and project artifacts, measuring true readiness against industry role standards without fabricating unverified credentials.");
        bannerText.setWrapText(true);
        bannerText.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-fg-default;");
        bannerBox.getChildren().addAll(bannerTitle, bannerText);

        // Quick Navigation Cards
        HBox cardsBox = new HBox(20);
        cardsBox.setAlignment(Pos.CENTER_LEFT);

        VBox studentCard = createActionCard(
                "Student Analysis",
                "Ingest resume, link GitHub profile, and evaluate multi-signal skill verification against role benchmarks.",
                "Start Verification",
                () -> coordinator.navigateTo(NavigationCoordinator.Screen.STUDENT)
        );

        VBox recruiterCard = createActionCard(
                "Recruiter Workspace",
                "Evaluate batches of candidate submissions with fair, evidence-grounded ranking and unverified GitHub fairness safeguards.",
                "Open Workspace",
                () -> coordinator.navigateTo(NavigationCoordinator.Screen.RECRUITER)
        );

        VBox settingsCard = createActionCard(
                "Settings & Vault",
                "Configure DPAPI-secured Gemini & Groq credentials, customize default models, and view system health.",
                "Manage Vault",
                () -> coordinator.navigateTo(NavigationCoordinator.Screen.SETTINGS)
        );

        cardsBox.getChildren().addAll(studentCard, recruiterCard, settingsCard);

        // Telemetry & Status Bar
        CredentialManager credMgr = coordinator.getSpringContext().getBean(CredentialManager.class);
        HBox statusBox = new HBox(25);
        statusBox.setStyle("-fx-padding: 15; -fx-background-color: -color-bg-subtle; -fx-background-radius: 6;");

        boolean geminiOk = credMgr.hasApiKey(CredentialManager.KEY_GEMINI);
        boolean groqOk = credMgr.hasApiKey(CredentialManager.KEY_GROQ);

        Label geminiStatus = new Label("Primary AI (Gemini 3.5 Flash): " + (geminiOk ? "Ready" : "Key Needed"));
        geminiStatus.setStyle(geminiOk ? "-fx-text-fill: -color-success-fg;" : "-fx-text-fill: -color-warning-fg;");

        Label groqStatus = new Label("Fallback AI (Groq GPT-OSS 120B): " + (groqOk ? "Ready" : "Key Needed"));
        groqStatus.setStyle(groqOk ? "-fx-text-fill: -color-success-fg;" : "-fx-text-fill: -color-warning-fg;");

        Label dbStatus = new Label("Embedded DB: Connected (H2 / SQLite)");
        dbStatus.setStyle("-fx-text-fill: -color-success-fg;");

        statusBox.getChildren().addAll(geminiStatus, groqStatus, dbStatus);

        getChildren().addAll(headerBox, bannerBox, cardsBox, statusBox);
    }

    private VBox createActionCard(String title, String desc, String buttonText, Runnable action) {
        VBox card = new VBox(12);
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color: -color-bg-default; -fx-padding: 20; -fx-background-radius: 8; -fx-border-color: -color-border-muted; -fx-border-radius: 8;");

        Label cardTitle = new Label(title);
        cardTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label cardDesc = new Label(desc);
        cardDesc.setWrapText(true);
        cardDesc.setPrefHeight(60);
        cardDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-fg-muted;");

        Button actionBtn = new Button(buttonText);
        actionBtn.setStyle("-fx-font-weight: bold; -fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-on-emphasis;");
        actionBtn.setOnAction(e -> action.run());

        card.getChildren().addAll(cardTitle, cardDesc, actionBtn);
        return card;
    }
}
