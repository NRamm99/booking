package com.booking.booking.service;

import com.booking.booking.model.Customer;
import com.booking.booking.repository.CustomerRepository;
import com.booking.booking.util.InputValidator;

import java.util.List;

public class CustomerService {
    private final CustomerRepository customerRepo;

    public CustomerService(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    public String createCustomer(String fullName, String email, String phone, String notes) {
        if (InputValidator.isNullOrBlank(fullName)) return "Navn er påkrævet.";
        if (!InputValidator.isNullOrBlank(email) && !InputValidator.isValidEmail(email))
            return "Ugyldig e-mailadresse.";
        if (!InputValidator.isNullOrBlank(phone) && !InputValidator.isValidPhone(phone))
            return "Ugyldigt telefonnummer.";

        Customer c = new Customer(0, fullName.trim(), email, phone, notes);
        return customerRepo.save(c) ? null : "Fejl ved oprettelse af kunde.";
    }

    public String updateCustomer(Customer c, String fullName, String email, String phone, String notes) {
        if (InputValidator.isNullOrBlank(fullName)) return "Navn er påkrævet.";
        c.setFullName(fullName.trim());
        c.setEmail(email);
        c.setPhone(phone);
        c.setNotes(notes);
        return customerRepo.update(c) ? null : "Fejl ved opdatering af kunde.";
    }

    public List<Customer> getAllCustomers() { return customerRepo.findAll(); }
    public List<Customer> searchCustomers(String query) { return customerRepo.search(query); }
}
