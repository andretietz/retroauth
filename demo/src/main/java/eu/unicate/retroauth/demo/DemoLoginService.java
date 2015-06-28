package eu.unicate.retroauth.demo;

import android.content.Context;

import eu.unicate.retroauth.AuthenticationService;

public class DemoLoginService extends AuthenticationService {
	@Override
	public String getLoginAction(Context context) {
		return context.getString(R.string.authentication_action);
	}
}
