package com.example.poseperfect.homeNav;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.poseperfect.R;
import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private final List<CheckResult> checkResults;
    private final Context context;

    public ResultsAdapter(Context context, List<CheckResult> checkResults) {
        this.context = context;
        this.checkResults = checkResults;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result_card,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CheckResult result = checkResults.get(position);
        holder.title.setText("Check " + (position + 1));
        holder.feedback.setText(result.feedback);
        if (result.passed) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.pass_color));
            holder.icon.setImageResource(R.drawable.ic_check);
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.fail_color));
            holder.icon.setImageResource(R.drawable.ic_cross);
        }
    }

    @Override
    public int getItemCount() {
        return checkResults.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, feedback;
        ImageView icon;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            title = itemView.findViewById(R.id.check_title);
            feedback = itemView.findViewById(R.id.check_feedback);
            icon = itemView.findViewById(R.id.result_icon);
        }
    }
}
