package com.booking.booking.controller;

import com.booking.booking.model.Customer;
import com.booking.booking.repository.CustomerRepository;
import com.booking.booking.service.CustomerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class CustomersController implements MainController.HasMainController {
    @FXML private TextField searchField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colNotes;
    @FXML private TableColumn<Customer, Void> colActions;
    @FXML private VBox editPanel;
    @FXML private Label editTitle;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea notesField;
    @FXML private Label editErrorLabel;

    private MainController mainController;
    private Customer editingCustomer = null;
    private final CustomerService customerService = new CustomerService(new CustomerRepository());

    @Override public void setMainController(MainController mc) { this.mainController = mc; }

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone() != null ? c.getValue().getPhone() : ""));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail() != null ? c.getValue().getEmail() : ""));
        colNotes.setCellValueFactory(c -> {
            String notes = c.getValue().getNotes();
            return new SimpleStringProperty(notes != null && notes.length() > 40
                ? notes.substring(0, 40) + "…" : (notes != null ? notes : ""));
        });

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
        customerTable.setItems(FXCollections.observableArrayList(customerService.getAllCustomers()));
    }

    @FXML private void handleSearch() {
        String q = searchField.getText().trim();
        List<Customer> result = q.isEmpty()
            ? customerService.getAllCustomers()
            : customerService.searchCustomers(q);
        customerTable.setItems(FXCollections.observableArrayList(result));
    }

    @FXML private void handleNew() {
        editingCustomer = null;
        editTitle.setText("Ny Kunde");
        nameField.clear(); phoneField.clear(); emailField.clear(); notesField.clear();
        editErrorLabel.setText("");
        editPanel.setVisible(true); editPanel.setManaged(true);
    }

    private void openEdit(Customer c) {
        editingCustomer = c;
        editTitle.setText("Rediger: " + c.getFullName());
        nameField.setText(c.getFullName());
        phoneField.setText(c.getPhone() != null ? c.getPhone() : "");
        emailField.setText(c.getEmail() != null ? c.getEmail() : "");
        notesField.setText(c.getNotes() != null ? c.getNotes() : "");
        editErrorLabel.setText("");
        editPanel.setVisible(true); editPanel.setManaged(true);
    }

    @FXML private void handleSave() {
        String err;
        if (editingCustomer == null) {
            err = customerService.createCustomer(
                nameField.getText(), emailField.getText(), phoneField.getText(), notesField.getText());
        } else {
            err = customerService.updateCustomer(editingCustomer,
                nameField.getText(), emailField.getText(), phoneField.getText(), notesField.getText());
        }
        if (err != null) {
            editErrorLabel.setText(err);
        } else {
            handleCancelEdit();
            loadData();
        }
    }

    @FXML private void handleCancelEdit() {
        editPanel.setVisible(false); editPanel.setManaged(false);
        editingCustomer = null;
    }
}
