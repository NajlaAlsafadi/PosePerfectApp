package com.example.poseperfect;

import static androidx.constraintlayout.widget.StateSet.TAG;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poseperfect.homeNav.CheckResult;
import com.example.poseperfect.homeNav.ResultsAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostPoseActivity extends AppCompatActivity {

    TextView posename, outcome;
    TextView dateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postpose);
        RecyclerView resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        outcome = findViewById(R.id.outcome);
        posename = findViewById(R.id.posename);
        dateTextView = findViewById(R.id.textViewDate);

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(new Date());
        dateTextView.setText(currentDate);

        long poseDuration = getIntent().getLongExtra("POSE_DURATION", 0);
        String poseName = getIntent().getStringExtra("pose_name");
        posename.setText(poseName);
        Bundle poseChecks = getIntent().getBundleExtra("pose_checks");

        if (poseChecks.containsKey("Outcome")) {
            outcome.setText("Overall Pose Outcome: " + (poseChecks.getBoolean(
                    "Outcome") ? "Passed" : "Failed"));

        }

        Bundle analysisResults = getIntent().getBundleExtra("analysisResults");
        HashMap<String, Object[]> feedbackMap = (HashMap<String, Object[]>) getIntent()
                .getSerializableExtra("FeedbackMap");
        List<CheckResult> results = new ArrayList<>();
        for (Map.Entry<String, Object[]> entry : feedbackMap.entrySet()) {
            Object[] feedback = entry.getValue();
            boolean passed = (Boolean) feedback[0];
            String message = (String) feedback[1];
            results.add(new CheckResult(passed, message));
        }

        ResultsAdapter adapter = new ResultsAdapter(this, results);
        resultsRecyclerView.setAdapter(adapter);

        if (poseChecks.containsKey("Outcome")) {
            boolean poseResult = poseChecks.getBoolean("Outcome");
            Log.d("PostPoseActivity", "Received duration: " + poseDuration);
            storePoseResult(poseName, poseResult, poseDuration);
        }
        Button btnDownloadResults = findViewById(R.id.btnDownloadResults);
        btnDownloadResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveResultsAsImage();
            }
        });

    }
    private List<String> getFailedResultsWithMessages() {
        List<String> failedResults = new ArrayList<>();

        HashMap<String, Object[]> feedbackMap = (HashMap<String, Object[]>) getIntent()
                .getSerializableExtra("FeedbackMap");

        if (feedbackMap != null) {
            for (Map.Entry<String, Object[]> entry : feedbackMap.entrySet()) {
                boolean passed = (Boolean) entry.getValue()[0];
                String message = (String) entry.getValue()[1];


                if (!passed) {
                    failedResults.add(message);
                }
            }
        }

        return failedResults;
    }

    private void saveResultsAsImage() {
        View content = findViewById(R.id.postPose);
        Bitmap bitmap = Bitmap.createBitmap(content.getWidth(), content.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        content.draw(canvas);
        content.setBackgroundColor(Color.WHITE);
        content.draw(canvas);
        content.setBackgroundColor(0);
        String fileName = "PoseResults_" + System.currentTimeMillis() + ".png";
        File imagePath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), fileName);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Results saved to " + imagePath.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

        private void storePoseResult(String poseName, boolean poseResult, long durationMillis) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        .format(new Date());
            String entryId = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(currentUser.getUid())
                        .child("PoseResult")
                        .child(poseName)
                        .child(entryId);

                Map<String, Object> poseData = new HashMap<>();
                poseData.put("result", poseResult);
                poseData.put("date", currentDate);
                Log.d(TAG, "Storing duration: " + durationMillis);
                poseData.put("durationMillis", durationMillis);
            if (!poseResult) {
                List<String> failedMessages = getFailedResultsWithMessages();
                poseData.put("failedMessages", failedMessages);
            }
                userDbRef.setValue(poseData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "Pose result stored successfully.");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to store pose result.", e);
                            }
                        });
            }
        }





