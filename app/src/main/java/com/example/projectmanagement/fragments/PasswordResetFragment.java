package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.projectmanagement.databinding.FragmentPasswordResetBinding;
import com.example.projectmanagement.viewmodels.UserViewModel;

public class PasswordResetFragment extends Fragment {
    private FragmentPasswordResetBinding binding;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPasswordResetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        binding.btnResetPassword.setOnClickListener(v -> attemptResetPassword());
        binding.tvBackToLogin.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    private void attemptResetPassword() {
        String email = binding.etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        userViewModel.passwordForgot(email).observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Reset email sent! Please check your inbox.", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
            } else {
                Toast.makeText(requireContext(), "Failed to send reset email!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 