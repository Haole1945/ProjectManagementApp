package com.example.projectmanagement.models;

import java.util.List;

public class TaskListResponse {
    private String message;
    private List<Task> tasks;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
} 