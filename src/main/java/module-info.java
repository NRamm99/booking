module com.booking.booking {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.booking.booking.controller;
    exports com.booking.booking.app;

    opens com.booking.booking.controller to javafx.fxml;
    opens com.booking.booking.app to javafx.fxml;
}