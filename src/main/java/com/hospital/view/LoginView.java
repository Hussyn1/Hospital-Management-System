package com.hospital.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginView {

    private final StackPane        rootPane;
    private final TextField        usernameField;
    private final PasswordField    passwordField;
    private final TextField        visiblePasswordField;
    private final Button           togglePasswordButton;
    private final Button           loginButton;
    private final Label            errorLabel;

    public LoginView() {
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("text-field");

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("password-field");

        visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter your password");
        visiblePasswordField.getStyleClass().add("text-field");
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);           // doesn't take space when hidden

        togglePasswordButton = new Button("Show");
        togglePasswordButton.getStyleClass().add("btn-secondary");
        togglePasswordButton.setPrefWidth(60);

        loginButton = new Button("LOG IN");
        loginButton.getStyleClass().add("btn-primary");
        loginButton.setPrefWidth(320);

        errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        rootPane = buildRoot();
    }


    private StackPane buildRoot() {
        StackPane root = new StackPane();
        root.getStyleClass().add("login-bg");
        root.setPrefSize(1000, 650);

        VBox centreColumn = new VBox(10);
        centreColumn.setAlignment(Pos.CENTER);
        centreColumn.setFillWidth(false);

        centreColumn.getChildren().addAll(buildCard(), buildHintLabel());

        root.getChildren().add(centreColumn);
        return root;
    }

    private VBox buildCard() {
        VBox card = new VBox(20);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(400);

        card.getChildren().addAll(
            buildBrandingBlock(),
            buildUsernameBlock(),
            buildPasswordBlock(),
            errorLabel,
            loginButton
        );
        return card;
    }

    /** "Hospital Management System" subtitle */
    private VBox buildBrandingBlock() {
    	 Label logo = new Label("Hospital Management\nSystem");
         logo.getStyleClass().add("sidebar-logo");

         logo.setWrapText(true);

         VBox box = new VBox(2);
         box.setAlignment(Pos.CENTER_LEFT);

         box.setFillWidth(true);

         box.getChildren().add(logo);

         return box;
    }

    /** USERNAME label + text field */
    private VBox buildUsernameBlock() {
        Label lbl = new Label("USERNAME");
        lbl.getStyleClass().add("form-label");

        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setFillWidth(true);
        box.getChildren().addAll(lbl, usernameField);
        return box;
    }

    /**
     * PASSWORD label + HBox containing:
     *   StackPane(visiblePasswordField, passwordField)  |  togglePasswordButton
     */
    private VBox buildPasswordBlock() {
        Label lbl = new Label("PASSWORD");
        lbl.getStyleClass().add("form-label");

        // Stack the two fields so they occupy exactly the same space
        StackPane fieldStack = new StackPane(visiblePasswordField, passwordField);
        HBox.setHgrow(fieldStack, Priority.ALWAYS);

        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(fieldStack, togglePasswordButton);

        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setFillWidth(true);
        box.getChildren().addAll(lbl, row);
        return box;
    }

    /** Default-credentials hint at the bottom */
    private Label buildHintLabel() {
        Label hint = new Label(
            "Default credentials: admin / admin123  |  dr_smith / doctor123");
        hint.getStyleClass().add("label-muted");
        return hint;
    }

    // ── Public accessors (mirrors of fx:id fields) ─────────────────────────────

    /** Root node – pass to {@code new Scene(view.getRoot(), w, h)} */
    public StackPane getRoot() { return rootPane; }

    public TextField       getUsernameField()        { return usernameField; }
    public PasswordField   getPasswordField()         { return passwordField; }
    public TextField       getVisiblePasswordField()  { return visiblePasswordField; }
    public Button          getTogglePasswordButton()  { return togglePasswordButton; }
    public Button          getLoginButton()           { return loginButton; }
    public Label           getErrorLabel()            { return errorLabel; }
}