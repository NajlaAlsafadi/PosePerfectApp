package com.example.poseperfect;

import static androidx.constraintlayout.widget.StateSet.TAG;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.transferwise.sequencelayout.SequenceStep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PostPoseActivity extends AppCompatActivity {

    SequenceStep outcome, check1, check2, check3, check4;
    TextView posename;
    TextView dateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postpose);
        check1 = findViewById(R.id.check1);
        check2 = findViewById(R.id.check2);
        check3 = findViewById(R.id.check3);
        check4 = findViewById(R.id.check4);
        outcome = findViewById(R.id.outcome);
        posename = findViewById(R.id.posename);
        dateTextView = findViewById(R.id.textViewDate);
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);
        String poseName = getIntent().getStringExtra("pose_name");
        Bundle poseChecks = getIntent().getBundleExtra("pose_checks");

        posename.setText(poseName);
        if (poseChecks.containsKey("Outcome")) {
            outcome.setTitle("Overall Pose Outcome");
            outcome.setSubtitle(poseChecks.getBoolean("Outcome") ? "Passed" : "Failed");
            outcome.setActive(poseChecks.getBoolean("Outcome"));
        }

        HashMap<String, Object[]> feedbackMap = (HashMap<String, Object[]>) getIntent().getSerializableExtra("FeedbackMap");
        SequenceStep[] steps = new SequenceStep[]{
                findViewById(R.id.check1),
                findViewById(R.id.check2),
                findViewById(R.id.check3),
                findViewById(R.id.check4)
        };

        for (int i = 0; i < steps.length; i++) {
            String checkKey = "Check" + (i + 1);
            if (feedbackMap != null && feedbackMap.containsKey(checkKey)) {
                Object[] feedbackData = feedbackMap.get(checkKey);
                if (feedbackData != null && feedbackData.length == 2) {
                    boolean checkResult = (boolean) feedbackData[0];
                    String feedbackMessage = (String) feedbackData[1];
                    steps[i].setTitle("Check " + (i + 1) + (checkResult ? " - Passed" : " - Failed"));
                    steps[i].setSubtitle(feedbackMessage);
                    steps[i].setActive(checkResult);
                    steps[i].setVisibility(View.VISIBLE);
                }
            } else {

                steps[i].setVisibility(View.INVISIBLE);
            }
        }
        if (poseChecks.containsKey("Outcome")) {
            boolean poseResult = poseChecks.getBoolean("Outcome");
            storePoseResult(poseName, poseResult);
        }
        Button btnDownloadResults = findViewById(R.id.btnDownloadResults);
        btnDownloadResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveResultsAsImage();
            }
        });

    }
    private void saveResultsAsImage() {
        View content = findViewById(R.id.postPose);
        Bitmap bitmap = Bitmap.createBitmap(content.getWidth(), content.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        content.draw(canvas);

        String fileName = "PoseResults_" + System.currentTimeMillis() + ".png";
        File imagePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Results saved to " + imagePath.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private long lastPoseTime = 0;
    private void storePoseResult(String poseName, boolean poseResult) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastPoseTime < 20 * 1000) {
                return;
            }
            lastPoseTime = currentTime;

            String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(currentUser.getUid())
                    .child("PoseResult")
                    .child(poseName)
                    .child(currentDate)
                    .push();

            Map<String, Object> poseData = new HashMap<>();
            poseData.put("result", poseResult);
            poseData.put("date", currentDate);

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

}


