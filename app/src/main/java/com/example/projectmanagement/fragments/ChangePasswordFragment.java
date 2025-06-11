package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.projectmanagement.R;
import com.example.projectmanagement.databinding.FragmentChangePasswordBinding;
import com.example.projectmanagement.viewmodels.UserViewModel;
import com.google.android.material.snackbar.Snackbar;

public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;
    private UserViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupListeners();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonChangePassword.setEnabled(!isLoading);
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
            }
        });
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> navigateBack());

        binding.buttonChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        // Get input values
        String currentPassword = binding.editTextCurrentPassword.getText().toString().trim();
        String newPassword = binding.editTextNewPassword.getText().toString().trim();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString().trim();

        // Clear previous errors
        binding.tilCurrentPassword.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        // Validate input
        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.setError("Current password is required");
            return;
        }

        if (newPassword.isEmpty()) {
            binding.tilNewPassword.setError("New password is required");
            return;
        }

        if (!viewModel.validatePassword(newPassword)) {
            binding.tilNewPassword.setError("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character");
            return;
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError("Please confirm your new password");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Change password
//        viewModel.changePassword(currentPassword, newPassword).observe(getViewLifecycleOwner(), success -> {
//            if (Boolean.TRUE.equals(success)) {
//                showSuccess("Password changed successfully");
//                // Clear input fields
//                binding.editTextCurrentPassword.setText("");
//                binding.editTextNewPassword.setText("");
//                binding.editTextConfirmPassword.setText("");
//                // Navigate back
//                navigateBack();
//            }
//        });
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    private void navigateBack() {
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 