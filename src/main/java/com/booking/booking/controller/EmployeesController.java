package com.booking.booking.controller;

import com.booking.booking.model.Employee;
import com.booking.booking.repository.EmployeeRepository;
import com.booking.booking.service.AuthService;
import com.booking.booking.util.AppLogger;
import com.booking.booking.util.InputValidator;
import com.booking.booking.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EmployeesController implements MainController.HasMainController {
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colUsername;
    @FXML private TableColumn<Employee, String> colEmail;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colRole;
    @FXML private TableColumn<Employee, Void> colActions;
    @FXML private VBox editPanel;
    @FXML private Label editTitle;
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label editErrorLabel;
    @FXML private Button newBtn;

    private MainController mainController;
    private Employee editingEmployee = null;
    private final EmployeeRepository employeeRepo = new EmployeeRepository();
    private final AuthService authService = new AuthService(employeeRepo);

    @Override public void setMainController(MainController mc) { this.mainController = mc; }

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("EMPLOYEE", "ADMIN"));
        roleCombo.setValue("EMPLOYEE");

        // Only admin can manage
        boolean isAdmin = SessionManager.getInstance().getCurrentEmployee() != null
            && SessionManager.getInstance().getCurrentEmployee().getRole() == Employee.Role.ADMIN;
        if (!isAdmin) {
            newBtn.setDisable(true);
        }

        setupTable();
        loadData();
    }

    private void setupTable() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail() != null ? c.getValue().getEmail() : ""));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone() != null ? c.getValue().getPhone() : ""));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getRole() == Employee.Role.ADMIN ? "Administrator" : "Medarbejder"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Rediger");
            {
                editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
                editBtn.setOnAction(e -> openEdit(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(editBtn));
            }
        });
    }

    private void loadData() {
        employeeTable.setItems(FXCollections.observableArrayList(employeeRepo.findAll()));
    }

    @FXML private void handleNew() {
        editingEmployee = null;
        editTitle.setText("Ny Medarbejder");
        nameField.clear(); usernameField.clear(); emailField.clear(); phoneField.clear();
        passwordField.clear(); roleCombo.setValue("EMPLOYEE");
        editErrorLabel.setText("");
        usernameField.setDisable(false);
        editPanel.setVisible(true); editPanel.setManaged(true);
    }

    private void openEdit(Employee e) {
        editingEmployee = e;
        editTitle.setText("Rediger: " + e.getFullName());
        nameField.setText(e.getFullName());
        usernameField.setText(e.getUsername());
        usernameField.setDisable(true); // can't change username
        emailField.setText(e.getEmail() != null ? e.getEmail() : "");
        phoneField.setText(e.getPhone() != null ? e.getPhone() : "");
        passwordField.clear();
        roleCombo.setValue(e.getRole().name());
        editErrorLabel.setText("");
        editPanel.setVisible(true); editPanel.setManaged(true);
    }

    @FXML private void handleSave() {
        editErrorLabel.setText("");
        if (InputValidator.isNullOrBlank(nameField.getText())) {
            editErrorLabel.setText("Navn er påkrævet."); return;
        }

        if (editingEmployee == null) {
            // New employee
            if (InputValidator.isNullOrBlank(usernameField.getText())) {
                editErrorLabel.setText("Brugernavn er påkrævet."); return;
            }
            if (!InputValidator.isValidPassword(passwordField.getText())) {
                editErrorLabel.setText("Adgangskode skal være mindst 6 tegn."); return;
            }
            Employee emp = new Employee();
            emp.setFullName(nameField.getText().trim());
            emp.setUsername(usernameField.getText().trim());
            emp.setPasswordHash(authService.hashPassword(passwordField.getText()));
            emp.setEmail(emailField.getText().trim());
            emp.setPhone(phoneField.getText().trim());
            emp.setRole(Employee.Role.valueOf(roleCombo.getValue()));

            if (!employeeRepo.save(emp)) {
                editErrorLabel.setText("Fejl ved oprettelse. Brugernavnet er måske allerede i brug.");
                return;
            }
        } else {
            editingEmployee.setFullName(nameField.getText().trim());
            editingEmployee.setEmail(emailField.getText().trim());
            editingEmployee.setPhone(phoneField.getText().trim());
            editingEmployee.setRole(Employee.Role.valueOf(roleCombo.getValue()));
            employeeRepo.update(editingEmployee);

            if (!passwordField.getText().isBlank()) {
                if (!InputValidator.isValidPassword(passwordField.getText())) {
                    editErrorLabel.setText("Ny adgangskode skal være mindst 6 tegn."); return;
                }
                employeeRepo.updatePassword(editingEmployee.getId(),
                    authService.hashPassword(passwordField.getText()));
                AppLogger.info("Password updated for: " + editingEmployee.getUsername());
            }
        }
        handleCancelEdit();
        loadData();
    }

    @FXML private void handleCancelEdit() {
        editPanel.setVisible(false); editPanel.setManaged(false);
        editingEmployee = null;
    }
}
