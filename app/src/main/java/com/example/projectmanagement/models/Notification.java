//package com.example.projectmanagement.models;
//
//import com.google.gson.annotations.SerializedName;
//
//import java.util.Date;
//
//public class Notification {
//    @SerializedName("id")
//    private String id;
//
//    @SerializedName("title")
//    private String title;
//
//    @SerializedName("content")
//    private String content;
//
//    @SerializedName("type")
//    private String type;
//
//    @SerializedName("projectId")
//    private String projectId;
//
//    @SerializedName("taskId")
//    private String taskId;
//
//    @SerializedName("isRead")
//    private boolean isRead;
//
//    @SerializedName("createdAt")
//    private Date createdAt;
//
//    @SerializedName("updatedAt")
//    private Date updatedAt;
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public String getProjectId() {
//        return projectId;
//    }
//
//    public void setProjectId(String projectId) {
//        this.projectId = projectId;
//    }
//
//    public String getTaskId() {
//        return taskId;
//    }
//
//    public void setTaskId(String taskId) {
//        this.taskId = taskId;
//    }
//
//    public boolean isRead() {
//        return isRead;
//    }
//
//    public void setRead(boolean read) {
//        isRead = read;
//    }
//
//    public Date getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(Date createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public Date getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(Date updatedAt) {
//        this.updatedAt = updatedAt;
//    }
//
//    public static class Builder {
//        private final Notification notification;
//
//        public Builder() {
//            notification = new Notification();
//        }
//
//        public Builder id(String id) {
//            notification.id = id;
//            return this;
//        }
//
//        public Builder title(String title) {
//            notification.title = title;
//            return this;
//        }
//
//        public Builder content(String content) {
//            notification.content = content;
//            return this;
//        }
//
//        public Builder type(String type) {
//            notification.type = type;
//            return this;
//        }
//
//        public Builder projectId(String projectId) {
//            notification.projectId = projectId;
//            return this;
//        }
//
//        public Builder taskId(String taskId) {
//            notification.taskId = taskId;
//            return this;
//        }
//
//        public Builder isRead(boolean isRead) {
//            notification.isRead = isRead;
//            return this;
//        }
//
//        public Builder createdAt(Date createdAt) {
//            notification.createdAt = createdAt;
//            return this;
//        }
//
//        public Builder updatedAt(Date updatedAt) {
//            notification.updatedAt = updatedAt;
//            return this;
//        }
//
//        public Notification build() {
//            return notification;
//        }
//    }
//
//    public static Builder builder() {
//        return new Builder();
//    }
//}