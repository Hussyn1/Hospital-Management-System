package com.hospital.controllers;

import com.hospital.services.AuthService;
import com.hospital.view.DashboardView;
import com.hospital.view.LoginView;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Drop-in replacement for the old LoginController.
 *
 * No FXML – all UI is built by LoginView.
 * To use: create a LoginController, call buildScene() to get the Scene,
 * then set it on the primary stage.
 *
 *   LoginController ctrl = new LoginController(primaryStage);
 *   primaryStage.setScene(ctrl.buildScene());
 */
public class LoginController {

    private final Stage     stage;
    private final LoginView view;
    private final AuthService authService = new AuthService();

    // ── References to view controls ──────────────────────────────────────────
    private final TextField     usernameField;
    private final PasswordField passwordField;
    private final TextField     visiblePasswordField;
    private final Button        togglePasswordButton;
    private final Button        loginButton;
    private final Label         errorLabel;

    private boolean passwordVisible = false;

    // ── Constructor ──────────────────────────────────────────────────────────
    public LoginController(Stage stage) {
        this.stage = stage;
        this.view  = new LoginView();

        // Grab references
        usernameField        = view.getUsernameField();
        passwordField        = view.getPasswordField();
        visiblePasswordField = view.getVisiblePasswordField();
        togglePasswordButton = view.getTogglePasswordButton();
        loginButton          = view.getLoginButton();
        errorLabel           = view.getErrorLabel();

        wireHandlers();
    }

    // ── Build the Scene ───────────────────────────────────────────────────────
    public Scene buildScene() {
        Scene scene = new Scene(view.getRoot(), 1000, 650);
        // Load the same CSS as before
        scene.getStylesheets().add(
            getClass().getResource("/css/theme.css").toExternalForm());
        return scene;
    }

    // ── Event wiring ──────────────────────────────────────────────────────────
    private void wireHandlers() {

        // LOG IN button
        loginButton.setOnAction(e -> handleLogin());

        // Allow pressing Enter in either field to trigger login
        usernameField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
        visiblePasswordField.setOnAction(e -> handleLogin());

        // Show / Hide password toggle
        togglePasswordButton.setOnAction(e -> handleTogglePassword());

        // Keep visible and hidden field text in sync
        visiblePasswordField.textProperty().addListener(
            (obs, old, val) -> passwordField.setText(val));
        passwordField.textProperty().addListener(
            (obs, old, val) -> visiblePasswordField.setText(val));
    }

    // ── Handlers ──────────────────────────────────────────────────────────────
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordVisible
            ? visiblePasswordField.getText()
            : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty.");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        try {
            boolean success = authService.login(username, password);
            if (success) {
                navigateToDashboard();
            } else {
                showError("Invalid username or password.");
                loginButton.setDisable(false);
            }
        } catch (SQLException ex) {
            showError("Database connection error: " + ex.getMessage());
            loginButton.setDisable(false);
            ex.printStackTrace();
        }
    }

    private void handleTogglePassword() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            // Show plain text field, hide password field
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordButton.setText("Hide");
        } else {
            // Restore password field
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            togglePasswordButton.setText("Show");
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    private void navigateToDashboard() {
        DashboardController ctrl = new DashboardController(stage);
        stage.setScene(ctrl.buildScene());
        stage.setTitle("Hospital Management System — Dashboard");
        stage.centerOnScreen();
        stage.show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}