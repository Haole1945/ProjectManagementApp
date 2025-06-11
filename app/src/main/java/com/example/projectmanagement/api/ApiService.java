package com.example.projectmanagement.api;

import android.content.Context;
import com.example.projectmanagement.models.Comment;
import com.example.projectmanagement.models.LoginRequest;
import com.example.projectmanagement.models.LoginResponse;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.models.User;
import com.example.projectmanagement.models.TaskListResponse;
import com.example.projectmanagement.models.TaskUpdateResponse;
import com.example.projectmanagement.models.UserSearchResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Part;
import retrofit2.http.PUT;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface ApiService {
    // Singleton instance (now requires context to get Retrofit client)
    // ApiService INSTANCE = ApiClient.getClient().create(ApiService.class);
    // Removed direct static instance as it now depends on context

    static ApiService getInstance(Context context) {
        return ApiClient.getClient(context).create(ApiService.class);
    }

    // Auth endpoints
    @POST("auth/sign-up")
    Call<User> signUp(@Body User user);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/logout")
    Call<Void> logout(@Header("Authorization") String token);

    @POST("auth/google")
    Call<LoginResponse> googleLogin(@Body Map<String, String> body);

    @POST("auth/password-forgot")
    Call<Void> passwordForgot(@Body Map<String, String> body);

    @POST("auth/password-otp")
    Call<Void> passwordOtp(@Body Map<String, String> body);

    @POST("auth/password-reset")
    Call<Void> passwordReset(@Body Map<String, String> body);

    @POST("auth/refresh-token")
    Call<LoginResponse> refreshToken(@Header("Authorization") String refreshToken);

    // User endpoints
    @POST("user/update-profile/{id}")
    @Multipart
    Call<User> updateProfile(
        @Path("id") String id,
        @Part MultipartBody.Part image,
        @Part("user") User user
    );

    @POST("user/update-account/{id}")
    Call<User> updateAccount(@Path("id") String id, @Body User user);

    // Project endpoints
    @POST("project/create")
    Call<Project> createProject(@Body Project project);

    @PATCH("project/update")
    Call<Project> updateProject(@Body Project project);

    @GET("project/get-all")
    Call<com.example.projectmanagement.models.ProjectListResponse> getProjects(@Header("Authorization") String token);

    @GET("project/get-percent-completed")
    Call<Map<String, Object>> getProjectPercentCompleted();

    @GET("project/{id}")
    Call<Project> getProject(@Path("id") String id);

    @POST("project/{id}/add-member")
    Call<Void> addMemberToProject(@Path("id") String id, @Body Map<String, String> body);

    @GET("project/data/chart")
    Call<Map<String, Object>> getProjectChartData();

    @GET("project/invite/confirm")
    Call<Void> confirmInvite(@Query("token") String token);

    @GET("user/get-all")
    Call<List<User>> getAllUsers(@Header("Authorization") String token);

    // Search endpoints
    @POST("search/add-member/{projectId}")
    Call<UserSearchResponse> searchAddMemberToProject(@Path("projectId") String projectId, @Body Map<String, String> body);

    // Task endpoints
    @POST("task/create")
    Call<Task> createTask(@Body Task task);

    @GET("task/get-all")
    Call<TaskListResponse> getTasks(@Header("Authorization") String token, @Query("projectId") String projectId);

    @GET("task/get-all")
    Call<TaskListResponse> getAllTasks(@Header("Authorization") String token);

    @DELETE("task/delete")
    Call<Void> deleteTasks(@Body List<String> taskIds);

    @PATCH("task/update-status")
    Call<TaskUpdateResponse> updateTaskStatus(@Body Map<String, String> body);

    @PATCH("task/update/{taskId}")
    Call<Task> updateTask(@Path("taskId") String taskId, @Body Task task);

    @DELETE("task/delete/{taskId}")
    Call<Void> deleteTask(@Path("taskId") String taskId);

    @GET("task/{taskId}")
    Call<Task> getTask(@Path("taskId") String taskId);

    @PATCH("task/update-completed/{taskId}")
    Call<Task> updateTaskCompleted(@Path("taskId") String taskId, @Body Map<String, Boolean> body);

    @GET("task/data/chart")
    Call<Map<String, Object>> getTaskChartData();

    // Comment endpoints
    @POST("comment/{taskId}")
    Call<Comment> addComment(@Path("taskId") String taskId, @Body Comment comment);

    @GET("comment/{taskId}")
    Call<List<Comment>> getTaskComments(@Path("taskId") String taskId);

    @PATCH("comment/{id}")
    Call<Comment> updateComment(@Path("id") String id, @Body Comment comment);

    @DELETE("comment/{id}")
    Call<Void> deleteComment(@Path("id") String id);

    @PATCH("comment/like/{id}")
    Call<Comment> updateCommentLike(@Path("id") String id);

    @DELETE("project/{id}")
    Call<Void> deleteProject(@Path("id") String projectId);
}