package com.example.poseperfect;

import com.google.mlkit.vision.pose.Pose;

public interface PoseCheckDelegate {
    void checkPose(Pose pose, PoseAnalysisCallback callback);
}
