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
                                            case "Warrior":
                                                checkWarriorPose(pose);
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

                if (isPoseCorrect) {
                    exerciseActivity.runOnUiThread(() -> {
                        exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                        exerciseActivity.feedback1.setText("Pose is correct, Please Hold Form");

                    });
                    exerciseActivity.speakFeedback("Pose is correct, Please Hold Form");
                    exerciseActivity.textToSpeech.stop();

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

                if (isPoseCorrect) {
                    exerciseActivity.runOnUiThread(() -> {
                        exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                        exerciseActivity.feedback1.setText("Pose is correct, Please Hold Form");
                    });
                    exerciseActivity.speakFeedback("Pose is correct, Please Hold Form");
                    exerciseActivity.textToSpeech.stop();
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
                if (isPoseCorrect) {
                    exerciseActivity.runOnUiThread(() -> {
                        exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                        exerciseActivity.feedback1.setText("Pose is correct, Please Hold Form");
                    });
                    exerciseActivity.speakFeedback("Pose is correct, Please Hold Form");
                    exerciseActivity.textToSpeech.stop();
                } else {
                    if (!isHipsAngleCorrect) {
                        exerciseActivity.runOnUiThread(() -> {
                            exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                            exerciseActivity.feedback1.setText("Lift legs to be about 50 degrees");
                        });
                        exerciseActivity.speakFeedback("Lift legs to be about 50 degrees");
                    }
                    if (!isLegsStraight) {
                        exerciseActivity.runOnUiThread(() -> {
                            exerciseActivity.feedback2.setVisibility(View.VISIBLE);
                            exerciseActivity.feedback2.setText("Keep legs straight");
                        });

                        exerciseActivity.speakFeedback("Keep legs straight");
                    }
                    if (!isArmsParallelToGround) {
                        exerciseActivity.runOnUiThread(() -> {
                            exerciseActivity.feedback3.setVisibility(View.VISIBLE);
                            exerciseActivity.feedback3.setText("Extend arms parallel to the ground");
                        });
                        exerciseActivity.speakFeedback("Extend arms parallel to the ground");
                    }
                }
                exerciseActivity.poseChecks.putBoolean("Outcome", isPoseCorrect);
                exerciseActivity.feedbackMap.put("Check1", new Object[]{isHipsAngleCorrect, isHipsAngleCorrect ? "Legs were lifted high enough" : "Legs were not lifted high enough"});
                exerciseActivity.feedbackMap.put("Check2", new Object[]{isLegsStraight, isLegsStraight ? "Legs were straight" : "Legs were not straight"});
                exerciseActivity.feedbackMap.put("Check3", new Object[]{isArmsParallelToGround, isArmsParallelToGround ? "Arms were parallel to ground" : "Arms were not parallel to ground"});
            }
        }, 3000);
    }

    private void checkWarriorPose(Pose pose) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

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


                if (isPoseCorrect) {
                    exerciseActivity.runOnUiThread(() -> {
                        exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                        exerciseActivity.feedback1.setText("Pose is correct, Please Hold Form");

                    });
                    exerciseActivity.speakFeedback("Pose is correct, Please Hold Form");
                    exerciseActivity.textToSpeech.stop();
                } else {
                if (!isFrontKneeBentCorrectly) {
                    exerciseActivity.runOnUiThread(() -> {
                        exerciseActivity.feedback1.setVisibility(View.VISIBLE);
                        exerciseActivity.feedback1.setText("Front Knee is not bent enough");
                    });
                    exerciseActivity.speakFeedback("Front Knee is not bent enough");
                }
                if (!isBackLegStraight) {
                    exerciseActivity.runOnUiThread(() -> {
                        exerciseActivity.feedback2.setVisibility(View.VISIBLE);
                        exerciseActivity.feedback2.setText("Keep back leg straight");
                    });

                    exerciseActivity.speakFeedback("Keep back leg straight");
                }
                if (!isArmsCorrectlyPositioned) {
                    exerciseActivity.runOnUiThread(() -> {
                        exerciseActivity.feedback3.setVisibility(View.VISIBLE);
                        exerciseActivity.feedback3.setText("Keep arms straight out");
                    });
                    exerciseActivity.speakFeedback("Keep arms straight out");
                }
                    if (!isBodyCorrectlyPositioned) {
                        exerciseActivity.runOnUiThread(() -> {
                            exerciseActivity.feedback4.setVisibility(View.VISIBLE);
                            exerciseActivity.feedback4.setText("Keep body straight");
                        });
                        exerciseActivity.speakFeedback("Keep body straight");
                    }
            }
                exerciseActivity.poseChecks.putBoolean("Outcome", isPoseCorrect);
                exerciseActivity.feedbackMap.put("Check1", new Object[]{isFrontKneeBentCorrectly, isFrontKneeBentCorrectly ? "Front Knee was at correct angle" : "Front Knee was not at correct angle"});
                exerciseActivity.feedbackMap.put("Check2", new Object[]{isBackLegStraight, isBackLegStraight ? "Back leg was straight" : "Bck leg was not straight"});
                exerciseActivity.feedbackMap.put("Check3", new Object[]{isArmsCorrectlyPositioned, isArmsCorrectlyPositioned ? "Arms were parallel to the ground" : "Arms were not parallel to the ground"});
                exerciseActivity.feedbackMap.put("Check4", new Object[]{isBodyCorrectlyPositioned, isBodyCorrectlyPositioned ? "Torso was in correct position" : "Torso was not upright"});

            }
        }, 3000);
    }
}