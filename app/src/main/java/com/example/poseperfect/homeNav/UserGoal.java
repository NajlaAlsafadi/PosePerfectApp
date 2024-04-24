package com.example.poseperfect.homeNav;

import java.util.List;

public class UserGoal {
    public long goalTimeSeconds; // Goal time in seconds
    public String goalType; // daily or weekly
    public List<String> activeDays; // Active days for weekly goals

    public UserGoal() {

    }

    public UserGoal(long goalTimeSeconds, String goalType, List<String> activeDays) {
        this.goalTimeSeconds = goalTimeSeconds;
        this.goalType = goalType;
        this.activeDays = activeDays;
    }
}
