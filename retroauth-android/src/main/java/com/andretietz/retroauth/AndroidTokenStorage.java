package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;

/**
 * Created by andre on 13/04/16.
 */
public class AndroidTokenStorage implements TokenStorage<AndroidTokenType, String> {

	private final AccountManager accountManager;
	private final AuthAccountManager authAccountManager;

	public AndroidTokenStorage(Activity activity) {
		ContextManager contextManager = ContextManager.get(activity);
		accountManager = AccountManager.get(contextManager.getContext());
		authAccountManager = new AuthAccountManager(contextManager);
	}


	@Override
	public void removeToken(AndroidTokenType type) {
		accountManager.invalidateAuthToken(type.accountType, getToken(type));
	}

	@Override
	public void saveToken(AndroidTokenType type, String token) {
		// this has been done in login activity already
	}

	@Override
	public String getToken(AndroidTokenType type) {
		try {
			Account account = authAccountManager.getActiveAccount(type.accountType, false);
			return accountManager.peekAuthToken(account, type.tokenType);
		} catch (Exception e) {
			// TODO improve
		}
		return null;
	}
}
