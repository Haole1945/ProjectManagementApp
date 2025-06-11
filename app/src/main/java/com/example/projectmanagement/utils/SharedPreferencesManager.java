package com.example.projectmanagement.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "ProjectManagementAppPrefs";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_USER_FULLNAME = "userFullname";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SharedPreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveTokens(String accessToken, String refreshToken) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public void saveAccessToken(String accessToken) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.apply();
    }

    public void saveRefreshToken(String refreshToken) {
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveUserDetail(String fullname, String email) {
        editor.putString(KEY_USER_FULLNAME, fullname);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserFullname() {
        return prefs.getString(KEY_USER_FULLNAME, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public void setRememberMe(boolean rememberMe) {
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    public boolean getRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public void clearUserData() {
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_USER_FULLNAME);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_REMEMBER_ME);
        editor.apply();
    }
} 