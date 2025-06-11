package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.R;
import com.example.projectmanagement.adapters.ProjectAdapter;
import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.databinding.FragmentProjectListBinding;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.ProjectListResponse;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.utils.SharedPreferencesManager;
import com.example.projectmanagement.viewmodels.ProjectViewModel;
import com.example.projectmanagement.viewmodels.TaskListViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import android.util.Log;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectListFragment extends Fragment {
    private FragmentProjectListBinding binding;
    private ApiService apiService;
    private SharedPreferencesManager prefsManager;
    private ProjectAdapter projectAdapter;
    private ProjectViewModel projectViewModel;
    private List<Project> projects = new ArrayList<>();
    private static final String TAG = "ProjectListFragment";
    private Context fragmentContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        apiService = ApiService.getInstance(requireContext());
        prefsManager = new SharedPreferencesManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProjectListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();
        observeProjects();
        projectViewModel.loadProjects();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_project_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_create_project) {
            navigateToCreateProject();
            return true;
        } else if (itemId == R.id.action_profile) {
            navigateToStatistics();
            return true;
        } else if (itemId == R.id.action_settings) {
            navigateToSettings();
            return true;
        } else if (itemId == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewModel() {
        projectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
    }

    private void setupRecyclerView() {
        projectAdapter = new ProjectAdapter(new ArrayList<>(), project -> {
            Bundle args = new Bundle();
            args.putString("project", new Gson().toJson(project));
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_projectListFragment_to_projectDetailFragment, args);
        });

        binding.recyclerViewProjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewProjects.setAdapter(projectAdapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            projectViewModel.loadProjects();
        });
    }

    private void setupFab() {
        binding.fabAddProject.setOnClickListener(v -> navigateToCreateProject());
    }

    private void observeProjects() {
        projectViewModel.getProjects().observe(getViewLifecycleOwner(), projects -> {
            this.projects = projects;
            projectAdapter.updateProjects(projects);
            updateUI();
        });

        projectViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                showSnackbar(error);
                updateUI();
            }
        });
    }

    private void updateUI() {
        binding.progressBar.setVisibility(View.GONE);
        binding.swipeRefresh.setRefreshing(false);
        boolean isEmpty = projects.isEmpty();
        binding.recyclerViewProjects.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void loadProjects() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        String token = prefsManager.getAccessToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        apiService.getProjects("Bearer " + token).enqueue(new Callback<ProjectListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProjectListResponse> call, @NonNull Response<ProjectListResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    projects = response.body().getProjects();
                    projectAdapter.updateProjects(projects);
                    binding.tvEmpty.setVisibility(projects.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(requireContext(), "Failed to load projects: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProjectListResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToCreateProject() {
        Navigation.findNavController(requireView())
            .navigate(R.id.action_projectListFragment_to_createProjectFragment);
    }

    private void navigateToStatistics() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_projectListFragment_to_statisticsFragment);
    }

    private void navigateToSettings() {
        Navigation.findNavController(requireView())
            .navigate(R.id.action_projectListFragment_to_settingsFragment);
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> logout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void logout() {
        prefsManager.clearUserData();
        Navigation.findNavController(requireView())
            .navigate(R.id.action_projectListFragment_to_loginFragment);
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 