package eu.unicate.android.auth;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by André on 24.06.2015.
 */
public class AuthenticationActivity extends AppCompatActivity {

	private String accountType;
	private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
	private Bundle mResultBundle = null;

	/**
	 * Set the result that is to be sent as the result of the request that caused this
	 * Activity to be launched. If result is null or this method is never called then
	 * the request will be canceled.
	 *
	 * @param result this is returned as the result of the AbstractAccountAuthenticator request
	 */
	public final void setAccountAuthenticatorResult(Bundle result) {
		mResultBundle = result;
	}

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
	}

	protected void finalizeAuthentication(String accountName, String token, Bundle userData) {
		mResultBundle = new Bundle();
		mResultBundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
		mResultBundle.putString(AccountManager.KEY_AUTHTOKEN, token);
		mResultBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		mResultBundle.putParcelable(AccountManager.KEY_USERDATA, userData);
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
		}
		super.finish();
	}
}
