package com.example.projectmanagement.models;

import java.util.List;

public class ProjectListResponse {
    private String message;
    private List<Project> projects;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
} 