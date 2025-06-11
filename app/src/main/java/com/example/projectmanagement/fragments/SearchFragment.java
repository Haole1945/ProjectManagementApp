package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projectmanagement.R;
import com.example.projectmanagement.adapters.SearchPagerAdapter;
import com.example.projectmanagement.adapters.SearchProjectAdapter;
import com.example.projectmanagement.adapters.SearchTaskAdapter;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.viewmodels.SearchViewModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

public class SearchFragment extends Fragment implements SearchProjectAdapter.OnProjectClickListener, SearchTaskAdapter.OnTaskClickListener {

    private SearchViewModel viewModel;
    private EditText etSearch;
    private com.google.android.material.textfield.TextInputLayout tilSearch;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private RecyclerView rvProjects;
    private RecyclerView rvTasks;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CircularProgressIndicator progressIndicator;
    private SearchProjectAdapter projectAdapter;
    private SearchTaskAdapter taskAdapter;
    private Map<String, Integer> projectProgressMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar(view);
        setupSearchView(view);
        setupTabLayout(view);
        setupRecyclerViews(view);
        setupSwipeRefresh(view);
        setupProgressIndicator(view);
        observeViewModel();
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());
    }

    private void setupSearchView(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        tilSearch = view.findViewById(R.id.tilSearch);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    viewModel.search(s.toString());
                }
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    viewModel.search(query);
                }
                return true;
            }
            return false;
        });

        tilSearch.setEndIconOnClickListener(v -> {
            etSearch.setText("");
            viewModel.clearSearch();
        });
    }

    private void setupTabLayout(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        // Create fragments for each tab
        SearchProjectsFragment projectsFragment = new SearchProjectsFragment();
        SearchTasksFragment tasksFragment = new SearchTasksFragment();

        // Set up ViewPager with fragments
        SearchPagerAdapter pagerAdapter = new SearchPagerAdapter(this);
        pagerAdapter.addFragment(projectsFragment, "Projects");
        pagerAdapter.addFragment(tasksFragment, "Tasks");
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Projects" : "Tasks");
        }).attach();
    }

    private void setupRecyclerViews(View view) {
        rvProjects = view.findViewById(R.id.rvProjects);
        rvTasks = view.findViewById(R.id.rvTasks);

        projectAdapter = new SearchProjectAdapter(this);
        taskAdapter = new SearchTaskAdapter(this);

        rvProjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));

        rvProjects.setAdapter(projectAdapter);
        rvTasks.setAdapter(taskAdapter);
    }

    private void setupSwipeRefresh(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                viewModel.search(query);
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setupProgressIndicator(View view) {
        progressIndicator = view.findViewById(R.id.progressIndicator);
    }

    private void observeViewModel() {
        viewModel.getProjects().observe(getViewLifecycleOwner(), projects -> {
            projectAdapter.updateProjects(projects);
            projectAdapter.setProjectProgressMap(projectProgressMap);
            updateEmptyState();
        });

        viewModel.getProjectProgressMap().observe(getViewLifecycleOwner(), progressMap -> {
            if (progressMap != null) {
                projectProgressMap = progressMap;
                projectAdapter.setProjectProgressMap(progressMap);
            }
        });

        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.updateTasks(tasks);
            updateEmptyState();
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateEmptyState() {
        boolean hasProjects = !viewModel.getProjects().getValue().isEmpty();
        boolean hasTasks = !viewModel.getTasks().getValue().isEmpty();
        boolean hasResults = hasProjects || hasTasks;

        // Update empty state visibility based on search results
        View emptyState = requireView().findViewById(R.id.emptyState);
        if (emptyState != null) {
            emptyState.setVisibility(hasResults ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onProjectClick(Project project) {
        Navigation.findNavController(requireView())
                .navigate(SearchFragmentDirections.actionSearchToProjectDetails(project.getId()));
    }

    @Override
    public void onTaskClick(Task task) {
        Navigation.findNavController(requireView())
                .navigate(SearchFragmentDirections.actionSearchToTaskDetails(task.getId()));
    }
} 