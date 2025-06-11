package com.example.projectmanagement.viewmodels;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.ProjectListResponse;
import com.example.projectmanagement.repositories.UserRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectViewModel extends AndroidViewModel {
    private static final String TAG = "ProjectViewModel";
    private final MutableLiveData<List<Project>> projects = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final ApiService apiService;
    private final UserRepository userRepository;

    public ProjectViewModel(Application application) {
        super(application);
        apiService = ApiService.getInstance(application.getApplicationContext());
        userRepository = UserRepository.getInstance(application.getApplicationContext());
    }

    public LiveData<List<Project>> getProjects() {
        return projects;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadProjects() {
        String accessToken = userRepository.getAccessToken();
        Log.d(TAG, "Access Token: " + (accessToken != null ? accessToken : "null"));

        if (accessToken == null || accessToken.isEmpty()) {
            error.postValue("User not authenticated. Please login.");
            Log.e(TAG, "No access token found. Cannot load projects.");
            return;
        }
        
        apiService.getProjects("Bearer " + accessToken).enqueue(new Callback<ProjectListResponse>() {
            @Override
            public void onResponse(Call<ProjectListResponse> call, Response<ProjectListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Project> projectList = response.body().getProjects();
                    Log.d(TAG, "Projects loaded successfully: " + projectList.size() + " projects");
                    projects.postValue(projectList);
                } else {
                    String errorMessage = "Failed to load projects: " + response.message();
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            errorMessage += ": " + errorBodyString;
                            Log.e(TAG, "API Error Response: " + errorBodyString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e(TAG, errorMessage + " (HTTP Status: " + response.code() + ")");
                    error.postValue(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ProjectListResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage(), t);
                error.postValue(t.getMessage());
            }
        });
    }

    public LiveData<Boolean> deleteProject(String projectId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        apiService.deleteProject(projectId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(true);
                    loadProjects();
                } else {
                    result.postValue(false);
                    error.postValue("Failed to delete project");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(false);
                error.postValue(t.getMessage());
            }
        });
        return result;
    }

    public LiveData<Project> updateProject(Project project) {
        MutableLiveData<Project> result = new MutableLiveData<>();
        apiService.updateProject(project).enqueue(new Callback<Project>() {
            @Override
            public void onResponse(Call<Project> call, Response<Project> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(response.body());
                    loadProjects();
                } else {
                    result.postValue(null);
                    error.postValue("Failed to update project");
                }
            }

            @Override
            public void onFailure(Call<Project> call, Throwable t) {
                result.postValue(null);
                error.postValue(t.getMessage());
            }
        });
        return result;
    }
}