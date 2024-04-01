package com.example.poseperfect.homeNav;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.poseperfect.ExerciseActivity;
import com.example.poseperfect.PoseDetectorAnalyzer;
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
    public static final String POSE_NAME = "pose_name";
    private PieChart pieChart;
    private Button goalButton;
    private ProgressBar goalProgressBar;
    private TextView goalProgressText;
    private HashMap<String, int[]> poseResults = new HashMap<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_progress, container, false);
        goalProgressBar = rootView.findViewById(R.id.goalProgressBar);
        goalProgressText = rootView.findViewById(R.id.goalProgressText);
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
        goalButton = rootView.findViewById(R.id.goalButton);
        goalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoalDialog();
            }
        });
        return rootView;
    }

    private void showGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_goal, null);

        final NumberPicker goalTimePicker = view.findViewById(R.id.goalTimePicker);
        goalTimePicker.setMinValue(0);
        goalTimePicker.setMaxValue(60);
        final RadioButton dailyButton = view.findViewById(R.id.dailyButton);
        final RadioButton weeklyButton = view.findViewById(R.id.weeklyButton);
        final LinearLayout daysLayout = view.findViewById(R.id.daysLayout);
        final CheckBox checkBoxMonday = view.findViewById(R.id.checkBoxMonday);
        final CheckBox checkBoxTuesday = view.findViewById(R.id.checkBoxTuesday);
        final CheckBox checkBoxWednesday = view.findViewById(R.id.checkBoxWednesday);
        final CheckBox checkBoxThursday = view.findViewById(R.id.checkBoxThursday);
        final CheckBox checkBoxFriday = view.findViewById(R.id.checkBoxFriday);
        final CheckBox checkBoxSaturday = view.findViewById(R.id.checkBoxSaturday);
        final CheckBox checkBoxSunday = view.findViewById(R.id.checkBoxSunday);
        dailyButton.setChecked(true);
        dailyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dailyButton.isChecked()) {
                    weeklyButton.setChecked(false);
                    daysLayout.setVisibility(View.GONE);
                }
            }
        });

        weeklyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (weeklyButton.isChecked()) {
                    dailyButton.setChecked(false);
                    daysLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        final CheckBox[] checkBoxes = new CheckBox[] {
                checkBoxMonday, checkBoxTuesday, checkBoxWednesday,
                checkBoxThursday, checkBoxFriday, checkBoxSaturday, checkBoxSunday
        };
        View.OnClickListener checkBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox clickedCheckBox = (CheckBox) v;
                int selectedDays = 0;
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.isChecked()) selectedDays++;
                }

                if (selectedDays > 6) {
                    Toast.makeText(getContext(), "For More Than 6 Days, Please use Daily goal", Toast.LENGTH_LONG).show();
                    clickedCheckBox.setChecked(false);
                }
            }
        };

        for (CheckBox checkBox : checkBoxes) {
            checkBox.setOnClickListener(checkBoxListener);
        }
        builder.setView(view)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int time = goalTimePicker.getValue();
                        if (dailyButton.isChecked()) {
                            saveDailyGoal(time);
                            Toast.makeText(getContext(), "Your New Goal has been set.", Toast.LENGTH_LONG).show();
                        } else if (weeklyButton.isChecked()) {
                            List<String> selectedDays = new ArrayList<>();
                            for (CheckBox checkBox : checkBoxes) {
                                if (checkBox.isChecked()) {
                                    selectedDays.add(checkBox.getText().toString());
                                }
                            }
                            saveWeeklyGoal(selectedDays, time);
                            Toast.makeText(getContext(), "Your New Goal has been set.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }
    private void saveDailyGoal(int time) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user.getUid()).child("goals");

            dbRef.child("weekly").removeValue();
            dbRef.child("daily").setValue(new DailyGoal(time)).addOnSuccessListener(aVoid -> {
                updateGoalProgress(time, "Daily", 1);
            });
        }
        scheduleReminder(null);
    }

    private void saveWeeklyGoal(List<String> days, int time) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user.getUid()).child("goals");

            dbRef.child("daily").removeValue();
            dbRef.child("weekly").setValue(new WeeklyGoal(days, time)).addOnSuccessListener(aVoid -> {
                updateGoalProgress(time, "Weekly", days.size());
            });
        }
        scheduleReminder(days);
    }

    private void updateGoalProgress(int goalTimeMinutes, String goalType, int numberOfDays) {
        int progress = (goalTimeMinutes * numberOfDays * 100) / (60 * 7);
        goalProgressBar.setProgress(progress);

        String goalTimeText = goalTimeMinutes + " min";
        if(goalTimeMinutes >= 60) {
            int hours = goalTimeMinutes / 60;
            int minutes = goalTimeMinutes % 60;
            goalTimeText = hours + "h " + minutes + "min";
        }

        String goalText = "Goal: " + goalType + " - " + goalTimeText;
        goalProgressText.setText(goalText);
    }
    private void fetchAndUpdateGoalProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not logged in.");
            return;
        }
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("goals");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("daily")) {
                    DailyGoal dailyGoal = dataSnapshot.child("daily").getValue(DailyGoal.class);
                    if (dailyGoal != null) {
                        updateGoalProgress(dailyGoal.getTimeInMinutes(), "Daily", 1);
                    }
                } else if (dataSnapshot.hasChild("weekly")) {
                    WeeklyGoal weeklyGoal = dataSnapshot.child("weekly").getValue(WeeklyGoal.class);
                    if (weeklyGoal != null) {
                        updateGoalProgress(weeklyGoal.getTimeInMinutes(), "Weekly", weeklyGoal.getDays().size());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch goal.", databaseError.toException());
            }
        });
    }
    private void scheduleReminder(List<String> daysOfWeek) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        int hour = 17;
        int minute = 0;


        cancelExistingReminders(alarmManager);

        // daily reminders
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            Intent intent = new Intent(getContext(), ReminderBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            // weekly reminders
            for (int i = 0; i < daysOfWeek.size(); i++) {
                Intent intent = new Intent(getContext(), ReminderBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), i, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.DAY_OF_WEEK, convertDayToInt(daysOfWeek.get(i)));

                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            }
        }
    }
    private void cancelExistingReminders(AlarmManager alarmManager) {
        Intent intent = new Intent(getContext(), ReminderBroadcastReceiver.class);

        for (int i = 0; i < 7; i++) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
        }
    }

    private int convertDayToInt(String dayOfWeek) {
        int day = Calendar.MONDAY;
        if ("Monday".equalsIgnoreCase(dayOfWeek)) {
            day = Calendar.MONDAY;
        } else if ("Tuesday".equalsIgnoreCase(dayOfWeek)) {
            day = Calendar.TUESDAY;
        } else if ("Wednesday".equalsIgnoreCase(dayOfWeek)) {
            day = Calendar.WEDNESDAY;
        } else if ("Thursday".equalsIgnoreCase(dayOfWeek)) {
            day = Calendar.THURSDAY;
        } else if ("Friday".equalsIgnoreCase(dayOfWeek)) {
            day = Calendar.FRIDAY;
        } else if ("Saturday".equalsIgnoreCase(dayOfWeek)) {
            day = Calendar.SATURDAY;
        } else if ("Sunday".equalsIgnoreCase(dayOfWeek)) {
            day = Calendar.SUNDAY;
        }
        return day;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchAndUpdateGoalProgress();
        fetchPoseData();
    }
    @Override
    public void onResume() {
        super.onResume();
        fetchAndUpdateGoalProgress();
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (suggestedPose != null) {
            final String poseForButton = suggestedPose;
            builder.setMessage("Based on your previous attempts we recommend you work on : " + suggestedPose)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setNegativeButton("Start Now", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getActivity(), ExerciseActivity.class);
                            intent.putExtra(POSE_NAME, poseForButton);
                            startActivity(intent);;
                        }
                    });
        } else {
            builder.setMessage("No poses to suggest")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }
//    private void fetchPoseData() {
//        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PoseResult");
//        dbRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                List<String> poseNames = new ArrayList<>();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    poseNames.add(snapshot.getKey());
//                }
//
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, poseNames);
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                poseSpinner.setAdapter(adapter);
//
//                poseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        String poseName = (String) parent.getItemAtPosition(position);
//
//
//                        fetchPoseResults(poseName);
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
private void fetchPoseData() {
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .child("PoseResult");

    dbRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Check if Fragment is attached
            if (getActivity() == null) {
                Log.e(TAG, "Activity is null, skipping fetchPoseData.");
                return;
            }

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