package eu.unicate.retroauth;

import android.app.Activity;

public class AndroidAuthenticationHandler extends AuthenticationHandler {

	private final Activity activity;
	private final String accountType;
	private final String tokenType;

	public AndroidAuthenticationHandler(Activity activity, String accountType, String tokenType) {
		super(activity);
		this.activity = activity;
		this.accountType = accountType;
		this.tokenType = tokenType;
	}

	@Override
	public String getAccountType() {
		return accountType;
	}

	@Override
	public String getTokenType() {
		return tokenType;
	}
}
