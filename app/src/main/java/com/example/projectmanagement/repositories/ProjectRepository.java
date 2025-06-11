package com.example.projectmanagement.repositories;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.api.ApiClient;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.ProjectListResponse;
import com.example.projectmanagement.utils.SharedPreferencesManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ProjectRepository {
    private final ApiService apiService;
    private final SharedPreferencesManager prefsManager;

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public ProjectRepository(Context context) {
        apiService = ApiClient.getClient(context).create(ApiService.class);
        prefsManager = new SharedPreferencesManager(context);
    }

//    public void searchProjects(String query, Callback<List<Project>> callback) {
//        apiService.searchProjects(query).enqueue(new retrofit2.Callback<List<Project>>() {
//            @Override
//            public void onResponse(@NonNull Call<List<Project>> call, @NonNull Response<List<Project>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    callback.onSuccess(response.body());
//                } else {
//                    callback.onError("Failed to search projects");
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<List<Project>> call, @NonNull Throwable t) {
//                callback.onError(t.getMessage());
//            }
//        });
//    }

    public void getProjects(Callback<List<Project>> callback) {
        String token = prefsManager.getAccessToken();
        if (token == null) {
            callback.onError("Access token not available. Please login again.");
            return;
        }

        apiService.getProjects("Bearer " + token).enqueue(new retrofit2.Callback<ProjectListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProjectListResponse> call, @NonNull Response<ProjectListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getProjects());
                } else {
                    callback.onError("Failed to load projects: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProjectListResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public LiveData<java.util.Map<String, Project>> getAllProjects() {
        MutableLiveData<java.util.Map<String, Project>> result = new MutableLiveData<>();
        String token = prefsManager.getAccessToken();
        if (token == null) {
            result.setValue(new java.util.HashMap<>());
            return result;
        }

        apiService.getProjects("Bearer " + token).enqueue(new retrofit2.Callback<ProjectListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProjectListResponse> call, @NonNull Response<ProjectListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getProjects() != null) {
                    java.util.Map<String, Project> projectsMap = new java.util.HashMap<>();
                    for (Project project : response.body().getProjects()) {
                        projectsMap.put(project.getId(), project);
                    }
                    result.setValue(projectsMap);
                } else {
                    result.setValue(new java.util.HashMap<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProjectListResponse> call, @NonNull Throwable t) {
                result.setValue(new java.util.HashMap<>());
            }
        });
        return result;
    }
} 