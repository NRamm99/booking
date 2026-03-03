package com.booking.booking.repository;

import com.booking.booking.model.Treatment;
import com.booking.booking.util.AppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TreatmentRepository {

    private Connection getConn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Treatment> findAll() {
        List<Treatment> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM treatments ORDER BY name");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findAll treatments: " + e.getMessage());
        }
        return list;
    }

    public Optional<Treatment> findById(int id) {
        try (PreparedStatement ps = getConn().prepareStatement("SELECT * FROM treatments WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findById treatment: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean save(Treatment t) {
        String sql = "INSERT INTO treatments (name, duration_minutes, price) VALUES (?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getName());
            ps.setInt(2, t.getDurationMinutes());
            ps.setDouble(3, t.getPrice());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) t.setId(keys.getInt(1));
            return true;
        } catch (SQLException e) {
            AppLogger.error("save treatment: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Treatment t) {
        String sql = "UPDATE treatments SET name=?, duration_minutes=?, price=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setInt(2, t.getDurationMinutes());
            ps.setDouble(3, t.getPrice());
            ps.setInt(4, t.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            AppLogger.error("update treatment: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM treatments WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            AppLogger.error("delete treatment: " + e.getMessage());
            return false;
        }
    }

    private Treatment mapRow(ResultSet rs) throws SQLException {
        return new Treatment(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("duration_minutes"),
            rs.getDouble("price")
        );
    }
}
