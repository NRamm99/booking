package com.booking.booking.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //for at teste om log virkede
            //AppLogger.info("Logger test; app started");
            //AppLogger.warn("Logger test: warning");
            //AppLogger.error("Logger test: error");

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/booking/ui/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
