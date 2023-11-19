package com.example.poseperfect;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ExerciseActivity extends AppCompatActivity {

    private PreviewView previewView;
    private CountDownTimer countDownTimer;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private ImageAnalysis imageAnalysis;
    private TextToSpeech textToSpeech;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        String exerciseName = getIntent().getStringExtra("EXERCISE_NAME");
        previewView = findViewById(R.id.previewView);
        progressBar = findViewById(R.id.progressBar);
        timerTextView = findViewById(R.id.timer);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        if (allPermissionsGranted()) {
            startCamera();
            startTimer();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        }

    }


    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
        PoseOverlayView poseOverlayView = findViewById(R.id.poseOverlayView);
        imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        YogaPose targetPose = new YogaPose(180.0f);
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new PoseAnalyzer(poseOverlayView, targetPose, textToSpeech));

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }
    private void startTimer() {
        countDownTimer = new CountDownTimer(25000, 1000) {
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress((int) (millisUntilFinished / 1000));
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                // When timer finishes, close the camera and go back to ExerciseFragment
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ExerciseActivity", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
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
