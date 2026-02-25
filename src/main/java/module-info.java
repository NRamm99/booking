module com.booking.booking {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.booking.booking to javafx.fxml;
    exports com.booking.booking;
}