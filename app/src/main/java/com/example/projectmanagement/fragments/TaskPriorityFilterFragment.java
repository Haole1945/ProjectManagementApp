package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.adapters.TaskAdapter;
import com.example.projectmanagement.databinding.FragmentTaskPriorityFilterBinding;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.viewmodels.TaskListViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaskPriorityFilterFragment extends Fragment implements TaskAdapter.OnTaskDragListener, TaskAdapter.OnTaskClickListener {

    private FragmentTaskPriorityFilterBinding binding;
    private String selectedPriority = "All"; // Default to "All Priorities"
    private TaskAdapter taskAdapter;
    private TaskListViewModel taskListViewModel;
    private List<Task> allTasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaskPriorityFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TaskPriorityFilterFragment", "onViewCreated called.");

        setupToolbar();
        setupRecyclerView();
        taskListViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(TaskListViewModel.class);

        taskListViewModel.getAllCombinedTasks().observe(getViewLifecycleOwner(), combinedTasks -> {
            if (combinedTasks != null) {
                allTasks = combinedTasks; // Update allTasks with the combined list
                Log.d("TaskPriorityFilterFragment", "All combined tasks updated. Size: " + allTasks.size());
                applyFilter(); // Apply the current filter whenever combined tasks are updated
            }
        });

        // The loadAllTasks() call is handled by the ViewModel observing projects now.
        // We don't need to explicitly call it here.
        // Log.d("TaskPriorityFilterFragment", "Loading all tasks.");
        // taskListViewModel.loadAllTasks();

        setupPriorityFilters();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this, this);
        binding.rvFilteredTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFilteredTasks.setAdapter(taskAdapter);
    }

    private void setupPriorityFilters() {
        binding.cgPriorityFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipAllPriorities) {
                    selectedPriority = "All";
                } else if (checkedId == R.id.chipUrgent) {
                    selectedPriority = "Urgent";
                } else if (checkedId == R.id.chipHigh) {
                    selectedPriority = "High";
                } else if (checkedId == R.id.chipMedium) {
                    selectedPriority = "Medium";
                } else if (checkedId == R.id.chipLow) {
                    selectedPriority = "Low";
                } else if (checkedId == R.id.chipBacklog) {
                    selectedPriority = "Backlog";
                }
            } else {
                selectedPriority = "All"; // Default if nothing is selected
            }
            Log.d("TaskPriorityFilterFragment", "Chip selected. New filter: " + selectedPriority);
            applyFilter(); // Apply filter immediately on chip selection
        });

        // Set initial checked state if coming back with a filter
        if (getArguments() != null && getArguments().containsKey("priorityFilter")) {
            String currentFilter = getArguments().getString("priorityFilter");
            if (currentFilter != null) {
                switch (currentFilter) {
                    case "Urgent":
                        binding.chipUrgent.setChecked(true);
                        break;
                    case "High":
                        binding.chipHigh.setChecked(true);
                        break;
                    case "Medium":
                        binding.chipMedium.setChecked(true);
                        break;
                    case "Low":
                        binding.chipLow.setChecked(true);
                        break;
                    case "Backlog":
                        binding.chipBacklog.setChecked(true);
                        break;
                    default:
                        binding.chipAllPriorities.setChecked(true);
                        break;
                }
                selectedPriority = currentFilter; // Update selected priority after setting checked state
                Log.d("TaskPriorityFilterFragment", "Initial filter from arguments: " + selectedPriority);
            }
        } else {
            binding.chipAllPriorities.setChecked(true); // Default to All Priorities if no argument
            Log.d("TaskPriorityFilterFragment", "Defaulting to All Priorities filter.");
        }
    }

    private void applyFilter() {
        List<Task> filteredTasks = new ArrayList<>();
        if (Objects.equals(selectedPriority, "All")) {
            filteredTasks.addAll(allTasks);
        } else {
            for (Task task : allTasks) {
                if (task.getPriority() != null && task.getPriority().equalsIgnoreCase(selectedPriority)) {
                    filteredTasks.add(task);
                }
            }
        }
        Log.d("TaskPriorityFilterFragment", "Applying filter: " + selectedPriority + ". Filtered tasks count: " + filteredTasks.size());
        taskAdapter.setTasks(filteredTasks);
    }

    @Override
    public void onTaskDragged(Task task) {
        // Handle task dragged event if needed
    }

    @Override
    public void onTaskClick(Task task) {
        Bundle args = new Bundle();
        args.putString("task", new Gson().toJson(task));
        NavHostFragment.findNavController(this).navigate(R.id.taskDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 