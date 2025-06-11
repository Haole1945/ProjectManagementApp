package com.example.projectmanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.models.Task;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchTaskAdapter extends RecyclerView.Adapter<SearchTaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private OnTaskClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public SearchTaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvTaskName;
        private final TextView tvTaskStatus;
        private final TextView tvProjectName;
        private final TextView tvTaskDescription;
        private final TextView tvAssignee;
        private final TextView tvDueDate;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvAssignee = itemView.findViewById(R.id.tvAssignee);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);

            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(tasks.get(position));
                }
            });
        }

        void bind(Task task) {
            tvTaskName.setText(task.getTitle());
            tvTaskStatus.setText(task.getStatus());
            tvProjectName.setText(task.getProject() != null ? task.getProject().getName() : "");
            tvTaskDescription.setText(task.getDescription());
            tvAssignee.setText(task.getAssigneeUserId() != null ? task.getAssigneeUserId().getFullname() : "Unassigned");
            tvDueDate.setText(String.format("Due: %s", dateFormat.format(task.getDueDate())));

            // Set status background color
            int statusColor;
            switch (task.getStatus().toLowerCase()) {
                case "completed":
                    statusColor = itemView.getContext().getColor(R.color.colorSuccess);
                    break;
                case "in progress":
                    statusColor = itemView.getContext().getColor(R.color.colorPrimary);
                    break;
                case "not started":
                    statusColor = itemView.getContext().getColor(R.color.colorWarning);
                    break;
                default:
                    statusColor = itemView.getContext().getColor(R.color.colorError);
            }
            tvTaskStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));
        }
    }
} 