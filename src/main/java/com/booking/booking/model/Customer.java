package com.booking.booking.model;

public class Customer {
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String notes;

    public Customer() {
    }

    public Customer(int id, String fullName, String email, String phone, String notes) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return fullName + " (" + phone + ")";
    }
}

