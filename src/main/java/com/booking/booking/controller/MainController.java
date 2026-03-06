package com.booking.booking.controller;

import com.booking.booking.repository.EmployeeRepository;
import com.booking.booking.service.AuthService;
import com.booking.booking.util.AppLogger;
import com.booking.booking.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML private StackPane contentPane;
    @FXML private Label currentUserLabel;
    @FXML private Label currentRoleLabel;

    @FXML private Button btnDashboard;
    @FXML private Button btnAppointments;
    @FXML private Button btnNewAppointment;
    @FXML private Button btnCustomers;
    @FXML private Button btnEmployees;
    @FXML private Button btnTreatments;

    private Button activeBtn;
    private final AuthService authService = new AuthService(new EmployeeRepository());

    @FXML
    public void initialize() {
        var emp = SessionManager.getInstance().getCurrentEmployee();
        if (emp != null) {
            currentUserLabel.setText(emp.getFullName());
            currentRoleLabel.setText(emp.getRole() == com.booking.booking.model.Employee.Role.ADMIN
                ? "Administrator" : "Medarbejder");

            // Only admin can manage employees
            btnEmployees.setVisible(emp.getRole() == com.booking.booking.model.Employee.Role.ADMIN);
            btnEmployees.setManaged(emp.getRole() == com.booking.booking.model.Employee.Role.ADMIN);
        }
        showDashboard();
    }

    @FXML public void showDashboard() { navigate("DashboardView", btnDashboard); }
    @FXML public void showAppointments() { navigate("AppointmentsView", btnAppointments); }
    @FXML public void showNewAppointment() { navigate("AppointmentFormView", btnNewAppointment); }
    @FXML public void showCustomers() { navigate("CustomersView", btnCustomers); }
    @FXML public void showEmployees() { navigate("EmployeesView", btnEmployees); }
    @FXML public void showTreatments() { navigate("TreatmentsView", btnTreatments); }

    public void navigateTo(String viewName) {
        navigate(viewName, null);
    }

    private void navigate(String viewName, Button btn) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/booking/ui/" + viewName + ".fxml"));
            Node view = loader.load();

            // Pass mainController reference if controller supports it
            Object controller = loader.getController();
            if (controller instanceof HasMainController hmc) {
                hmc.setMainController(this);
            }

            contentPane.getChildren().setAll(view);

            // Update active sidebar button styling
            if (activeBtn != null) {
                activeBtn.getStyleClass().remove("sidebar-btn-active");
            }
            if (btn != null) {
                btn.getStyleClass().add("sidebar-btn-active");
                activeBtn = btn;
            }
        } catch (IOException e) {
            AppLogger.error("Navigation failed to " + viewName + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/booking/ui/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 900, 650);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Hårmoni'ka – Log ind");
        } catch (IOException e) {
            AppLogger.error("Logout navigation failed: " + e.getMessage());
        }
    }

    public interface HasMainController {
        void setMainController(MainController mc);
    }
}
