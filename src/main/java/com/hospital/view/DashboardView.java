package com.hospital.view;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Pure-Java replacement for DashboardView.fxml.
 *
 * Usage – wherever you previously loaded DashboardView.fxml:
 *
 *   DashboardView view  = new DashboardView();
 *   Scene         scene = new Scene(view.getRoot(), 1200, 800);
 *   // wire up references:
 *   VBox      navBox     = view.getNavigationBox();
 *   Label     nameLbl    = view.getUserNameLabel();
 *   Label     roleLbl    = view.getUserRoleLabel();
 *   StackPane content    = view.getContentArea();
 *   Button    logoutBtn  = view.getLogoutButton();
 *   // then attach your existing handler logic (or keep DashboardController)
 */
public class DashboardView {

    // ── fx:id equivalents ──────────────────────────────────────────────────────
    private final BorderPane mainContainer;
    private final VBox       navigationBox;
    private final Label      userNameLabel;
    private final Label      userRoleLabel;
    private final StackPane  contentArea;
    private final Button     logoutButton;   // wired to #handleLogout

    // ── Constructor ────────────────────────────────────────────────────────────
    public DashboardView() {

        // ── controls that the controller needs ──────────────────────────────
        userNameLabel = new Label("Dr. Sarah Smith");
        userNameLabel.setStyle(
            "-fx-font-weight: bold; -fx-text-fill: -hms-text-primary; -fx-font-size: 13;");

        userRoleLabel = new Label("CARDIOLOGIST");
        userRoleLabel.setStyle(
            "-fx-text-fill: -hms-accent; -fx-font-size: 11; -fx-font-weight: bold;");

        logoutButton = new Button("Sign Out");
        logoutButton.getStyleClass().add("btn-secondary");
        logoutButton.setPrefWidth(200);
        logoutButton.setStyle("-fx-font-size: 11; -fx-padding: 5 10 5 10;");

        navigationBox = new VBox(5);
        navigationBox.setStyle("-fx-background-color: transparent;");

        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        // ── assemble ────────────────────────────────────────────────────────
        mainContainer = buildRoot();
    }

    // ── Scene-graph construction ───────────────────────────────────────────────

    private BorderPane buildRoot() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-bg");
        root.setPrefSize(1200, 800);

        root.setLeft(buildSidebar());
        root.setCenter(contentArea);
        return root;
    }

    // ── Sidebar ────────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setAlignment(Pos.TOP_CENTER);

        sidebar.getChildren().addAll(
            buildLogoBlock(),
            buildDivider(),
            buildNavScrollPane(),
            buildDivider(),
            buildUserInfoCard()
        );
        return sidebar;
    }

    /** "HMS" + "INTEGRATED CLINICAL SUITE" */
    private VBox buildLogoBlock() {
        Label logo = new Label("HMS");
        logo.getStyleClass().add("sidebar-logo");

        logo.setWrapText(true);

        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER_LEFT);

        box.setFillWidth(true);

        box.getChildren().add(logo);

        return box;
    }

    private Separator buildDivider() {
        Separator sep = new Separator();
        sep.getStyleClass().add("sidebar-divider");
        return sep;
    }

    /**
     * ScrollPane wrapping navigationBox (grows to fill remaining height).
     * DashboardController populates navigationBox with sidebar buttons.
     */
    private ScrollPane buildNavScrollPane() {
        ScrollPane sp = new ScrollPane(navigationBox);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }

    /** Logged-in user card at the bottom of the sidebar */
    private VBox buildUserInfoCard() {
        VBox card = new VBox(6);
        card.setStyle(
            "-fx-padding: 15 20 15 20; " +
            "-fx-background-color: rgba(255,255,255,0.03); " +
            "-fx-background-radius: 8;");

        card.getChildren().addAll(userNameLabel, userRoleLabel, logoutButton);

        VBox.setMargin(card, new Insets(5, 15, 10, 15));
        return card;
    }

    // ── Public accessors ───────────────────────────────────────────────────────

    /** Root node – pass to {@code new Scene(view.getRoot(), w, h)} */
    public BorderPane getRoot()          { return mainContainer; }

    /** The VBox DashboardController fills with nav buttons */
    public VBox       getNavigationBox() { return navigationBox; }

    public Label      getUserNameLabel() { return userNameLabel; }
    public Label      getUserRoleLabel() { return userRoleLabel; }

    /** The StackPane DashboardController swaps content into */
    public StackPane  getContentArea()   { return contentArea;   }

    /** The Sign-Out button – attach onAction in the controller */
    public Button     getLogoutButton()  { return logoutButton;  }
}