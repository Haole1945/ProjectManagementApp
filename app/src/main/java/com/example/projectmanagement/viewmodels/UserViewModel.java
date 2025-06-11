package com.example.projectmanagement.viewmodels;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.example.projectmanagement.models.User;
import com.example.projectmanagement.repositories.UserRepository;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository repository;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> error;
    private final MutableLiveData<Boolean> passwordResetSuccess;
    private final MutableLiveData<List<User>> availableUsers;

    public UserViewModel(Application application) {
        super(application);
        repository = UserRepository.getInstance(application.getApplicationContext());
        isLoading = new MutableLiveData<>(false);
        error = new MutableLiveData<>();
        passwordResetSuccess = new MutableLiveData<>();
        availableUsers = new MutableLiveData<>();
    }

    public LiveData<User> getCurrentUser() {
        return repository.getCurrentUserLiveData();
    }

    public void setCurrentUser(User user) {
        repository.setCurrentUser(user);
    }

    public LiveData<User> updateProfile(String userId, String fullName, String email, String phone, File avatarFile) {
        isLoading.setValue(true);
        User currentUser = repository.getCurrentUser();
        if (currentUser == null) {
            error.setValue("No user profile found");
            isLoading.setValue(false);
            return null;
        }

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setFullname(fullName);
        updatedUser.setEmail(email);
        updatedUser.setPhone(phone);
        updatedUser.setAvatar(currentUser.getAvatar());

        return repository.updateProfile(userId, updatedUser, avatarFile);
    }

    public LiveData<User> updateAccount(String userId, String fullName, String email, String phone) {
        isLoading.setValue(true);
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setFullname(fullName);
        updatedUser.setEmail(email);
        updatedUser.setPhone(phone);

        return repository.updateAccount(userId, updatedUser);
    }

    public LiveData<User> signUp(String email, String password, String fullName) {
        isLoading.setValue(true);
        MutableLiveData<User> result = new MutableLiveData<>();
        repository.signUp(email, password, fullName).observeForever(user -> {
            isLoading.setValue(false);
            if (user != null) {
                result.setValue(user);
            } else {
                error.setValue("Sign up failed");
            }
        });
        return result;
    }

    public LiveData<Boolean> passwordForgot(String email) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);
        repository.passwordForgot(email).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    result.setValue(true);
                } else {
                    error.setValue("Failed to send password reset email: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
        return result;
    }

    public LiveData<Boolean> verifyOtp(String email, String otp) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);

        repository.passwordOtp(email, otp).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    result.setValue(true);
                } else {
                    error.setValue("Invalid OTP: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });

        return result;
    }

    public LiveData<Boolean> resetPassword(String email, String otp, String newPassword) {
        isLoading.setValue(true);

        repository.passwordReset(email, otp, newPassword).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    passwordResetSuccess.setValue(true);
                } else {
                    error.setValue("Failed to reset password: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });

        return passwordResetSuccess;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void logout() {
        repository.clearCurrentUser();
    }


    public boolean validatePassword(String password) {
        // Password must be at least 8 characters long
        if (password.length() < 8) {
            return false;
        }

        // Check for at least one uppercase letter
        boolean hasUppercase = false;
        // Check for at least one lowercase letter
        boolean hasLowercase = false;
        // Check for at least one number
        boolean hasNumber = false;
        // Check for at least one special character
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            else if (Character.isLowerCase(c)) hasLowercase = true;
            else if (Character.isDigit(c)) hasNumber = true;
            else hasSpecial = true;
        }

        return hasUppercase && hasLowercase && hasNumber && hasSpecial;
    }

    public LiveData<List<User>> getAvailableUsers() {
        return availableUsers;
    }

    public void loadAvailableUsers() {
        isLoading.setValue(true);
        repository.getAllUsers().observeForever(users -> {
            isLoading.setValue(false);
            if (users != null) {
                availableUsers.setValue(users);
            } else {
                error.setValue("Failed to load users");
            }
        });
    }

    public LiveData<User> login(String email, String password) {
        isLoading.setValue(true);
        return repository.login(email, password);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any observers if needed
    }
} 