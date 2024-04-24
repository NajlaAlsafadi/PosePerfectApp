package com.example.poseperfect.homeNav;


public class WeeklyPoseData {
    private int successCount;
    private int failureCount;
    private long totalDuration;

    public void incrementSuccessCount() {
        successCount++;
    }

    public void incrementFailureCount() {
        failureCount++;
    }

    public void addDuration(long duration) {
        totalDuration += duration;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public long getTotalDuration() {
        return totalDuration;
    }
}