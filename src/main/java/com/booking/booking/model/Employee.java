package com.booking.booking.model;

public class Employee {

    String name;

    TimeSchedule timeSchedule;

    public Employee(String name, TimeSchedule timeSchedule) {
        this.name = name;
        this.timeSchedule = timeSchedule;
    }

    public String getName() {
        return name;
    }

    public TimeSchedule getTimeSchedule() {
        return timeSchedule;
    }
}
