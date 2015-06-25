package eu.unicate.retroauth.demo;

import android.content.Context;

import eu.unicate.retroauth.AuthService;

public class ExampleLoginService extends AuthService {
	@Override
	public String getLoginAction(Context context) {
		return context.getString(R.string.authentication_action);
	}
}
