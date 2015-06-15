package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;

import retrofit.RetrofitError;

public class AndroidAuthenticationHandler implements AuthenticationHandler {


	private static final int MAX_RETRIES = 3;
	private static final int HTTP_UNAUTHORIZED = 401;

	private final int maxRetries;
	private final Activity activity;
	private final String accountType;
	private final String tokenType;

	public AndroidAuthenticationHandler(Activity activity, String accountType, String tokenType) {
		this(activity, accountType, tokenType, MAX_RETRIES);

	}

	public AndroidAuthenticationHandler(Activity activity, String accountType, String tokenType, int maxRetries) {
		this.maxRetries = maxRetries;
		this.activity = activity;
		this.accountType = accountType;
		this.tokenType = tokenType;
	}

	@Override
	public Account getAccount() {
		AccountManager accountManager = AccountManager.get(activity);
		Account[] accounts = accountManager.getAccountsByType(accountType);
		if(accounts.length >= 1) {
			return accounts[0];
		}
		accountManager.addAccount(accountType, tokenType, null, null, activity, null, null);
		return null;
	}

	@Override
	public boolean retry(int count, Throwable error) {
		if (error instanceof RetrofitError) {
			int status = ((RetrofitError) error).getResponse().getStatus();
			if (HTTP_UNAUTHORIZED == status
					&& count < maxRetries) {
				// TODO: some re-authentication work
				return true;
			}
		}
		return false;
	}
}
