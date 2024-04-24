package com.example.poseperfect.homeNav;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.poseperfect.ExerciseActivity;
import com.example.poseperfect.R;
import com.example.poseperfect.ReminderBroadcast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class ProgressFragment extends Fragment {
    private TextView dateTextView;
    private Spinner poseSpinner;
    public static final String POSE_NAME = "pose_name";
    private PieChart pieChart;
    private Button goalButton;
    private HashMap<String, int[]> poseResults = new HashMap<>();


    private DatabaseReference dbRef;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_progress, container, false);

        dateTextView = rootView.findViewById(R.id.textViewDate);
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);
        poseSpinner = rootView.findViewById(R.id.poseSpinner);

        pieChart = rootView.findViewById(R.id.pieChartPlaceholder);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
              //  fetchAllPosesStatistics();
                String poseName = poseSpinner.getSelectedItem().toString();
                displayPoseStatistics(poseName);


            }

            @Override
            public void onNothingSelected() {
            }
        });


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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            dbRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        }


        return rootView;
    }



    private void showGoalDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_goal);
        dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.95), ViewGroup.LayoutParams.WRAP_CONTENT);

        final NumberPicker numberPickerHours = dialog.findViewById(R.id.numberPickerHours);
        numberPickerHours.setMinValue(0);  //minimum number of hours
        numberPickerHours.setMaxValue(6);//max
        numberPickerHours.setValue(0);

        final NumberPicker numberPickerMinutes = dialog.findViewById(R.id.numberPickerMinutes);
        numberPickerMinutes.setMinValue(0);  //minimum number of minutes
        numberPickerMinutes.setMaxValue(59);  //max
        numberPickerMinutes.setValue(30);
        final CheckBox checkBoxMonday = dialog.findViewById(R.id.checkBoxMonday);
        final CheckBox checkBoxTuesday = dialog.findViewById(R.id.checkBoxTuesday);
        final CheckBox checkBoxWednesday = dialog.findViewById(R.id.checkBoxWednesday);
        final CheckBox checkBoxThursday = dialog.findViewById(R.id.checkBoxThursday);
        final CheckBox checkBoxFriday = dialog.findViewById(R.id.checkBoxFriday);
        final CheckBox checkBoxSaturday = dialog.findViewById(R.id.checkBoxSaturday);
        final CheckBox checkBoxSunday = dialog.findViewById(R.id.checkBoxSunday);
        Button btnSave = dialog.findViewById(R.id.btnSaveGoal);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hours = numberPickerHours.getValue();
                int minutes = numberPickerMinutes.getValue();
                List<String> reminderDays = new ArrayList<>();
                if (checkBoxMonday.isChecked()) reminderDays.add("Monday");
                if (checkBoxTuesday.isChecked()) reminderDays.add("Tuesday");
                if (checkBoxWednesday.isChecked()) reminderDays.add("Wednesday");
                if (checkBoxThursday.isChecked()) reminderDays.add("Thursday");
                if (checkBoxFriday.isChecked()) reminderDays.add("Friday");
                if (checkBoxSaturday.isChecked()) reminderDays.add("Saturday");
                if (checkBoxSunday.isChecked()) reminderDays.add("Sunday");

                saveGoalToFirebase(hours, minutes, reminderDays);
                scheduleReminder(reminderDays);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveGoalToFirebase(int hour, int minute, List<String> reminderDays) {
        Map<String, Object> goalData = new HashMap<>();
        goalData.put("goalTimeHour", hour);
        goalData.put("goalTimeMinute", minute);
        goalData.put("reminderDays", reminderDays);

        dbRef.child("goals").setValue(goalData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Goal saved successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save goal", Toast.LENGTH_SHORT).show());
    }

//    private void scheduleReminder(List<String> reminderDays) {
//        Intent intent = new Intent(getContext(), ReminderBroadcast.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
//        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
//        long timeAtButtonClick = System.currentTimeMillis();
//        long tenSecondsMillis = 1000 * 10;
//        alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtButtonClick + tenSecondsMillis,pendingIntent);

    private void scheduleReminder(List<String> reminderDays) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("ScheduleReminder", "Failed to get AlarmManager service");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            return; // Stop further execution until the permission is granted
        }

        for (String day : reminderDays) {
            int dayOfWeek = getDayOfWeekInt(day);
            Calendar alarmTime = getNextAlarmTime(dayOfWeek, 10, 05); // Set for 9:30 PM

            Intent intent = new Intent(getContext(), ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), dayOfWeek, intent, PendingIntent.FLAG_IMMUTABLE);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
            Log.d("ScheduleReminder", "Alarm set for " + day + " at 9:30 PM.");
        }
    }

    private Calendar getNextAlarmTime(int dayOfWeek, int hourOfDay, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);
        alarmTime.set(Calendar.DAY_OF_WEEK, dayOfWeek);

        // If the alarm time is before the current time, set it for the next week
        if (alarmTime.before(now)) {
            alarmTime.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return alarmTime;
    }

    private int getDayOfWeekInt(String day) {
        switch (day.toLowerCase()) {
            case "sunday": return Calendar.SUNDAY;
            case "monday": return Calendar.MONDAY;
            case "tuesday": return Calendar.TUESDAY;
            case "wednesday": return Calendar.WEDNESDAY;
            case "thursday": return Calendar.THURSDAY;
            case "friday": return Calendar.FRIDAY;
            case "saturday": return Calendar.SATURDAY;
            default: throw new IllegalArgumentException("Invalid day of the week: " + day);
        }
    }



    // Clear any previous alarms
       // alarmManager.cancel(pendingIntent);
//        Calendar now = Calendar.getInstance();
//        for (String day : reminderDays) {
//            Calendar calendar = Calendar.getInstance(); // Use the default time zone and locale
//            calendar.set(Calendar.HOUR_OF_DAY, 21);
//            calendar.set(Calendar.MINUTE, 17);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MILLISECOND, 0);
//
//            int today = calendar.get(Calendar.DAY_OF_WEEK);
//            int targetDayOfWeek = getDayOfWeek(day);
//
//            // Calculate how much to add to get to the next occurrence of the target day
//            int daysUntilTarget = targetDayOfWeek - today;
////            if (daysUntilTarget <= 0) {
////                daysUntilTarget += 7; // Make sure it's a future date
////            }
//
//            calendar.add(Calendar.DAY_OF_YEAR, daysUntilTarget);
//
//            if (calendar.after(now)) {  // Just in case, double-check it's in the future
//                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
//                Log.d("ScheduleReminder", "Setting alarm for: " + calendar.getTime() + " [" + day + "]");
//            }
//        }

   // }

//    private int getDayOfWeek(String day) {
//        switch (day) {
//            case "Monday": return Calendar.MONDAY;
//            case "Tuesday": return Calendar.TUESDAY;
//            case "Wednesday": return Calendar.WEDNESDAY;
//            case "Thursday": return Calendar.THURSDAY;
//            case "Friday": return Calendar.FRIDAY;
//            case "Saturday": return Calendar.SATURDAY;
//            case "Sunday": return Calendar.SUNDAY;
//            default: return Calendar.MONDAY;
//        }
//    }


    private HashMap<String, Double> poseSuccessRatios = new HashMap<>(); // Track success ratios for all poses



    private void displayPoseStatistics(String poseName) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not logged in.");
            return;
        }

        // Construct the path for the whole month
        String currentYear = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
        String currentMonth = new SimpleDateFormat("MM", Locale.US).format(new Date());
        String posePath = "PoseResult/" + poseName + "/" + currentYear + "/" + currentMonth;

        DatabaseReference poseRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUser.getUid())
                .child(posePath);

        Log.d("FirebasePath", "Path used: " + posePath);

        poseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot monthSnapshot) {
                if (!monthSnapshot.exists()) {
                    Log.d("FirebaseData", "No data exists at this path!");
                    Toast.makeText(getContext(), "No data available for " + poseName, Toast.LENGTH_SHORT).show();
                    return;
                }

                int successCount = 0;
                int totalCount = 0;
                HashMap<String, Integer> failedMessageCounts = new HashMap<>();

                for (DataSnapshot daySnapshot : monthSnapshot.getChildren()) { // Iterate over each day in the month
                    for (DataSnapshot timeSnapshot : daySnapshot.getChildren()) { // Iterate over each time entry for the day
                        totalCount++;
                        Boolean result = timeSnapshot.child("result").getValue(Boolean.class);
                        if (Boolean.TRUE.equals(result)) {
                            successCount++;
                        } else {
                            DataSnapshot messagesSnapshot = timeSnapshot.child("failedMessages");
                            if (messagesSnapshot.exists()) {
                                for (DataSnapshot msgSnapshot : messagesSnapshot.getChildren()) {
                                    String msg = msgSnapshot.getValue(String.class);
                                    failedMessageCounts.put(msg, failedMessageCounts.getOrDefault(msg, 0) + 1);
                                }
                            }
                        }
                    }
                }

                double successRatio = totalCount > 0 ? (double) successCount / totalCount : 0;
                poseSuccessRatios.put(poseName, successRatio);
                Log.d("StatsCalculation", "Total: " + totalCount + ", Successes: " + successCount + ", Ratio: " + successRatio);

                double selectedPoseSuccessRatio = poseSuccessRatios.get(poseName);
                showStatistics(poseName, selectedPoseSuccessRatio, failedMessageCounts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error", databaseError.toException());
            }
        });
    }


    private void showStatistics(String poseName, double selectedPoseSuccessRatio, HashMap<String, Integer> failedMessageCounts) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pose_statistics, null);

        TextView tvPoseName = dialogView.findViewById(R.id.tvPoseName);
        TextView tvSuccessRate = dialogView.findViewById(R.id.tvSuccessRate);
        TextView tvFailedTips = dialogView.findViewById(R.id.tvFailedTips);
        TextView tvComparativeRates = dialogView.findViewById(R.id.tvComparativeRates);
        Button btnDismiss = dialogView.findViewById(R.id.btnDismiss);

        tvPoseName.setText(String.format(Locale.US, "Statistics for %s", poseName));
        tvSuccessRate.setText(String.format(Locale.US, "Success Rate: %.2f%%", selectedPoseSuccessRatio * 100));

        StringBuilder failedTipsMessage = new StringBuilder("Failed Tips:\n");
        if (failedMessageCounts != null && !failedMessageCounts.isEmpty()) {
            for (Map.Entry<String, Integer> entry : failedMessageCounts.entrySet()) {
                failedTipsMessage.append(String.format(Locale.US, "%s: %d times\n", entry.getKey(), entry.getValue()));
            }
        } else {
            failedTipsMessage.append("No failed tips recorded.");
        }
        tvFailedTips.setText(failedTipsMessage.toString());

//        StringBuilder comparativeRatesMessage = new StringBuilder("Comparative Success Rates:\n");
//        for (Map.Entry<String, Double> entry : poseSuccessRatios.entrySet()) {
//            if (!entry.getKey().equals(poseName)) {
//                comparativeRatesMessage.append(String.format(Locale.US, "%s: %.2f%%\n", entry.getKey(), entry.getValue() * 100));
//            }
//        }
//        tvComparativeRates.setText(comparativeRatesMessage.toString());

        AlertDialog dialog = builder.setView(dialogView).create();  // Create the dialog here to use it inside the listener

        btnDismiss.setOnClickListener(v -> dialog.dismiss());  // Use the dialog reference to dismiss

        dialog.show();
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            fetchUserGoal();
            fetchWeeklyPoseResults();
            //fetchAllPosesStatistics();

        }
        fetchPoseData();
    }
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


    private void displayResultsInPieChart(int trueCount, int falseCount) {
        if (isAdded()) {
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(trueCount, "Success"));
            entries.add(new PieEntry(falseCount, "Failed"));

            PieDataSet set = new PieDataSet(entries, "");
            if (getContext() != null) {
                set.setColors(new int[]{R.color.green, R.color.red}, getContext());
                set.setSliceSpace(3f);
                set.setSelectionShift(5f);
            }

            PieData data = new PieData(set);
            data.setValueTextSize(12f);
            data.setValueTextColor(Color.WHITE);

            pieChart.setData(data);
            pieChart.setDescription(new Description());
            Description desc = new Description();
            desc.setText("Monthly Results");
            desc.setTextSize(12f);
            desc.setTextColor(Color.BLACK);
            pieChart.setDescription(desc);

            pieChart.invalidate();
        }
    }



    private void fetchPoseResults(String poseName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not logged in.");
            return;
        }

        String currentYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        String currentMonth = new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("PoseResult")
                .child(poseName);

        dbRef.child(currentYear).child(currentMonth).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot monthSnapshot) {
                int trueCount = 0;
                int falseCount = 0;

                // Iterate over all days in the month
                for (DataSnapshot daySnapshot : monthSnapshot.getChildren()) {
                    for (DataSnapshot entrySnapshot : daySnapshot.getChildren()) {
                        Boolean result = entrySnapshot.child("result").getValue(Boolean.class);
                        if (Boolean.TRUE.equals(result)) {
                            trueCount++;
                        } else if (Boolean.FALSE.equals(result)) {
                            falseCount++;
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

        private void fetchWeeklyPoseResults () {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.e(TAG, "User not logged in.");
                return;
            }

            DatabaseReference posesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("PoseResult");

            posesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, WeeklyPoseData> weeklyResults = new HashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (DataSnapshot poseSnapshot : dataSnapshot.getChildren()) { // Loop over each pose
                        for (DataSnapshot yearSnapshot : poseSnapshot.getChildren()) {
                            for (DataSnapshot monthSnapshot : yearSnapshot.getChildren()) {
                                for (DataSnapshot daySnapshot : monthSnapshot.getChildren()) {
                                    String dateStr = yearSnapshot.getKey() + "-" + monthSnapshot.getKey() + "-" + daySnapshot.getKey();
                                    Date date;
                                    try {
                                        date = sdf.parse(dateStr);
                                    } catch (ParseException e) {
                                        Log.e(TAG, "Date parsing error", e);
                                        continue;
                                    }
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                                    String weekStart = sdf.format(cal.getTime());

                                    WeeklyPoseData data = weeklyResults.getOrDefault(weekStart, new WeeklyPoseData());

                                    for (DataSnapshot resultSnapshot : daySnapshot.getChildren()) {
                                        Boolean result = resultSnapshot.child("result").getValue(Boolean.class);
                                        Long duration = resultSnapshot.child("durationMillis").getValue(Long.class);

                                        if (duration == null) {
                                            //Handle case where duration is null
                                            Log.e(TAG, "Duration is null for " + resultSnapshot.getKey());
                                            continue;
                                        }

                                        //Defaulting duration to 0 if null
                                        long durationValue = (duration != null) ? duration : 0L;

                                        //Checking result in a null-safe manner
                                        if (Boolean.TRUE.equals(result)) {
                                            data.incrementSuccessCount();
                                            data.addDuration(durationValue);
                                        } else {
                                            data.incrementFailureCount();
                                            data.addDuration(durationValue);
                                        }
                                    }
                                    weeklyResults.put(weekStart, data);
                                }
                            }
                        }
                    }
                    displayWeeklyResults(weeklyResults);
                    saveWeeklyResults(weeklyResults);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                }
            });
        }
    private void saveWeeklyResults(HashMap<String, WeeklyPoseData> weeklyResults) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        for (Map.Entry<String, WeeklyPoseData> entry : weeklyResults.entrySet()) {
            String weekStart = entry.getKey();
            WeeklyPoseData data = entry.getValue();

            DatabaseReference weeklyTotalRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("weeklyTotals")
                    .child(weekStart);

            Map<String, Object> weeklyData = new HashMap<>();
            long totalDurationMillis = data.getTotalDuration();
            Map<String, Long> time = convertMillisToHoursMinutes(totalDurationMillis);
            weeklyData.put("totalHours", time.get("hours"));
            weeklyData.put("totalMinutes", time.get("minutes"));
            weeklyData.put("totalSuccesses", data.getSuccessCount());
            weeklyData.put("totalFailures", data.getFailureCount());

            weeklyTotalRef.setValue(weeklyData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Weekly total saved successfully for " + weekStart))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save weekly total for " + weekStart, e));
        }
    }


    private Map<String, Long> convertMillisToHoursMinutes(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long hours = minutes / 60;
        minutes = minutes % 60;

        Map<String, Long> time = new HashMap<>();
        time.put("hours", hours);
        time.put("minutes", minutes);
        return time;
    }


    private Handler handler = new Handler(Looper.getMainLooper());
    public void displayWeeklyResults(HashMap<String, WeeklyPoseData> weeklyResults) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String currentWeekStart = sdf.format(cal.getTime());

        WeeklyPoseData currentWeekData = weeklyResults.getOrDefault(currentWeekStart, new WeeklyPoseData());
        long totalDurationMillis = currentWeekData.getTotalDuration();
        int totalDurationMinutes = (int) (totalDurationMillis / 60000);  //convert milliseconds to minutes

        handler.post(() -> updateProgressBarAndText(totalDurationMinutes, totalGoalMinutes));
    }

    private void updateProgressBarAndText(int totalDurationMinutes, int goalMinutes) {
        View view = getView();
        if (view == null) {
            Log.e(TAG, "View is not available.");
            return;
        }
        ProgressBar progressBar = view.findViewById(R.id.goalProgressBar);
        TextView currentProgressText = view.findViewById(R.id.currentProgressText);

        int progressPercentage = (int) ((totalDurationMinutes / (float) goalMinutes) * 100);
        progressBar.setProgress(Math.min(progressPercentage, 100));  // Ensure not exceeding 100%

        // Calculate hours and minutes from totalDurationMinutes
        int hours = totalDurationMinutes / 60;
        int minutes = totalDurationMinutes % 60;

        String timeSpent;
        if (hours > 0) {
            // Format time spent as hours and minutes if hours are present
            timeSpent = String.format(Locale.getDefault(), "%d hr %02d min", hours, minutes);
        } else {
            // Format time spent as minutes only if less than an hour
            timeSpent = String.format(Locale.getDefault(), "%d min", minutes);
        }

        currentProgressText.setText(String.format(Locale.getDefault(), "Current Progress: %d%% (%s)", progressPercentage, timeSpent));
    }

    private void fetchUserGoal() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference goalRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid()).child("goals");

            goalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        int hours = dataSnapshot.child("goalTimeHour").getValue(Integer.class);
                        int minutes = dataSnapshot.child("goalTimeMinute").getValue(Integer.class);
                        totalGoalMinutes = hours * 60 + minutes;  //convert goal to total minutes
                        updateGoalText(totalGoalMinutes);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to read goal data.", databaseError.toException());
                }
            });
        }
    }

    private void updateGoalText(int totalMinutes) {
        TextView goalTimeText = getView().findViewById(R.id.goalTimeText);
        goalTimeText.setText("Goal Time: " + totalMinutes + " mins");
    }
    private int totalGoalMinutes = 0;
    @Override
    public void onResume() {
        super.onResume();
        fetchPoseData();
        fetchUserGoal();
        fetchWeeklyPoseResults();
    }

    @Override
    public void onPause() {
        super.onPause();


    }


}