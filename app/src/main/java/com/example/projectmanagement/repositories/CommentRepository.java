package com.example.projectmanagement.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.models.Comment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentRepository {
    private static CommentRepository instance;
    private final ApiService apiService;
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private CommentRepository(Context context) {
        apiService = ApiService.getInstance(context);
    }

    public static CommentRepository getInstance(Context context) {
        if (instance == null) {
            instance = new CommentRepository(context);
        }
        return instance;
    }

    public LiveData<List<Comment>> getTaskComments(String taskId) {
        MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>();
        apiService.getTaskComments(taskId).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commentsLiveData.postValue(response.body());
                } else {
                    error.postValue("Failed to load comments");
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                error.postValue(t.getMessage());
            }
        });
        return commentsLiveData;
    }

    public LiveData<Comment> addComment(String taskId, String content) {
        MutableLiveData<Comment> commentLiveData = new MutableLiveData<>();
        Comment comment = new Comment();
        comment.setContent(content);
        apiService.addComment(taskId, comment).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commentLiveData.postValue(response.body());
                } else {
                    error.postValue("Failed to add comment");
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                error.postValue(t.getMessage());
            }
        });
        return commentLiveData;
    }

    public LiveData<Boolean> deleteComment(String commentId) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();
        apiService.deleteComment(commentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                resultLiveData.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                error.postValue(t.getMessage());
                resultLiveData.postValue(false);
            }
        });
        return resultLiveData;
    }

    public MutableLiveData<String> getError() {
        return error;
    }
} 