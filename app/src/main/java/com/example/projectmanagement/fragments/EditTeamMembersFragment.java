package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.R;
import com.example.projectmanagement.api.ApiService;
import com.example.projectmanagement.adapters.TeamMemberAdapter;
import com.example.projectmanagement.adapters.SelectedMembersAdapter;
import com.example.projectmanagement.databinding.FragmentEditTeamMembersBinding;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.User;
import com.example.projectmanagement.models.UserSearchResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTeamMembersFragment extends Fragment implements TeamMemberAdapter.OnRemoveMemberListener, SelectedMembersAdapter.OnRemoveMemberListener {

    private FragmentEditTeamMembersBinding binding;
    private Project currentProject;
    private ApiService apiService;
    private TeamMemberAdapter currentMembersAdapter;
    private SelectedMembersAdapter selectedMembersAdapter;
    private ArrayAdapter<String> searchAdapter;
    private List<User> searchResults = new ArrayList<>();
    private List<User> selectedMembers = new ArrayList<>();

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiService.getInstance(requireContext());
        if (getArguments() != null) {
            String projectJson = getArguments().getString("project");
            if (projectJson != null) {
                currentProject = new Gson().fromJson(projectJson, Project.class);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditTeamMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupCurrentMembersRecyclerView();
        setupSelectedMembersRecyclerView();
        setupSearch();
        loadCurrentMembers();
        setupClickListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    private void setupCurrentMembersRecyclerView() {
        currentMembersAdapter = new TeamMemberAdapter(new ArrayList<>(), this);
        binding.rvCurrentMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCurrentMembers.setAdapter(currentMembersAdapter);
    }

    private void setupSelectedMembersRecyclerView() {
        selectedMembersAdapter = new SelectedMembersAdapter(selectedMembers, this);
        binding.rvSelectedMembers.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvSelectedMembers.setAdapter(selectedMembersAdapter);
        updateSelectedMembersVisibility();
    }

    private void loadCurrentMembers() {
        if (currentProject != null && currentProject.getMembers() != null) {
            List<User> combinedMembers = new ArrayList<>();
            if (currentProject.getOwner() != null) {
                currentProject.getOwner().setOwner(true);
                combinedMembers.add(currentProject.getOwner());
            }
            combinedMembers.addAll(currentProject.getMembers());
            currentMembersAdapter.updateMembers(combinedMembers);
        }
    }

    private void setupSearch() {
        searchAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        binding.actvSearchMember.setAdapter(searchAdapter);
        binding.actvSearchMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 2) {
                    searchRunnable = () -> performSearch(s.toString());
                    handler.postDelayed(searchRunnable, 300);
                } else {
                    searchAdapter.clear();
                    searchAdapter.notifyDataSetChanged();
                }
            }
        });

        binding.actvSearchMember.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEmail = (String) parent.getItemAtPosition(position);
            Log.d("EditTeamMembersFragment", "Item clicked: " + selectedEmail);
            User selectedUser = findUserByEmail(selectedEmail);
            if (selectedUser != null) {
                Log.d("EditTeamMembersFragment", "Selected User found: " + selectedUser.getEmail());
                if (!isMemberAlreadySelected(selectedUser) && !isMemberAlreadyInProject(selectedUser)) {
                    Log.d("EditTeamMembersFragment", "Before adding, selectedMembers size: " + selectedMembers.size());
                    selectedMembers.add(selectedUser);
                    Log.d("EditTeamMembersFragment", "User added to selectedMembers: " + selectedUser.getEmail() + ", current total: " + selectedMembers.size());
                    Log.d("EditTeamMembersFragment", "Selected members list content: " + selectedMembers.toString());
                    selectedMembersAdapter.updateSelectedMembers();
                    updateSelectedMembersVisibility();
                } else {
                    Log.d("EditTeamMembersFragment", "User not added to selectedMembers: " + selectedUser.getEmail() + " (already selected or in project)");
                    Toast.makeText(requireContext(), "Selected user is already in the project or already selected.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w("EditTeamMembersFragment", "Selected User not found for email: " + selectedEmail);
            }
            binding.actvSearchMember.setText(""); // Clear search field
            searchAdapter.clear(); // Clear suggestions after selection
            searchAdapter.notifyDataSetChanged();
        });
    }

    private boolean isMemberAlreadySelected(User user) {
        for (User selected : selectedMembers) {
            Log.d("EditTeamMembersFragment", "Checking isMemberAlreadySelected: Comparing " + user.getEmail() + " with selected: " + selected.getEmail());
            if (selected.getId().equals(user.getId())) {
                Log.d("EditTeamMembersFragment", user.getEmail() + " is already selected.");
                return true;
            }
        }
        Log.d("EditTeamMembersFragment", user.getEmail() + " is NOT already selected.");
        return false;
    }

    private boolean isMemberAlreadyInProject(User user) {
        if (currentProject != null && currentProject.getMembers() != null) {
            for (User projectMember : currentProject.getMembers()) {
                Log.d("EditTeamMembersFragment", "Checking isMemberAlreadyInProject: Comparing " + user.getEmail() + " with project member: " + projectMember.getEmail());
                if (projectMember.getId().equals(user.getId())) {
                    Log.d("EditTeamMembersFragment", user.getEmail() + " is already in project.");
                    return true;
                }
            }
            if (currentProject.getOwner() != null && currentProject.getOwner().getId().equals(user.getId())) {
                Log.d("EditTeamMembersFragment", "Checking isMemberAlreadyInProject: Comparing " + user.getEmail() + " with project owner: " + currentProject.getOwner().getEmail());
                Log.d("EditTeamMembersFragment", user.getEmail() + " is already project owner.");
                return true;
            }
        }
        Log.d("EditTeamMembersFragment", user.getEmail() + " is NOT already in project.");
        return false;
    }

    private void performSearch(String query) {
        if (currentProject == null || currentProject.getId() == null) {
            Toast.makeText(requireContext(), "Project not loaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        Log.d("EditTeamMembersFragment", "ProgressBar VISIBLE for search.");
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("search", query);
        apiService.searchAddMemberToProject(currentProject.getId(), requestBody).enqueue(new Callback<UserSearchResponse>() {
            @Override
            public void onResponse(Call<UserSearchResponse> call, Response<UserSearchResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                Log.d("EditTeamMembersFragment", "ProgressBar GONE for search (response).");
                if (response.isSuccessful() && response.body() != null) {
                    searchResults = response.body().getUsers();
                    List<String> emails = new ArrayList<>();
                    for (User user : searchResults) {
                        if (!isMemberAlreadySelected(user) && !isMemberAlreadyInProject(user)) {
                            emails.add(user.getEmail());
                        }
                    }
                    searchAdapter.clear();
                    searchAdapter.addAll(emails);
                    searchAdapter.notifyDataSetChanged();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e("EditTeamMembersFragment", "Search failed: " + response.code() + ", Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e("EditTeamMembersFragment", "Error parsing search error body: " + e.getMessage());
                    }
                    Toast.makeText(requireContext(), "Search failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserSearchResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.d("EditTeamMembersFragment", "ProgressBar GONE for search (failure).");
                Log.e("EditTeamMembersFragment", "Error searching: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Error searching: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private User findUserByEmail(String email) {
        for (User user : searchResults) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    private void addMultipleMembersToProject(List<User> membersToInvite) {
        if (currentProject == null || currentProject.getId() == null || membersToInvite.isEmpty()) {
            Toast.makeText(requireContext(), "No members selected or project not loaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        List<String> memberIds = new ArrayList<>();
        for (User user : membersToInvite) {
            memberIds.add(user.getId());
        }

        int totalInvites = memberIds.size();
        final int[] invitesSent = {0};

        for (String memberId : memberIds) {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("memberId", memberId);
            apiService.addMemberToProject(currentProject.getId(), requestBody).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    invitesSent[0]++;
                    if (response.isSuccessful()) {
                        // No need for individual toasts here, will give a summary
                    } else {
                        String errorMessage = "Failed to invite member.";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e("EditTeamMembersFragment", "Invitation failed: " + response.code() + ", Error Body: " + errorBody);
                                errorMessage = "Failed to invite member: " + response.message() + " - " + errorBody;
                            } else {
                                Log.e("EditTeamMembersFragment", "Invitation failed: " + response.code() + ", No error body.");
                            }
                        } catch (Exception e) {
                            Log.e("EditTeamMembersFragment", "Error parsing invitation error body: " + e.getMessage());
                        }
                        // You can log this or collect errors to show a summary at the end
                    }

                    if (invitesSent[0] == totalInvites) {
                        binding.progressBar.setVisibility(View.GONE);
                        Log.d("EditTeamMembersFragment", "ProgressBar GONE for invitation (response).");
                        Toast.makeText(requireContext(), totalInvites + " invitation(s) sent.", Toast.LENGTH_SHORT).show();
                        selectedMembers.clear();
                        selectedMembersAdapter.updateSelectedMembers();
                        updateSelectedMembersVisibility();
                        fetchProjectDetails();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    invitesSent[0]++;
                    Log.e("EditTeamMembersFragment", "Error inviting member: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), "Error inviting member: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    if (invitesSent[0] == totalInvites) {
                        binding.progressBar.setVisibility(View.GONE);
                        Log.d("EditTeamMembersFragment", "ProgressBar GONE for invitation (failure).");
                        selectedMembers.clear();
                        selectedMembersAdapter.updateSelectedMembers();
                        updateSelectedMembersVisibility();
                        fetchProjectDetails();
                    }
                }
            });
        }
    }

    private void fetchProjectDetails() {
        if (currentProject == null || currentProject.getId() == null) return;

        apiService.getProject(currentProject.getId()).enqueue(new Callback<Project>() {
            @Override
            public void onResponse(Call<Project> call, Response<Project> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProject = response.body();
                    loadCurrentMembers();
                } else {
                    Toast.makeText(requireContext(), "Failed to refresh project details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Project> call, Throwable t) {
                Toast.makeText(requireContext(), "Error refreshing project details: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnAddSelectedMembers.setOnClickListener(v -> {
            addMultipleMembersToProject(new ArrayList<>(selectedMembers));
        });
    }

    @Override
    public void onRemoveCurrentProjectMember(User member) {
        Toast.makeText(requireContext(), "Remove current project member: " + member.getFullname(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveMember(User member) {
        selectedMembers.remove(member);
        selectedMembersAdapter.updateSelectedMembers();
        updateSelectedMembersVisibility();
        Toast.makeText(requireContext(), member.getEmail() + " removed from selection.", Toast.LENGTH_SHORT).show();
    }

    private void updateSelectedMembersVisibility() {
        Log.d("EditTeamMembersFragment", "updateSelectedMembersVisibility called. selectedMembers size: " + selectedMembers.size());
        if (selectedMembers.isEmpty()) {
            binding.tvSelectedMembersLabel.setVisibility(View.GONE);
            binding.rvSelectedMembers.setVisibility(View.GONE);
            binding.btnAddSelectedMembers.setVisibility(View.GONE);
        } else {
            binding.tvSelectedMembersLabel.setVisibility(View.VISIBLE);
            binding.rvSelectedMembers.setVisibility(View.VISIBLE);
            binding.btnAddSelectedMembers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        handler.removeCallbacksAndMessages(null);
    }

    private static class ErrorResponse {
        private String message;
        public String getMessage() { return message; }
    }
} 