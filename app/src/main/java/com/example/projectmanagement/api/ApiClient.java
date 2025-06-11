package com.example.projectmanagement.api;

import android.content.Context;
import android.util.Log;
import com.example.projectmanagement.models.LoginResponse;
import com.example.projectmanagement.utils.SharedPreferencesManager;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:3000";
    private static Retrofit retrofit = null;
    private static final String TAG = "ApiClient";

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor authInterceptor = chain -> {
                Request.Builder requestBuilder = chain.request().newBuilder();
                SharedPreferencesManager prefsManager = new SharedPreferencesManager(context);
                String accessToken = prefsManager.getAccessToken();
                if (accessToken != null && !accessToken.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
                }
                return chain.proceed(requestBuilder.build());
            };

            Authenticator tokenAuthenticator = (Route route, Response response) -> {
                if (response.request().header("Authorization") == null) {
                    return null; // Nothing to authenticate
                }

                SharedPreferencesManager prefsManager = new SharedPreferencesManager(context);
                String refreshToken = prefsManager.getRefreshToken();

                if (refreshToken == null || refreshToken.isEmpty()) {
                    Log.e(TAG, "Refresh token not available. Logging out.");
                    // Clear user data and indicate need to re-login
                    prefsManager.clearUserData();
                    return null; // Will cause the original request to fail with 401
                }

                Log.d(TAG, "Attempting to refresh token...");
                AuthService authService = ApiClient.getAuthService(context);
                try {
                    retrofit2.Response<LoginResponse> refreshResponse = authService.refreshToken("Bearer " + refreshToken).execute();

                    if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                        LoginResponse loginResponse = refreshResponse.body();
                        String newAccessToken = loginResponse.getAccessToken();
                        String newRefreshToken = loginResponse.getRefreshToken();

                        prefsManager.saveAccessToken(newAccessToken);
                        prefsManager.saveRefreshToken(newRefreshToken);

                        Log.d(TAG, "Token refreshed successfully. Retrying request.");
                        return response.request().newBuilder()
                                .header("Authorization", "Bearer " + newAccessToken)
                                .build();
                    } else {
                        Log.e(TAG, "Failed to refresh token: " + refreshResponse.message());
                        if (refreshResponse.errorBody() != null) {
                            Log.e(TAG, "Refresh Error Body: " + refreshResponse.errorBody().string());
                        }
                        prefsManager.clearUserData(); // Invalidate tokens, force re-login
                        return null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Token refresh network error: " + e.getMessage(), e);
                    prefsManager.clearUserData(); // Invalidate tokens, force re-login
                    return null;
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .authenticator(tokenAuthenticator)
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }

    // Separate Retrofit instance for auth service to avoid circular dependency
    private static Retrofit authRetrofit = null;

    public static AuthService getAuthService(Context context) {
        if (authRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient authClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            authRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(authClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authRetrofit.create(AuthService.class);
    }
} 