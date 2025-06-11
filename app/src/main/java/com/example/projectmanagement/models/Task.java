package com.example.projectmanagement.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class Task {
    @SerializedName("_id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("startDate")
    private Date startDate;
    @SerializedName("endDate")
    private Date endDate;
    @SerializedName("status")
    private String status;
    @SerializedName("completed")
    private boolean completed;

    @SerializedName("authorUserId")
    private User authorUserId;
    @SerializedName("assigneeUserId")
    private User assigneeUserId;

    @SerializedName("project")
    private Project project;
    @SerializedName("comments")
    private List<Comment> comments;
    @SerializedName("dueDate")
    private Date dueDate;
    @SerializedName("priority")
    private String priority;
    @SerializedName("tags")
    private String tags;
    @SerializedName("sub_tasks")
    private List<SubTask> sub_tasks;
    @SerializedName("projectId")
    private String projectId;
    @SerializedName("createdAt")
    private Date createdAt;
    @SerializedName("updatedAt")
    private Date updatedAt;

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public User getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(User authorUserId) {
        this.authorUserId = authorUserId;
    }

    public User getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(User assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<SubTask> getSub_tasks() {
        return sub_tasks;
    }

    public void setSub_tasks(List<SubTask> sub_tasks) {
        this.sub_tasks = sub_tasks;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}