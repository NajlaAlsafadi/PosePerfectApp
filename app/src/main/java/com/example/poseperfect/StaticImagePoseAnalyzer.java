package com.example.poseperfect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.poseperfect.homeNav.ExerciseFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;

import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import java.io.IOException;
import java.util.HashMap;

public class StaticImagePoseAnalyzer {


    private Bundle analysisResults = new Bundle();
    public Bundle poseChecks = new Bundle();
    private HashMap<String, Object[]> feedbackMap = new HashMap<>();

    private final Context context;
    private String poseName;

    public StaticImagePoseAnalyzer(Context context) {
        this.context = context;
    }
    public void setPoseName(String poseName) {
        this.poseName = poseName;
    }
    public void analyzeImage(Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(context, imageUri);
            PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                    .build();

            PoseDetector poseDetector = PoseDetection.getClient(options);

            poseDetector.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Pose>() {
                        @Override
                        public void onSuccess(Pose pose) {
                            if (pose.getAllPoseLandmarks().isEmpty()) {
                                Log.e("StaticImagePoseAnalyzer", "No pose landmarks detected in the image.");
                                Toast.makeText(context, "Image unclear, please use another photo.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            switch (poseName) {
                                case "Bridge":
                                    checkPelvicCurlPose(pose);
                                    break;
                                case "Boat":
                                    checkBoatPose(pose);
                                    break;
                                case "Warrior":
                                    checkWarriorPose(pose);
                                    break;
                                case "T-Pose":
                                    checkStandingStraightArmsOutPose(pose);
                                    break;
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e("PoseDetection", "Pose detection failed", e);
                            Toast.makeText(context, "Failed to analyze image. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (IOException e) {
            Log.e("PoseDetection", "Failed to load image from URI", e);
            Toast.makeText(context, "Failed to load image. Please check the image path and try again.", Toast.LENGTH_SHORT).show();

        }

    }

    private void checkWarriorPose(Pose pose) {

                boolean isPoseCorrect = true;
                boolean isFrontKneeBentCorrectly = true;
                boolean isBackLegStraight = true;
                boolean isArmsCorrectlyPositioned = true;
                boolean isBodyCorrectlyPositioned = true;

                PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
                PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
                PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
                PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
                PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
                PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
                PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
                PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);

                if (leftShoulder == null || rightShoulder == null || leftHip == null || rightHip == null ||
                        leftKnee == null || rightKnee == null || leftAnkle == null || rightAnkle == null) {
                    return;
                }

                // 1. Check if front knee is bent correctly
                float frontKneeAngle = calculateAngle(rightAnkle, rightKnee, rightHip);
                Log.d("PoseCheck", "Initial front knee angle: " + frontKneeAngle);
                if (Math.abs(frontKneeAngle - 110) > 10) {
                    isPoseCorrect = false;
                    isFrontKneeBentCorrectly = false;
                }
                Log.d("PoseCheck", "Is front knee bent correctly: " + isFrontKneeBentCorrectly);

                // 2. Check if back leg is straight
                float backLegAngle = calculateAngle(leftHip, leftKnee, leftAnkle);
                Log.d("PoseCheck", "Initial back leg angle: " + backLegAngle);
                if (Math.abs(backLegAngle - 180) > 10) {
                    isPoseCorrect = false;
                    isBackLegStraight = false;
                }
                Log.d("PoseCheck", "Is back leg straight: " + isBackLegStraight);

                // 3. Check if arms are correctly straight
                float armsAngle = calculateAngle(leftShoulder, rightShoulder, rightElbow);
                Log.d("PoseCheck", "Initial arms angle: " + armsAngle);
                if (Math.abs(armsAngle - 180) > 15) {
                    isPoseCorrect = false;
                    isArmsCorrectlyPositioned = false;
                }
                Log.d("PoseCheck", "Are arms correctly positioned: " + isArmsCorrectlyPositioned);

                // 4. Check if body is in correct position
                float bodyAngle = calculateAngle(rightShoulder, rightHip, rightKnee);
                Log.d("PoseCheck", "Initial body angle: " + bodyAngle);
                if (Math.abs(bodyAngle - 90) > 12) {
                    isPoseCorrect = false;
                    isBodyCorrectlyPositioned = false;
                }
                Log.d("PoseCheck", "Is body correctly positioned: " + isBodyCorrectlyPositioned);

        poseChecks.putBoolean("Outcome", isPoseCorrect);
        feedbackMap.put("Check1", new Object[]{isFrontKneeBentCorrectly, isFrontKneeBentCorrectly ? "Front Knee was at correct angle" : "Front Knee was not at correct angle"});
        feedbackMap.put("Check2", new Object[]{isBackLegStraight, isBackLegStraight ? "Back leg was straight" : "Bck leg was not straight"});
        feedbackMap.put("Check3", new Object[]{isArmsCorrectlyPositioned, isArmsCorrectlyPositioned ? "Arms were parallel to the ground" : "Arms were not parallel to the ground"});
        feedbackMap.put("Check4", new Object[]{isBodyCorrectlyPositioned, isBodyCorrectlyPositioned ? "Torso was in correct position" : "Torso was not upright"});
        onFinishAnalysis();
    }
    private void checkPelvicCurlPose(Pose pose) {


                boolean isPoseCorrect = true;
                boolean isBodyStraight = true;
                boolean isLegBent = true;


                PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
                PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
                PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
                PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

                if (rightShoulder == null || rightHip == null || rightKnee == null || rightAnkle == null) {
                    return;
                }

                // Check if body is straight
                float bodyAngle = calculateAngle(rightShoulder, rightHip, rightKnee);
                Log.d("PoseCheck", "Right body angle before calculations: " + bodyAngle);

                if (Math.abs(bodyAngle - 180) > 20) {
                    isPoseCorrect = false;
                    isBodyStraight = false;
                }
                Log.d("PoseCheck", "Is body straight: " + isBodyStraight);

                // Check if leg is bent 90 degrees
                float legAngle = calculateAngle(rightHip, rightKnee, rightAnkle);
                Log.d("PoseCheck", "Right leg angle before calculations: " + legAngle);

                if (Math.abs(legAngle - 90) > 15) {
                    isPoseCorrect = false;
                    isLegBent = false;
                }
                Log.d("PoseCheck", "Is leg bent: " + isLegBent);


        poseChecks.putBoolean("Outcome", isPoseCorrect);
                feedbackMap.put("Check1", new Object[]{isBodyStraight, isBodyStraight ? "Body was Straight" : "Your body was not straight"});
                feedbackMap.put("Check2", new Object[]{isLegBent, isLegBent ? "Leg was in Correct position" : "Your hips were not inline with knees"});
        onFinishAnalysis();
    }
    private float calculateAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        float angle = (float) Math.abs(
                Math.atan2(lastPoint.getPosition().y - midPoint.getPosition().y,
                        lastPoint.getPosition().x - midPoint.getPosition().x) -
                        Math.atan2(firstPoint.getPosition().y - midPoint.getPosition().y,
                                firstPoint.getPosition().x - midPoint.getPosition().x));
        angle = (float) Math.toDegrees(angle);
        if (angle > 180) {
            angle = 360.0f - angle;
        }

        return angle;
    }
    protected void checkStandingStraightArmsOutPose(Pose pose) {

        Log.d("StaticImagePoseAnalyzer", "Checking standing straight arms out pose");
                boolean isPoseCorrect = true;
                boolean isBodyStraight = true;
                boolean areArmsOut = true;




                PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
                PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
                PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
                PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
                PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
                PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
                PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);

                if (rightShoulder == null || leftShoulder == null || rightHip == null || leftHip == null || rightElbow == null || leftElbow == null) {
                    return;
                }

                // Check if body is straight
                float rightBodyAngle = calculateAngle(rightShoulder, rightHip, rightKnee);
                float leftBodyAngle = calculateAngle(leftShoulder, leftHip, leftKnee);
                Log.d("PoseCheck", "Right body angle before calculations: " + rightBodyAngle);
                Log.d("PoseCheck", "Left body angle before calculations: " + leftBodyAngle);

                if (Math.abs(rightBodyAngle - 180) > 20 || Math.abs(leftBodyAngle - 180) > 20) {
                    isPoseCorrect = false;
                    isBodyStraight = false;
                }
                Log.d("PoseCheck", "Is body straight: " + isBodyStraight);

                // Check if arms are out
                float rightArmAngle = calculateAngle(rightElbow, rightShoulder, rightHip);
                float leftArmAngle = calculateAngle(leftElbow, leftShoulder, leftHip);
                Log.d("PoseCheck", "Right arm angle before calculations: " + rightArmAngle);
                Log.d("PoseCheck", "Left arm angle before calculations: " + leftArmAngle);

                if (Math.abs(rightArmAngle - 90) > 15 || Math.abs(leftArmAngle - 90) > 15) {
                    isPoseCorrect = false;
                    areArmsOut = false;
                }
                Log.d("PoseCheck", "Are arms out: " + areArmsOut);


        poseChecks.putBoolean("Outcome", isPoseCorrect);
                feedbackMap.put("Check1", new Object[]{isBodyStraight, isBodyStraight ? "Body was Straight" : "Your body was not straight"});
                feedbackMap.put("Check2", new Object[]{areArmsOut, areArmsOut ? "Arms were perpendicular to body" : "Your arms were not perpendicular to body"});
        onFinishAnalysis();
    }
    private void checkBoatPose(Pose pose) {

                boolean isPoseCorrect = true;
                boolean isHipsAngleCorrect = true;
                boolean isLegsStraight = true;
                boolean isArmsParallelToGround = true;

                PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
                PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
                PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
                PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
                PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
                PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
                PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
                PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
                PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);

                if (leftShoulder == null || rightShoulder == null || leftHip == null || rightHip == null ||
                        leftKnee == null || rightKnee == null || leftAnkle == null || rightAnkle == null ||
                        leftWrist == null || rightWrist == null) {
                    return;
                }

                //1.  Check if thighs are 45 degrees to the ground
                float rightHipAngle = calculateAngle(rightShoulder, rightHip, rightKnee);
                Log.d("PoseCheck", "Right Hip before calculations: " + rightHipAngle);
                if (Math.abs(rightHipAngle - 50) > 20) {
                    isPoseCorrect = false;
                    isHipsAngleCorrect = false;
                }
                Log.d("PoseCheck", "Are hips at correct angle: " + isHipsAngleCorrect);
                // 2. Check if legs are straight
                float rightLegStraightness = calculateAngle(rightHip, rightKnee, rightAnkle);
                Log.d("PoseCheck", "Right leg straightness before calculations:" + rightLegStraightness);
                if (Math.abs(rightLegStraightness - 180) > 15) {
                    isPoseCorrect = false;
                    isLegsStraight = false;
                }
                Log.d("PoseCheck", "Are legs straight: " + isLegsStraight);
                // 3. Check if arms are 45 degrees to body
                float armAngleWithGround = calculateAngle(rightWrist, rightShoulder, rightHip);
                Log.d("PoseCheck", "armAngleWithGround before calculations:" + armAngleWithGround);
                if (Math.abs(armAngleWithGround - 50) > 10) {
                    isPoseCorrect = false;
                    isArmsParallelToGround = false;
                }
                Log.d("PoseCheck", "Are arms parallel: " + isArmsParallelToGround);

        poseChecks.putBoolean("Outcome", isPoseCorrect);
                feedbackMap.put("Check1", new Object[]{isHipsAngleCorrect, isHipsAngleCorrect ? "Legs were lifted high enough" : "Legs were not lifted high enough"});
                feedbackMap.put("Check2", new Object[]{isLegsStraight, isLegsStraight ? "Legs were straight" : "Legs were not straight"});
                feedbackMap.put("Check3", new Object[]{isArmsParallelToGround, isArmsParallelToGround ? "Arms were parallel to ground" : "Arms were not parallel to ground"});
        onFinishAnalysis();
    }

    public void openPostPoseActivity() {
        Intent intent = new Intent(context, PostPoseActivity.class);
        intent.putExtra("pose_checks", poseChecks);
        intent.putExtra("FeedbackMap", feedbackMap);
        intent.putExtra("pose_name", poseName);
        context.startActivity(intent);
    }

    private void onFinishAnalysis() {
        openPostPoseActivity();
    }



}
