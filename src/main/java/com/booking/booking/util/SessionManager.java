package com.booking.booking.util;

import com.booking.booking.model.Employee;

public class SessionManager {
    private static SessionManager instance;
    private Employee currentEmployee;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public Employee getCurrentEmployee() { return currentEmployee; }
    public void setCurrentEmployee(Employee employee) { this.currentEmployee = employee; }
    public boolean isLoggedIn() { return currentEmployee != null; }
    public void logout() { currentEmployee = null; }
}
