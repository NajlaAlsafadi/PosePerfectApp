package com.example.poseperfect;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.transferwise.sequencelayout.SequenceStep;

import java.util.HashMap;

public class PostPoseActivity extends AppCompatActivity {

    SequenceStep outcome, check1, check2, check3, check4;
    TextView posename;

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

        // Iterate through potential checks, fix this as it is removing the progress bar**********************
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

                steps[i].setVisibility(View.GONE);
            }
        }
    }
}