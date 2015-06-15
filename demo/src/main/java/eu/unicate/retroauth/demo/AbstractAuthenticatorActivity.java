package eu.unicate.retroauth.demo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by a.tietz on 09.06.15.
 */
public abstract class AbstractAuthenticatorActivity extends AppCompatActivity {

	public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
	public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
	public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
	public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

	private AccountManager accountManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountManager = AccountManager.get(this);
	}

//	private void finishLogin(Intent intent) {
//		String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
//		String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
//		final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
//		if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
//			String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
//			String authtokenType = mAuthTokenType;
//			// Creating the account on the device and setting the auth token we got
//			// (Not setting the auth token will cause another call to the server to authenticate the user)
//			mAccountManager.addAccountExplicitly(account, accountPassword, null);
//			mAccountManager.setAuthToken(account, authtokenType, authtoken);
//		} else {
//			mAccountManager.setPassword(account, accountPassword);
//		}
//		setAccountAuthenticatorResult(intent.getExtras());
//		setResult(RESULT_OK, intent);
//		finish();
//	}

	public abstract String[] getTokenTypes();

	public Account[] getAccounts() {
		return accountManager.getAccountsByType("TYPE");
	}
}
