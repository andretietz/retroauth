package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;

import retrofit.RetrofitError;

public class AndroidAuthenticationHandler implements AuthenticationHandler {

	private static final String DEFAULT_TOKENTYPE = "eu.unicate.retroauth.token.default";

	private static final int MAX_RETRIES = 3;
	private static final int HTTP_UNAUTHORIZED = 401;

	private final int maxRetries;
	private final Activity activity;
	private final String accountType;
	private final String tokenType;

	public AndroidAuthenticationHandler(Activity activity, String accountType) {
		this(activity, accountType, DEFAULT_TOKENTYPE, MAX_RETRIES);

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
		AccountManagerFuture<Bundle> future = accountManager.addAccount(accountType, tokenType, null, null, activity, null, null);
		try {
			future.getResult();
		} catch (OperationCanceledException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AuthenticatorException e) {
			e.printStackTrace();
		}
		throw new NoAccountAvailableException();
	}

	@Override
	public boolean retry(int count, Throwable error) {
		if(error instanceof NoAccountAvailableException) {
			return false;
		} else
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
