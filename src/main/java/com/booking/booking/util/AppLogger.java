package com.booking.booking.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {


    private static final String LOG_FILE = "booking.log" ;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static void log(String level, String message) {
        String entry = "[" + LocalDateTime.now().format(FMT) + "] [" + level + "] " + message;
        System.out.println(entry);
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println(entry);
        } catch (IOException e) {
            System.err.println("Logger write failed: " + e.getMessage());
        }
    }

    public static void info(String msg) {
        log("INFO", msg);
    }

    public static void warn(String msg) {
        log("WARN", msg);
    }

    public static void error(String msg) {
        log("ERROR", msg);
    }

}
