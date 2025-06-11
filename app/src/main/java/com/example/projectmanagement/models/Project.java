package com.example.projectmanagement.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class Project {
    @SerializedName("_id")
    private String id;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    @SerializedName("status")
    private String status;
    private int percentCompleted;
    @SerializedName("authorUserId")
    private User owner;
    @SerializedName("membersId")
    private List<User> members;
    private List<Task> tasks;
    private Date createdAt;
    private Date updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getPercentCompleted() {
        return percentCompleted;
    }

    public void setPercentCompleted(int percentCompleted) {
        this.percentCompleted = percentCompleted;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
} 
