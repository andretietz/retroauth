package eu.unicate.retroauth.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import eu.unicate.retroauth.AuthenticationActivity;

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
				// do the login
				String token = demoLogin(textUser.getText().toString(), textPass.getText().toString());
				if (null != token) {
					finalizeAuthentication(textUser.getText().toString(), getString(R.string.auth_token_type), token, null);
				}
			}
		});

		String username = getAccountName();
		if (null != username) {
			textUser.setEnabled(false);
			textUser.setText(username);
		}
	}

	private String demoLogin(String username, String password) {
		if (errorCheck(username, textUser, "Don't leave the username empty!"))
			return null;
		if (errorCheck(password, textPass, "Don't leave the password empty!"))
			return null;
		if ("test".equalsIgnoreCase(password))
			return "this-is-a-demo-token-from-user: " + username;
		return null;
	}

	private boolean errorCheck(String s, TextView tv, String message) {
		boolean error = TextUtils.isEmpty(s);
		tv.setError(error ? message : null);
		tv.requestFocus();
		return error;
	}
}
