package com.skilllens.ui.views;

import com.skilllens.service.ai.AiRoutingService;
import com.skilllens.service.security.CredentialManager;
import com.skilllens.ui.NavigationCoordinator;
import com.skilllens.ui.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Settings & Security View.
 * Manages Windows DPAPI-backed credential storage, AI model selection, and UI theme.
 */
public class SettingsView extends VBox {

    private final CredentialManager credentialManager;
    private final AiRoutingService aiRouter;

    private final PasswordField geminiKeyField = new PasswordField();
    private final PasswordField groqKeyField = new PasswordField();
    private final PasswordField githubTokenField = new PasswordField();
    private final PasswordField youtubeKeyField = new PasswordField();

    private final TextField geminiModelField = new TextField("gemini-3.5-flash");
    private final TextField groqModelField = new TextField("openai/gpt-oss-120b");

    private final Label vaultStatusLabel = new Label();

    public SettingsView(NavigationCoordinator coordinator) {
        this.credentialManager = coordinator.getSpringContext().getBean(CredentialManager.class);
        this.aiRouter = coordinator.getSpringContext().getBean(AiRoutingService.class);

        setSpacing(20);
        setPadding(new Insets(30));
        setAlignment(Pos.TOP_LEFT);

        Label header = new Label("Settings & Secure Credential Vault");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subHeader = new Label("Credentials are encrypted via OS-backed Windows DPAPI (CryptProtectData). .env is never used for end-user production storage.");
        subHeader.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-fg-muted;");

        // Two-column settings layout
        HBox columns = new HBox(30);

        // Column 1: API Keys Vault
        VBox keysCol = new VBox(15);
        keysCol.setPrefWidth(480);

        Label keysTitle = new Label("Encrypted Credential Storage");
        keysTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox geminiRow = createKeyInputRow("Google Gemini API Key (Primary AI)", CredentialManager.KEY_GEMINI, geminiKeyField);
        VBox groqRow = createKeyInputRow("Groq API Key (Fallback AI)", CredentialManager.KEY_GROQ, groqKeyField);
        VBox githubRow = createKeyInputRow("GitHub Personal Access Token (Optional)", CredentialManager.KEY_GITHUB, githubTokenField);
        VBox youtubeRow = createKeyInputRow("YouTube Data API Key (Optional)", CredentialManager.KEY_YOUTUBE, youtubeKeyField);

        Button saveKeysBtn = new Button("Save Credentials to Windows DPAPI Vault");
        saveKeysBtn.setStyle("-fx-font-weight: bold; -fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-on-emphasis;");
        saveKeysBtn.setOnAction(e -> saveCredentials());

        vaultStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-accent-fg;");

        keysCol.getChildren().addAll(keysTitle, geminiRow, groqRow, githubRow, youtubeRow, saveKeysBtn, vaultStatusLabel);

        // Column 2: Models & System Preferences
        VBox prefCol = new VBox(15);
        prefCol.setPrefWidth(420);

        Label prefTitle = new Label("AI Model Configuration & Appearance");
        prefTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox geminiModelBox = new VBox(4, new Label("Primary AI Model:"), geminiModelField);
        VBox groqModelBox = new VBox(4, new Label("Fallback AI Model:"), groqModelField);

        Button applyModelsBtn = new Button("Apply Model Configuration");
        applyModelsBtn.setOnAction(e -> applyModels());

        // Theme Toggle
        Label themeLabel = new Label("Application Appearance:");
        Button themeBtn = new Button(ThemeManager.isDarkMode() ? "Switch to Light Theme" : "Switch to Dark Theme");
        themeBtn.setOnAction(e -> {
            ThemeManager.toggleTheme();
            themeBtn.setText(ThemeManager.isDarkMode() ? "Switch to Light Theme" : "Switch to Dark Theme");
        });

        // System Diagnostics
        VBox diagBox = new VBox(6);
        diagBox.setStyle("-fx-padding: 15; -fx-background-color: -color-bg-subtle; -fx-background-radius: 8;");
        Label diagTitle = new Label("System Diagnostics");
        diagTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label runtimeLbl = new Label("Runtime: Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        Label osLbl = new Label("Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        Label memLbl = new Label("JVM Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB used");

        diagBox.getChildren().addAll(diagTitle, runtimeLbl, osLbl, memLbl);

        prefCol.getChildren().addAll(prefTitle, geminiModelBox, groqModelBox, applyModelsBtn, new Separator(), themeLabel, themeBtn, diagBox);

        columns.getChildren().addAll(keysCol, prefCol);

        getChildren().addAll(header, subHeader, columns);
    }

    private VBox createKeyInputRow(String label, String keyName, PasswordField field) {
        VBox row = new VBox(4);
        boolean isConfigured = credentialManager.hasApiKey(keyName);
        String statusText = isConfigured ? "✓ Currently Configured" : "Not Configured";

        HBox labelRow = new HBox(8);
        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        Label statLbl = new Label(statusText);
        statLbl.setStyle(isConfigured ? "-fx-font-size: 11px; -fx-text-fill: -color-success-fg;" : "-fx-font-size: 11px; -fx-text-fill: -color-fg-muted;");

        labelRow.getChildren().addAll(nameLbl, statLbl);

        field.setPromptText("Enter new key value to update...");
        row.getChildren().addAll(labelRow, field);
        return row;
    }

    private void saveCredentials() {
        if (!geminiKeyField.getText().trim().isEmpty()) {
            credentialManager.setApiKey(CredentialManager.KEY_GEMINI, geminiKeyField.getText().trim());
            geminiKeyField.clear();
        }
        if (!groqKeyField.getText().trim().isEmpty()) {
            credentialManager.setApiKey(CredentialManager.KEY_GROQ, groqKeyField.getText().trim());
            groqKeyField.clear();
        }
        if (!githubTokenField.getText().trim().isEmpty()) {
            credentialManager.setApiKey(CredentialManager.KEY_GITHUB, githubTokenField.getText().trim());
            githubTokenField.clear();
        }
        if (!youtubeKeyField.getText().trim().isEmpty()) {
            credentialManager.setApiKey(CredentialManager.KEY_YOUTUBE, youtubeKeyField.getText().trim());
            youtubeKeyField.clear();
        }

        vaultStatusLabel.setText("Credentials saved and protected using Windows DPAPI vault.");
    }

    private void applyModels() {
        String pModel = geminiModelField.getText().trim();
        String fModel = groqModelField.getText().trim();

        if (!pModel.isEmpty()) {
            aiRouter.getGeminiClient().setConfiguredModel(pModel);
        }
        if (!fModel.isEmpty()) {
            aiRouter.getGroqClient().setConfiguredModel(fModel);
        }

        vaultStatusLabel.setText("Active models updated: Primary=" + pModel + ", Fallback=" + fModel);
    }
}
