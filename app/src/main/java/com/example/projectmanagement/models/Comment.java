package com.example.projectmanagement.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Comment {
    @SerializedName("id")
    private String id;

    @SerializedName("content")
    private String content;

    @SerializedName("taskId")
    private String taskId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("user")
    private User user;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Builder pattern for easier object creation
    public static class Builder {
        private final Comment comment;

        public Builder() {
            comment = new Comment();
        }

        public Builder id(String id) {
            comment.setId(id);
            return this;
        }

        public Builder content(String content) {
            comment.setContent(content);
            return this;
        }

        public Builder taskId(String taskId) {
            comment.setTaskId(taskId);
            return this;
        }

        public Builder userId(String userId) {
            comment.setUserId(userId);
            return this;
        }

        public Builder user(User user) {
            comment.setUser(user);
            return this;
        }

        public Builder createdAt(Date createdAt) {
            comment.setCreatedAt(createdAt);
            return this;
        }

        public Builder updatedAt(Date updatedAt) {
            comment.setUpdatedAt(updatedAt);
            return this;
        }

        public Comment build() {
            return comment;
        }
    }

    // Factory method for creating a new comment
    public static Builder builder() {
        return new Builder();
    }
} 