package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.example.projectmanagement.utils.SharedPreferencesManager;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView signUpLink;
    private ProgressBar progressBar;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private CheckBox rememberMeCheckBox;
    private UserRepository userRepository;
    private SharedPreferencesManager prefsManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = UserRepository.getInstance(requireContext());
        prefsManager = new SharedPreferencesManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailInput = view.findViewById(R.id.etEmail);
        passwordInput = view.findViewById(R.id.etPassword);
        loginButton = view.findViewById(R.id.btnLogin);
        signUpLink = view.findViewById(R.id.tvSignUp);
        progressBar = view.findViewById(R.id.progressBar);
        emailLayout = view.findViewById(R.id.tilEmail);
        passwordLayout = view.findViewById(R.id.tilPassword);
        rememberMeCheckBox = view.findViewById(R.id.cbRememberMe);

        // Auto-login logic removed from here as it's handled in MainActivity
        /*
        if (prefsManager.getRememberMe() && prefsManager.getAccessToken() != null) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_loginFragment_to_projectListFragment);
            return;
        }
        */

        loginButton.setOnClickListener(v -> {
            if (validateInputs()) {
                login();
            }
        });

        signUpLink.setOnClickListener(v -> 
            Navigation.findNavController(view).navigate(R.id.signUpFragment)
        );
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        showLoading(true);
        userRepository.login(email, password).observe(getViewLifecycleOwner(), user -> {
            showLoading(false);
            if (user != null) {
                prefsManager.setRememberMe(rememberMeCheckBox.isChecked());
                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                // Navigate to ProjectListFragment and pop the LoginFragment from the back stack
                Navigation.findNavController(requireView()).navigate(
                    R.id.projectListFragment, // Destination fragment
                    null, // No arguments
                    new androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.loginFragment, true) // Pop up to the loginFragment and make it inclusive
                        .build()
                );
            } else {
                Toast.makeText(requireContext(), 
                    "Invalid email or password", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        userRepository.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showLoading(false);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        emailInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
    }
} 