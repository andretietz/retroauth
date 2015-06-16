package eu.unicate.retroauth.demo;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.IOException;

import eu.unicate.retroauth.AndroidAuthenticationHandler;
import eu.unicate.retroauth.AuthRestAdapter;
import retrofit.RestAdapter;

public class MainActivity extends AppCompatActivity {

	private AuthenticationService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
				.setEndpoint("http://api.atekk.de/auth")
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.setAuthHandler(new AndroidAuthenticationHandler(this, LoginActivity.ACCOUNT_TYPE, LoginActivity.TOKEN_TYPE, 3))
				.build();
		service = restAdapter.create(AuthenticationService.class);
//
		findViewById(R.id.buttonRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				service.getProfile().subscribe();
//				AccountManager am = AccountManager.get(MainActivity.this);
//				AccountManagerFuture<Bundle> bundleAccountManagerFuture = am.addAccount(LoginActivity.ACCOUNT_TYPE, LoginActivity.TOKEN_TYPE, null, null, MainActivity.this, null, null);
			}
		});


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}
