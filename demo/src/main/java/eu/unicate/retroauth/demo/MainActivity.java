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

public class MainActivity extends AppCompatActivity {

	private AuthenticationService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
//				.setEndpoint("http://api.atekk.de/auth")
//				.setLogLevel(RestAdapter.LogLevel.FULL)
//				.setAuthHandler(new AndroidAuthenticationHandler(this, "eu.unicate.retroauth.demo"))
//				.build();
//		service = restAdapter.create(AuthenticationService.class);
//
		findViewById(R.id.buttonRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AccountManager.get(MainActivity.this).addAccount(LoginActivity.ACCOUNT_TYPE, "default", null, null, MainActivity.this, new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						try {
							Bundle bundle = future.getResult();
							bundle.containsKey("");
						} catch (OperationCanceledException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (AuthenticatorException e) {
							e.printStackTrace();
						}
					}
				}, null);
			}
		});


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}
