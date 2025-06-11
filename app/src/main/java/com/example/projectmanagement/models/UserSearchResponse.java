package com.example.projectmanagement.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserSearchResponse {
    @SerializedName("users")
    private List<User> users;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
} 