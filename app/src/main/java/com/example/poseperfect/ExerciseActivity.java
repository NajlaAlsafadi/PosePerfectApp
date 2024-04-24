package com.example.poseperfect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
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
    HashMap<String, Object[]> feedbackMap = new HashMap<>();
    public boolean isTimerRunning = false;
    private PoseDetectorAnalyzer poseDetectorAnalyzer;
    public Bundle poseChecks = new Bundle();
    private PoseOverlayView poseOverlayView;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private boolean longPressed = false;
    private CameraSelector backCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private CameraSelector frontCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
    private CameraSelector currentCameraSelector = backCameraSelector;
    private String lastFeedback = null;

    private long lastFeedbackTime = 0;
    private long poseDuration = 30000;
    protected Handler poseCheckHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        initializeViews();
        setupTextToSpeech();
        configureCameraSwitch();
        checkCameraPermission();
    }

    private void initializeViews() {
        poseName = getIntent().getStringExtra(ExerciseFragment.POSE_NAME);
        previewView = findViewById(R.id.previewView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(30);
        timerTextView = findViewById(R.id.timer);
        poseOverlayView = findViewById(R.id.poseOverlayView);
        feedback1 = findViewById(R.id.feedback1);
        feedback2 = findViewById(R.id.feedback2);
        feedback3 = findViewById(R.id.feedback3);
        feedback4 = findViewById(R.id.feedback4);
        setFeedbackVisibility(View.GONE);

        timerTextView.setVisibility(View.VISIBLE);
        timerTextView.setText("Start Timer");
        timerTextView.setOnClickListener(v -> startTimer());
        timerTextView.setOnLongClickListener(v -> {
            longPressed = true;
            stopTimerAndCleanup();
            return true;
        });

        poseDetectorAnalyzer = new PoseDetectorAnalyzer(poseName, poseOverlayView, this);
    }

    private void setFeedbackVisibility(int visibility) {
        feedback1.setVisibility(visibility);
        feedback2.setVisibility(visibility);
        feedback3.setVisibility(visibility);
        feedback4.setVisibility(visibility);
    }

    private void stopTimerAndCleanup() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        shutdownTextToSpeech();
        cleanupCamera();
        if (longPressed) {
            navigateBack();
        }
    }

    private void navigateBack() {
        finish();
    }

    private void cleanupCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdownNow();
        }
    }

    private void shutdownTextToSpeech() {
        if (textToSpeech != null) {
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            textToSpeech.shutdown();
        }
    }

    private void setupTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
            } else {
                Log.e("TTS", "Initialization failed!");
            }
        });
    }

    private void configureCameraSwitch() {
        findViewById(R.id.camera_switch_button).setOnClickListener(v -> switchCamera());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        } else {
            startCamera();
        }
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


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraFuture", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        //make sure there's nothing bound to the life cycle already
        if (cameraProvider != null) {
            cameraProvider.unbindAll();

            //setting up the preview use case
            Preview preview = new Preview.Builder().build();

            //set the surface provider of the PreviewView to display the camera preview
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            //stting up the ImageAnalysis use case
            imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();
            imageAnalysis.setAnalyzer(cameraExecutor, poseDetectorAnalyzer);

            //use the currentCameraSelector here instead of creating a new CameraSelector
            cameraProvider.bindToLifecycle(this, currentCameraSelector, preview, imageAnalysis);
        }
    }



    protected void speakFeedback(String feedback) {
        long currentTime = System.currentTimeMillis();
        if (!feedback.equals(lastFeedback) || currentTime - lastFeedbackTime > 5000) {
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop(); // Ensure no concurrent speech conflict
            }
            textToSpeech.speak(feedback, TextToSpeech.QUEUE_FLUSH, null, null); //QUEUE_FLUSH to clear the previous queue
            lastFeedback = feedback;
            lastFeedbackTime = currentTime;
        }
    }

    private void startTimer() {
        isTimerRunning = true;
        longPressed = false;
        countDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress((int) (millisUntilFinished / 1000));
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                if (!longPressed) {
                    openPostPoseActivity();
                }
            }
        }.start();
    }

    public void openPostPoseActivity() {
        Intent intent = new Intent(this, PostPoseActivity.class);
        intent.putExtra("pose_checks", poseChecks);
        intent.putExtra("FeedbackMap", feedbackMap);
        intent.putExtra("pose_name", poseName);
        intent.putExtra("POSE_DURATION", poseDuration);
        startActivity(intent);
        finish();
        Log.d("ExerciseActivity", "Sending duration: " + 30000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimerAndCleanup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        poseCheckHandler.removeCallbacksAndMessages(null);
        cleanupCamera();
        shutdownTextToSpeech();
    }
}
