package eu.unicate.retroauth;

import android.app.Activity;

public abstract class AuthenticationHandler {

	private final Activity activity;

	protected AuthenticationHandler(Activity activity) {
		this.activity = activity;
	}

	public Activity getActivity() {
		return activity;
	}

	public abstract String getAccountType();

	public abstract String getTokenType();
}
