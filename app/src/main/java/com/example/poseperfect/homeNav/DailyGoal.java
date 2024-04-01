package com.example.poseperfect.homeNav;

public class DailyGoal {
    private int timeInMinutes;


    public DailyGoal() {
    }

    public DailyGoal(int timeInMinutes) {
        this.timeInMinutes = timeInMinutes;
    }

    public int getTimeInMinutes() {
        return timeInMinutes;
    }

    public void setTimeInMinutes(int timeInMinutes) {
        this.timeInMinutes = timeInMinutes;
    }
}
