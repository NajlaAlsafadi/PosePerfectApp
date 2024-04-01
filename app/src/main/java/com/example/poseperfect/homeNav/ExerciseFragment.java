package com.example.poseperfect.homeNav;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poseperfect.ExerciseActivity;
import com.example.poseperfect.PoseAnalysisCallback;
import com.example.poseperfect.PoseDetectorAnalyzer;
import com.example.poseperfect.R;
import com.example.poseperfect.StaticImagePoseAnalyzer;
import com.example.poseperfect.overlay.PoseOverlayView;
import com.google.android.material.chip.ChipGroup;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ExerciseFragment extends Fragment implements ChipGroup.OnCheckedChangeListener {

    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;
    public static final String POSE_NAME = "pose_name";
    private static final int REQUEST_CHOOSE_IMAGE = 1002;
    private String currentPoseName;
    private List<Exercise> exercises;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        exercises = Arrays.asList(
                new Exercise("T-Pose", getResources().getString(R.string.T_Pose_desc), getResources().getString(R.string.T_Pose_health), R.drawable.t_pose, "Beginner", "Standing", "https://youtu.be/ToXlJxuFLmU?si=vESk5CNaoA4bcWcU"),
                new Exercise("Bridge", getResources().getString(R.string.Bridge_desc), getResources().getString(R.string.Bridge_health), R.drawable.bridge, "Intermediate", "Supine / Backbend", "https://youtu.be/XUcAuYd7VU0?si=dOrUm-HD20ST8FrQ"),
                new Exercise("Boat", getResources().getString(R.string.Boat_desc), getResources().getString(R.string.Boat_health), R.drawable.boat, "Intermediate", "Seated/Balancing", "https://youtu.be/QVEINjrYUPU?si=SfFzigx1JKQwYqAy"),
                new Exercise("Warrior", getResources().getString(R.string.Warrior_desc), getResources().getString(R.string.Warrior_health), R.drawable.warrior, "Beginner", "Standing/Balancing", "https://youtu.be/5rT--p_cLOc?si=gy7D3mzH0dUsvvOj")

        );
        exerciseAdapter = new ExerciseAdapter(exercises, new ExerciseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Exercise item) {

                Intent intent = new Intent(getActivity(), ExerciseActivity.class);
                intent.putExtra(POSE_NAME, item.getName());
                startActivity(intent);
            }
        }, new ExerciseAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Exercise item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                currentPoseName = item.getName();
                builder.setTitle("More Info");
                String[] options = {"YouTube Tutorial", "Description", "Health Benefits", "Analyze Image"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(item.getYoutubeUrl()));
                                startActivity(intent);
                                break;
                            case 1:
                                showScrollableDialog("Description", item.getDescription());
                                break;
                            case 2:
                                showScrollableDialog("Health Benefits", item.getHealthBenefits());
                                break;
                            case 3:
                                startChooseImageIntentForResult();
                                break;
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        recyclerView.setAdapter(exerciseAdapter);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group);
        chipGroup.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == getActivity().RESULT_OK) {
            Uri imageUri = data.getData();
            handleImageResult(imageUri);

        }
    }
    private void startChooseImageIntentForResult() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
    }
    private void handleImageResult(Uri imageUri) {
        StaticImagePoseAnalyzer analyzer = new StaticImagePoseAnalyzer(getContext());
        analyzer.setPoseName(currentPoseName);
        analyzer.analyzeImage(imageUri);

    }




    private void showResultDialog(boolean isPoseCorrect, String feedback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(isPoseCorrect ? "Pose Correct" : "Pose Incorrect");
        builder.setMessage(feedback);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public void onCheckedChanged(ChipGroup group, int checkedId) {
        if (checkedId == R.id.chip_all) {
            exerciseAdapter.filter("All");
        } else if (checkedId == R.id.chip_beginner) {
            exerciseAdapter.filter("Beginner");
        } else if (checkedId == R.id.chip_intermediate) {
            exerciseAdapter.filter("Intermediate");
        }
    }
    private void showScrollableDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);

        final ScrollView scrollView = new ScrollView(getContext());
        final TextView textView = new TextView(getContext());
        textView.setPadding(16, 16, 16, 16);
        textView.setText(content);
        scrollView.addView(textView);
        builder.setView(scrollView);

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
