package com.example.poseperfect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.poseperfect.PostPoseActivity;
import com.example.poseperfect.homeNav.ExerciseFragment;
import com.example.poseperfect.overlay.PoseOverlayView;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExerciseActivity extends AppCompatActivity {

    private PreviewView previewView;
    private CountDownTimer countDownTimer;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private ImageAnalysis imageAnalysis;
    public TextToSpeech textToSpeech;
    public TextView feedback1, feedback2, feedback3, feedback4;
    private String poseName;
    public boolean isTimerRunning = false;
    private PoseDetectorAnalyzer poseDetectorAnalyzer;
    public Bundle poseChecks = new Bundle();
    private PoseOverlayView poseOverlayView;
    private String lastFeedback = null;
    private long lastFeedbackTime = 0;
    HashMap<String, Object[]> feedbackMap = new HashMap<>();
    private ProcessCameraProvider cameraProvider;
    private boolean isTTSInitialized = false;
    private boolean isTransitioningToPostPose = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        poseName = getIntent().getStringExtra(ExerciseFragment.POSE_NAME);
        poseOverlayView = findViewById(R.id.poseOverlayView);
        previewView = findViewById(R.id.previewView);
        progressBar = findViewById(R.id.progressBar);
        timerTextView = findViewById(R.id.timer);
        Button startButton = findViewById(R.id.startButton);
        startButton.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.GONE);
        feedback1 = findViewById(R.id.feedback1);
        feedback2 = findViewById(R.id.feedback2);
        feedback3 = findViewById(R.id.feedback3);
        feedback4 = findViewById(R.id.feedback4);

        feedback1.setVisibility(View.GONE);
        feedback2.setVisibility(View.GONE);
        feedback3.setVisibility(View.GONE);
        feedback4.setVisibility(View.GONE);

        // Call checkStandingStraightArmsOutPose and get the result Bundle
        poseDetectorAnalyzer = new PoseDetectorAnalyzer(poseName, poseOverlayView, this);
        Button cameraSwitchButton = findViewById(R.id.camera_switch_button);
        cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                    isTTSInitialized = true;
                }
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButton.setVisibility(View.GONE);
                timerTextView.setVisibility(View.VISIBLE);
                startTimer();
            }
        });
        timerTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Finish();
                return true;
            }
        });

        if (allPermissionsGranted()) {
            startCamera();
            //startTimer();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        }

    }
    private CameraSelector backCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private CameraSelector frontCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
    private CameraSelector currentCameraSelector = backCameraSelector;

    ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();

        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor, new PoseDetectorAnalyzer(poseName, poseOverlayView, this));
        backCameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        frontCameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        currentCameraSelector = backCameraSelector;
        this.cameraProvider = cameraProvider;

        cameraProvider.bindToLifecycle(this, currentCameraSelector, preview, imageAnalysis);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());


    }

    public void switchCamera() {
        Log.d("ExerciseActivity", "Current camera after switch: " + (currentCameraSelector == frontCameraSelector ? "Front" : "Back"));
        if (currentCameraSelector.equals(backCameraSelector)) {
            currentCameraSelector = frontCameraSelector;
            Log.d("ExerciseActivity", "Switching to front camera");
        } else {
            currentCameraSelector = backCameraSelector;
            Log.d("ExerciseActivity", "Switching to back camera");
        }
        try {
            if (cameraProvider != null) {
                cameraProvider.unbindAll();
                bindCameraUseCases();
            }
        } catch (Exception e) {
            Log.e("ExerciseActivity", "Failed to switch camera", e);
        }
    }

    private void bindCameraUseCases() {

        if (cameraProvider != null) {
            cameraProvider.unbindAll();


            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();
            imageAnalysis.setAnalyzer(cameraExecutor, poseDetectorAnalyzer);

            cameraProvider.bindToLifecycle(
                    this,
                    currentCameraSelector,
                    preview,
                    imageAnalysis);
        }
    }

    private void startTimer() {

        countDownTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                progressBar.setProgress((int) (millisUntilFinished / 1000));
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
                isTimerRunning = true;
            }

            public void onFinish() {

                Finish();

            }
        }.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
    public void Finish() {
        isTimerRunning = false;
        if (isTTSInitialized) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        openPostPoseActivity();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }


    protected void speakFeedback(String feedback) {
        long currentTime = System.currentTimeMillis();
        if (!feedback.equals(lastFeedback) || currentTime - lastFeedbackTime > 5000) {
            textToSpeech.speak(feedback, TextToSpeech.QUEUE_ADD, null, null);
            lastFeedback = feedback;
            lastFeedbackTime = currentTime;
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ExerciseActivity", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void openPostPoseActivity() {
        Intent intent = new Intent(this, PostPoseActivity.class);
        intent.putExtra("pose_checks", poseChecks);
        intent.putExtra("FeedbackMap", feedbackMap);
        intent.putExtra("pose_name", poseName);
        startActivity(intent);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                finish();
            }
        }
    }
}