package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.R;
import com.example.projectmanagement.adapters.ProjectAdapter;
import com.example.projectmanagement.databinding.FragmentProjectsBinding;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.viewmodels.ProjectViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProjectsFragment extends Fragment {
    private FragmentProjectsBinding binding;
    private ProjectAdapter projectAdapter;
    private List<Project> projects = new ArrayList<>();
    private ProjectViewModel projectViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(ProjectViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProjectsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        observeViewModel();
        projectViewModel.loadProjects();
    }

    private void setupRecyclerView() {
        projectAdapter = new ProjectAdapter(projects, project -> {
            // Navigate to project detail
            Bundle args = new Bundle();
            args.putString("projectId", project.getId());
            Navigation.findNavController(requireView())
                .navigate(R.id.action_projects_to_project_detail, args);
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(projectAdapter);
    }

    private void observeViewModel() {
        projectViewModel.getProjects().observe(getViewLifecycleOwner(), projectList -> {
            binding.progressBar.setVisibility(View.GONE);
            if (projectList != null) {
                projects.clear();
                projects.addAll(projectList);
                projectAdapter.notifyDataSetChanged();

                if (projects.isEmpty()) {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerView.setVisibility(View.VISIBLE);
                }
            } else {
                binding.tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        projectViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            binding.tvEmpty.setVisibility(View.VISIBLE); // Show empty text on error as well
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 