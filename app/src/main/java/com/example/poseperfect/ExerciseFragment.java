package com.example.poseperfect;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poseperfect.homeNav.Exercise;
import com.google.android.material.chip.ChipGroup;

import java.util.Arrays;
import java.util.List;

public class ExerciseFragment extends Fragment implements ChipGroup.OnCheckedChangeListener {

    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;

    private List<Exercise> exercises = Arrays.asList(
            new Exercise("T-Pose", R.drawable.t_pose, "Beginner", "Standing"),
            new Exercise("Bridge", R.drawable.bridge, "Intermediate", "Supine / Backbend"),
            new Exercise("Boat", R.drawable.boat, "Intermediate", "Seated / Balancing")
    );
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        exerciseAdapter = new ExerciseAdapter(exercises, new ExerciseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Exercise item) {
                Intent intent = new Intent(getActivity(), ExerciseActivity.class);
                intent.putExtra("EXERCISE_NAME", item.getName());
                startActivity(intent);
            }
        }, new ExerciseAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Exercise item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("More Info");
                String[] options = {"YouTube Tutorial", "Description", "Health Benefits"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Handle option 1
                                break;
                            case 1:
                                // Handle option 2
                                break;
                            case 2:
                                // Handle option 3
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
    public void onCheckedChanged(ChipGroup group, int checkedId) {
        if (checkedId == R.id.chip_all) {
            exerciseAdapter.filter("All");
        } else if (checkedId == R.id.chip_beginner) {
            exerciseAdapter.filter("Beginner");
        } else if (checkedId == R.id.chip_intermediate) {
            exerciseAdapter.filter("Intermediate");
        }
    }
}
