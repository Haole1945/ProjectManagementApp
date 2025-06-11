package com.example.projectmanagement.repositories;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.api.ApiClient;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.models.TaskListResponse;
import com.example.projectmanagement.models.TaskUpdateResponse;
import com.example.projectmanagement.utils.SharedPreferencesManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class TaskRepository {
    private final ApiService apiService;
    private final SharedPreferencesManager prefsManager;
    private final Context context;

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public TaskRepository(Context context) {
        this.context = context;
        apiService = ApiClient.getClient(context).create(ApiService.class);
        prefsManager = new SharedPreferencesManager(context);
    }

    //    public void searchTasks(String query, Callback<List<Task>> callback) {
//        apiService.searchTasks(query).enqueue(new retrofit2.Callback<List<Task>>() {
//            @Override
//            public void onResponse(@NonNull Call<List<Task>> call, @NonNull Response<List<Task>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    callback.onSuccess(response.body());
//                } else {
//                    callback.onError("Failed to search tasks");
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<List<Task>> call, @NonNull Throwable t) {
//                callback.onError(t.getMessage());
//            }
//        });
//    }

    public LiveData<List<Task>> getTasks(String projectId) {
        MutableLiveData<List<Task>> result = new MutableLiveData<>();
        String token = prefsManager.getAccessToken();
        if (token == null) {
            result.setValue(null);
            return result;
        }

        apiService.getTasks("Bearer " + token, projectId).enqueue(new retrofit2.Callback<TaskListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TaskListResponse> call, @NonNull Response<TaskListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getTasks());
                } else {
                    result.setValue(null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<TaskListResponse> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public LiveData<List<Task>> getAllTasks() {
        MutableLiveData<List<Task>> result = new MutableLiveData<>();
        String token = prefsManager.getAccessToken();
        if (token == null) {
            result.setValue(null);
            return result;
        }

        apiService.getAllTasks("Bearer " + token).enqueue(new retrofit2.Callback<TaskListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TaskListResponse> call, @NonNull Response<TaskListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getTasks());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TaskListResponse> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public LiveData<Task> updateTaskStatus(String taskId, String newStatus, String projectId) {
        MutableLiveData<Task> result = new MutableLiveData<>();
        String token = prefsManager.getAccessToken();
        if (token == null) {
            Toast.makeText(context, "Error: No authentication token found", Toast.LENGTH_LONG).show();
            result.setValue(null);
            return result;
        }

        Map<String, String> body = new HashMap<>();
        body.put("taskId", taskId);
        body.put("toStatus", newStatus);
        body.put("projectId", projectId);

        Toast.makeText(context, "Sending update request for task: " + taskId + " to status: " + newStatus, Toast.LENGTH_LONG).show();

        apiService.updateTaskStatus(body).enqueue(new retrofit2.Callback<TaskUpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<TaskUpdateResponse> call, @NonNull Response<TaskUpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getTask() != null) {
                    Toast.makeText(context, "Task status updated successfully", Toast.LENGTH_LONG).show();
                    result.setValue(response.body().getTask());
                } else {
                    String errorMessage = "Error updating task status. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " Message: " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMessage += " Could not read error message";
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TaskUpdateResponse> call, @NonNull Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                result.setValue(null);
            }
        });
        return result;
    }
} 