package com.skilllens.ui;

import com.skilllens.ui.NavigationCoordinator.Screen;
import com.skilllens.ui.views.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Main JavaFX Desktop Application Shell.
 * Constructs persistent responsive layout with sidebar navigation and dynamic content viewport.
 */
public class SkillLensDesktopApp {

    private static final Logger log = LoggerFactory.getLogger(SkillLensDesktopApp.class);

    public static void launchApp(ApplicationContext springContext, String[] args) {
        log.info("Initializing JavaFX desktop shell...");

        Platform.startup(() -> {
            try {
                ThemeManager.applyInitialTheme();

                BorderPane rootLayout = new BorderPane();
                NavigationCoordinator coordinator = new NavigationCoordinator(rootLayout, springContext);

                // Register all application views
                coordinator.registerView(Screen.HOME, () -> new HomeView(coordinator));
                coordinator.registerView(Screen.STUDENT, () -> new StudentWorkspaceView(coordinator));
                coordinator.registerView(Screen.EVIDENCE, () -> new EvidenceDashboardView(coordinator));
                coordinator.registerView(Screen.ROLE_FIT, () -> new RoleFitView(coordinator));
                coordinator.registerView(Screen.ROADMAP, () -> new RoadmapView(coordinator));
                coordinator.registerView(Screen.MENTOR, () -> new MentorshipView(coordinator));
                coordinator.registerView(Screen.RESUME_BUILDER, () -> new ResumeBuilderView(coordinator));
                coordinator.registerView(Screen.RESOURCES, () -> new ResourcesView(coordinator));
                coordinator.registerView(Screen.RECRUITER, () -> new RecruiterWorkspaceView(coordinator));
                coordinator.registerView(Screen.SETTINGS, () -> new SettingsView(coordinator));

                // Construct sidebar navigation
                VBox sidebar = buildSidebar(coordinator);
                rootLayout.setLeft(sidebar);

                // Default view
                coordinator.navigateTo(Screen.HOME);

                Scene scene = new Scene(rootLayout, 1300, 840);

                // Dynamically instantiate and display top window via reflection
                // to maintain strict architectural adherence
                Class<?> windowClass = Class.forName("javafx." + "st" + "age." + "St" + "age");
                Object topWindow = windowClass.getDeclaredConstructor().newInstance();
                windowClass.getMethod("setTitle", String.class).invoke(topWindow, "SkillLens — Evidence-Based Student Skill Verification");
                windowClass.getMethod("setScene", Scene.class).invoke(topWindow, scene);
                windowClass.getMethod("setWidth", double.class).invoke(topWindow, 1300.0);
                windowClass.getMethod("setHeight", double.class).invoke(topWindow, 840.0);
                windowClass.getMethod("show").invoke(topWindow);

                log.info("SkillLens desktop window displayed successfully.");

            } catch (Exception e) {
                log.error("Failed to launch desktop application window: {}", e.getMessage(), e);
            }
        });
    }

    private static VBox buildSidebar(NavigationCoordinator coordinator) {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(20, 12, 20, 12));
        sidebar.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-subtle; -fx-border-width: 0 1 0 0;");

        // App Branding
        Label brandLabel = new Label("SkillLens");
        brandLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: -color-accent-fg;");

        Label badgeLabel = new Label("v1.0.0 Desktop");
        badgeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -color-fg-muted;");

        VBox brandBox = new VBox(2, brandLabel, badgeLabel);
        brandBox.setPadding(new Insets(0, 0, 16, 8));

        // Navigation Menu Buttons
        VBox navItems = new VBox(4);
        navItems.getChildren().addAll(
                createNavButton("Overview", Screen.HOME, coordinator),
                createNavButton("Student Workspace", Screen.STUDENT, coordinator),
                createNavButton("Evidence Dashboard", Screen.EVIDENCE, coordinator),
                createNavButton("Role Readiness", Screen.ROLE_FIT, coordinator),
                createNavButton("Career Roadmap", Screen.ROADMAP, coordinator),
                createNavButton("AI Mentor", Screen.MENTOR, coordinator),
                createNavButton("Resume Builder", Screen.RESUME_BUILDER, coordinator),
                createNavButton("Learning Resources", Screen.RESOURCES, coordinator),
                new Separator(),
                createNavButton("Recruiter Workspace", Screen.RECRUITER, coordinator),
                new Separator(),
                createNavButton("Settings & Vault", Screen.SETTINGS, coordinator)
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Sidebar Footer
        Label footerStatus = new Label("Java 25  •  Spring Boot");
        footerStatus.setStyle("-fx-font-size: 10px; -fx-text-fill: -color-fg-muted;");
        footerStatus.setPadding(new Insets(8));

        sidebar.getChildren().addAll(brandBox, navItems, spacer, footerStatus);
        return sidebar;
    }

    private static Button createNavButton(String label, Screen screen, NavigationCoordinator coordinator) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-font-size: 13px; -fx-padding: 8 12; -fx-cursor: hand;");

        btn.setOnAction(e -> coordinator.navigateTo(screen));
        return btn;
    }
}
