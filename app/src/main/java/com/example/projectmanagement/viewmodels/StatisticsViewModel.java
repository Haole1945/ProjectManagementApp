package com.example.projectmanagement.viewmodels;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.models.TaskListResponse;
import com.example.projectmanagement.repositories.ProjectRepository;
import com.example.projectmanagement.repositories.TaskRepository;
import com.example.projectmanagement.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class StatisticsViewModel extends AndroidViewModel {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SharedPreferencesManager prefsManager;
    private final ApiService apiService;

    private final MutableLiveData<List<Project>> projects = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>(new ArrayList<>()); // This might not be needed for all tasks
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // Project statistics
    private final MutableLiveData<Integer> totalProjects = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> completedProjects = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> inProgressProjects = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> notStartedProjects = new MutableLiveData<>(0);

    // Task statistics (These statistics will now be calculated based on tasks loaded via project, or if a specific task loading method is implemented later)
    private final MutableLiveData<Integer> totalTasks = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> completedTasks = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> inProgressTasks = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> notStartedTasks = new MutableLiveData<>(0);

    // Performance statistics
    private final MutableLiveData<Float> avgProjectTime = new MutableLiveData<>(0f);
    private final MutableLiveData<Float> avgTaskTime = new MutableLiveData<>(0f);
    private final MutableLiveData<Float> onTimeRate = new MutableLiveData<>(0f);

    // Chart data
    private final MutableLiveData<Map<String, Integer>> projectStatusData = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<String, Integer>> taskStatusData = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<String, Float>> performanceData = new MutableLiveData<>(new HashMap<>());

    public StatisticsViewModel(Application application) {
        super(application);
        projectRepository = new ProjectRepository(application.getApplicationContext());
        taskRepository = new TaskRepository(application.getApplicationContext());
        prefsManager = new SharedPreferencesManager(application.getApplicationContext());
        apiService = ApiService.getInstance(application.getApplicationContext());
        loadData();
    }

    private void loadData() {
        isLoading.setValue(true);
        error.setValue(null);

        // Load projects
        projectRepository.getProjects(new ProjectRepository.Callback<List<Project>>() {
            @Override
            public void onSuccess(List<Project> result) {
                projects.postValue(result);
                calculateProjectStatistics(result);
                checkLoadingComplete();
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                checkLoadingComplete();
            }
        });

        // Removed the problematic task loading logic from here
        // Since we only need tasks per project, ProjectListFragment will handle loading tasks for each project.
        // If StatisticsViewModel needs task data, it should also be designed to load tasks per project or for specific purposes.
        // Currently, it's attempting to load all tasks, which causes a 500 error due to missing projectId.

        // No direct global task loading in StatisticsViewModel for now
        // until a clear requirement for it is defined and the backend supports it without projectId.

        // For now, consider loading complete for tasks if projects are loaded, or adjust checkLoadingComplete based on actual data needs
        // checkLoadingComplete(); // Re-evaluate if this is appropriate now that task loading is removed
    }

    private void calculateProjectStatistics(List<Project> projectList) {
        int total = projectList.size();
        int completed = 0;
        int inProgress = 0;
        int notStarted = 0;
        Map<String, Integer> statusData = new HashMap<>();

        for (Project project : projectList) {
            String status = project.getStatus(); // Get status safely
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "completed":
                        completed++;
                        break;
                    case "in progress":
                        inProgress++;
                        break;
                    case "not started":
                        notStarted++;
                        break;
                }
            }
        }

        statusData.put("Completed", completed);
        statusData.put("In Progress", inProgress);
        statusData.put("Not Started", notStarted);

        totalProjects.postValue(total);
        completedProjects.postValue(completed);
        inProgressProjects.postValue(inProgress);
        notStartedProjects.postValue(notStarted);
        projectStatusData.postValue(statusData);

        // Calculate average project completion time
        if (completed > 0) {
            long totalDays = 0;
            for (Project project : projectList) {
                if (project.getStatus() != null && project.getStatus().equalsIgnoreCase("completed") && project.getCreatedAt() != null && project.getUpdatedAt() != null) {
                    long startTime = project.getCreatedAt().getTime();
                    long endTime = project.getUpdatedAt().getTime();
                    long days = (endTime - startTime) / (1000 * 60 * 60 * 24);
                    totalDays += days;
                }
            }
            avgProjectTime.postValue((float) totalDays / completed);
        }
    }

    private void calculateTaskStatistics(List<Task> taskList) {
        // This method will only be called if tasks are loaded via other means, not globally from here.
        int total = taskList.size();
        int completed = 0;
        int inProgress = 0;
        int notStarted = 0;
        Map<String, Integer> statusData = new HashMap<>();
        int onTimeCount = 0;

        for (Task task : taskList) {
            String status = task.getStatus(); // Get status safely
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "completed":
                        completed++;
                        if (task.getUpdatedAt() != null && task.getDueDate() != null && task.getUpdatedAt().before(task.getDueDate())) {
                            onTimeCount++;
                        }
                        break;
                    case "in progress":
                        inProgress++;
                        break;
                    case "not started":
                        notStarted++;
                        break;
                }
            }
        }

        statusData.put("Completed", completed);
        statusData.put("In Progress", inProgress);
        statusData.put("Not Started", notStarted);

        totalTasks.postValue(total);
        completedTasks.postValue(completed);
        inProgressTasks.postValue(inProgress);
        notStartedTasks.postValue(notStarted);
        taskStatusData.postValue(statusData);

        // Calculate average task completion time
        if (completed > 0) {
            long totalDays = 0;
            for (Task task : taskList) {
                if (task.getStatus() != null && task.getStatus().equalsIgnoreCase("completed") && task.getCreatedAt() != null && task.getUpdatedAt() != null) {
                    long startTime = task.getCreatedAt().getTime();
                    long endTime = task.getUpdatedAt().getTime();
                    long days = (endTime - startTime) / (1000 * 60 * 60 * 24);
                    totalDays += days;
                }
            }
            avgTaskTime.postValue((float) totalDays / completed);
        }

        // Calculate on-time completion rate
        if (completed > 0) {
            onTimeRate.postValue((float) onTimeCount / completed * 100);
        }
    }

    private void checkLoadingComplete() {
        // Now, loading is considered complete once projects are loaded, as global task loading is removed
        if (projects.getValue() != null) {
            isLoading.postValue(false);
        }
    }

    public void refresh() {
        loadData();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Integer> getTotalProjects() {
        return totalProjects;
    }

    public LiveData<Integer> getCompletedProjects() {
        return completedProjects;
    }

    public LiveData<Integer> getInProgressProjects() {
        return inProgressProjects;
    }

    public LiveData<Integer> getNotStartedProjects() {
        return notStartedProjects;
    }

    public LiveData<Integer> getTotalTasks() {
        return totalTasks;
    }

    public LiveData<Integer> getCompletedTasks() {
        return completedTasks;
    }

    public LiveData<Integer> getInProgressTasks() {
        return inProgressTasks;
    }

    public LiveData<Integer> getNotStartedTasks() {
        return notStartedTasks;
    }

    public LiveData<Float> getAvgProjectTime() {
        return avgProjectTime;
    }

    public LiveData<Float> getAvgTaskTime() {
        return avgTaskTime;
    }

    public LiveData<Float> getOnTimeRate() {
        return onTimeRate;
    }

    public LiveData<Map<String, Integer>> getProjectStatusData() {
        return projectStatusData;
    }

    public LiveData<Map<String, Integer>> getTaskStatusData() {
        return taskStatusData;
    }

    public LiveData<Map<String, Float>> getPerformanceData() {
        return performanceData;
    }
} 