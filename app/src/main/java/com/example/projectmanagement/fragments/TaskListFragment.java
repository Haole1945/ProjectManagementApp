package com.example.projectmanagement.fragments;

import android.os.Bundle;
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
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.viewmodels.TaskListViewModel;
import com.google.gson.Gson;

import java.util.List;

public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskDragListener, TaskAdapter.OnTaskClickListener {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private TaskListViewModel viewModel;
    private String projectId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        recyclerView = view.findViewById(R.id.rvTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(this, this);
        recyclerView.setAdapter(taskAdapter);

        if (getArguments() != null) {
            projectId = getArguments().getString("projectId");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(TaskListViewModel.class);

        viewModel.getProjectTasksMap().observe(getViewLifecycleOwner(), tasksMap -> {
            if (tasksMap != null && projectId != null) {
                List<Task> tasksForProject = tasksMap.get(projectId);
                if (tasksForProject != null) {
                    taskAdapter.setTasks(tasksForProject);
                } else {
                    taskAdapter.setTasks(new java.util.ArrayList<>());
                }
            } else {
                taskAdapter.setTasks(new java.util.ArrayList<>());
            }
        });

        if (projectId != null) {
            viewModel.loadTasks(projectId);
        }
    }

    @Override
    public void onTaskDragged(Task task) {
        // Handle task dragged event if needed, e.g., show a toast
    }

    @Override
    public void onTaskClick(Task task) {
        Bundle args = new Bundle();
        args.putString("task", new Gson().toJson(task));
        NavHostFragment.findNavController(this).navigate(R.id.action_taskListFragment_to_taskDetailFragment, args);
    }
} 