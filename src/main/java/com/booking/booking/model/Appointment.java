package com.booking.booking.model;


import java.time.LocalDateTime;

public class Appointment {
    private int id;
    private Customer customer;
    private Employee employee;
    private Treatment treatment;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;
    private String notes;
    private boolean hasPayed;
    private LocalDateTime createdAt;

    public enum Status {
        ACTIVE("Aktiv"),
        COMPLETED("Gennemført"),
        CANCELLED("Aflyst");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public Appointment() {
    }

    public Appointment(int id, Customer customer, Employee employee, Treatment treatment,
                       LocalDateTime startTime, LocalDateTime endTime,
                       Status status, String notes, boolean hasPayed, LocalDateTime createdAt) {
        this.id = id;
        this.customer = customer;
        this.employee = employee;
        this.treatment = treatment;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.notes = notes;
        this.hasPayed = hasPayed;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isHasPayed() {
        return hasPayed;
    }

    public void setHasPayed(boolean hasPayed) {
        this.hasPayed = hasPayed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
