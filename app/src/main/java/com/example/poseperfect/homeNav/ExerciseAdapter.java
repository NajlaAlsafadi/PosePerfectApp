package com.example.poseperfect.homeNav;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poseperfect.R;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exerciseList;
    private List<Exercise> allExercises;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Exercise item);
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(Exercise item);
    }

    public ExerciseAdapter(List<Exercise> exerciseList, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.exerciseList = exerciseList;
        this.allExercises = new ArrayList<>(exerciseList);
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    public void filter(String level) {
        if (level.equals("All")) {
            exerciseList = new ArrayList<>(allExercises);
        } else {
            exerciseList = new ArrayList<>();
            for (Exercise exercise : allExercises) {
                if (exercise.getLevel().equals(level)) {
                    exerciseList.add(exercise);
                }
            }
        }
        notifyDataSetChanged();
    }
    @NonNull
    @Override// viewholder for individual exercise items
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view, listener, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        holder.bind(exerciseList.get(position));
    }

    @Override // get count of exercise items
    public int getItemCount() {
        return exerciseList.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView categoryTextView;
        private TextView difficultyTextView;
        private ImageView imageView;
        private OnItemClickListener listener;
        private OnItemLongClickListener longClickListener;

        public ExerciseViewHolder(@NonNull View itemView, OnItemClickListener listener, OnItemLongClickListener longClickListener){
            super(itemView);
            nameTextView = itemView.findViewById(R.id.exercise_name);
            categoryTextView = itemView.findViewById(R.id.exercise_category);
            difficultyTextView = itemView.findViewById(R.id.exercise_difficulty);
            imageView = itemView.findViewById(R.id.exercise_image);
            this.listener = listener;
            this.longClickListener = longClickListener;
        }
        // bind data to the views in the layout
        public void bind(final Exercise exercise) {
            nameTextView.setText(exercise.getName());
            categoryTextView.setText("Category: " + exercise.getCategory());
            difficultyTextView.setText("Difficulty: " + exercise.getLevel());
            imageView.setImageResource(exercise.getImageResId());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(exercise);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longClickListener.onItemLongClick(exercise);
                    return true;
                }
            });
        }
    }
}