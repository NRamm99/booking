package com.booking.booking.repository;

import com.booking.booking.model.Employee;
import com.booking.booking.util.AppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class EmployeeRepository {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    public Optional<Employee> findByUsername(String username) {
        String sql = "SELECT * FROM employees WHERE username = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findByUsername failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Employee> findById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findById failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Employee> findAll() {
        List<Employee> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM employees ORDER BY full_name");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findAll employees failed: " + e.getMessage());
        }
        return list;
    }

    public boolean save(Employee e) {
        String sql = "INSERT INTO employees (username, password_hash, full_name, email, phone, role) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getUsername());
            ps.setString(2, e.getPasswordHash());
            ps.setString(3, e.getFullName());
            ps.setString(4, e.getEmail());
            ps.setString(5, e.getPhone());
            ps.setString(6, e.getRole().name());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) e.setId(keys.getInt(1));
            return true;
        } catch (SQLException ex) {
            AppLogger.error("save employee failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean update(Employee e) {
        String sql = "UPDATE employees SET full_name=?, email=?, phone=?, role=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, e.getFullName());
            ps.setString(2, e.getEmail());
            ps.setString(3, e.getPhone());
            ps.setString(4, e.getRole().name());
            ps.setInt(5, e.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            AppLogger.error("update employee failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean updatePassword(int employeeId, String newHash) {
        String sql = "UPDATE employees SET password_hash=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            AppLogger.error("updatePassword failed: " + e.getMessage());
            return false;
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"),
            Employee.Role.valueOf(rs.getString("role"))
        );
    }
}
