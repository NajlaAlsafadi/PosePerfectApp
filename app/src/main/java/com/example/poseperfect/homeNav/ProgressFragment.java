package com.example.poseperfect.homeNav;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.poseperfect.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ProgressFragment extends Fragment {
    private TextView dateTextView;
    private Spinner poseSpinner;
    private PieChart pieChart;
    private HashMap<String, int[]> poseResults = new HashMap<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_progress, container, false);

        dateTextView = rootView.findViewById(R.id.textViewDate);
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);
        poseSpinner = rootView.findViewById(R.id.poseSpinner);
        pieChart = rootView.findViewById(R.id.pieChartPlaceholder);
        Button suggestPoseButton = rootView.findViewById(R.id.suggestPoseButton);
        suggestPoseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestPose();
            }
        });
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchPoseData();
    }
    private void suggestPose() {
        String suggestedPose = null;
        double highestRatio = 0;

        for (Map.Entry<String, int[]> entry : poseResults.entrySet()) {
            int trueCount = entry.getValue()[0];
            int falseCount = entry.getValue()[1];
            double ratio = trueCount > 0 ? (double) falseCount / trueCount : falseCount;

            if (ratio > highestRatio) {
                highestRatio = ratio;
                suggestedPose = entry.getKey();
            }
        }

        if (suggestedPose != null) {
            Toast.makeText(getContext(), "Suggested Pose: " + suggestedPose, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "No poses to suggest", Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchPoseData() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PoseResult");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> poseNames = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    poseNames.add(snapshot.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, poseNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                poseSpinner.setAdapter(adapter);

                poseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String poseName = (String) parent.getItemAtPosition(position);


                        fetchPoseResults(poseName);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchPoseResults(String poseName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not logged in.");
            return;
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("PoseResult")
                .child(poseName);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int trueCount = 0;
                int falseCount = 0;


                for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot monthSnapshot : daySnapshot.getChildren()) {
                        for (DataSnapshot yearSnapshot : monthSnapshot.getChildren()) {
                            for (DataSnapshot entrySnapshot : yearSnapshot.getChildren()) {
                                Boolean result = entrySnapshot.child("result").getValue(Boolean.class);
                                if (Boolean.TRUE.equals(result)) {
                                    trueCount++;
                                } else if (Boolean.FALSE.equals(result)) {
                                    falseCount++;
                                }
                            }
                        }
                    }
                }
                poseResults.put(poseName, new int[]{trueCount, falseCount});
                displayResultsInPieChart(trueCount, falseCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch pose results.", databaseError.toException());
            }
        });
    }

    private void displayResultsInPieChart(int trueCount, int falseCount) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(trueCount, "True"));
        entries.add(new PieEntry(falseCount, "False"));

        PieDataSet set = new PieDataSet(entries, "Results");
        set.setColors(new int[]{R.color.green, R.color.red}, getContext());

        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.invalidate();
    }
}


