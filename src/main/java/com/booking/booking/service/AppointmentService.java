package com.booking.booking.service;

import com.booking.booking.model.*;
import com.booking.booking.repository.AppointmentRepository;
import com.booking.booking.util.AppLogger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for appointments.
 * Single Responsibility: appointment rules and orchestration.
 */
public class AppointmentService {
    private final AppointmentRepository appointmentRepo;

    public AppointmentService(AppointmentRepository appointmentRepo) {
        this.appointmentRepo = appointmentRepo;
    }

    /**
     * Creates a new appointment after validation and conflict check.
     * @return error message, or null on success
     */
    public String createAppointment(Customer customer, Employee employee, Treatment treatment,
                                    LocalDateTime startTime, String notes) {
        String validationError = validateAppointmentInput(customer, employee, treatment, startTime);
        if (validationError != null) return validationError;

        LocalDateTime endTime = startTime.plusMinutes(treatment.getDurationMinutes());

        if (appointmentRepo.hasConflict(employee.getId(), startTime, endTime, -1)) {
            return employee.getFullName() + " har allerede en aftale i dette tidsrum. Vælg et andet tidspunkt.";
        }

        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setEmployee(employee);
        appointment.setTreatment(treatment);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(Appointment.Status.ACTIVE);
        appointment.setHasPayed(false);
        appointment.setNotes(notes);
        appointment.setCreatedAt(LocalDateTime.now());

        if (!appointmentRepo.save(appointment)) {
            AppLogger.error("Database error saving appointment.");
            return "Databasefejl ved oprettelse af aftale. Prøv igen.";
        }

        AppLogger.info("Appointment created: id=" + appointment.getId() + " for " + customer.getFullName());
        return null;
    }

    /**
     * Updates an existing appointment.
     * @return error message, or null on success
     */
    public String updateAppointment(Appointment appointment, Customer customer, Employee employee,
                                    Treatment treatment, LocalDateTime startTime, String notes) {
        String validationError = validateAppointmentInput(customer, employee, treatment, startTime);
        if (validationError != null) return validationError;

        LocalDateTime endTime = startTime.plusMinutes(treatment.getDurationMinutes());

        if (appointmentRepo.hasConflict(employee.getId(), startTime, endTime, appointment.getId())) {
            return employee.getFullName() + " har allerede en aftale i dette tidsrum. Vælg et andet tidspunkt.";
        }

        appointment.setCustomer(customer);
        appointment.setEmployee(employee);
        appointment.setTreatment(treatment);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setNotes(notes);

        if (!appointmentRepo.update(appointment)) {
            return "Databasefejl ved opdatering. Prøv igen.";
        }

        AppLogger.info("Appointment updated: id=" + appointment.getId());
        return null;
    }

    /**
     * Cancels an appointment (keeps record per tax requirement).
     */
    public String cancelAppointment(int id) {
        if (!appointmentRepo.cancel(id)) {
            return "Kunne ikke aflyse aftalen. Prøv igen.";
        }
        AppLogger.info("Appointment cancelled: id=" + id);
        return null;
    }

    public String completeAppointment(int id) {
        if (!appointmentRepo.complete(id)) {
            return "Kunne ikke markere aftalen som gennemført.";
        }
        return null;
    }

    public String setPaymentStatus(int id, boolean hasPayed) {
        if (!appointmentRepo.setPaymentStatus(id, hasPayed)) {
            return "Kunne ikke opdatere betalingsstatus.";
        }
        return null;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public List<Appointment> getAppointmentsForDay(LocalDateTime day) {
        return appointmentRepo.findByDate(day.toLocalDate().atStartOfDay(),
                                          day.toLocalDate().atStartOfDay().plusDays(1));
    }

    public List<Appointment> getAppointmentsForEmployee(int employeeId) {
        return appointmentRepo.findByEmployee(employeeId);
    }

    public Optional<Appointment> getAppointmentById(int id) {
        return appointmentRepo.findById(id);
    }

    private String validateAppointmentInput(Customer customer, Employee employee,
                                             Treatment treatment, LocalDateTime startTime) {
        if (customer == null) return "Vælg venligst en kunde.";
        if (employee == null) return "Vælg venligst en medarbejder.";
        if (treatment == null) return "Vælg venligst en behandling.";
        if (startTime == null) return "Vælg venligst et tidspunkt.";
        if (startTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
            return "Tidspunktet kan ikke ligge i fortiden.";
        }
        return null;
    }
}
