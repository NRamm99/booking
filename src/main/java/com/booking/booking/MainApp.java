package com.booking.booking;

import com.booking.booking.repository.DatabaseManager;
import com.booking.booking.util.AppLogger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseManager.getInstance().initialize();
            AppLogger.info("Database initialized successfully.");

            URL loginView = getClass().getResource("/com/booking/ui/LoginView.fxml");
            if (loginView == null) {
                throw new IllegalStateException("Missing FXML resource: /com/booking/ui/LoginView.fxml");
            }
            FXMLLoader loader = new FXMLLoader(loginView);
            Scene scene = new Scene(loader.load(), 900, 650);
            URL styleUrl = getClass().getResource("/css/style.css");
            if (styleUrl != null) {
                scene.getStylesheets().add(styleUrl.toExternalForm());
            }

            primaryStage.setTitle("Hårmoni'ka – Booking System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (IOException e) {
            AppLogger.error("Failed to load main view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
