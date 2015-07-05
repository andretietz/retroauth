/*
 * Copyright (c) 2015 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.unicate.retroauth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * This is a the AccountAuthenticator
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {

	private final String action;

	/**
	 * This creates the AccountAuthenticator
	 *
	 * @param context The context (needed by the {@link AbstractAccountAuthenticator}
	 * @param action  The Action String to open the Activity to login
	 */
	public AccountAuthenticator(Context context, String action) {
		super(context);
		this.action = action;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		return createAuthBundle(response, accountType, null);
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		return createAuthBundle(response, account.type, account.name);
	}

	/**
	 * Creates an Intent to open the Activity to login
	 *
	 * @param response    needed parameter
	 * @param accountType The account Type
	 * @param accountName The name of the account
	 * @return a bundle to open the activity
	 */
	private Bundle createAuthBundle(AccountAuthenticatorResponse response, String accountType, String accountName) {
		Intent intent = new Intent(action);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		if (null != accountName) {
			intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName);
		}
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		return null;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
		return null;
	}
}
