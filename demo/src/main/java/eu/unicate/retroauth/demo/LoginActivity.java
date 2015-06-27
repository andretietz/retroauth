package eu.unicate.retroauth.demo;

import android.os.Bundle;
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
				if(null == token) {
					textUser.setError("Wrong credentials");
				} else {
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
		if("testuser".equals(username) && "testpassword".equals(password))
			return "This is a demo token";
		return null;
	}
}
