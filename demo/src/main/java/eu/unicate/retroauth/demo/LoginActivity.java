package eu.unicate.retroauth.demo;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AccountAuthenticatorActivity {

	public static final String ACCOUNT_TYPE = "eu.unicate.retroauth.demo";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent res = new Intent();
				res.putExtra(AccountManager.KEY_ACCOUNT_NAME, "unicate");
				res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
				res.putExtra(AccountManager.KEY_AUTHTOKEN, "test");
				setAccountAuthenticatorResult(res.getExtras());
				finish();

			}
		});
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
