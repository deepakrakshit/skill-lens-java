package com.skilllens.ui;

import com.skilllens.domain.model.AnalysisResult;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Coordinates screen transitions and holds active candidate analysis state.
 */
public class NavigationCoordinator {

    public enum Screen {
        HOME,
        STUDENT,
        EVIDENCE,
        ROLE_FIT,
        ROADMAP,
        MENTOR,
        RESUME_BUILDER,
        RESOURCES,
        RECRUITER,
        SETTINGS
    }

    private final BorderPane rootContainer;
    private final ApplicationContext springContext;
    private final Map<Screen, Supplier<Node>> viewSuppliers = new HashMap<>();

    private AnalysisResult currentAnalysisResult;
    private Screen activeScreen = Screen.HOME;

    public NavigationCoordinator(BorderPane rootContainer, ApplicationContext springContext) {
        this.rootContainer = rootContainer;
        this.springContext = springContext;
    }

    public void registerView(Screen screen, Supplier<Node> supplier) {
        viewSuppliers.put(screen, supplier);
    }

    public void navigateTo(Screen screen) {
        this.activeScreen = screen;
        Supplier<Node> supplier = viewSuppliers.get(screen);
        if (supplier != null) {
            Node viewNode = supplier.get();
            rootContainer.setCenter(viewNode);
        }
    }

    public Screen getActiveScreen() {
        return activeScreen;
    }

    public AnalysisResult getCurrentAnalysisResult() {
        return currentAnalysisResult;
    }

    public void setCurrentAnalysisResult(AnalysisResult result) {
        this.currentAnalysisResult = result;
    }

    public ApplicationContext getSpringContext() {
        return springContext;
    }
}
