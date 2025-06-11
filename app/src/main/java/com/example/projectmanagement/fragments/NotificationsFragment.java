//package com.example.projectmanagement.fragments;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.navigation.Navigation;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import com.example.projectmanagement.R;
//import com.example.projectmanagement.adapters.NotificationAdapter;
//import com.example.projectmanagement.databinding.FragmentNotificationsBinding;
//import com.example.projectmanagement.models.Notification;
//import com.example.projectmanagement.viewmodels.NotificationViewModel;
//import com.google.android.material.dialog.MaterialAlertDialogBuilder;
//import com.google.android.material.snackbar.Snackbar;
//
//public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {
//
//    private FragmentNotificationsBinding binding;
//    private NotificationViewModel notificationViewModel;
//    private NotificationAdapter adapter;
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        setupToolbar();
//        setupRecyclerView();
//        setupSwipeRefresh();
//        setupFab();
//        observeNotifications();
//    }
//
//    private void setupToolbar() {
//        binding.toolbar.setNavigationOnClickListener(v -> {
//            Navigation.findNavController(requireView()).navigateUp();
//        });
//    }
//
//    private void setupRecyclerView() {
//        adapter = new NotificationAdapter(this);
//        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
//        binding.recyclerViewNotifications.setAdapter(adapter);
//    }
//
//    private void setupSwipeRefresh() {
//        binding.swipeRefresh.setOnRefreshListener(() -> {
//            notificationViewModel.refreshNotifications();
//        });
//    }
//
//    private void setupFab() {
//        binding.fabMarkAllRead.setOnClickListener(v -> showMarkAllReadDialog());
//    }
//
//    private void observeNotifications() {
//        notificationViewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
//            adapter.updateNotifications(notifications);
//            binding.swipeRefresh.setRefreshing(false);
//            binding.progressBar.setVisibility(View.GONE);
//            binding.recyclerViewNotifications.setVisibility(notifications.isEmpty() ? View.GONE : View.VISIBLE);
//            binding.textEmpty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
//        });
//
//        notificationViewModel.getError().observe(getViewLifecycleOwner(), error -> {
//            if (error != null) {
//                binding.swipeRefresh.setRefreshing(false);
//                binding.progressBar.setVisibility(View.GONE);
//                showSnackbar(error);
//            }
//        });
//    }
//
//    private void showMarkAllReadDialog() {
//        new MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Mark All as Read")
//            .setMessage("Are you sure you want to mark all notifications as read?")
//            .setPositiveButton("Mark All Read", (dialog, which) -> {
//                notificationViewModel.markAllAsRead();
//            })
//            .setNegativeButton("Cancel", null)
//            .show();
//    }
//
//    @Override
//    public void onNotificationClick(Notification notification) {
//        if (!notification.isRead()) {
//            notificationViewModel.markAsRead(notification.getId());
//        }
//
//        // Navigate based on notification type
//        switch (notification.getType()) {
//            case "task_comment":
//            case "task_assigned":
//            case "task_status":
//                if (notification.getTaskId() != null) {
//                    Bundle args = new Bundle();
//                    args.putString("taskId", notification.getTaskId());
//                    Navigation.findNavController(requireView())
//                        .navigate(R.id.action_notificationsFragment_to_taskDetailFragment, args);
//                }
//                break;
//            case "project_update":
//                if (notification.getProjectId() != null) {
//                    Bundle args = new Bundle();
//                    args.putString("projectId", notification.getProjectId());
//                    Navigation.findNavController(requireView())
//                        .navigate(R.id.action_notificationsFragment_to_projectDetailFragment, args);
//                }
//                break;
//        }
//    }
//
//    private void showSnackbar(String message) {
//        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//}