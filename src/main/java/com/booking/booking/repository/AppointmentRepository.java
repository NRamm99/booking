package com.booking.booking.repository;

import com.booking.booking.model.*;
import com.booking.booking.util.AppLogger;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentRepository {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CustomerRepository customerRepo = new CustomerRepository();
    private final EmployeeRepository employeeRepo = new EmployeeRepository();
    private final TreatmentRepository treatmentRepo = new TreatmentRepository();

    private Connection getConn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY start_time DESC";
        try (Statement stmt = getConn().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findAll appointments: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> findByDate(LocalDateTime from, LocalDateTime to) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE start_time >= ? AND start_time < ? ORDER BY start_time";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, from.format(FMT));
            ps.setString(2, to.format(FMT));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findByDate: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> findByEmployee(int employeeId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE employee_id = ? ORDER BY start_time DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findByEmployee: " + e.getMessage());
        }
        return list;
    }

    public Optional<Appointment> findById(int id) {
        try (PreparedStatement ps = getConn().prepareStatement("SELECT * FROM appointments WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("findById appointment: " + e.getMessage());
        }
        return Optional.empty();
    }

    // Check for scheduling conflicts: same employee, overlapping time, not cancelled.
    // excludeId = -1 means no exclusion (new appointment).
    public boolean hasConflict(int employeeId, LocalDateTime start, LocalDateTime end, int excludeId) {
        String sql = """
            SELECT COUNT(*) FROM appointments
            WHERE employee_id = ?
              AND status != 'CANCELLED'
              AND id != ?
              AND start_time < ?
              AND end_time > ?
        """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, excludeId);
            ps.setString(3, end.format(FMT));
            ps.setString(4, start.format(FMT));
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            AppLogger.error("hasConflict: " + e.getMessage());
            return true; // Fail-safe: assume conflict on error
        }
    }

    public boolean save(Appointment a) {
        String sql = """
            INSERT INTO appointments (customer_id, employee_id, treatment_id, start_time, end_time, status, has_payed, notes)
            VALUES (?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getCustomer().getId());
            ps.setInt(2, a.getEmployee().getId());
            ps.setInt(3, a.getTreatment().getId());
            ps.setString(4, a.getStartTime().format(FMT));
            ps.setString(5, a.getEndTime().format(FMT));
            ps.setString(6, a.getStatus().name());
            ps.setBoolean(7, a.isHasPayed());
            ps.setString(8, a.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) a.setId(keys.getInt(1));
            return true;
        } catch (SQLException e) {
            AppLogger.error("save appointment: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Appointment a) {
        String sql = """
            UPDATE appointments SET customer_id=?, employee_id=?, treatment_id=?,
            start_time=?, end_time=?, status=?, has_payed=?, notes=? WHERE id=?
        """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, a.getCustomer().getId());
            ps.setInt(2, a.getEmployee().getId());
            ps.setInt(3, a.getTreatment().getId());
            ps.setString(4, a.getStartTime().format(FMT));
            ps.setString(5, a.getEndTime().format(FMT));
            ps.setString(6, a.getStatus().name());
            ps.setBoolean(7, a.isHasPayed());
            ps.setString(8, a.getNotes());
            ps.setInt(9, a.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            AppLogger.error("update appointment: " + e.getMessage());
            return false;
        }
    }

    public boolean setPaymentStatus(int id, boolean hasPayed) {
        try (PreparedStatement ps = getConn().prepareStatement(
            "UPDATE appointments SET has_payed=? WHERE id=?")) {
            ps.setBoolean(1, hasPayed);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            AppLogger.error("setPaymentStatus appointment: " + e.getMessage());
            return false;
        }
    }

    //Cancel: sets status to CANCELLED (never truly deletes, per tax requirement)
    public boolean cancel(int id) {
        try (PreparedStatement ps = getConn().prepareStatement(
                "UPDATE appointments SET status='CANCELLED' WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            AppLogger.error("cancel appointment: " + e.getMessage());
            return false;
        }
    }

    // Mark as completed
    public boolean complete(int id) {
        try (PreparedStatement ps = getConn().prepareStatement(
                "UPDATE appointments SET status='COMPLETED' WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            AppLogger.error("complete appointment: " + e.getMessage());
            return false;
        }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        int customerId = rs.getInt("customer_id");
        int employeeId = rs.getInt("employee_id");
        int treatmentId = rs.getInt("treatment_id");

        Customer customer = customerRepo.findById(customerId).orElse(null);
        Employee employee = employeeRepo.findById(employeeId).orElse(null);
        Treatment treatment = treatmentRepo.findById(treatmentId).orElse(null);

        LocalDateTime start = LocalDateTime.parse(rs.getString("start_time"), FMT);
        LocalDateTime end = LocalDateTime.parse(rs.getString("end_time"), FMT);
        String createdStr = rs.getString("created_at");
        LocalDateTime created = createdStr != null ? LocalDateTime.parse(createdStr, FMT) : LocalDateTime.now();

        return new Appointment(
            rs.getInt("id"), customer, employee, treatment,
            start, end,
            Appointment.Status.valueOf(rs.getString("status")),
            rs.getString("notes"),
            rs.getBoolean("has_payed"),
            created
        );
    }
}
