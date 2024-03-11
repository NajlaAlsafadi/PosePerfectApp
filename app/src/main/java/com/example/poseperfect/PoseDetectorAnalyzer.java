package com.example.poseperfect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.poseperfect.overlay.PoseOverlayView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

public class PoseDetectorAnalyzer implements ImageAnalysis.Analyzer {
    private boolean isProcessing = false;
    private String poseName;
    private PoseDetector poseDetector;
    private PoseOverlayView poseOverlayView;

    private ExerciseActivity exerciseActivity;

    public PoseDetectorAnalyzer(String poseName, PoseOverlayView poseOverlayView, ExerciseActivity exerciseActivity) {
        this.poseName = poseName;
        this.poseOverlayView = poseOverlayView;
        this.exerciseActivity = exerciseActivity;

        PoseDetectorOptionsBase options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        this.poseDetector = PoseDetection.getClient(options);
    }
    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy image) {
        if (isProcessing || !exerciseActivity.isTimerRunning ) {
            image.close();
            return;
        }
        isProcessing = true;
        InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());

        Task<Pose> result =
                poseDetector.process(inputImage)
                        .addOnSuccessListener(
                                new OnSuccessListener<Pose>() {
                                    @Override
                                    public void onSuccess(Pose pose) {
                                        // Task completed successfully
                                        if (!exerciseActivity.isTimerRunning) {
                                            isProcessing = false;
                                            image.close();
                                            return;
                                        }
                                        switch (poseName) {
                                            case "plank":
                                                checkPlankPose(pose);

                                                break;
                                            case "Bridge":
                                                checkPelvicCurlPose(pose);
                                                break;
                                            case "Boat":
                                                checkBoatPose(pose);
                                                break;
                                            case "T-Pose":
                                                checkStandingStraightArmsOutPose(pose);
                                                break;
                                        }
                                        poseOverlayView.updatePose(pose, inputImage.getWidth(), inputImage.getHeight());
                                        isProcessing = false;
                                        image.close();
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        isProcessing = false;
                                        image.close();
                                    }
                                });
    }

    private void checkPlankPose(Pose pose) {
        boolean isPlankCorrect = true;
        boolean isArmsStraight = true;
        boolean isBodyStraight = true;
        boolean isLegsStraight = true;
        Handler handler = new Handler(Looper.getMainLooper());
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        //check if landmarks are null before checking for pose so app doesnt crash
        if (rightShoulder == null || rightElbow == null || rightWrist == null ||
                rightHip == null || rightKnee == null || rightAnkle == null) {
            // If any of the landmarks are null, return early without checking the pose
            return;
        }

        // 1.Check if arms are straight if no display message


        float rightArmAngle = calculateAngle(rightShoulder, rightElbow, rightWrist);

        Log.d("PoseCheck", "Right arm angle: " + rightArmAngle);


        if (rightArmAngle > 180) {
            rightArmAngle = 360 - rightArmAngle;
        }

        if (Math.abs(rightArmAngle - 180) > 20) {
            // Display message: "Please keep your arms straight"
            isArmsStraight = false;
            isPlankCorrect = false;
        }
        // 2. Check if body is straight if no display message

        float rightBodyAngle = calculateAngle(rightShoulder, rightHip, rightKnee);

        Log.d("PoseCheck", "Right body angle: " + rightBodyAngle);

        if (rightBodyAngle > 180) {
            rightBodyAngle = 360 - rightBodyAngle;
        }

        if (Math.abs(rightBodyAngle - 180) > 15) {
            // Display message or handle the case when the body is not straight
            isPlankCorrect = false;
            isBodyStraight = false;
        }


        // 3. Check if legs are straight if no display message
        float rightLegAngle = calculateAngle(rightHip, rightKnee, rightAnkle);
        Log.d("PoseCheck", "Right leg angle: " + rightLegAngle);

        if (rightLegAngle > 180) {
            rightLegAngle = 360 - rightLegAngle;
        }

        if (Math.abs(rightLegAngle - 180) > 15) {
            // Display message or handle the case when the legs are not straight
            isLegsStraight = false;
            isPlankCorrect = false;
        }

        //Then handle if plank has been completed correctly or not
        if (isPlankCorrect) {
            exerciseActivity.runOnUiThread(() -> {
                exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                exerciseActivity.feedback1.setText("Pose is correct");
            });
            exerciseActivity.textToSpeech.speak("Pose is correct", TextToSpeech.QUEUE_ADD, null, null);
        } else {
            // Handle the case when the plank pose is incorrect
            if (!isArmsStraight) {
                exerciseActivity.runOnUiThread(() -> {
                    exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                    exerciseActivity.feedback1.setText("Please keep your arms straight");
                });
                handler.postDelayed(() -> {
                    exerciseActivity.textToSpeech.speak("Please keep your arms straight", TextToSpeech.QUEUE_ADD, null, null);
                }, 3000);
            }
            if (!isBodyStraight) {
                exerciseActivity.runOnUiThread(() -> {
                    exerciseActivity.feedback2.setVisibility(View.VISIBLE);
                    exerciseActivity.feedback2.setText("Please keep your body straight");
                });
                handler.postDelayed(() -> {
                    exerciseActivity.textToSpeech.speak("Please keep your body straight", TextToSpeech.QUEUE_ADD, null, null);
                }, 3000);
            }
            if (!isLegsStraight) {
                exerciseActivity.runOnUiThread(() -> {
                    exerciseActivity.feedback3.setVisibility(View.VISIBLE);
                    exerciseActivity.feedback3.setText("Please keep your legs straight");
                });
                handler.postDelayed(() -> {
                    exerciseActivity.textToSpeech.speak("Please keep your legs straight",
                            TextToSpeech.QUEUE_ADD, null, null);
                }, 3000);
            }
        }
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                boolean isPoseCorrect = true;
                boolean isBodyStraight = true;
                boolean areArmsOut = true;
                boolean areArmsTooHigh = false;
                boolean areArmsTooLow = false;



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

                if (Math.abs(rightBodyAngle - 180) > 15 || Math.abs(leftBodyAngle - 180) > 15) {
                    isPoseCorrect = false;
                    isBodyStraight = false;
                }
                Log.d("PoseCheck", "Is body straight: " + isBodyStraight);

                // Check if arms are out
                float rightArmAngle = calculateAngle(rightShoulder, rightElbow, rightHip);
                float leftArmAngle = calculateAngle(leftShoulder, leftElbow, leftHip);
                Log.d("PoseCheck", "Right arm angle before calculations: " + rightArmAngle);
                Log.d("PoseCheck", "Left arm angle before calculations: " + leftArmAngle);

                if (Math.abs(rightArmAngle - 90) > 20 || Math.abs(leftArmAngle - 90) > 20) {
                    isPoseCorrect = false;
                    areArmsOut = false;
                }
                Log.d("PoseCheck", "Are arms out: " + areArmsOut);

                if (isPoseCorrect) {
                    exerciseActivity.runOnUiThread(() -> {
                        exerciseActivity.feedback1.setVisibility(View.VISIBLE);

                    });
//                    exerciseActivity.speakFeedback("Pose is correct");
//
//                    exerciseActivity.openPostPoseActivity();
//                    exerciseActivity.textToSpeech.stop();
//                    exerciseActivity.finish();
                } else {
                    if (!isBodyStraight) {
                        exerciseActivity.runOnUiThread(() -> {
                            exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                            exerciseActivity.feedback1.setText("Please keep your body straight");
                        });

                        exerciseActivity.speakFeedback("Please keep your body straight");

                    }
                    if (!areArmsOut) {
                        exerciseActivity.runOnUiThread(() -> {
                            exerciseActivity.feedback2.setVisibility(View.VISIBLE);
                            exerciseActivity.feedback2.setText("Please keep your arms out");
                        });

                        exerciseActivity.speakFeedback("Please keep your arms out");

                    }

                }
                exerciseActivity.poseChecks.putBoolean("Outcome", isPoseCorrect);
                exerciseActivity.feedbackMap.put("Check1", new Object[]{isBodyStraight, isBodyStraight ? "Body was Straight" : "Your body was not straight"});
                exerciseActivity.feedbackMap.put("Check2", new Object[]{areArmsOut, areArmsOut ? "Arms were perpendicular to body" : "Your arms were not perpendicular to body"});
            }
        }, 3000);
    }



    private void checkPelvicCurlPose(Pose pose) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

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

                if (Math.abs(bodyAngle - 180) > 15) {
                    isPoseCorrect = false;
                    isBodyStraight = false;
                }
                Log.d("PoseCheck", "Is body straight: " + isBodyStraight);

                // Check if leg is bent
                float legAngle = calculateAngle(rightHip, rightKnee, rightAnkle);
                Log.d("PoseCheck", "Right leg angle before calculations: " + legAngle);

                if (Math.abs(legAngle - 90) > 15) {
                    isPoseCorrect = false;
                    isLegBent = false;
                }
                Log.d("PoseCheck", "Is leg bent: " + isLegBent);

                if (isPoseCorrect) {
                    exerciseActivity.runOnUiThread(() -> {
                        exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                       // exerciseActivity.feedback1.setText("Pose is correct");
                    });
//                    exerciseActivity.speakFeedback("Pose is correct");
//                    exerciseActivity.openPostPoseActivity();
//                    exerciseActivity.textToSpeech.stop();
//                    exerciseActivity.finish();
                } else {
                    if (!isBodyStraight) {
                        exerciseActivity.runOnUiThread(() -> {
                            exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                            exerciseActivity.feedback1.setText("Please keep your body straight");
                        });

                        exerciseActivity.speakFeedback("Please keep your body straight");

                    }
                    if (!isLegBent) {
                        exerciseActivity.runOnUiThread(() -> {
                            exerciseActivity.feedback2.setVisibility(View.VISIBLE);
                            exerciseActivity.feedback2.setText("Please adjust your hips to be inline with knees");
                        });

                        exerciseActivity.speakFeedback("Please adjust your hips to be inline with knees");

                    }

                }
                exerciseActivity.poseChecks.putBoolean("Outcome", isPoseCorrect);
                exerciseActivity.feedbackMap.put("Check1", new Object[]{isBodyStraight, isBodyStraight ? "Body was Straight" : "Your body was not straight"});
                exerciseActivity.feedbackMap.put("Check2", new Object[]{isLegBent, isLegBent ? "Leg was in Correct position" : "Your hips were not inline with knees"});
            }

        }, 3000);
    }

    private void checkBoatPose(Pose pose) {

    }
}