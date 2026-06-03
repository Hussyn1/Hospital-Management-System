package com.hospital;

import com.hospital.controllers.LoginController;
import com.hospital.dao.DBConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;

/**
 * Updated MainApp – no FXMLLoader needed.
 * Everything else (DB init, window sizing) is identical to the original.
 */
public class MainApp extends Application {

    @Override
    public void init() throws Exception {
        // Force-load database schema + mock data on startup (unchanged)
        Connection conn = DBConnection.getConnection();
        conn.close();
        System.out.println("Database initialized on application startup.");
    }

    @Override
    public void start(Stage primaryStage) {
        // Build the login screen using pure Java
        LoginController loginCtrl = new LoginController(primaryStage);
        Scene scene = loginCtrl.buildScene();

        primaryStage.setTitle("Hospital Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.out.println("Application shutting down.");
    }
}