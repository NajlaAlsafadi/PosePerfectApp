package com.example.poseperfect.homeNav;

import java.util.List;

public class WeeklyGoal {
    private List<String> days;
    private int timeInMinutes;


    public WeeklyGoal() {
    }

    public WeeklyGoal(List<String> days, int timeInMinutes) {
        this.days = days;
        this.timeInMinutes = timeInMinutes;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public int getTimeInMinutes() {
        return timeInMinutes;
    }

    public void setTimeInMinutes(int timeInMinutes) {
        this.timeInMinutes = timeInMinutes;
    }
}
