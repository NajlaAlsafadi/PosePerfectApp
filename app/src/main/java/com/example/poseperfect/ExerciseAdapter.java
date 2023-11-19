package com.example.poseperfect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<String> exerciseList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public ExerciseAdapter(List<String> exerciseList, OnItemClickListener listener) {
        this.exerciseList = exerciseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        holder.bind(exerciseList.get(position));
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private OnItemClickListener listener;

        public ExerciseViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            textView = itemView.findViewById(R.id.exercise_name);
            this.listener = listener;
        }

        public void bind(final String exerciseName) {
            textView.setText(exerciseName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(exerciseName);
                }
            });
        }
    }
}