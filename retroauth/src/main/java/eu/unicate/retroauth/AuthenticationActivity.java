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

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


/**
 * Activity that creates the Account
 */
public abstract class AuthenticationActivity extends AppCompatActivity {

	private String accountType;
	private String accountName;
	private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
	private Bundle mResultBundle = null;

	/**
	 * Retrieves the AccountAuthenticatorResponse from either the intent of the icicle, if the
	 * icicle is non-zero.
	 *
	 * @param icicle the save instance data of this Activity, may be null
	 */
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Intent intent = getIntent();
		mAccountAuthenticatorResponse =
				intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
		if (mAccountAuthenticatorResponse != null) {
			mAccountAuthenticatorResponse.onRequestContinued();
		}
		accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
		accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
	}

	/**
	 * This method will finish the login process and add an account to
	 * the {@link AccountManager}
	 *
	 * @param accountName Name of the account owner
	 * @param tokenType   Type of the auth token provided by this login
	 * @param token       Token to store
	 * @param userData    Additional Userdata to store
	 */
	protected void finalizeAuthentication(@NonNull String accountName, @NonNull String tokenType, @NonNull String token, @Nullable Bundle userData) {
		AccountManager accountManager = AccountManager.get(this);
		Account account = getAccount(accountManager);
		mResultBundle = new Bundle();
		mResultBundle.putString(AccountManager.KEY_AUTHTOKEN, token);
		mResultBundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
		mResultBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		if (null == account) {
			mResultBundle.putParcelable(AccountManager.KEY_USERDATA, userData);
			account = new Account(accountName, accountType);
			accountManager.addAccountExplicitly(account, null, userData);
		}
		accountManager.setAuthToken(account, tokenType, token);
		finish();
	}

	private Account getAccount(AccountManager accountManager) {
		// if this is a relogin
		if (null != accountName) {
			Account[] accountList = accountManager.getAccountsByType(accountType);
			for (Account account : accountList) {
				if (account.name.equals(accountName))
					return account;
			}

		}
		return null;
	}

	/**
	 * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
	 */
	public void finish() {
		if (mAccountAuthenticatorResponse != null) {
			// send the result bundle back if set, otherwise send an error.
			if (mResultBundle != null) {
				mAccountAuthenticatorResponse.onResult(mResultBundle);
			} else {
				mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
						"canceled");
			}
			mAccountAuthenticatorResponse = null;
		} else {
			if (mResultBundle != null) {
				Intent intent = new Intent();
				intent.putExtras(mResultBundle);
				setResult(RESULT_OK, intent);
			} else {
				setResult(RESULT_CANCELED);
			}
		}
		super.finish();
	}

	/**
	 * When the login token is not valid anymore, but the account already exists
	 * this will return the account name of the user
	 *
	 * @return account name of the user
	 */
	@Nullable
	protected String getAccountName() {
		return accountName;
	}
}
