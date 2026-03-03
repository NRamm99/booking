package com.booking.booking.controller;

import com.booking.booking.model.Employee;
import com.booking.booking.repository.EmployeeRepository;
import com.booking.booking.service.AuthService;
import com.booking.booking.util.AppLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService(new EmployeeRepository());

    @FXML
    public void initialize() {
        // Allow Enter key on password field to trigger login
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        errorLabel.setText("");
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            errorLabel.setText("Udfyld venligst brugernavn og adgangskode.");
            return;
        }

        Optional<Employee> result = authService.login(username, password);
        if (result.isEmpty()) {
            errorLabel.setText("Forkert brugernavn eller adgangskode. Prøv igen.");
            passwordField.clear();
            AppLogger.warn("Failed login attempt for: " + username);
            return;
        }

        // Navigate to main view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/booking/ui/MainView.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Hårmoni'ka – " + result.get().getFullName());
            stage.setMinWidth(900);
            stage.setMinHeight(650);

            // Trigger dashboard on load
            MainController controller = loader.getController();
            controller.showDashboard();
        } catch (IOException e) {
            AppLogger.error("Failed to load MainView: " + e.getMessage());
            errorLabel.setText("Systemfejl ved indlæsning. Kontakt support.");
        }
    }
}
