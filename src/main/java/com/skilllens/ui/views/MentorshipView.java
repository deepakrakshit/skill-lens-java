package com.skilllens.ui.views;

import com.skilllens.domain.model.AnalysisResult;
import com.skilllens.service.mentor.MentorshipEngine;
import com.skilllens.ui.NavigationCoordinator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Conversational Career Mentorship View.
 * Grounded in local RAG retrieved evidence, resume claims, and GitHub commit activity.
 */
public class MentorshipView extends VBox {

    private final NavigationCoordinator coordinator;
    private final MentorshipEngine mentorshipEngine;

    private final VBox chatMessagesBox = new VBox(15);
    private final ScrollPane chatScrollPane = new ScrollPane(chatMessagesBox);
    private final TextField inputField = new TextField();
    private final Button sendButton = new Button("Send");
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    public MentorshipView(NavigationCoordinator coordinator) {
        this.coordinator = coordinator;
        this.mentorshipEngine = coordinator.getSpringContext().getBean(MentorshipEngine.class);

        setSpacing(15);
        setPadding(new Insets(25));
        setAlignment(Pos.TOP_LEFT);

        Label header = new Label("AI Career Mentor & Technical Advisor");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subHeader = new Label("Grounded in your verified GitHub evidence and roadmap. Ask questions about skill gaps, project architecture, and interview prep.");
        subHeader.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-fg-muted;");

        // Chat Container
        chatMessagesBox.setPadding(new Insets(15));
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setStyle("-fx-background-color: -color-bg-subtle; -fx-background: -color-bg-subtle; -fx-background-radius: 8;");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        // Initial welcome message
        AnalysisResult result = coordinator.getCurrentAnalysisResult();
        String initialGreeting = result != null
                ? String.format("Hello %s! I've analyzed your resume and GitHub profile for the %s role. Your current Role Fit is %.1f/100. How can I assist you with your roadmap or technical projects today?",
                result.candidate().getFullName(), result.targetRole().title(), result.roleFit().overallScore())
                : "Hello! I am your SkillLens Career Mentor. Run a candidate analysis in the Student Workspace so I can ground advice in your actual GitHub projects and resume claims.";

        addMessageBubble("MENTOR", initialGreeting);

        // Input bar
        inputField.setPromptText("Ask a question about your verified skills, roadmap, or technical concepts...");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setOnAction(e -> sendMessage());

        progressIndicator.setMaxSize(24, 24);
        progressIndicator.setVisible(false);

        sendButton.setStyle("-fx-font-weight: bold; -fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-on-emphasis;");
        sendButton.setOnAction(e -> sendMessage());

        HBox inputBar = new HBox(12, inputField, progressIndicator, sendButton);
        inputBar.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(header, subHeader, chatScrollPane, inputBar);
    }

    private void sendMessage() {
        String question = inputField.getText();
        if (question == null || question.trim().isEmpty()) {
            return;
        }

        inputField.clear();
        addMessageBubble("YOU", question);

        sendButton.setDisable(true);
        inputField.setDisable(true);
        progressIndicator.setVisible(true);

        AnalysisResult result = coordinator.getCurrentAnalysisResult();
        String targetRole = result != null ? result.targetRole().title() : "Software Engineer";
        var fitResult = result != null ? result.roleFit() : null;

        Task<String> mentorTask = new Task<>() {
            @Override
            protected String call() {
                String candidateId = result != null ? result.candidate().getId() : "1";
                return mentorshipEngine.askMentor(candidateId, null, question, targetRole, fitResult, null);
            }
        };

        mentorTask.setOnSucceeded(e -> {
            String answer = mentorTask.getValue();
            addMessageBubble("MENTOR", answer);
            sendButton.setDisable(false);
            inputField.setDisable(false);
            progressIndicator.setVisible(false);
            inputField.requestFocus();
            chatScrollPane.setVvalue(1.0);
        });

        mentorTask.setOnFailed(e -> {
            Throwable err = mentorTask.getException();
            addMessageBubble("MENTOR", "Error generating response: " + (err != null ? err.getMessage() : "Unknown"));
            sendButton.setDisable(false);
            inputField.setDisable(false);
            progressIndicator.setVisible(false);
        });

        Thread t = new Thread(mentorTask, "SkillLens-Mentor-Worker");
        t.setDaemon(true);
        t.start();
    }

    private void addMessageBubble(String sender, String text) {
        VBox bubble = new VBox(5);
        boolean isUser = "YOU".equalsIgnoreCase(sender);

        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + (isUser ? "-color-accent-fg;" : "-color-fg-muted;"));

        Label textLabel = new Label(text);
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 13px;");

        bubble.getChildren().addAll(senderLabel, textLabel);
        bubble.setStyle("-fx-padding: 12 16; -fx-background-radius: 8; -fx-background-color: " +
                (isUser ? "-color-bg-default;" : "-color-bg-subtle;") +
                " -fx-border-color: -color-border-subtle; -fx-border-radius: 8;");
        bubble.setMaxWidth(750);

        HBox wrapper = new HBox(bubble);
        wrapper.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        chatMessagesBox.getChildren().add(wrapper);
    }
}
