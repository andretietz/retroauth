package eu.unicate.retroauth.demo;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

public class TokenAccountAuthenticator extends AbstractAccountAuthenticator {

	private Context context;
	private Class<? extends AppCompatActivity> loginActivityClass;

	public TokenAccountAuthenticator(Context context, Class<? extends AppCompatActivity> loginActivity) {
		super(context);
		this.context = context;
		this.loginActivityClass = loginActivity;
	}



	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
//		final Intent intent = new Intent(context, loginActivityClass);
//		intent.putExtra(AbstractAuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType);
//		intent.putExtra(AbstractAuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
//		intent.putExtra(AbstractAuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
//		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
//		final Bundle bundle = new Bundle();
//		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
//		return bundle;
		return createLoginBundle(response, accountType, authTokenType);
	}


	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		final AccountManager am = AccountManager.get(context);
		// try to get the auth token
		String authToken = am.peekAuthToken(account, authTokenType);

		// If we get an authToken - we return it
		if (!TextUtils.isEmpty(authToken)) {
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
			result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
			return result;
		}

		// If we get here, then we couldn't access the user's password - so we
		// need to re-prompt them for their credentials. We do that by creating
		// an intent to display our AuthenticatorActivity.
		return createLoginBundle(response, account.type, authTokenType);

	}

	private Bundle createLoginBundle(AccountAuthenticatorResponse response, String accountType, String tokenType) {
		final Intent intent = new Intent(context, loginActivityClass);
		if(null != response) intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

		intent.putExtra(AbstractAuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType);
		intent.putExtra(AbstractAuthenticatorActivity.ARG_AUTH_TYPE, tokenType);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
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

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
		return null;
	}
}
