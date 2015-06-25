package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import eu.unicate.retroauth.AccountAuthenticator;

public abstract class AuthenticationActivity extends AppCompatActivity {

	private String accountType;
	private String accountName;
	private String tokenType;
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
		tokenType = intent.getStringExtra(AccountAuthenticator.KEY_TOKEN_TYPE);
	}

	/**
	 * This method will finish the login process and add an account to
	 * the {@link AccountManager}
	 *
	 * @param accountName Name of the account owner
	 * @param token       Token to store
	 * @param userData    Additional Userdata to store
	 */
	protected void finalizeAuthentication(@NonNull String accountName, @NonNull String token, @Nullable Bundle userData) {
		mResultBundle = new Bundle();
		mResultBundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
		mResultBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		mResultBundle.putParcelable(AccountManager.KEY_USERDATA, userData);
		AccountManager accountManager = AccountManager.get(this);
		Account account = new Account(accountName, accountType);
		accountManager.addAccountExplicitly(account, null, userData);
		accountManager.setAuthToken(account, tokenType, token);
		finish();
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
				// TODO: send back the bundle
				setResult(RESULT_OK, null);
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
