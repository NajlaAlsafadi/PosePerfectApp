
package com.example.poseperfect;

import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

public class PoseAnalyzer implements ImageAnalysis.Analyzer {
    private PoseDetector poseDetector;
    private boolean isProcessing = false;
    private PoseOverlayView poseOverlayView;
    private YogaPose targetPose;
    private static final float ANGLE_THRESHOLD = 10.0f;
    private TextToSpeech textToSpeech;

    public PoseAnalyzer(PoseOverlayView poseOverlayView,YogaPose targetPose,
                        TextToSpeech textToSpeech) {
        PoseDetectorOptionsBase options = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build();
        poseDetector = PoseDetection.getClient(options);
        this.poseOverlayView = poseOverlayView;
        this.targetPose = targetPose;
        this.textToSpeech = textToSpeech;
    }
    private void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }
    @OptIn(markerClass = ExperimentalGetImage.class) @Override
    public void analyze(@NonNull ImageProxy image) {
        if (isProcessing) {
            image.close();
            return;
        }

        isProcessing = true;
        InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());

        poseDetector.process(inputImage).addOnSuccessListener(pose -> {
             float detectedShoulderHipAngle = calculateAngle(
                    pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
                    pose.getPoseLandmark(PoseLandmark.LEFT_HIP),
                    pose.getPoseLandmark(PoseLandmark.RIGHT_HIP),
                    pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
            );
            if (Math.abs(detectedShoulderHipAngle - targetPose.getShoulderHipAngle()) < ANGLE_THRESHOLD) {
                // The user is doing the pose correctly
                speak("Your pose is correct. Well Done.");
            } else {
                // The user is not doing the pose correctly
                speak("Your pose is incorrect. Please adjust.");
            }
            poseOverlayView.updatePose(pose, inputImage.getWidth(), inputImage.getHeight());

            isProcessing = false;
            image.close();
        }).addOnFailureListener(e -> {
            isProcessing = false;
            image.close();
        });
    }
    private float calculateAngle(PoseLandmark a, PoseLandmark b, PoseLandmark c, PoseLandmark d) {
        double abX = b.getPosition().x - a.getPosition().x;
        double abY = b.getPosition().y - a.getPosition().y;
        double cdX = d.getPosition().x - c.getPosition().x;
        double cdY = d.getPosition().y - c.getPosition().y;

        double angleAB = Math.atan2(abY, abX);
        double angleCD = Math.atan2(cdY, cdX);

        double angleBetween = Math.toDegrees(angleAB - angleCD);
        return (float) Math.abs(angleBetween);
    }

}
