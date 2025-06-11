package com.example.projectmanagement.network;

import android.app.Notification;

import com.example.projectmanagement.models.Comment;
//import com.example.projectmanagement.models.Notification;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Auth endpoints
    @POST("auth/login")
    Call<User> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<User> register(@Body RegisterRequest request);

    @POST("auth/change-password")
    Call<Void> changePassword(@Body ChangePasswordRequest request);

    // Project endpoints
    @GET("projects")
    Call<List<Project>> getProjects();

    @GET("projects/{id}")
    Call<Project> getProject(@Path("id") String projectId);

    @POST("projects")
    Call<Project> createProject(@Body Project project);

    @PUT("projects/{id}")
    Call<Project> updateProject(@Path("id") String projectId, @Body Project project);

    @DELETE("projects/{id}")
    Call<Void> deleteProject(@Path("id") String projectId);

    @GET("projects/search")
    Call<List<Project>> searchProjects(@Query("q") String query);

    // Task endpoints
    @GET("projects/{projectId}/tasks")
    Call<List<Task>> getTasks(@Path("projectId") String projectId);

    @GET("tasks/{id}")
    Call<Task> getTask(@Path("id") String taskId);

    @POST("projects/{projectId}/tasks")
    Call<Task> createTask(@Path("projectId") String projectId, @Body Task task);

    @PUT("tasks/{id}")
    Call<Task> updateTask(@Path("id") String taskId, @Body Task task);

    @DELETE("tasks/{id}")
    Call<Void> deleteTask(@Path("id") String taskId);

    @GET("tasks/search")
    Call<List<Task>> searchTasks(@Query("q") String query);

    // Comment endpoints
    @GET("tasks/{taskId}/comments")
    Call<List<Comment>> getComments(@Path("taskId") String taskId);

    @POST("tasks/{taskId}/comments")
    Call<Comment> createComment(@Path("taskId") String taskId, @Body Comment comment);

    @PUT("comments/{id}")
    Call<Comment> updateComment(@Path("id") String commentId, @Body Comment comment);

    @DELETE("comments/{id}")
    Call<Void> deleteComment(@Path("id") String commentId);

    // User endpoints
    @GET("users/profile")
    Call<User> getProfile();

    @PUT("users/profile")
    Call<User> updateProfile(@Body User user);

    // Notification endpoints
//    @GET("notifications")
//    Call<List<Notification>> getNotifications();
//
//    @POST("notifications/{id}/read")
//    Call<Void> markNotificationAsRead(@Path("id") String notificationId);
//
//    @POST("notifications/read-all")
//    Call<Void> markAllNotificationsAsRead();

    // Request models
    class LoginRequest {
        private String email;
        private String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    class RegisterRequest {
        private String name;
        private String email;
        private String password;

        public RegisterRequest(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }

    class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public ChangePasswordRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }
    }
} 