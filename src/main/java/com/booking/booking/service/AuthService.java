package com.booking.booking.service;

import com.booking.booking.model.Employee;
import com.booking.booking.repository.EmployeeRepository;
import com.booking.booking.util.AppLogger;
import com.booking.booking.util.InputValidator;
import com.booking.booking.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

//  auth logic only.

public class AuthService {
    private final EmployeeRepository employeeRepo;

    public AuthService(EmployeeRepository employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    // Attempts login. Returns the employee if successful, empty otherwise.
    public Optional<Employee> login(String username, String password) {
        if (InputValidator.isNullOrBlank(username) || InputValidator.isNullOrBlank(password)) {
            AppLogger.warn("Login attempt with blank credentials.");
            return Optional.empty();
        }

        Optional<Employee> opt = employeeRepo.findByUsername(username.trim());
        if (opt.isEmpty()) {
            AppLogger.warn("Login failed: unknown user '" + username + "'");
            return Optional.empty();
        }

        Employee employee = opt.get();
        if (!BCrypt.checkpw(password, employee.getPasswordHash())) {
            AppLogger.warn("Login failed: wrong password for '" + username + "'");
            return Optional.empty();
        }

        SessionManager.getInstance().setCurrentEmployee(employee);
        AppLogger.info("Login successful: " + employee.getUsername());
        return Optional.of(employee);
    }

    public void logout() {
        Employee e = SessionManager.getInstance().getCurrentEmployee();
        if (e != null) AppLogger.info("Logout: " + e.getUsername());
        SessionManager.getInstance().logout();
    }

    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
}
