package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.view.DragEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.R;
import com.example.projectmanagement.adapters.TeamMemberAdapter;
import com.example.projectmanagement.adapters.TaskAdapter;
import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.databinding.FragmentProjectDetailBinding;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.User;
import com.example.projectmanagement.viewmodels.ProjectViewModel;
import com.example.projectmanagement.viewmodels.TaskListViewModel;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import android.content.ClipData;

public class ProjectDetailFragment extends Fragment implements TaskAdapter.OnTaskDragListener, TaskAdapter.OnTaskClickListener {
    private FragmentProjectDetailBinding binding;
    private ApiService apiService;
    private Project currentProject;
    private TeamMemberAdapter memberAdapter;
    private TaskAdapter taskAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private ProjectViewModel projectViewModel;
    private TaskListViewModel taskListViewModel;

    // New variables for task filtering
    private RadioGroup rgTaskFilters;
    private RadioButton rbAll, rbTodo, rbWorkInProgress, rbUnderReview, rbCompleted;
    private List<com.example.projectmanagement.models.Task> allTasks = new ArrayList<>();
    private List<com.example.projectmanagement.models.Task> filteredTasks = new ArrayList<>();
    private String currentFilter = "All"; // Default filter

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiService.getInstance(requireContext());
        projectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
        taskListViewModel = new ViewModelProvider(this).get(TaskListViewModel.class);

        // Get project data from arguments
        String projectJson = getArguments().getString("project");
        if (projectJson != null) {
            currentProject = new Gson().fromJson(projectJson, Project.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProjectDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupRecyclerViews();
        setupClickListeners();
        setupTaskFilters();
        populateProjectData();
    }

    @Override
    public void onTaskDragged(com.example.projectmanagement.models.Task task) {
        Toast.makeText(requireContext(), "Dragging task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskClick(com.example.projectmanagement.models.Task task) {
        Bundle args = new Bundle();
        args.putString("task", new Gson().toJson(task));
        NavHostFragment.findNavController(this).navigate(R.id.action_projectDetailFragment_to_taskDetailFragment, args);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                navigateToEditProject();
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerViews() {
        // Setup team members RecyclerView
        List<User> initialMembers = new ArrayList<>();
        memberAdapter = new TeamMemberAdapter(initialMembers, null);
        binding.rvTeamMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTeamMembers.setAdapter(memberAdapter);
        binding.rvTeamMembers.setNestedScrollingEnabled(false);

        // Setup tasks RecyclerView
        taskAdapter = new TaskAdapter(this, this);
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTasks.setAdapter(taskAdapter);
        binding.rvTasks.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        binding.btnAddTask.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("project", new Gson().toJson(currentProject));
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_projectDetailFragment_to_createTaskFragment, args);
        });

        binding.btnAddMember.setOnClickListener(v -> {
            navigateToEditTeamMembers();
        });
    }

    private void setupTaskFilters() {
        rgTaskFilters = binding.rgTaskFilters;
        rbAll = binding.rbAll;
        rbTodo = binding.rbTodo;
        rbWorkInProgress = binding.rbWorkInProgress;
        rbUnderReview = binding.rbUnderReview;
        rbCompleted = binding.rbCompleted;

        rgTaskFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbAll) {
                currentFilter = "All";
            } else if (checkedId == R.id.rbTodo) {
                currentFilter = "To Do";
            } else if (checkedId == R.id.rbWorkInProgress) {
                currentFilter = "Work In Progress";
            } else if (checkedId == R.id.rbUnderReview) {
                currentFilter = "Under Review";
            } else if (checkedId == R.id.rbCompleted) {
                currentFilter = "Completed";
            }
            filterTasks();
        });

        rbTodo.setOnDragListener(new TaskStatusDragListener("To Do"));
        rbWorkInProgress.setOnDragListener(new TaskStatusDragListener("Work In Progress"));
        rbUnderReview.setOnDragListener(new TaskStatusDragListener("Under Review"));
        rbCompleted.setOnDragListener(new TaskStatusDragListener("Completed"));
    }

    private void filterTasks() {
        Log.d("ProjectDetailFragment", "Filtering tasks. Current filter: " + currentFilter + ", allTasks size: " + allTasks.size());
        filteredTasks.clear();
        if (Objects.equals(currentFilter, "All")) {
            filteredTasks.addAll(allTasks);
        } else {
            for (com.example.projectmanagement.models.Task task : allTasks) {
                if (task.getStatus() != null && task.getStatus().equalsIgnoreCase(currentFilter)) {
                    filteredTasks.add(task);
                }
            }
        }
        Log.d("ProjectDetailFragment", "Filtered tasks size: " + filteredTasks.size());
        taskAdapter.setTasks(filteredTasks);
        binding.tvNoTasks.setVisibility(filteredTasks.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvTasks.setVisibility(filteredTasks.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void populateProjectData() {
        if (currentProject != null) {
            binding.toolbar.setTitle(currentProject.getName());
            binding.tvProjectName.setText(currentProject.getName());
            binding.tvDescription.setText(currentProject.getDescription());

            // Set status chip
            Chip statusChip = binding.chipStatus;
            String status = currentProject.getStatus();
            if (status == null) {
                status = "Unknown"; // Default status if null
            }
            statusChip.setText(status);
            switch (status.toLowerCase()) {
                case "planning":
                    statusChip.setChipBackgroundColorResource(R.color.status_planning);
                    break;
                case "in progress":
                    statusChip.setChipBackgroundColorResource(R.color.status_in_progress);
                    break;
                case "on hold":
                    statusChip.setChipBackgroundColorResource(R.color.status_on_hold);
                    break;
                case "completed":
                    statusChip.setChipBackgroundColorResource(R.color.status_completed);
                    break;
                case "cancelled":
                    statusChip.setChipBackgroundColorResource(R.color.status_cancelled);
                    break;
                default:
                    statusChip.setChipBackgroundColorResource(R.color.status_unknown);
                    break;
            }

            // Set date range
            String dateRange = dateFormat.format(currentProject.getStartDate()) + " - " +
                    dateFormat.format(currentProject.getEndDate());
            binding.tvDateRange.setText(dateRange);

            // Update team members
            List<User> combinedMembers = new ArrayList<>();
            if (currentProject.getOwner() != null) {
                User ownerUser = currentProject.getOwner();
                ownerUser.setOwner(true); // Mark as owner
                combinedMembers.add(ownerUser);
            }
            if (currentProject.getMembers() != null) {
                for (User member : currentProject.getMembers()) {
                    member.setOwner(false); // Ensure members are not marked as owner
                    combinedMembers.add(member);
                }
            }
            memberAdapter.updateMembers(combinedMembers);

            // Load and update tasks
            if (currentProject.getId() != null) {
                taskListViewModel.loadTasks(currentProject.getId());
                taskListViewModel.getProjectTasksMap().observe(getViewLifecycleOwner(), projectTasksMap -> {
                    allTasks = projectTasksMap.get(currentProject.getId());
                    if (allTasks == null) {
                        allTasks = new ArrayList<>();
                    }
                    filterTasks(); // Apply filter after tasks are loaded
                });
            }
        }
    }

    private void navigateToEditProject() {
        Bundle args = new Bundle();
        args.putString("project", new Gson().toJson(currentProject));
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_projectDetailFragment_to_editProjectFragment, args);
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
                NavHostFragment.findNavController(ProjectDetailFragment.this)
                        .navigate(R.id.action_projectDetailFragment_to_projectListFragment);
            } else {
                Toast.makeText(requireContext(), "Failed to delete project", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToEditTeamMembers() {
        Bundle args = new Bundle();
        args.putString("project", new Gson().toJson(currentProject));
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_projectDetailFragment_to_editTeamMembersFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class TaskStatusDragListener implements View.OnDragListener {
        private final String targetStatus;

        public TaskStatusDragListener(String targetStatus) {
            this.targetStatus = targetStatus;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d("DragDrop", "ACTION_DRAG_STARTED for " + targetStatus);
                    if (event.getClipDescription().hasMimeType(android.content.ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        // The actual hover state will be managed by ACTION_DRAG_ENTERED
                        return true;
                    }
                    return false;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d("DragDrop", "ACTION_DRAG_ENTERED for " + targetStatus);
                    v.setHovered(true);
                    v.jumpDrawablesToCurrentState();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d("DragDrop", "ACTION_DRAG_EXITED for " + targetStatus);
                    v.setHovered(false);
                    v.jumpDrawablesToCurrentState();
                    return true;
                case DragEvent.ACTION_DROP:
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    String taskId = item.getText().toString();
                    Toast.makeText(requireContext(), "Task dropped: " + taskId + " to status: " + targetStatus, Toast.LENGTH_LONG).show();
                    // Call ViewModel to update task status
                    taskListViewModel.updateTaskStatus(taskId, targetStatus, currentProject.getId())
                            .observe(getViewLifecycleOwner(), updatedTask -> {
                                if (updatedTask != null) {
                                    // Find the task in allTasks and update it
                                    for (int i = 0; i < allTasks.size(); i++) {
                                        if (allTasks.get(i).getId().equals(updatedTask.getId())) {
                                            allTasks.set(i, updatedTask);
                                            break;
                                        }
                                    }
                                    // Update the UI to show the new status category
                                    String newStatusLower = updatedTask.getStatus() != null ? updatedTask.getStatus().toLowerCase() : "unknown";
                                    Log.d("ProjectDetailFragment", "Updated task status: " + updatedTask.getStatus() + ", New status lower: " + newStatusLower);

                                    switch (newStatusLower) {
                                        case "to do":
                                            rbTodo.setChecked(true);
                                            break;
                                        case "work in progress":
                                            rbWorkInProgress.setChecked(true);
                                            break;
                                        case "under review":
                                            rbUnderReview.setChecked(true);
                                            break;
                                        case "completed":
                                            rbCompleted.setChecked(true);
                                            break;
                                        default:
                                            rbAll.setChecked(true);
                                            break;
                                    }
                                    Toast.makeText(requireContext(), "Task status updated in UI!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to update task status in UI.", Toast.LENGTH_SHORT).show();
                                }
                            });
                    v.setHovered(false);
                    v.jumpDrawablesToCurrentState();
                    if (v instanceof RadioButton) {
                        RadioButton rb = (RadioButton) v;
                        Log.d("DragDropDebug", "ACTION_DROP - After update - " + targetStatus + ": Checked=" + rb.isChecked() + ", Hovered=" + rb.isHovered() + ", TextColor=" + String.format("#%06X", (0xFFFFFF & rb.getCurrentTextColor())) + ", Alpha=" + rb.getAlpha());
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    // Reset background for the target view after the drag operation ends
                    Log.d("DragDrop", "ACTION_DRAG_ENDED for " + targetStatus + ", Result: " + event.getResult());
                    v.setHovered(false);
                    v.jumpDrawablesToCurrentState();
                    if (v instanceof RadioButton) {
                        RadioButton rb = (RadioButton) v;
                        Log.d("DragDropDebug", "ACTION_DRAG_ENDED - " + targetStatus + ": Checked=" + rb.isChecked() + ", Hovered=" + rb.isHovered() + ", TextColor=" + String.format("#%06X", (0xFFFFFF & rb.getCurrentTextColor())) + ", Alpha=" + rb.getAlpha());
                    }
                    return true;
                default:
                    Log.d("DragDrop", "Unknown action type: " + action + " for " + targetStatus);
                    break;
            }
            return false;
        }
    }
} 