package com.example.poseperfect;

public interface PoseAnalysisCallback {
    void onPoseAnalysisCompleted(boolean isPoseCorrect, String feedback);
}

