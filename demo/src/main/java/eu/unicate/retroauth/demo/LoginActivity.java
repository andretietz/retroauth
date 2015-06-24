package eu.unicate.retroauth.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import eu.unicate.android.auth.AuthenticationActivity;

public class LoginActivity extends AuthenticationActivity {

	public static final String ACCOUNT_TYPE = "eu.unicate.retroauth.demo";
	public static final String TOKEN_TYPE = "default-token";
	private TextView textUser;
	private TextView textPass;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_login);

		textUser = (TextView) findViewById(R.id.textUser);
		textPass = (TextView) findViewById(R.id.textPassword);


		findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: do login request
				String token = "this will be a token";
				// TODO: do get userdata (name and additional stuff)
				String accountName = "unicate";
				Bundle userData = new Bundle();

				finalizeAuthentication(accountName, token, userData);
			}
		});
	}


}
