package com.example.projectmanagement.models;

public class SubTask {
    private String sub_title;
    private Boolean isChecked;

    public SubTask(String sub_title, Boolean isChecked) {
        this.sub_title = sub_title;
        this.isChecked = isChecked;
    }

    public String getSub_title() {
        return sub_title;
    }

    public void setSub_title(String sub_title) {
        this.sub_title = sub_title;
    }

    public Boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Boolean checked) {
        isChecked = checked;
    }
} 