package com.andretietz.retroauth.demo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.andretietz.retroauth.AndroidAuthenticationHandler;
import com.andretietz.retroauth.AndroidTokenType;
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

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_ACCOUNT_CHOOSER = 123;
    private GoogleService service;
    private AuthAccountManager authAccountManager;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        authAccountManager = new AuthAccountManager(getApplication());

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
        Retrofit retrofit = new Retroauth.Builder<>(
                AndroidAuthenticationHandler.Companion.create(getApplication(), provider,
                        AndroidTokenType.Factory.Companion.create(getApplicationContext())))
                .baseUrl("https://www.googleapis.com/")
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

        provider.onRetrofitCreated(retrofit);

        /**
         * Create your API Service
         */
        service = retrofit.create(GoogleService.class);


        findViewById(R.id.buttonRequestEmail).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Use it!
                 */
                service.getUserInfo().enqueue(new Callback<GoogleService.Info>() {
                    @Override
                    public void onResponse(Call<GoogleService.Info> call, Response<GoogleService.Info> response) {
                        if (response.isSuccessful()) {
                            show("Hello: " + response.body().name);
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


        findViewById(R.id.buttonInvalidateToken).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Account activeAccount = authAccountManager
                        .getActiveAccount(getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT));
                if (activeAccount != null) {
                    // This is for demo purposes only. We're "manually" setting some-invalid-token.
                    AccountManager.get(MainActivity.this).setAuthToken(activeAccount,
                            getString(R.string.com_andretietz_retroauth_authentication_TOKEN), "some-invalid-token");
                }
            }
        });
        findViewById(R.id.buttonInvalidateRefreshToken).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Account activeAccount = authAccountManager
                        .getActiveAccount(getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT));
                if (activeAccount != null) {
                    // This is for demo purposes only. We're "manually" setting some-invalid-token. (refresh token)
                    AccountManager.get(MainActivity.this)
                            .setAuthToken(activeAccount,
                                    String.format("%s_refresh",
                                            getString(R.string.com_andretietz_retroauth_authentication_TOKEN)),
                                    "some-invalid-token");
                }
            }
        });

        findViewById(R.id.buttonSwitchAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // warning can be ignored when using own account type
                Intent intent = authAccountManager.newChooseAccountIntent(
                        getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT)
                );
                startActivityForResult(intent, RC_ACCOUNT_CHOOSER);
            }
        });

        findViewById(R.id.buttonAddAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authAccountManager.addAccount(
                        getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT),
                        getString(R.string.com_andretietz_retroauth_authentication_TOKEN));
            }
        });

        findViewById(R.id.buttonRemoveAccount).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                authAccountManager.removeActiveAccount(getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT));
            }
        });
    }

    private void show(String toShow) {
        Toast.makeText(this, toShow, Toast.LENGTH_SHORT).show();
    }

    private void showError(Throwable error) {
        Log.e(TAG, "An error occured: ", error);
        show(error.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_ACCOUNT_CHOOSER) {
                String accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                authAccountManager.setActiveAccount(accountType, accountName);
            }
        }
    }
}
