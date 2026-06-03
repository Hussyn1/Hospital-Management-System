package com.hospital.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import java.util.Optional;

public class AlertHelper {

    /**
     * Shows a standard styled information dialog.
     */
    public static void showInfo(String title, String header, String content) {
        showAlert(AlertType.INFORMATION, title, header, content);
    }

    /**
     * Shows a standard styled warning dialog.
     */
    public static void showWarning(String title, String header, String content) {
        showAlert(AlertType.WARNING, title, header, content);
    }

    /**
     * Shows a standard styled error dialog.
     */
    public static void showError(String title, String header, String content) {
        showAlert(AlertType.ERROR, title, header, content);
    }

    /**
     * Shows a confirmation dialog and returns true if user clicks OK.
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        styleAlert(alert);
        
        alert.showAndWait();
    }

    private static void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        
        // Add premium CSS stylesheet if we want dialog to be dark-themed
        try {
            String cssPath = AlertHelper.class.getResource("/css/theme.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
            dialogPane.getStyleClass().add("alert-dialog");
        } catch (Exception e) {
            // Fallback if resource stylesheet not loaded yet
            System.err.println("Could not load alert CSS: " + e.getMessage());
        }

        // Set style class depending on dialog type
        if (alert.getAlertType() == AlertType.ERROR) {
            dialogPane.getStyleClass().add("alert-error");
        } else if (alert.getAlertType() == AlertType.CONFIRMATION) {
            dialogPane.getStyleClass().add("alert-confirm");
        } else {
            dialogPane.getStyleClass().add("alert-info");
        }

        // Keep standard window sizing and layout
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.setResizable(false);
    }
}
