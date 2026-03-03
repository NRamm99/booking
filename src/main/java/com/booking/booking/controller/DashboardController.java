package com.booking.booking.controller;

import com.booking.booking.model.Appointment;
import com.booking.booking.repository.AppointmentRepository;
import com.booking.booking.repository.CustomerRepository;
import com.booking.booking.service.AppointmentService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController implements MainController.HasMainController {
    @FXML private Label dateLabel;
    @FXML private Label todayCountLabel;
    @FXML private Label weekCountLabel;
    @FXML private Label activeCountLabel;
    @FXML private Label customerCountLabel;
    @FXML private TableView<Appointment> todayTable;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colCustomer;
    @FXML private TableColumn<Appointment, String> colEmployee;
    @FXML private TableColumn<Appointment, String> colTreatment;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, Boolean> colHasPayed;

    private MainController mainController;
    private final AppointmentService apptService = new AppointmentService(new AppointmentRepository());
    private final CustomerRepository customerRepo = new CustomerRepository();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE d. MMMM yyyy", new java.util.Locale("da", "DK"));

    @Override
    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDateTime.now().format(DATE_FMT));
        setupTable();
        loadData();
    }

    private void setupTable() {
        colTime.setCellValueFactory(c -> {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
            return new SimpleStringProperty(c.getValue().getStartTime().format(fmt));
        });
        colCustomer.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getCustomer() != null
                ? c.getValue().getCustomer().getFullName() : "–"));
        colEmployee.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getEmployee() != null
                ? c.getValue().getEmployee().getFullName() : "–"));
        colTreatment.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getTreatment() != null
                ? c.getValue().getTreatment().getName() : "–"));
        colStatus.setCellValueFactory(c ->
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
    }

    private void loadData() {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> today = apptService.getAppointmentsForDay(now);
        todayTable.setItems(FXCollections.observableArrayList(today));
        todayCountLabel.setText(String.valueOf(today.size()));

        // Week count
        List<Appointment> allAppts = apptService.getAllAppointments();
        long weekCount = allAppts.stream()
            .filter(a -> a.getStartTime().isAfter(now.toLocalDate().atStartOfDay())
                      && a.getStartTime().isBefore(now.toLocalDate().atStartOfDay().plusDays(7))
                      && a.getStatus() != Appointment.Status.CANCELLED)
            .count();
        weekCountLabel.setText(String.valueOf(weekCount));

        long activeCount = allAppts.stream()
            .filter(a -> a.getStatus() == Appointment.Status.ACTIVE).count();
        activeCountLabel.setText(String.valueOf(activeCount));

        long custCount = customerRepo.findAll().size();
        customerCountLabel.setText(String.valueOf(custCount));
    }

    @FXML
    private void handleNewAppointment() {
        if (mainController != null) mainController.showNewAppointment();
    }
}
