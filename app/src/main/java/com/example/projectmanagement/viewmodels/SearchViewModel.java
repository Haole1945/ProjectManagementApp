package com.example.projectmanagement.viewmodels;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.repositories.ProjectRepository;
import com.example.projectmanagement.repositories.TaskRepository;
import com.example.projectmanagement.api.ApiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchViewModel extends AndroidViewModel {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    private final MutableLiveData<List<Project>> projects = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> projectProgressMap = new MutableLiveData<>(new HashMap<>());

    public SearchViewModel(Application application) {
        super(application);
        projectRepository = new ProjectRepository(application.getApplicationContext());
        taskRepository = new TaskRepository(application.getApplicationContext());
    }

    public LiveData<List<Project>> getProjects() {
        return projects;
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Map<String, Integer>> getProjectProgressMap() {
        return projectProgressMap;
    }

    public void loadProjectProgress() {
        ApiService.getInstance(getApplication().getApplicationContext()).getProjectPercentCompleted().enqueue(new retrofit2.Callback<Map<String, Object>>() {
            @Override
            public void onResponse(retrofit2.Call<Map<String, Object>> call, retrofit2.Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Integer> progressMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : response.body().entrySet()) {
                        progressMap.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                    }
                    projectProgressMap.postValue(progressMap);
                }
            }
            @Override
            public void onFailure(retrofit2.Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    public void search(String query) {
        isLoading.setValue(true);
        error.setValue(null);
        loadProjectProgress();

        // Search projects
//        projectRepository.searchProjects(query, new ProjectRepository.Callback<List<Project>>() {
//            @Override
//            public void onSuccess(List<Project> result) {
//                projects.postValue(result);
//                checkLoadingComplete();
//            }
//
//            @Override
//            public void onError(String message) {
//                error.postValue(message);
//                checkLoadingComplete();
//            }
//        });

        // Search tasks
//        taskRepository.searchTasks(query, new TaskRepository.Callback<List<Task>>() {
//            @Override
//            public void onSuccess(List<Task> result) {
//                tasks.postValue(result);
//                checkLoadingComplete();
//            }
//
//            @Override
//            public void onError(String message) {
//                error.postValue(message);
//                checkLoadingComplete();
//            }
//        });
    }

    private void checkLoadingComplete() {
        // Only set isLoading to false if both search operations have completed
        if (projects.getValue() != null && tasks.getValue() != null) {
            isLoading.postValue(false);
        }
    }

    public void clearSearch() {
        projects.setValue(new ArrayList<>());
        tasks.setValue(new ArrayList<>());
        error.setValue(null);
    }
} 