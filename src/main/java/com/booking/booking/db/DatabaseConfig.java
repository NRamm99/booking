package com.booking.booking.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "frisør_db";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE +
            "?useSSL=false" +
            "&serverTimezone=UTC" +
            "&allowPublicKeyRetrieval=true" +
            "&useUnicode=true" +
            "&characterEncoding=UTF-8";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}