package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;

import retrofit.RetrofitError;

public class AndroidAuthenticationHandler extends AuthenticationHandler {

	private static final int MAX_RETRIES = 3;


	private final int maxRetries;
	private final Activity activity;
	private final String accountType;
	private final String tokenType;

	public AndroidAuthenticationHandler(Activity activity, String accountType, String tokenType, int maxRetries) {
		super(activity);
		this.maxRetries = maxRetries;
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
