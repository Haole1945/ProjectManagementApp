package com.example.projectmanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;
import android.os.Bundle;

import com.example.projectmanagement.R;
import com.example.projectmanagement.databinding.ItemProjectBinding;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Task;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private List<Project> projects;
    private OnProjectClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public ProjectAdapter(List<Project> projects, OnProjectClickListener listener) {
        this.projects = projects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProjectBinding binding = ItemProjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(projects.get(position));
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public void updateProjects(List<Project> newProjects) {
        this.projects = newProjects;
        notifyDataSetChanged();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final ItemProjectBinding binding;

        ProjectViewHolder(ItemProjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Project project) {
            binding.getRoot().setOnClickListener(v -> listener.onProjectClick(project));

            binding.tvProjectName.setText(project.getName());
            binding.tvDescription.setText(project.getDescription());

            // Set status chip
            Chip statusChip = binding.chipStatus;
            String status = project.getStatus();
            if (status == null) {
                status = "Unknown"; // Default status if null
            }
            statusChip.setText(status);
            switch (status.toLowerCase()) {
                case "planning":
                    statusChip.setChipBackgroundColorResource(R.color.status_planning);
                    break;
                case "in progress":
                    statusChip.setChipBackgroundColorResource(R.color.status_in_progress);
                    break;
                case "on hold":
                    statusChip.setChipBackgroundColorResource(R.color.status_on_hold);
                    break;
                case "completed":
                    statusChip.setChipBackgroundColorResource(R.color.status_completed);
                    break;
                case "cancelled":
                    statusChip.setChipBackgroundColorResource(R.color.status_cancelled);
                    break;
                default:
                    statusChip.setChipBackgroundColorResource(R.color.status_unknown);
                    break;
            }

            // Set date range
            String dateRange = "";
            if (project.getStartDate() != null && project.getEndDate() != null) {
                dateRange = dateFormat.format(project.getStartDate()) + " - " +
                        dateFormat.format(project.getEndDate());
            } else if (project.getStartDate() != null) {
                dateRange = dateFormat.format(project.getStartDate()) + " - N/A";
            } else if (project.getEndDate() != null) {
                dateRange = "N/A - " + dateFormat.format(project.getEndDate());
            } else {
                dateRange = "N/A";
            }
            binding.tvDateRange.setText(dateRange);

            // Set member count
            int memberCount = 0;
            if (project.getOwner() != null) {
                memberCount++;
            }
            if (project.getMembers() != null) {
                memberCount += project.getMembers().size();
            }
            binding.tvMemberCount.setText(memberCount + " members");
        }
    }
} 