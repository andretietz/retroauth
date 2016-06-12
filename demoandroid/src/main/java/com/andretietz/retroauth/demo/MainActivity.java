package com.andretietz.retroauth.demo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.andretietz.retroauth.AndroidAuthenticationHandler;
import com.andretietz.retroauth.AuthAccountManager;
import com.andretietz.retroauth.Retroauth;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class MainActivity extends AppCompatActivity {

    private GoogleService service;
    private AuthAccountManager authAccountManager;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View buttonRequestEmail = findViewById(R.id.buttonRequestEmail);
        View buttonInvalidateToken = findViewById(R.id.buttonInvalidateToken);
        View buttonInvalidateRefreshToken = findViewById(R.id.buttonInvalidateRefreshToken);
        View buttonResetPrefAccount = findViewById(R.id.buttonResetPrefAccount);
        View buttonAddAccount = findViewById(R.id.buttonAddAccount);


        /**
         * Optional: create your own OkHttpClient
         */
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        ProviderGoogle provider = new ProviderGoogle();

        /**
         * Create your Retrofit Object using the {@link Retroauth.Builder}
         */
        Retrofit retrofit = new Retroauth.Builder<>(new AndroidAuthenticationHandler(provider))
                .baseUrl("https://www.googleapis.com/")
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

        provider.setRetrofit(retrofit);

        /**
         * Create your API Service
         */
        service = retrofit.create(GoogleService.class);


        buttonRequestEmail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Use it!
                 */
                service.getUserInfo().enqueue(new Callback<GoogleService.Info>() {
                    @Override
                    public void onResponse(Call<GoogleService.Info> call, Response<GoogleService.Info> response) {
                        if (response.isSuccessful()) {
                            show("Hallo: " + response.body().name);
                        } else {
                            show("Error: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<GoogleService.Info> call, Throwable t) {
                        showError(t);
                    }
                });
            }
        });

        authAccountManager = new AuthAccountManager();


        buttonInvalidateToken.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Account activeAccount = authAccountManager.getActiveAccount(GoogleService.ACCOUNT_TYPE);
                if (activeAccount != null) {
                    AccountManager.get(MainActivity.this).setAuthToken(activeAccount, GoogleService.TOKEN_TYPE, "some-invalid-token");
                }
            }
        });
        buttonInvalidateRefreshToken.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Account activeAccount = authAccountManager.getActiveAccount(GoogleService.ACCOUNT_TYPE);
                if (activeAccount != null) {
                    AccountManager.get(MainActivity.this)
                            .setAuthToken(activeAccount,
                                    String.format("%s_refresh", GoogleService.TOKEN_TYPE),
                                    "some-invalid-token");
                }
            }
        });

        buttonResetPrefAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authAccountManager.resetActiveAccount(GoogleService.ACCOUNT_TYPE);
            }
        });

        buttonAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authAccountManager.addAccount(MainActivity.this, GoogleService.ACCOUNT_TYPE, GoogleService.TOKEN_TYPE);
            }
        });
    }

    private void show(String toShow) {
        Toast.makeText(this, toShow, Toast.LENGTH_SHORT).show();
    }

    private void showError(Throwable error) {
        error.printStackTrace();
        show(error.toString());
    }
}
