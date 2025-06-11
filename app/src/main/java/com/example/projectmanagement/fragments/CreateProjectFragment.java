package com.example.projectmanagement.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.projectmanagement.R;
import com.example.projectmanagement.api.ApiClient;
import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.databinding.FragmentCreateProjectBinding;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.User;
import com.example.projectmanagement.utils.SharedPreferencesManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateProjectFragment extends Fragment {
    private FragmentCreateProjectBinding binding;
    private ApiService apiService;
    private SharedPreferencesManager prefsManager;
    private Calendar startDate;
    private Calendar endDate;
    private List<User> selectedMembers = new ArrayList<>();
    private List<User> availableUsers = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        prefsManager = new SharedPreferencesManager(requireContext());
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1); // Default end date is 1 month from now
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateProjectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupDropdowns();
        setupDatePickers();
        setupClickListeners();
        loadAvailableUsers();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigateUp());
    }

    private void setupDropdowns() {
        // Setup status dropdown
        String[] statuses = {"Planning", "In Progress", "On Hold", "Completed", "Cancelled"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            statuses
        );
        binding.actvStatus.setAdapter(statusAdapter);
    }

    private void setupDatePickers() {
        binding.etStartDate.setOnClickListener(v -> showStartDatePicker());
        binding.etEndDate.setOnClickListener(v -> showEndDatePicker());
        
        // Set initial dates
        binding.etStartDate.setText(dateFormat.format(startDate.getTime()));
        binding.etEndDate.setText(dateFormat.format(endDate.getTime()));
    }

    private void showStartDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                startDate.set(year, month, dayOfMonth);
                binding.etStartDate.setText(dateFormat.format(startDate.getTime()));
                
                // If end date is before start date, update it
                if (endDate.before(startDate)) {
                    endDate.set(year, month, dayOfMonth);
                    endDate.add(Calendar.MONTH, 1);
                    binding.etEndDate.setText(dateFormat.format(endDate.getTime()));
                }
            },
            startDate.get(Calendar.YEAR),
            startDate.get(Calendar.MONTH),
            startDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                
                if (selectedDate.before(startDate)) {
                    Toast.makeText(requireContext(), 
                        "End date cannot be before start date", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                
                endDate = selectedDate;
                binding.etEndDate.setText(dateFormat.format(endDate.getTime()));
            },
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void setupClickListeners() {
        binding.btnAddMember.setOnClickListener(v -> showMemberSelectionDialog());
        binding.btnCreate.setOnClickListener(v -> createProject());
    }

    private void loadAvailableUsers() {
        String token = prefsManager.getAccessToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        apiService.getAllUsers("Bearer " + token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    availableUsers.clear();
                    availableUsers.addAll(response.body());
                } else {
                    Toast.makeText(requireContext(),
                        "Failed to load users: " + response.message(),
                        Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                    "Error: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMemberSelectionDialog() {
        if (availableUsers.isEmpty()) {
            Toast.makeText(requireContext(), "No users available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Filter out already selected members
        List<User> availableToAdd = new ArrayList<>(availableUsers);
        availableToAdd.removeAll(selectedMembers);

        if (availableToAdd.isEmpty()) {
            Toast.makeText(requireContext(), "All users are already added", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] userNames = availableToAdd.stream()
            .map(User::getFullname)
            .toArray(String[]::new);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Team Member")
            .setItems(userNames, (dialog, which) -> {
                User selectedUser = availableToAdd.get(which);
                selectedMembers.add(selectedUser);
                addMemberChip(selectedUser);
            })
            .show();
    }

    private void addMemberChip(User user) {
        Chip chip = new Chip(requireContext());
        chip.setText(user.getFullname());
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            selectedMembers.remove(user);
            binding.chipGroupMembers.removeView(chip);
        });
        binding.chipGroupMembers.addView(chip);
    }

    private void createProject() {
        // Validate input
        String name = binding.etProjectName.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String status = binding.actvStatus.getText().toString().trim();

        if (name.isEmpty()) {
            binding.tilProjectName.setError("Project name is required");
            return;
        }
        binding.tilProjectName.setError(null);

        if (description.isEmpty()) {
            binding.tilDescription.setError("Description is required");
            return;
        }
        binding.tilDescription.setError(null);

        if (binding.etStartDate.getText().toString().isEmpty()) {
            binding.tilStartDate.setError("Start date is required");
            return;
        }
        binding.tilStartDate.setError(null);

        if (binding.etEndDate.getText().toString().isEmpty()) {
            binding.tilEndDate.setError("End date is required");
            return;
        }
        binding.tilEndDate.setError(null);

        if (status.isEmpty()) {
            binding.tilStatus.setError("Status is required");
            return;
        }
        binding.tilStatus.setError(null);

        if (selectedMembers.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one team member", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create project object
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setStartDate(startDate.getTime());
        project.setEndDate(endDate.getTime());
        project.setStatus(status);
        project.setMembers(selectedMembers);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCreate.setEnabled(false);

        apiService.createProject(project).enqueue(new Callback<Project>() {
            @Override
            public void onResponse(@NonNull Call<Project> call, @NonNull Response<Project> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCreate.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Project created successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    String errorMessage = "Failed to create project";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ": " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage += ": " + response.message();
                        }
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Project> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCreate.setEnabled(true);
                Toast.makeText(requireContext(),
                    "Error: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 