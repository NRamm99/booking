package com.booking.booking.controller;

import com.booking.booking.model.Appointment;
import com.booking.booking.model.Employee;
import com.booking.booking.repository.AppointmentRepository;
import com.booking.booking.repository.EmployeeRepository;
import com.booking.booking.service.AppointmentService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AppointmentsController implements MainController.HasMainController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterEmployee;
    @FXML private ComboBox<String> filterStatus;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colId;
    @FXML private TableColumn<Appointment, String> colDate;
    @FXML private TableColumn<Appointment, String> colCustomerName;
    @FXML private TableColumn<Appointment, String> colEmpName;
    @FXML private TableColumn<Appointment, String> colTreat;
    @FXML private TableColumn<Appointment, String> colDuration;
    @FXML private TableColumn<Appointment, String> colStatusCol;
    @FXML private TableColumn<Appointment, Boolean> colHasPayed;
    @FXML private TableColumn<Appointment, Void> colActions;

    private MainController mainController;
    private final AppointmentService apptService = new AppointmentService(new AppointmentRepository());
    private final EmployeeRepository employeeRepo = new EmployeeRepository();
    private ObservableList<Appointment> allAppointments;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override public void setMainController(MainController mc) { this.mainController = mc; }

    @FXML
    public void initialize() {
        setupFilters();
        setupTable();
        loadData();

        // Live search
        searchField.textProperty().addListener((o, old, val) -> applyFilter());
        filterEmployee.valueProperty().addListener((o, old, val) -> applyFilter());
        filterStatus.valueProperty().addListener((o, old, val) -> applyFilter());
    }

    private void setupFilters() {
        filterStatus.setItems(FXCollections.observableArrayList(
            "Alle", "Aktiv", "Gennemført", "Aflyst"));
        filterStatus.setValue("Alle");

        List<String> empNames = employeeRepo.findAll().stream()
            .map(Employee::getFullName).collect(Collectors.toList());
        empNames.add(0, "Alle medarbejdere");
        filterEmployee.setItems(FXCollections.observableArrayList(empNames));
        filterEmployee.setValue("Alle medarbejdere");
    }

    private void setupTable() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartTime().format(FMT)));
        colCustomerName.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getCustomer() != null ? c.getValue().getCustomer().getFullName() : "–"));
        colEmpName.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getEmployee() != null ? c.getValue().getEmployee().getFullName() : "–"));
        colTreat.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getTreatment() != null ? c.getValue().getTreatment().getName() : "–"));
        colDuration.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getTreatment() != null
                ? c.getValue().getTreatment().getDurationMinutes() + " min" : "–"));
        colStatusCol.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStatus().getDisplayName()));
        colHasPayed.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isHasPayed()).asObject());
        colHasPayed.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    boolean newValue = checkBox.isSelected();
                    String err = apptService.setPaymentStatus(a.getId(), newValue);
                    if (err != null) {
                        checkBox.setSelected(a.isHasPayed());
                        showAlert("Fejl", err);
                        return;
                    }
                    a.setHasPayed(newValue);
                });
            }

            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                checkBox.setSelected(Boolean.TRUE.equals(item));
                setGraphic(checkBox);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });

        // Action buttons column
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Rediger");
            private final Button cancelBtn = new Button("Aflys");
            private final Button completeBtn = new Button("Gennemfør");
            private final HBox box = new HBox(4, editBtn, cancelBtn);

            {
                editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
                cancelBtn.getStyleClass().addAll("btn-danger", "btn-small");
                completeBtn.getStyleClass().addAll("btn-success", "btn-small");

                editBtn.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    navigateToEdit(a);
                });
                cancelBtn.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    handleCancel(a);
                });
                completeBtn.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    handleComplete(a);
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Appointment a = getTableView().getItems().get(getIndex());
                box.getChildren().clear();
                if (a.getStatus() == Appointment.Status.ACTIVE) {
                    box.getChildren().addAll(editBtn, completeBtn, cancelBtn);
                } else {
                    box.getChildren().add(editBtn);
                }
                setGraphic(box);
            }
        });
    }

    private void loadData() {
        allAppointments = FXCollections.observableArrayList(apptService.getAllAppointments());
        appointmentTable.setItems(allAppointments);
    }

    private void applyFilter() {
        String search = searchField.getText().toLowerCase().trim();
        String empFilter = filterEmployee.getValue();
        String statusFilter = filterStatus.getValue();

        List<Appointment> filtered = allAppointments.stream()
            .filter(a -> {
                boolean matchSearch = search.isEmpty()
                    || (a.getCustomer() != null && a.getCustomer().getFullName().toLowerCase().contains(search))
                    || (a.getEmployee() != null && a.getEmployee().getFullName().toLowerCase().contains(search))
                    || (a.getTreatment() != null && a.getTreatment().getName().toLowerCase().contains(search));
                boolean matchEmp = empFilter == null || empFilter.equals("Alle medarbejdere")
                    || (a.getEmployee() != null && a.getEmployee().getFullName().equals(empFilter));
                boolean matchStatus = statusFilter == null || statusFilter.equals("Alle")
                    || a.getStatus().getDisplayName().equals(statusFilter);
                return matchSearch && matchEmp && matchStatus;
            }).collect(Collectors.toList());

        appointmentTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void navigateToEdit(Appointment appointment) {
        if (mainController == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/booking/ui/AppointmentFormView.fxml"));
            Node view = loader.load();
            AppointmentFormController ctrl = loader.getController();
            ctrl.setMainController(mainController);
            ctrl.setAppointment(appointment);
            // Replace content directly
            StackPane content = (StackPane) appointmentTable.getScene().lookup("#contentPane");
            if (content != null) content.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCancel(Appointment a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Aflys Aftale");
        confirm.setHeaderText("Aflys aftale for " + (a.getCustomer() != null ? a.getCustomer().getFullName() : "?"));
        confirm.setContentText("Aftalen markeres som aflyst (gemmes i 5 år jf. skattekrav).");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String err = apptService.cancelAppointment(a.getId());
            if (err == null) loadData();
            else showAlert("Fejl", err);
        }
    }

    private void handleComplete(Appointment a) {
        String err = apptService.completeAppointment(a.getId());
        if (err == null) loadData();
        else showAlert("Fejl", err);
    }

    @FXML private void handleNew() {
        if (mainController != null) mainController.showNewAppointment();
    }

    @FXML private void handleReset() {
        searchField.clear();
        filterEmployee.setValue("Alle medarbejdere");
        filterStatus.setValue("Alle");
        appointmentTable.setItems(allAppointments);
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(message);
        a.showAndWait();
    }
}
