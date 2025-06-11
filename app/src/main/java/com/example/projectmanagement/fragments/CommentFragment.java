package com.example.projectmanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.R;
import com.example.projectmanagement.adapters.CommentAdapter;
import com.example.projectmanagement.databinding.FragmentCommentBinding;
import com.example.projectmanagement.models.Comment;
import com.example.projectmanagement.repositories.CommentRepository;
import com.example.projectmanagement.viewmodels.CommentViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class CommentFragment extends Fragment {
    private FragmentCommentBinding binding;
    private CommentViewModel viewModel;
    private CommentAdapter adapter;
    private String taskId;
    private CommentRepository commentRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        commentRepository = CommentRepository.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCommentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get task ID from arguments
        if (getArguments() != null) {
            taskId = getArguments().getString("taskId");
            if (taskId == null) {
                showError("Task ID is required");
                navigateBack();
                return;
            }
        }

        setupViewModel();
        setupRecyclerView();
        setupListeners();
        loadComments();
        observeViewModel();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CommentViewModel.class);
        viewModel.setTaskId(taskId);
    }

    private void setupRecyclerView() {
        adapter = new CommentAdapter(new ArrayList<>());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        // Set up swipe to delete
        adapter.setOnCommentLongClickListener((comment, position) -> {
            showDeleteCommentDialog(comment);
            return true;
        });
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> navigateBack());

        binding.fabAddComment.setOnClickListener(v -> {
            String content = binding.editTextComment.getText().toString().trim();
            if (content.isEmpty()) {
                binding.editTextComment.setError("Comment cannot be empty");
                return;
            }
            addComment(content);
        });

        // Observe error messages
        commentRepository.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
            }
        });
    }

    private void loadComments() {
        binding.progressBar.setVisibility(View.VISIBLE);
        commentRepository.getTaskComments(taskId).observe(getViewLifecycleOwner(), comments -> {
            binding.progressBar.setVisibility(View.GONE);
            if (comments != null) {
                adapter.updateComments(comments);
                binding.emptyView.setVisibility(comments.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getComments().observe(getViewLifecycleOwner(), comments -> {
            if (comments != null) {
                adapter.updateComments(comments);
                binding.emptyView.setVisibility(comments.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
            }
        });
    }

    private void addComment(String content) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.fabAddComment.setEnabled(false);
        binding.editTextComment.setEnabled(false);

        commentRepository.addComment(taskId, content).observe(getViewLifecycleOwner(), comment -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.fabAddComment.setEnabled(true);
            binding.editTextComment.setEnabled(true);

            if (comment != null) {
                binding.editTextComment.setText("");
                Snackbar.make(binding.getRoot(), "Comment added successfully", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteCommentDialog(Comment comment) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> deleteComment(comment))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteComment(Comment comment) {
        binding.progressBar.setVisibility(View.VISIBLE);
        commentRepository.deleteComment(comment.getId()).observe(getViewLifecycleOwner(), success -> {
            binding.progressBar.setVisibility(View.GONE);
            if (Boolean.TRUE.equals(success)) {
                Snackbar.make(binding.getRoot(), "Comment deleted successfully", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private void navigateBack() {
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_comment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_refresh) {
            loadComments();
            return true;
        } else if (itemId == R.id.sort_newest) {
            viewModel.setSortOrder(true);
            return true;
        } else if (itemId == R.id.sort_oldest) {
            viewModel.setSortOrder(false);
            return true;
        } else if (itemId == R.id.filter_all) {
            viewModel.setShowOnlyMyComments(false);
            return true;
        } else if (itemId == R.id.filter_my_comments) {
            viewModel.setShowOnlyMyComments(true);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 