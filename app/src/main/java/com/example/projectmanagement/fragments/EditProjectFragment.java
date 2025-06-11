package com.example.projectmanagement.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.projectmanagement.R;
import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.databinding.FragmentEditProjectBinding;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.User;
import com.example.projectmanagement.utils.SharedPreferencesManager;
import com.example.projectmanagement.viewmodels.ProjectViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditProjectFragment extends Fragment {
    private FragmentEditProjectBinding binding;
    private ProjectViewModel projectViewModel;
    private SharedPreferencesManager prefsManager;
    private Project currentProject;
    private Date startDate;
    private Date endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
        prefsManager = new SharedPreferencesManager(requireContext());

        // Get project data from arguments
        String projectJson = getArguments().getString("project");
        if (projectJson != null) {
            currentProject = new Gson().fromJson(projectJson, Project.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProjectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupDatePickers();
        setupClickListeners();
        populateProjectData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog();
                return true;
            }
            return false;
        });
    }

    private void setupDatePickers() {
        MaterialDatePicker<Long> startDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Start Date")
                .build();

        MaterialDatePicker<Long> endDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select End Date")
                .build();

        binding.etStartDate.setOnClickListener(v -> {
            startDatePicker.show(getChildFragmentManager(), "START_DATE_PICKER");
        });

        binding.etEndDate.setOnClickListener(v -> {
            endDatePicker.show(getChildFragmentManager(), "END_DATE_PICKER");
        });

        startDatePicker.addOnPositiveButtonClickListener(selection -> {
            startDate = new Date(selection);
            binding.etStartDate.setText(dateFormat.format(startDate));
            // If end date is before start date, update it
            if (endDate != null && endDate.before(startDate)) {
                endDate = startDate;
                binding.etEndDate.setText(dateFormat.format(endDate));
            }
        });

        endDatePicker.addOnPositiveButtonClickListener(selection -> {
            Date selectedDate = new Date(selection);
            if (startDate != null && selectedDate.before(startDate)) {
                Toast.makeText(requireContext(), "End date cannot be before start date", Toast.LENGTH_SHORT).show();
                return;
            }
            endDate = selectedDate;
            binding.etEndDate.setText(dateFormat.format(endDate));
        });
    }

    private void setupClickListeners() {
        binding.btnUpdate.setOnClickListener(v -> updateProject());
    }

    private void populateProjectData() {
        if (currentProject != null) {
            binding.etProjectName.setText(currentProject.getName());
            binding.etDescription.setText(currentProject.getDescription());

            startDate = currentProject.getStartDate();
            endDate = currentProject.getEndDate();
            binding.etStartDate.setText(dateFormat.format(startDate));
            binding.etEndDate.setText(dateFormat.format(endDate));
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete this project? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProject())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProject() {
        binding.progressBar.setVisibility(View.VISIBLE);
        projectViewModel.deleteProject(currentProject.getId()).observe(getViewLifecycleOwner(), success -> {
            binding.progressBar.setVisibility(View.GONE);
            if (success) {
                Toast.makeText(requireContext(), "Project deleted successfully", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(EditProjectFragment.this)
                        .navigate(R.id.action_editProjectFragment_to_projectListFragment);
            } else {
                Toast.makeText(requireContext(), "Failed to delete project", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProject() {
        String name = binding.etProjectName.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etProjectName.setError("Project name is required");
            return;
        }

        if (startDate == null || endDate == null) {
            Toast.makeText(requireContext(), "Please select both start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        Project updatedProject = new Project();
        updatedProject.setId(currentProject.getId());
        updatedProject.setName(name);
        updatedProject.setDescription(description);
        updatedProject.setStartDate(startDate);
        updatedProject.setEndDate(endDate);

        binding.progressBar.setVisibility(View.VISIBLE);
        projectViewModel.updateProject(updatedProject).observe(getViewLifecycleOwner(), project -> {
            binding.progressBar.setVisibility(View.GONE);
            if (project != null) {
                Toast.makeText(requireContext(), "Project updated successfully", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(EditProjectFragment.this)
                        .navigate(R.id.action_editProjectFragment_to_projectListFragment);
            } else {
                Toast.makeText(requireContext(), "Failed to update project", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 