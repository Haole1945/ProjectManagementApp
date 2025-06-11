package com.example.projectmanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.models.Project;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchProjectAdapter extends RecyclerView.Adapter<SearchProjectAdapter.ProjectViewHolder> {

    private List<Project> projects = new ArrayList<>();
    private OnProjectClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Map<String, Integer> projectProgressMap = new HashMap<>();

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public SearchProjectAdapter(OnProjectClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public void updateProjects(List<Project> newProjects) {
        this.projects = newProjects;
        notifyDataSetChanged();
    }

    public void setProjectProgressMap(Map<String, Integer> progressMap) {
        this.projectProgressMap = progressMap;
        notifyDataSetChanged();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvProjectName;
        private final TextView tvProjectStatus;
        private final TextView tvProjectDescription;
        private final TextView tvProjectProgress;
        private final TextView tvProjectDueDate;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectStatus = itemView.findViewById(R.id.tvProjectStatus);
            tvProjectDescription = itemView.findViewById(R.id.tvProjectDescription);
            tvProjectProgress = itemView.findViewById(R.id.tvProjectProgress);
            tvProjectDueDate = itemView.findViewById(R.id.tvProjectDueDate);

            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onProjectClick(projects.get(position));
                }
            });
        }

        void bind(Project project) {
            tvProjectName.setText(project.getName());
            tvProjectStatus.setText(project.getStatus());
            tvProjectDescription.setText(project.getDescription());
            int progress = projectProgressMap.getOrDefault(project.getId(), 0);
            tvProjectProgress.setText(String.format(Locale.getDefault(), "Progress: %d%%", progress));
            tvProjectDueDate.setText(String.format("Due: %s", dateFormat.format(project.getEndDate())));

            // Set status background color
            int statusColor;
            switch (project.getStatus().toLowerCase()) {
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
            tvProjectStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));
        }
    }
} 