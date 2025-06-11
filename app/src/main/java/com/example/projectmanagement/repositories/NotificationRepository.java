//package com.example.projectmanagement.repositories;
//
//import com.example.projectmanagement.models.Notification;
//import com.example.projectmanagement.api.ApiService;
//import com.example.projectmanagement.api.ApiClient;
//
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class NotificationRepository {
//    private final ApiService apiService;
//
//    public interface Callback<T> {
//        void onSuccess(T data);
//        void onError(String message);
//    }
//
//    public NotificationRepository() {
//        apiService = ApiClient.getClient().create(ApiService.class);
//    }
//
//    public void getNotifications(Callback<List<Notification>> callback) {
//        apiService.getNotifications().enqueue(new Callback<List<Notification>>() {
//            @Override
//            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    callback.onSuccess(response.body());
//                } else {
//                    callback.onError("Failed to load notifications");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Notification>> call, Throwable t) {
//                callback.onError(t.getMessage());
//            }
//        });
//    }
//
//    public void markAsRead(String notificationId, Callback<Void> callback) {
//        apiService.markNotificationAsRead(notificationId).enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.isSuccessful()) {
//                    callback.onSuccess(null);
//                } else {
//                    callback.onError("Failed to mark notification as read");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                callback.onError(t.getMessage());
//            }
//        });
//    }
//
//    public void markAllAsRead(Callback<Void> callback) {
//        apiService.markAllNotificationsAsRead().enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.isSuccessful()) {
//                    callback.onSuccess(null);
//                } else {
//                    callback.onError("Failed to mark all notifications as read");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                callback.onError(t.getMessage());
//            }
//        });
//    }
//}