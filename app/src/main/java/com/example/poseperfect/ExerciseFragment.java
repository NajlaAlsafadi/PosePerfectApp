package com.example.poseperfect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class ExerciseFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;
    private List<String> exercises = Arrays.asList("plank", "scissor", "tree");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        exerciseAdapter = new ExerciseAdapter(exercises, new ExerciseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                Intent intent = new Intent(getActivity(), ExerciseActivity.class);
                intent.putExtra("EXERCISE_NAME", item);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(exerciseAdapter);

        return view;
    }
}