package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.databinding.FragmentTaskDetailBinding;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.models.SubTask;
import com.example.projectmanagement.adapters.SubTaskAdapter;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskDetailFragment extends Fragment implements SubTaskAdapter.OnSubTaskCheckedChangeListener {
    private FragmentTaskDetailBinding binding;
    private Task currentTask;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SubTaskAdapter subTaskAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get task data from arguments
        if (getArguments() != null) {
            String taskJson = getArguments().getString("task");
            if (taskJson != null) {
                currentTask = new Gson().fromJson(taskJson, Task.class);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        displayTaskDetails();
        setupSubtasksRecyclerView();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    private void displayTaskDetails() {
        if (currentTask != null) {
            binding.tvTaskDetailTitle.setText(currentTask.getTitle());
            binding.tvTaskDetailDescription.setText(currentTask.getDescription() != null && !currentTask.getDescription().isEmpty() ? currentTask.getDescription() : "N/A");
            binding.tvTaskDetailStatus.setText(currentTask.getStatus());
            binding.tvTaskDetailPriority.setText(currentTask.getPriority());

            if (currentTask.getTags() != null && !currentTask.getTags().isEmpty()) {
                binding.tvTaskDetailTags.setText(currentTask.getTags());
                binding.tvTaskDetailTags.setVisibility(View.VISIBLE);
                binding.tvTaskDetailTagsLabel.setVisibility(View.VISIBLE);
            } else {
                binding.tvTaskDetailTags.setVisibility(View.GONE);
                binding.tvTaskDetailTagsLabel.setVisibility(View.GONE);
            }

            String startDate = currentTask.getStartDate() != null ? dateFormat.format(currentTask.getStartDate()) : "N/A";
            String dueDate = currentTask.getDueDate() != null ? dateFormat.format(currentTask.getDueDate()) : "N/A";
            binding.tvTaskDetailDates.setText(String.format("%s - %s", startDate, dueDate));
        }
    }

    private void setupSubtasksRecyclerView() {
        if (currentTask != null && currentTask.getSub_tasks() != null) {
            // Ensure all sub-tasks are unchecked by default
            for (SubTask subTask : currentTask.getSub_tasks()) {
                subTask.setIsChecked(false);
            }
            subTaskAdapter = new SubTaskAdapter(currentTask.getSub_tasks(), this);
            binding.rvSubtasks.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvSubtasks.setAdapter(subTaskAdapter);
        } else {
            binding.tvTaskDetailSubtasksLabel.setVisibility(View.GONE);
            binding.rvSubtasks.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSubTaskCheckedChanged(SubTask subTask, boolean isChecked) {
        // TODO: Implement API call to update sub-task status
        // For now, just log the change
        // Log.d("TaskDetailFragment", "SubTask: " + subTask.getSub_title() + " checked: " + isChecked);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 