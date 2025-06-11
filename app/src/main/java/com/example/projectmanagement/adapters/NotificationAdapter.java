//package com.example.projectmanagement.adapters;
//
//import android.text.format.DateUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.projectmanagement.R;
//import com.example.projectmanagement.databinding.ItemNotificationBinding;
//import com.example.projectmanagement.models.Notification;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
//
//    private final List<Notification> notifications = new ArrayList<>();
//    private final OnNotificationClickListener listener;
//
//    public interface OnNotificationClickListener {
//        void onNotificationClick(Notification notification);
//    }
//
//    public NotificationAdapter(OnNotificationClickListener listener) {
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
//            LayoutInflater.from(parent.getContext()),
//            parent,
//            false
//        );
//        return new NotificationViewHolder(binding);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
//        holder.bind(notifications.get(position));
//    }
//
//    @Override
//    public int getItemCount() {
//        return notifications.size();
//    }
//
//    public void updateNotifications(List<Notification> newNotifications) {
//        notifications.clear();
//        notifications.addAll(newNotifications);
//        notifyDataSetChanged();
//    }
//
//    public void markAsRead(String notificationId) {
//        for (int i = 0; i < notifications.size(); i++) {
//            Notification notification = notifications.get(i);
//            if (notification.getId().equals(notificationId)) {
//                notification.setRead(true);
//                notifyItemChanged(i);
//                break;
//            }
//        }
//    }
//
//    public void markAllAsRead() {
//        for (Notification notification : notifications) {
//            notification.setRead(true);
//        }
//        notifyDataSetChanged();
//    }
//
//    class NotificationViewHolder extends RecyclerView.ViewHolder {
//        private final ItemNotificationBinding binding;
//
//        NotificationViewHolder(ItemNotificationBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//        }
//
//        void bind(Notification notification) {
//            binding.textTitle.setText(notification.getTitle());
//            binding.textContent.setText(notification.getContent());
//            binding.textTime.setText(DateUtils.getRelativeTimeSpanString(
//                notification.getCreatedAt().getTime(),
//                System.currentTimeMillis(),
//                DateUtils.MINUTE_IN_MILLIS
//            ));
//            binding.viewUnread.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
//
//            // Set icon based on notification type
//            int iconResId;
//            switch (notification.getType()) {
//                case "task_comment":
//                    iconResId = R.drawable.ic_comment;
//                    break;
//                case "task_assigned":
//                    iconResId = R.drawable.ic_assignment;
//                    break;
//                case "task_status":
//                    iconResId = R.drawable.ic_status;
//                    break;
//                case "project_update":
//                    iconResId = R.drawable.ic_project;
//                    break;
//                default:
//                    iconResId = R.drawable.ic_notification;
//            }
//            binding.imageIcon.setImageResource(iconResId);
//
//            // Set click listener
//            itemView.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onNotificationClick(notification);
//                }
//            });
//        }
//    }
//}