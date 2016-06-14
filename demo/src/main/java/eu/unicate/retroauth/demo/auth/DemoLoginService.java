package eu.unicate.retroauth.demo.auth;

import android.content.Context;

import eu.unicate.retroauth.AuthenticationService;
import eu.unicate.retroauth.demo.R;

public class DemoLoginService extends AuthenticationService {
	@Override
	public String getLoginAction(Context context) {
		return context.getString(R.string.authentication_action);
	}
}
