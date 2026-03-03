package com.booking.booking.controller;

import com.booking.booking.model.*;
import com.booking.booking.repository.*;
import com.booking.booking.service.AppointmentService;
import com.booking.booking.service.CustomerService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentFormController implements MainController.HasMainController {
    @FXML private Label formTitle;
    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<Employee> employeeCombo;
    @FXML private ComboBox<Treatment> treatmentCombo;
    @FXML private Label durationLabel;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private Label endTimeLabel;
    @FXML private TextArea notesArea;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;

    private MainController mainController;
    private Appointment editingAppointment = null;

    private final AppointmentService apptService = new AppointmentService(new AppointmentRepository());
    private final CustomerService customerService = new CustomerService(new CustomerRepository());
    private final EmployeeRepository employeeRepo = new EmployeeRepository();
    private final TreatmentRepository treatmentRepo = new TreatmentRepository();

    @Override public void setMainController(MainController mc) { this.mainController = mc; }

    @FXML
    public void initialize() {
        loadCombos();
        datePicker.setValue(LocalDate.now());
        setupSpinners();
        setupChangeListeners();
    }

    private void loadCombos() {
        List<Customer> customers = customerService.getAllCustomers();
        customerCombo.getItems().setAll(customers);
        customerCombo.setConverter(new StringConverter<>() {
            public String toString(Customer c) { return c == null ? "" : c.getFullName() + " (" + c.getPhone() + ")"; }
            public Customer fromString(String s) { return null; }
        });

        List<Employee> employees = employeeRepo.findAll();
        employeeCombo.getItems().setAll(employees);
        employeeCombo.setConverter(new StringConverter<>() {
            public String toString(Employee e) { return e == null ? "" : e.getFullName(); }
            public Employee fromString(String s) { return null; }
        });

        List<Treatment> treatments = treatmentRepo.findAll();
        treatmentCombo.getItems().setAll(treatments);
        treatmentCombo.setConverter(new StringConverter<>() {
            public String toString(Treatment t) { return t == null ? "" : t.getName() + " (" + t.getDurationMinutes() + " min)"; }
            public Treatment fromString(String s) { return null; }
        });
    }

    private void setupSpinners() {
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(7, 19, 9));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5));
        hourSpinner.setEditable(true);
        minuteSpinner.setEditable(true);
    }

    private void setupChangeListeners() {
        treatmentCombo.valueProperty().addListener((o, old, t) -> updateDurationDisplay());
        datePicker.valueProperty().addListener((o, old, v) -> updateEndTimeDisplay());
        hourSpinner.valueProperty().addListener((o, old, v) -> updateEndTimeDisplay());
        minuteSpinner.valueProperty().addListener((o, old, v) -> updateEndTimeDisplay());
    }

    private void updateDurationDisplay() {
        Treatment t = treatmentCombo.getValue();
        if (t != null) {
            durationLabel.setText(t.getDurationMinutes() + " minutter");
            updateEndTimeDisplay();
        } else {
            durationLabel.setText("–");
            endTimeLabel.setText("–");
        }
    }

    private void updateEndTimeDisplay() {
        Treatment t = treatmentCombo.getValue();
        LocalDate date = datePicker.getValue();
        if (t != null && date != null) {
            LocalDateTime start = LocalDateTime.of(date, java.time.LocalTime.of(
                hourSpinner.getValue(), minuteSpinner.getValue()));
            LocalDateTime end = start.plusMinutes(t.getDurationMinutes());
            endTimeLabel.setText(end.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    }

    // Called when editing an existing appointment
    public void setAppointment(Appointment a) {
        this.editingAppointment = a;
        formTitle.setText("Rediger Tidsbestilling #" + a.getId());
        saveBtn.setText("Gem ændringer");

        customerCombo.setValue(a.getCustomer());
        employeeCombo.setValue(a.getEmployee());
        treatmentCombo.setValue(a.getTreatment());
        datePicker.setValue(a.getStartTime().toLocalDate());
        hourSpinner.getValueFactory().setValue(a.getStartTime().getHour());
        minuteSpinner.getValueFactory().setValue(a.getStartTime().getMinute());
        notesArea.setText(a.getNotes() != null ? a.getNotes() : "");
        updateDurationDisplay();
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");
        Customer customer = customerCombo.getValue();
        Employee employee = employeeCombo.getValue();
        Treatment treatment = treatmentCombo.getValue();
        LocalDate date = datePicker.getValue();
        String notes = notesArea.getText();

        if (date == null) {
            errorLabel.setText("Vælg venligst en dato.");
            return;
        }

        LocalDateTime startTime = LocalDateTime.of(date,
            java.time.LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue()));

        String error;
        if (editingAppointment == null) {
            error = apptService.createAppointment(customer, employee, treatment, startTime, notes);
        } else {
            error = apptService.updateAppointment(editingAppointment, customer, employee, treatment, startTime, notes);
        }

        if (error != null) {
            errorLabel.setText(error);
        } else {
            if (mainController != null) mainController.showAppointments();
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) mainController.showAppointments();
    }

    @FXML
    private void handleNewCustomer() {
        // in-lkine hurtig add: show dialog
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Ny Kunde");
        dialog.setHeaderText("Opret ny kunde hurtigt");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameF = new TextField(); nameF.setPromptText("Fulde navn *");
        TextField phoneF = new TextField(); phoneF.setPromptText("Telefon");
        TextField emailF = new TextField(); emailF.setPromptText("E-mail");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(8,
            new Label("Navn:"), nameF, new Label("Telefon:"), phoneF,
            new Label("E-mail:"), emailF);
        content.setPadding(new javafx.geometry.Insets(16));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm());

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String err = customerService.createCustomer(
                    nameF.getText(), emailF.getText(), phoneF.getText(), "");
                if (err != null) { errorLabel.setText(err); return null; }
                var customers = customerService.getAllCustomers();
                return customers.isEmpty() ? null : customers.get(customers.size() - 1);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            loadCombos();
            customerCombo.setValue(c);
        });
    }
}
