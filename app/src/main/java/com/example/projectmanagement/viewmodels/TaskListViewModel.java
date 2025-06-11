package com.example.projectmanagement.viewmodels;

import android.app.Application;
import android.util.Log; // Import for logging
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.repositories.ProjectRepository;
import com.example.projectmanagement.repositories.TaskRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger; // For tracking completed API calls

public class TaskListViewModel extends AndroidViewModel {
    private final MutableLiveData<Map<String, List<Task>>> projectTasksMap = new MutableLiveData<>(new HashMap<>());
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    // New LiveData to hold all tasks combined with project info
    private final MutableLiveData<List<Task>> allCombinedTasks = new MutableLiveData<>(new ArrayList<>());
    private Map<String, Project> allProjectsMap = new HashMap<>();

    public TaskListViewModel(Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
        projectRepository = new ProjectRepository(application);

        // Observe all projects
        projectRepository.getAllProjects().observeForever(projectsMap -> {
            if (projectsMap != null) {
                allProjectsMap = projectsMap;
                Log.d("TaskListViewModel", "Projects loaded. Size: " + allProjectsMap.size());
                // Trigger a re-load of all tasks to ensure project info is attached
                loadAllTasksInternal(); // Call internal method to refresh all tasks with new project info
            }
        });
    }

    // This method will be used by TaskPriorityFilterFragment
    public LiveData<List<Task>> getAllCombinedTasks() {
        return allCombinedTasks;
    }

    public LiveData<Map<String, List<Task>>> getProjectTasksMap() {
        // This LiveData is still used by ProjectDetailFragment, for tasks specific to a project.
        return projectTasksMap;
    }

    // Internal method to load all tasks and combine with project info
    private void loadAllTasksInternal() {
        projectTasksMap.setValue(new HashMap<>()); // Clear the map before re-populating

        if (allProjectsMap.isEmpty()) {
            allCombinedTasks.setValue(new ArrayList<>()); // No projects, no tasks
            return;
        }

        List<String> projectIds = new ArrayList<>(allProjectsMap.keySet());
        AtomicInteger completedCalls = new AtomicInteger(0);
        List<Task> tempAllTasks = new ArrayList<>(); // Temporarily hold all tasks

        for (String projectId : projectIds) {
            taskRepository.getTasks(projectId).observeForever(tasks -> {
                Log.d("TaskListViewModel", "Received tasks from repository for project " + projectId + ": " + (tasks != null ? tasks.size() : "null") + " tasks.");
                if (tasks != null) {
                    // Attach project info to tasks
                    Project project = allProjectsMap.get(projectId);
                    for (Task task : tasks) {
                        if (project != null) {
                            task.setProject(project);
                        }
                        tempAllTasks.add(task);
                    }
                    Log.d("TaskListViewModel", "Loaded tasks for project " + projectId + ". Tasks count after adding: " + tasks.size());
                } else {
                    Log.w("TaskListViewModel", "Tasks for project " + projectId + " are null.");
                }

                // Increment counter and check if all calls are complete
                if (completedCalls.incrementAndGet() == projectIds.size()) {
                    allCombinedTasks.setValue(tempAllTasks); // Update the LiveData for all tasks
                    Log.d("TaskListViewModel", "All project tasks loaded and combined. Total tasks: " + tempAllTasks.size());
                }
            });
        }
    }

    public void loadTasks(String projectId) {
        taskRepository.getTasks(projectId).observeForever(taskList -> {
            Log.d("TaskListViewModel", "Received tasks for specific project " + projectId + ": " + (taskList != null ? taskList.size() : "null") + " tasks.");
            if (taskList != null) {
                Map<String, List<Task>> currentMap = projectTasksMap.getValue();
                if (currentMap == null) {
                    currentMap = new HashMap<>();
                }
                currentMap.put(projectId, taskList);
                projectTasksMap.setValue(currentMap);
                Log.d("TaskListViewModel", "Loaded tasks for project " + projectId + " (specific call). Tasks count: " + taskList.size());
            }
        });
    }

    public void loadAllTasks() {
        // This is called by TaskPriorityFilterFragment initially.
        // It should trigger the full task aggregation process if projects are already loaded.
        // If projects are not yet loaded, the project observer will eventually trigger loadAllTasksInternal.
        if (!allProjectsMap.isEmpty()) {
            loadAllTasksInternal();
        } else {
            // If projects are not loaded yet, the projectRepository.getAllProjects() observer
            // will eventually populate allProjectsMap and call loadAllTasksInternal().
            Log.d("TaskListViewModel", "Projects not yet loaded, waiting for projectRepository.getAllProjects().");
        }
    }

    public LiveData<Task> updateTaskStatus(String taskId, String newStatus, String projectId) {
        MutableLiveData<Task> updatedTaskLiveData = new MutableLiveData<>();
        taskRepository.updateTaskStatus(taskId, newStatus, projectId).observeForever(updatedTask -> {
            if (updatedTask != null) {
                Map<String, List<Task>> currentMap = projectTasksMap.getValue();
                if (currentMap != null) {
                    List<Task> tasksForProject = currentMap.get(projectId);
                    if (tasksForProject != null) {
                        // Find the task and update it in the list
                        for (int i = 0; i < tasksForProject.size(); i++) {
                            if (tasksForProject.get(i).getId().equals(updatedTask.getId())) {
                                tasksForProject.set(i, updatedTask);
                                break;
                            }
                        }
                        projectTasksMap.setValue(currentMap); // Trigger observer with updated map
                        Log.d("TaskListViewModel", "Task status updated for task: " + taskId);
                    }
                }
                updatedTaskLiveData.setValue(updatedTask);
            } else {
                updatedTaskLiveData.setValue(null);
            }
        });
        return updatedTaskLiveData;
    }
} 