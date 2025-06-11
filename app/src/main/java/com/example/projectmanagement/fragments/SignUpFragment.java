package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.projectmanagement.R;
import com.example.projectmanagement.models.User;
import com.example.projectmanagement.repositories.UserRepository;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpFragment extends Fragment {
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button signUpButton;
    private TextView loginLink;
    private ProgressBar progressBar;
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private UserRepository userRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = UserRepository.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameInput = view.findViewById(R.id.etFullName);
        emailInput = view.findViewById(R.id.etEmail);
        passwordInput = view.findViewById(R.id.etPassword);
        confirmPasswordInput = view.findViewById(R.id.etConfirmPassword);
        signUpButton = view.findViewById(R.id.btnSignUp);
        loginLink = view.findViewById(R.id.tvLogin);
        progressBar = view.findViewById(R.id.progressBar);
        nameLayout = view.findViewById(R.id.tilFullName);
        emailLayout = view.findViewById(R.id.tilEmail);
        passwordLayout = view.findViewById(R.id.tilPassword);
        confirmPasswordLayout = view.findViewById(R.id.tilConfirmPassword);

        signUpButton.setOnClickListener(v -> {
            if (validateInputs()) {
                signUp();
            }
        });

        loginLink.setOnClickListener(v -> 
            Navigation.findNavController(view).navigate(R.id.loginFragment)
        );
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameLayout.setError("Name is required");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email format");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Please confirm your password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return isValid;
    }

    private void signUp() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        showLoading(true);
        userRepository.signUp(email, password, name).observe(getViewLifecycleOwner(), user -> {
            showLoading(false);
            if (user != null) {
                Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView())
                    .navigate(R.id.loginFragment);
            } else {
                Toast.makeText(requireContext(), 
                    "Registration failed. Please try again.", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Observe error messages
        userRepository.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showLoading(false);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        signUpButton.setEnabled(!isLoading);
        nameInput.setEnabled(!isLoading);
        emailInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
        confirmPasswordInput.setEnabled(!isLoading);
    }
} 