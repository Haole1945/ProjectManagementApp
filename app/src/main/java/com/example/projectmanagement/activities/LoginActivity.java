package com.example.projectmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectmanagement.R;
import com.example.projectmanagement.api.ApiClient;
import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.models.LoginRequest;
import com.example.projectmanagement.models.LoginResponse;
import com.example.projectmanagement.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.projectmanagement.activities.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ApiService apiService;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Initialize API service and SharedPreferences
        apiService = ApiClient.getClient(this).create(ApiService.class);
        prefsManager = new SharedPreferencesManager(this);

        // Check if user is already logged in (This logic should ideally be in LoginFragment or a splash screen)
        if (prefsManager.getAccessToken() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Set up click listeners
        btnLogin.setOnClickListener(v -> handleLogin());
        findViewById(R.id.tvSignUp).setOnClickListener(v -> {
            // TODO: Navigate to SignUpActivity
        });
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            // TODO: Navigate to ForgotPasswordActivity
        });
    }

    private void handleLogin() {
        // Validate input
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            return;
        }

        // Clear errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Make API call
        LoginRequest loginRequest = new LoginRequest(email, password);
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    LoginResponse loginResponse = response.body();
                    // Save tokens and user data
                    prefsManager.saveTokens(loginResponse.getAccessToken(), loginResponse.getRefreshToken());
                    prefsManager.saveUserDetail(loginResponse.getUser().getFullname(), loginResponse.getUser().getEmail());

                    // Navigate to MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this,
                        "Login failed: " + response.message(),
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                Toast.makeText(LoginActivity.this,
                    "Login failed: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
} 