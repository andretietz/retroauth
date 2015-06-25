package eu.unicate.retroauth.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import eu.unicate.android.auth.AuthenticationActivity;

public class LoginActivity extends AuthenticationActivity {

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
				String accountName = textUser.getText().toString();

				finalizeAuthentication(accountName, token, null);
			}
		});

		String username = getAccountName();
		if (null != username) {
			textUser.setText(username);
		}

	}


}
