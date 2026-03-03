package com.booking.booking.controller;

import com.booking.booking.model.Treatment;
import com.booking.booking.repository.TreatmentRepository;
import com.booking.booking.util.InputValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TreatmentsController implements MainController.HasMainController {
    @FXML private TableView<Treatment> treatmentTable;
    @FXML private TableColumn<Treatment, String> colName;
    @FXML private TableColumn<Treatment, String> colDuration;
    @FXML private TableColumn<Treatment, String> colPrice;
    @FXML private TableColumn<Treatment, Void> colActions;
    @FXML private VBox editPanel;
    @FXML private Label editTitle;
    @FXML private TextField nameField;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private Spinner<Integer> priceSpinner;
    @FXML private Label editErrorLabel;

    private MainController mainController;
    private Treatment editingTreatment = null;
    private final TreatmentRepository treatmentRepo = new TreatmentRepository();

    @Override public void setMainController(MainController mc) { this.mainController = mc; }

    @FXML
    public void initialize() {
        durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 300, 60));
        priceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 400, 50));
        durationSpinner.setEditable(true);
        priceSpinner.setEditable(true);
        setupTable();
        loadData();
    }

    private void setupTable() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colDuration.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDurationMinutes() + " min"));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(
            String.format("%.0f kr.", c.getValue().getPrice())));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Rediger");
            private final Button delBtn = new Button("Slet");
            private final HBox box = new HBox(6, editBtn, delBtn);
            {
                editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
                delBtn.getStyleClass().addAll("btn-danger", "btn-small");
                editBtn.setOnAction(e -> openEdit(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadData() {
        treatmentTable.setItems(FXCollections.observableArrayList(treatmentRepo.findAll()));
    }

    @FXML private void handleNew() {
        editingTreatment = null;
        editTitle.setText("Ny Behandling");
        nameField.clear();
        durationSpinner.getValueFactory().setValue(60);
        priceSpinner.getValueFactory().setValue(400);
        editErrorLabel.setText("");
        editPanel.setVisible(true); editPanel.setManaged(true);
    }

    private void openEdit(Treatment t) {
        editingTreatment = t;
        editTitle.setText("Rediger: " + t.getName());
        nameField.setText(t.getName());
        durationSpinner.getValueFactory().setValue(t.getDurationMinutes());
        priceSpinner.getValueFactory().setValue((int) t.getPrice());
        editErrorLabel.setText("");
        editPanel.setVisible(true); editPanel.setManaged(true);
    }

    private void handleDelete(Treatment t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Slet behandling");
        confirm.setContentText("Er du sikker på, at du vil slette '" + t.getName() + "'?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                treatmentRepo.delete(t.getId());
                loadData();
            }
        });
    }

    @FXML private void handleSave() {
        editErrorLabel.setText("");
        if (InputValidator.isNullOrBlank(nameField.getText())) {
            editErrorLabel.setText("Behandlingsnavn er påkrævet."); return;
        }
        if (editingTreatment == null) {
            Treatment t = new Treatment(0, nameField.getText().trim(),
                durationSpinner.getValue(), priceSpinner.getValue());
            treatmentRepo.save(t);
        } else {
            editingTreatment.setName(nameField.getText().trim());
            editingTreatment.setDurationMinutes(durationSpinner.getValue());
            editingTreatment.setPrice(priceSpinner.getValue());
            treatmentRepo.update(editingTreatment);
        }
        handleCancelEdit();
        loadData();
    }

    @FXML private void handleCancelEdit() {
        editPanel.setVisible(false); editPanel.setManaged(false);
        editingTreatment = null;
    }
}
