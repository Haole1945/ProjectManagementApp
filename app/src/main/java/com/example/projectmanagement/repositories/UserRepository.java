package com.example.projectmanagement.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.models.LoginRequest;
import com.example.projectmanagement.models.LoginResponse;
import com.example.projectmanagement.models.User;
import com.example.projectmanagement.utils.SharedPreferencesManager;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Map;
import java.util.HashMap;

public class UserRepository {
    private final ApiService apiService;
    private static UserRepository instance;
    private final MutableLiveData<User> currentUser;
    private final MutableLiveData<String> error;
    private SharedPreferencesManager prefsManager;

    private UserRepository(Context context) {
        apiService = ApiService.getInstance(context);
        currentUser = new MutableLiveData<>();
        error = new MutableLiveData<>();
        prefsManager = new SharedPreferencesManager(context);
        // The auto-login and user state persistence is now handled by LoginFragment
        // and SharedPreferencesManager directly. No need to load current user here.
    }

    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context);
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        currentUser.setValue(user);
        if (user != null) {
            prefsManager.saveUserDetail(user.getFullname(), user.getEmail());
        } else {
            prefsManager.clearUserData();
        }
    }

    public LiveData<User> updateProfile(String userId, User user, File imageFile) {
        MutableLiveData<User> result = new MutableLiveData<>();
        
        // Create multipart request for image if provided
        MultipartBody.Part imagePart = null;
        if (imageFile != null) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
        }

        // Create request body for user data
        RequestBody userPart = RequestBody.create(
            MediaType.parse("application/json"),
            new com.google.gson.Gson().toJson(user)
        );

        apiService.updateProfile(userId, imagePart, user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser.setValue(response.body());
                    prefsManager.saveUserDetail(response.body().getFullname(), response.body().getEmail()); // Save updated user details
                    result.setValue(response.body());
                } else {
                    error.setValue("Failed to update profile");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
        return result;
    }

    public LiveData<User> updateAccount(String userId, User user) {
        MutableLiveData<User> result = new MutableLiveData<>();
        apiService.updateAccount(userId, user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser.setValue(response.body());
                    prefsManager.saveUserDetail(response.body().getFullname(), response.body().getEmail()); // Save updated user details
                    result.setValue(response.body());
                } else {
                    error.setValue("Failed to update account");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
        return result;
    }

    public Call<Void> passwordForgot(String email) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        return apiService.passwordForgot(body);
    }

    public Call<Void> passwordOtp(String email, String otp) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);
        return apiService.passwordOtp(body);
    }

    public Call<Void> passwordReset(String email, String otp, String newPassword) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);
        body.put("newPassword", newPassword);
        return apiService.passwordReset(body);
    }

    public LiveData<String> getError() {
        return error;
    }

    public User getCurrentUser() {
        return currentUser.getValue();
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUser;
    }

    public String getAccessToken() {
        return prefsManager.getAccessToken();
    }

    public void clearCurrentUser() {
        currentUser.setValue(null);
        prefsManager.clearUserData(); // Clear user data from SharedPreferences on logout
    }

    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> result = new MutableLiveData<>();
        String token = prefsManager.getAccessToken();
        if (token == null) {
            error.setValue("Access token not available. Please login again.");
            result.setValue(null);
            return result;
        }
        apiService.getAllUsers("Bearer " + token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    error.setValue("Failed to load users: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
        return result;
    }

    public LiveData<User> signUp(String email, String password, String fullname) {
        MutableLiveData<User> result = new MutableLiveData<>();
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFullname(fullname);
        apiService.signUp(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                    // No need to save user on sign up, only after successful login
                } else {
                    error.setValue("Sign up failed: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
        return result;
    }

    public LiveData<User> login(String email, String password) {
        MutableLiveData<User> result = new MutableLiveData<>();
        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    User loggedInUser = response.body().getUser();
                    currentUser.setValue(loggedInUser);
                    prefsManager.saveUserDetail(loggedInUser.getFullname(), loggedInUser.getEmail()); // Save user on successful login
                    prefsManager.saveTokens(response.body().getAccessToken(), response.body().getRefreshToken());
                    result.setValue(loggedInUser);
                } else {
                    error.setValue("Login failed: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
        return result;
    }
} 