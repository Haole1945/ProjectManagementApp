package com.example.projectmanagement.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.projectmanagement.R;
import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.databinding.DialogDeleteTaskBinding;
import com.example.projectmanagement.models.Task;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteTaskFragment extends DialogFragment {
    private DialogDeleteTaskBinding binding;
    private ApiService apiService;
    private Task currentTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiService.getInstance(requireContext());

        // Get task data from arguments
        String taskJson = getArguments().getString("task");
        if (taskJson != null) {
            currentTask = new Gson().fromJson(taskJson, Task.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogDeleteTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnDelete.setOnClickListener(v -> deleteTask());
    }

    private void deleteTask() {
        if (currentTask == null) {
            Toast.makeText(requireContext(), "Task data not found", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnDelete.setEnabled(false);
        binding.btnCancel.setEnabled(false);

        apiService.deleteTask(currentTask.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnDelete.setEnabled(true);
                binding.btnCancel.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                    NavHostFragment.findNavController(DeleteTaskFragment.this).navigateUp();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnDelete.setEnabled(true);
                binding.btnCancel.setEnabled(true);
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