package com.example.projectmanagement.viewmodels;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.example.projectmanagement.models.Comment;
import com.example.projectmanagement.models.User;
import com.example.projectmanagement.repositories.CommentRepository;
import com.example.projectmanagement.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommentViewModel extends AndroidViewModel {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<List<Comment>> comments;
    private final MutableLiveData<String> error;
    private String taskId;
    private String currentUserId;
    private boolean sortNewestFirst = true;
    private boolean showOnlyMyComments = false;
    private List<Comment> originalComments = new ArrayList<>();

    public CommentViewModel(Application application) {
        super(application);
        commentRepository = CommentRepository.getInstance(application.getApplicationContext());
        userRepository = UserRepository.getInstance(application.getApplicationContext());
        comments = new MutableLiveData<>();
        error = new MutableLiveData<>();
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        userRepository.getCurrentUserLiveData().observeForever(user -> {
            if (user != null) {
                currentUserId = user.getId();
                applyFilters();
            }
        });
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
        loadComments();
    }

    public LiveData<List<Comment>> getComments() {
        return comments;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadComments() {
        if (taskId == null) {
            error.setValue("Task ID is required");
            return;
        }

        commentRepository.getTaskComments(taskId).observeForever(newComments -> {
            if (newComments != null) {
                originalComments = new ArrayList<>(newComments);
                applyFilters();
            }
        });

        commentRepository.getError().observeForever(errorMessage -> {
            if (errorMessage != null) {
                error.setValue(errorMessage);
            }
        });
    }

    public void setSortOrder(boolean newestFirst) {
        this.sortNewestFirst = newestFirst;
        applyFilters();
    }

    public void setShowOnlyMyComments(boolean showOnlyMyComments) {
        this.showOnlyMyComments = showOnlyMyComments;
        applyFilters();
    }

    private void applyFilters() {
        List<Comment> filteredComments = new ArrayList<>(originalComments);

        // Apply filter for my comments
        if (showOnlyMyComments && currentUserId != null) {
            filteredComments.removeIf(comment -> 
                !currentUserId.equals(comment.getUserId()));
        }

        // Apply sorting
        Collections.sort(filteredComments, (c1, c2) -> {
            int comparison = c1.getCreatedAt().compareTo(c2.getCreatedAt());
            return sortNewestFirst ? -comparison : comparison;
        });

        comments.setValue(filteredComments);
    }

    public void addComment(String content) {
        if (taskId == null) {
            error.setValue("Task ID is required");
            return;
        }

        commentRepository.addComment(taskId, content).observeForever(comment -> {
            if (comment != null) {
                originalComments.add(0, comment);
                applyFilters();
            }
        });

        commentRepository.getError().observeForever(errorMessage -> {
            if (errorMessage != null) {
                error.setValue(errorMessage);
            }
        });
    }

    public void deleteComment(String commentId) {
        commentRepository.deleteComment(commentId).observeForever(success -> {
            if (Boolean.TRUE.equals(success)) {
                originalComments.removeIf(comment -> comment.getId().equals(commentId));
                applyFilters();
            }
        });

        commentRepository.getError().observeForever(errorMessage -> {
            if (errorMessage != null) {
                error.setValue(errorMessage);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any observers if needed
    }
} 