package com.booking.booking.repository;

import com.booking.booking.model.Customer;
import com.booking.booking.util.AppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    public Optional<Customer> findById(int id) {
        try (PreparedStatement ps = getConn().prepareStatement("SELECT * FROM customers WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findById customer: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM customers ORDER BY full_name");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findAll customers: " + e.getMessage());
        }
        return list;
    }

    public List<Customer> search(String query) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE full_name LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY full_name";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            String q = "%" + query + "%";
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("search customers: " + e.getMessage());
        }
        return list;
    }

    public boolean save(Customer c) {
        String sql = "INSERT INTO customers (full_name, email, phone, notes) VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) c.setId(keys.getInt(1));
            return true;
        } catch (SQLException e) {
            AppLogger.error("save customer: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Customer c) {
        String sql = "UPDATE customers SET full_name=?, email=?, phone=?, notes=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getNotes());
            ps.setInt(5, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            AppLogger.error("update customer: " + e.getMessage());
            return false;
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("notes")
        );
    }
}
