package com.andretietz.retroauth.demo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.andretietz.retroauth.AndroidAuthenticationHandler;
import com.andretietz.retroauth.AndroidTokenType;
import com.andretietz.retroauth.AuthAccountManager;
import com.andretietz.retroauth.Retroauth;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_ACCOUNT_CHOOSER = 123;
    private GithubService service;
    private AuthAccountManager authAccountManager;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.plant(new Timber.DebugTree());

        authAccountManager = new AuthAccountManager(getApplication());

        /**
         * Optional: create your own OkHttpClient
         */
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        ProviderGithub provider = new ProviderGithub();

        /**
         * Create your Retrofit Object using the {@link Retroauth.Builder}
         */
        Retrofit retrofit = new Retroauth.Builder<>(
                AndroidAuthenticationHandler.create(getApplication(), provider,
                        AndroidTokenType.Factory.create(getApplicationContext())))
                .baseUrl("https://api.github.com/")
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

//        provider.onRetrofitCreated(retrofit);

        /**
         * Create your API Service
         */
        service = retrofit.create(GithubService.class);


        findViewById(R.id.buttonRequestEmail).setOnClickListener(v -> {
            /**
             * Use it!
             */
            service.getEmails()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            item -> show(item.toString()),
                            Timber::e
                    );
        });


        findViewById(R.id.buttonInvalidateToken).setOnClickListener(v -> {
            Account activeAccount = authAccountManager
                    .getActiveAccount(getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT));
            if (activeAccount != null) {
                // This is for demo purposes only. We're "manually" setting some-invalid-token.
                AccountManager.get(MainActivity.this).setAuthToken(activeAccount,
                        getString(R.string.com_andretietz_retroauth_authentication_TOKEN), "some-invalid-token");
            }
        });
        findViewById(R.id.buttonInvalidateRefreshToken).setOnClickListener(v -> {
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
        });

        findViewById(R.id.buttonSwitchAccount).setOnClickListener(v -> {
            // warning can be ignored when using own account type
            Intent intent = authAccountManager.newChooseAccountIntent(
                    getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT)
            );
            startActivityForResult(intent, RC_ACCOUNT_CHOOSER);
        });

        findViewById(R.id.buttonAddAccount).setOnClickListener(v -> authAccountManager.addAccount(
                getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT),
                getString(R.string.com_andretietz_retroauth_authentication_TOKEN)));

        findViewById(R.id.buttonRemoveAccount).setOnClickListener(view ->
                authAccountManager.removeActiveAccount(getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT)));
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
