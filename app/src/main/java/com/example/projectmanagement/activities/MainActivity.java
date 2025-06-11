package com.example.projectmanagement.activities;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.projectmanagement.R;
import com.example.projectmanagement.repositories.UserRepository;
import com.example.projectmanagement.utils.SharedPreferencesManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import androidx.navigation.NavGraph;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private SharedPreferencesManager prefsManager;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        prefsManager = new SharedPreferencesManager(this);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);

            if (prefsManager.getRememberMe() && prefsManager.getAccessToken() != null) {
                navGraph.setStartDestination(R.id.projectListFragment);
            } else {
                navGraph.setStartDestination(R.id.loginFragment);
            }
            navController.setGraph(navGraph);

            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (navController != null) {
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            bottomNavigationView.setOnItemSelectedListener(item -> {
                Log.d(TAG, "Item selected: " + item.getTitle() + ", ID: " + item.getItemId());
                return NavigationUI.onNavDestinationSelected(item, navController);
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.signUpFragment) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        }
    }
} 