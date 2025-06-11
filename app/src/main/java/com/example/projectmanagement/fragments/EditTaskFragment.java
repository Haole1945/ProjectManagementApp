package com.example.projectmanagement.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.projectmanagement.R;
import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.databinding.FragmentEditTaskBinding;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.models.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTaskFragment extends Fragment {
    private FragmentEditTaskBinding binding;
    private ApiService apiService;
    private Task currentTask;
    private Project currentProject;
    private Date dueDate;
    private User selectedAssignee;
    private List<User> availableUsers = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiService.getInstance(requireContext());

        // Get task and project data from arguments
        String taskJson = getArguments().getString("task");
        String projectJson = getArguments().getString("project");
        if (taskJson != null) {
            currentTask = new Gson().fromJson(taskJson, Task.class);
        }
        if (projectJson != null) {
            currentProject = new Gson().fromJson(projectJson, Project.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupDropdowns();
        setupDatePicker();
        setupClickListeners();
        loadAvailableUsers();
        loadTaskData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    private void setupDropdowns() {
        // Setup priority dropdown
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_dropdown, priorities);
        ((AutoCompleteTextView) binding.tilPriority.getEditText())
                .setAdapter(priorityAdapter);

        // Setup status dropdown
        String[] statuses = {"To Do", "In Progress", "Done"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_dropdown, statuses);
        ((AutoCompleteTextView) binding.tilStatus.getEditText())
                .setAdapter(statusAdapter);
    }

    private void setupDatePicker() {
        binding.tilDueDate.setEndIconOnClickListener(v -> showDatePicker());
        binding.etDueDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (dueDate != null) {
            calendar.setTime(dueDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    dueDate = calendar.getTime();
                    binding.etDueDate.setText(dateFormat.format(dueDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupClickListeners() {
        binding.btnSelectAssignee.setOnClickListener(v -> showAssigneeSelectionDialog());
        binding.btnUpdateTask.setOnClickListener(v -> updateTask());
    }

    private void loadAvailableUsers() {
        if (currentProject == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        apiService.getProject(currentProject.getId()).enqueue(new Callback<Project>() {
            @Override
            public void onResponse(@NonNull Call<Project> call, @NonNull Response<Project> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Project project = response.body();
                    availableUsers.clear();
                    if (project.getOwner() != null) {
                        availableUsers.add(project.getOwner());
                    }
                    if (project.getMembers() != null) {
                        availableUsers.addAll(project.getMembers());
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load team members", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Project> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTaskData() {
        if (currentTask == null) return;

        binding.etTitle.setText(currentTask.getTitle());
        binding.etDescription.setText(currentTask.getDescription());
        binding.actvPriority.setText(currentTask.getPriority(), false);
        binding.actvStatus.setText(currentTask.getStatus(), false);

        if (currentTask.getDueDate() != null) {
            dueDate = currentTask.getDueDate();
            binding.etDueDate.setText(dateFormat.format(dueDate));
        }

        if (currentTask.getAssigneeUserId() != null) {
            selectedAssignee = currentTask.getAssigneeUserId();
            updateAssigneeChip();
        }
    }

    private void showAssigneeSelectionDialog() {
        if (availableUsers.isEmpty()) {
            Toast.makeText(requireContext(), "No team members available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] userNames = new String[availableUsers.size()];
        for (int i = 0; i < availableUsers.size(); i++) {
            userNames[i] = availableUsers.get(i).getFullname();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Assignee")
                .setItems(userNames, (dialog, which) -> {
                    selectedAssignee = availableUsers.get(which);
                    updateAssigneeChip();
                })
                .show();
    }

    private void updateAssigneeChip() {
        binding.chipGroupAssignee.removeAllViews();
        if (selectedAssignee != null) {
            Chip chip = new Chip(requireContext());
            chip.setText(selectedAssignee.getFullname());
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedAssignee = null;
                binding.chipGroupAssignee.removeAllViews();
            });
            binding.chipGroupAssignee.addView(chip);
        }
    }

    private void updateTask() {
        // Validate input
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String priority = binding.actvPriority.getText().toString().trim();
        String status = binding.actvStatus.getText().toString().trim();

        if (title.isEmpty()) {
            binding.tilTitle.setError("Title is required");
            return;
        }
        binding.tilTitle.setError(null);

        if (description.isEmpty()) {
            binding.tilDescription.setError("Description is required");
            return;
        }
        binding.tilDescription.setError(null);

        if (dueDate == null) {
            binding.tilDueDate.setError("Due date is required");
            return;
        }
        binding.tilDueDate.setError(null);

        if (priority.isEmpty()) {
            binding.tilPriority.setError("Priority is required");
            return;
        }
        binding.tilPriority.setError(null);

        if (status.isEmpty()) {
            binding.tilStatus.setError("Status is required");
            return;
        }
        binding.tilStatus.setError(null);

        if (selectedAssignee == null) {
            Toast.makeText(requireContext(), "Please select an assignee", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update task object
        currentTask.setTitle(title);
        currentTask.setDescription(description);
        currentTask.setDueDate(dueDate);
        currentTask.setPriority(priority);
        currentTask.setStatus(status);
        currentTask.setAssigneeUserId(selectedAssignee);

        // Call API to update task
        binding.progressBar.setVisibility(View.VISIBLE);
        apiService.updateTask(currentTask.getId(), currentTask).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(@NonNull Call<Task> call, @NonNull Response<Task> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(EditTaskFragment.this).navigateUp();
                } else {
                    Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Task> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 