package com.example.projectmanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.ClipData;
import android.content.ClipDescription;
import android.view.View.DragShadowBuilder;
import android.util.Log;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks = new ArrayList<>();
    private OnTaskDragListener dragListener;
    private OnTaskClickListener clickListener;

    public interface OnTaskDragListener {
        void onTaskDragged(Task task);
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(OnTaskDragListener dragListener, OnTaskClickListener clickListener) {
        this.dragListener = dragListener;
        this.clickListener = clickListener;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTaskClick(task);
            }
        });

        // Set OnLongClickListener for drag functionality
        holder.itemView.setOnLongClickListener(v -> {
            if (dragListener != null) {
                ClipData.Item item = new ClipData.Item(task.getId());
                ClipData dragData = new ClipData(
                    task.getId(), // Label
                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                    item
                );
                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                // Log the task being dragged
                Log.d("TaskAdapter", "Starting drag for task: " + task.getTitle() + " (" + task.getId() + ")");
                v.startDragAndDrop(dragData, shadowBuilder, task, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTaskTitle;
        private final TextView tvTaskStatus;
        private final TextView tvTaskTags;
        private final TextView tvTaskProjectName;
        private final CardView taskCardView;
        private final ImageView ivTaskTagIcon;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCardView = itemView.findViewById(R.id.task_card_view);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvTaskTags = itemView.findViewById(R.id.tvTaskTags);
            tvTaskProjectName = itemView.findViewById(R.id.tvTaskProjectName);
            ivTaskTagIcon = itemView.findViewById(R.id.ivTaskTagIcon);
        }

        public void bind(Task task) {
            tvTaskTitle.setText(task.getTitle());
            tvTaskStatus.setText(task.getStatus());

            if (task.getTags() != null && !task.getTags().isEmpty()) {
                tvTaskTags.setText(task.getTags());
                tvTaskTags.setVisibility(View.VISIBLE);
                ivTaskTagIcon.setVisibility(View.VISIBLE);
            } else {
                tvTaskTags.setVisibility(View.GONE);
                ivTaskTagIcon.setVisibility(View.GONE);
            }

            if (task.getProject() != null && task.getProject().getName() != null) {
                tvTaskProjectName.setText(String.format("Project: %s", task.getProject().getName()));
                tvTaskProjectName.setVisibility(View.VISIBLE);
            } else {
                tvTaskProjectName.setVisibility(View.GONE);
            }

            // Set background color based on priority
            int colorResId;
            if (task.getPriority() != null) {
                switch (task.getPriority()) {
                    case "Urgent":
                        colorResId = R.color.task_priority_urgent;
                        break;
                    case "High":
                        colorResId = R.color.task_priority_high;
                        break;
                    case "Medium":
                        colorResId = R.color.task_priority_medium;
                        break;
                    case "Low":
                        colorResId = R.color.task_priority_low;
                        break;
                    case "Backlog":
                        colorResId = R.color.task_priority_backlog;
                        break;
                    default:
                        colorResId = android.R.color.white; // Default color
                        break;
                }
            } else {
                colorResId = android.R.color.white; // Default color if priority is null
            }
            taskCardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), colorResId));
        }
    }
} 