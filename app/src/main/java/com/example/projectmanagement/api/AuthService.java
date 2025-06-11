package com.example.projectmanagement.api;

import com.example.projectmanagement.models.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthService {
    @POST("auth/refresh-token")
    Call<LoginResponse> refreshToken(@Header("Authorization") String refreshToken);
} 