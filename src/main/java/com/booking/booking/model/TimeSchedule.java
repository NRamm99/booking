package com.booking.booking.model;

import java.time.LocalTime;

public class TimeSchedule {

    WeekDay weekDay;
    LocalTime startTime;
    LocalTime endTime;

    public TimeSchedule(WeekDay weekDay, LocalTime startTime, LocalTime endTime) {
        this.weekDay = weekDay;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public WeekDay getWeekDay() {
        return weekDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    // TODO: SKal være time slots
}
