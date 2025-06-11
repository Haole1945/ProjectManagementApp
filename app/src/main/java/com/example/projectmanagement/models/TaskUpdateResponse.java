package com.example.projectmanagement.models;

import com.google.gson.annotations.SerializedName;

public class TaskUpdateResponse {
    @SerializedName("message")
    private String message;
    @SerializedName("task")
    private Task task;

    public String getMessage() {
        return message;
    }

    public Task getTask() {
        return task;
    }
} 