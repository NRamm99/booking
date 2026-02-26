package com.booking.booking.model;

public class Service {

    String name;
    double price;
    int duration;

    public Service(String name, double price, int duration) {
        this.name = name;
        this.price = price;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getDuration() {
        return duration;
    }
}
