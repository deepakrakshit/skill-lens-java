package com.skilllens.ui.views;

import com.skilllens.domain.model.AnalysisResult;
import com.skilllens.domain.model.LearningResource;
import com.skilllens.service.resources.OfflineResourceCatalog;
import com.skilllens.ui.NavigationCoordinator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

/**
 * Learning Resource Discovery View.
 * Displays authoritative documentation, books, and tutorials with offline indicators.
 */
public class ResourcesView extends VBox {

    public ResourcesView(NavigationCoordinator coordinator) {
        setSpacing(20);
        setPadding(new Insets(30));
        setAlignment(Pos.TOP_LEFT);

        Label header = new Label("Curated Technical Learning Resources");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subHeader = new Label("Official documentation, authoritative specifications, and recommended tutorials tailored to your verified skill gaps.");
        subHeader.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-fg-muted;");

        VBox cardsContainer = new VBox(14);
        cardsContainer.setPadding(new Insets(10, 0, 10, 0));

        AnalysisResult result = coordinator.getCurrentAnalysisResult();
        List<LearningResource> resources;
        if (result != null && !result.recommendedResources().isEmpty()) {
            resources = result.recommendedResources();
        } else {
            OfflineResourceCatalog catalog = coordinator.getSpringContext().getBean(OfflineResourceCatalog.class);
            resources = catalog.getAllResources();
        }

        for (LearningResource res : resources) {
            cardsContainer.getChildren().add(createResourceCard(res));
        }

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, subHeader, scrollPane);
    }

    private VBox createResourceCard(LearningResource res) {
        VBox card = new VBox(8);
        card.setStyle("-fx-padding: 15; -fx-background-color: -color-bg-subtle; -fx-background-radius: 8; -fx-border-color: -color-border-subtle; -fx-border-radius: 8;");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label providerBadge = new Label(res.provider());
        providerBadge.setStyle("-fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-on-emphasis; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 10px;");

        Label skillBadge = new Label(res.skill());
        skillBadge.setStyle("-fx-padding: 3 8; -fx-background-radius: 4; -fx-border-color: -color-border-muted; -fx-font-size: 10px;");

        Label offlineBadge = new Label(res.isOfflineAvailable() ? "✓ Offline Available" : "Online Resource");
        offlineBadge.setStyle(res.isOfflineAvailable()
                ? "-fx-text-fill: -color-success-fg; -fx-font-size: 10px; -fx-font-weight: bold;"
                : "-fx-text-fill: -color-fg-muted; -fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button openLinkBtn = new Button("Open Resource");
        openLinkBtn.setStyle("-fx-font-size: 11px;");
        openLinkBtn.setOnAction(e -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(URI.create(res.url()));
                }
            } catch (Exception ignored) {}
        });

        topRow.getChildren().addAll(providerBadge, skillBadge, offlineBadge, spacer, openLinkBtn);

        Label title = new Label(res.title());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label desc = new Label(res.description());
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-fg-muted;");

        card.getChildren().addAll(topRow, title, desc);
        return card;
    }
}
