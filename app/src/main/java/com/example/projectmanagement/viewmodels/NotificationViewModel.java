//package com.example.projectmanagement.viewmodels;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.projectmanagement.models.Notification;
//import com.example.projectmanagement.repositories.NotificationRepository;
//
//import java.util.List;
//
//public class NotificationViewModel extends ViewModel {
//
//    private final NotificationRepository repository;
//    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();
//    private final MutableLiveData<String> error = new MutableLiveData<>();
//
//    public NotificationViewModel() {
//        repository = new NotificationRepository();
//        loadNotifications();
//    }
//
//    public LiveData<List<Notification>> getNotifications() {
//        return notifications;
//    }
//
//    public LiveData<String> getError() {
//        return error;
//    }
//
//    public void loadNotifications() {
//        repository.getNotifications(new NotificationRepository.Callback<List<Notification>>() {
//            @Override
//            public void onSuccess(List<Notification> data) {
//                notifications.postValue(data);
//                error.postValue(null);
//            }
//
//            @Override
//            public void onError(String message) {
//                error.postValue(message);
//            }
//        });
//    }
//
//    public void refreshNotifications() {
//        loadNotifications();
//    }
//
//    public void markAsRead(String notificationId) {
//        repository.markAsRead(notificationId, new NotificationRepository.Callback<Void>() {
//            @Override
//            public void onSuccess(Void data) {
//                // Update local list
//                List<Notification> currentList = notifications.getValue();
//                if (currentList != null) {
//                    for (Notification notification : currentList) {
//                        if (notification.getId().equals(notificationId)) {
//                            notification.setRead(true);
//                            break;
//                        }
//                    }
//                    notifications.postValue(currentList);
//                }
//            }
//
//            @Override
//            public void onError(String message) {
//                error.postValue(message);
//            }
//        });
//    }
//
//    public void markAllAsRead() {
//        repository.markAllAsRead(new NotificationRepository.Callback<Void>() {
//            @Override
//            public void onSuccess(Void data) {
//                // Update local list
//                List<Notification> currentList = notifications.getValue();
//                if (currentList != null) {
//                    for (Notification notification : currentList) {
//                        notification.setRead(true);
//                    }
//                    notifications.postValue(currentList);
//                }
//            }
//
//            @Override
//            public void onError(String message) {
//                error.postValue(message);
//            }
//        });
//    }
//}