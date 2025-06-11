package com.example.projectmanagement.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.projectmanagement.R;
import com.example.projectmanagement.databinding.FragmentSettingsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = requireContext().getSharedPreferences("app_settings", 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        loadSettings();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void loadSettings() {
        // Load notification settings
        binding.switchEmailNotifications.setChecked(
            preferences.getBoolean("email_notifications", true)
        );
        binding.switchPushNotifications.setChecked(
            preferences.getBoolean("push_notifications", true)
        );

        // Load display settings
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        binding.switchDarkMode.setChecked(isDarkMode);
        binding.switchAutoRefresh.setChecked(
            preferences.getBoolean("auto_refresh", true)
        );
    }

    private void setupListeners() {
        // Notification settings
        binding.switchEmailNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("email_notifications", isChecked).apply();
            showSnackbar("Email notifications " + (isChecked ? "enabled" : "disabled"));
        });

        binding.switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("push_notifications", isChecked).apply();
            showSnackbar("Push notifications " + (isChecked ? "enabled" : "disabled"));
        });

        // Display settings
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        binding.switchAutoRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("auto_refresh", isChecked).apply();
            showSnackbar("Auto refresh " + (isChecked ? "enabled" : "disabled"));
        });

        // Data settings
        binding.buttonClearCache.setOnClickListener(v -> showClearCacheDialog());
        binding.buttonExportData.setOnClickListener(v -> exportData());

        // About section
        binding.buttonPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
        binding.buttonTermsOfService.setOnClickListener(v -> openTermsOfService());
    }

    private void showClearCacheDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear Cache")
            .setMessage("Are you sure you want to clear the app cache? This will remove all temporary data.")
            .setPositiveButton("Clear", (dialog, which) -> {
                clearCache();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void clearCache() {
        binding.progressBar.setVisibility(View.VISIBLE);
        // TODO: Implement cache clearing logic
        // For now, just show a success message
        binding.progressBar.setVisibility(View.GONE);
        showSnackbar("Cache cleared successfully");
    }

    private void exportData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        // TODO: Implement data export logic
        // For now, just show a success message
        binding.progressBar.setVisibility(View.GONE);
        showSnackbar("Data exported successfully");
    }

    private void openPrivacyPolicy() {
        // TODO: Implement privacy policy navigation
        Toast.makeText(requireContext(), "Privacy Policy", Toast.LENGTH_SHORT).show();
    }

    private void openTermsOfService() {
        // TODO: Implement terms of service navigation
        Toast.makeText(requireContext(), "Terms of Service", Toast.LENGTH_SHORT).show();
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 