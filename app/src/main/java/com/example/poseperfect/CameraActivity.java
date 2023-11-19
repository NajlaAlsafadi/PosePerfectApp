//package com.example.poseperfect;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CaptureRequest;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.ImageProxy;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.content.ContextCompat;
//
//import com.google.common.util.concurrent.ListenableFuture;
//
//import java.nio.ByteBuffer;
//import java.util.concurrent.ExecutionException;
//
//public class CameraActivity extends AppCompatActivity {
//
//    private PreviewView previewView;
//    private ImageAnalysis imageAnalysis;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_camera);
//
//        previewView = findViewById(R.id.view_finder);
//
//        startCamera();
//    }
//
//    private void startCamera() {
//        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                bindImageAnalysis(cameraProvider);
//            } catch (ExecutionException | InterruptedException e) {
//                // Handle any errors
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
//        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
//        imageAnalysis = builder.build();
//
//        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), image -> {
//            // Analyze the image
//            int rotationDegrees = image.getImageInfo().getRotationDegrees();
//            // Pass the image to the ExerciseActivity
//            Intent intent = new Intent(this, ExerciseActivity.class);
//            intent.putExtra("EXTRA_IMAGE", toBitmap(image));
//            startActivity(intent);
//        });
//
//        Preview preview = new Preview.Builder().build();
//        preview.setSurfaceProvider(previewView.getSurfaceProvider());
//
//        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
//        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
//    }
//    private Bitmap toBitmap(ImageProxy image) {
//        @SuppressLint("UnsafeExperimentalUsageError") ImageProxy.PlaneProxy planeProxy = image.getImage().getPlanes()[0];
//        ByteBuffer buffer = planeProxy.getBuffer();
//        byte[] bytes = new byte[buffer.remaining()];
//        buffer.get(bytes);
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//    }
//}