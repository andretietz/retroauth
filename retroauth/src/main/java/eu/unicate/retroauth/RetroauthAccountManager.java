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
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;

/**
 * This class wraps the Android AccountManager and adds some retroauth specific
 * functionality. This is the main helper class, when working with retroauth.
 * <p/>
 * <ul>
 *     <li>Creating an instance of this class:<br />
 *     Do it as with the Android AccountManager using {@link #get(Context)}. This is returning
 *     a singleton instance of this class, but using the given context
 *     </li>
 *     <li>Adding a users account:<br />
 *     Call {@link #addAccount(Activity, String, String)}
 *     </li>
 * </ul>
 */
public final class RetroauthAccountManager implements AuthAccountManager {

	private static final String RETROAUTH_ACCOUNTNAME_KEY = "retroauthActiveAccount";
	private static RetroauthAccountManager instance;
	private Context context;
	private AccountManager accountManager;

	private RetroauthAccountManager() {
	}

	/**
	 * @param context the Android Context
	 * @return singleton instance of the RetroauthAccountManager
	 */
	public static RetroauthAccountManager get(Context context) {
		if (instance == null) {
			instance = new RetroauthAccountManager();
		}
		instance.init(context, AccountManager.get(context));
		return instance;
	}

	/**
	 * This method will be mainly used for testing. Please use {@link RetroauthAccountManager#get(Context)} instead.
	 *
	 * @param context        the Android Context
	 * @param accountManager an AccountManager to use
	 * @return singleton instance of the RetroauthAccountManager
	 */
	public static RetroauthAccountManager get(Context context, AccountManager accountManager) {
		if (instance == null) {
			instance = new RetroauthAccountManager();
		}
		instance.init(context, accountManager);
		return instance;
	}

	/**
	 * initializes the class with a context and an AccountManager
	 * @param context        the Android Context
	 * @param accountManager an AccountManager to use
	 */
	private void init(Context context, AccountManager accountManager) {
		this.context = context;
		this.accountManager = accountManager;
	}

	/**
	 * Gets the currently active account by the account type. The active account name is determined
	 * by the method {@link RetroauthAccountManager#getActiveAccountName(String, boolean)}
	 *
	 * @param accountType Account Type you want to retreive
	 * @param showDialog  If there is more than one account and there is no
	 *                    current active account you can show an AlertDialog to
	 *                    let the user choose one. If you want to do so, set this to <code>true</code>
	 *                    else to <code>false</code>.
	 * @return the Active account or <code>null</code>
	 */
	@Nullable
	@Override
	public Account getActiveAccount(String accountType, boolean showDialog) {
		return getAccountByName(getActiveAccountName(accountType, showDialog), accountType);
	}

	/**
	 * Gets an account by the name of the account and it's type
	 *
	 * @param accountName Name of the Account you want to get
	 * @param accountType Account Type of which your account is
	 * @return The Account by Name or <code>null</code>
	 */
	@Nullable
	@Override
	public Account getAccountByName(String accountName, String accountType) {
		// if there's no name, there's no account
		if (accountName == null) return null;
		Account[] accounts = accountManager.getAccountsByType(accountType);
		if (accounts.length == 0) return null;
		if (accounts.length > 1) {
			for (Account account : accounts) {
				if (accountName.equals(account.name)) return account;
			}
			return null;
		}
		return accounts[0];
	}

	/**
	 * Get the currently active account name
	 *
	 * @param accountType Type of the Account you want the usernames from <code>null</code> for
	 *                    all types
	 * @param showDialog  If there is more than one account and there is no
	 *                    current active account you can show an AlertDialog to
	 *                    let the user choose one. If you want to do so, set this to <code>true</code>
	 *                    else to <code>false</code>.
	 * @return The currently active account name or <code>null</code>
	 */
	@Nullable
	@Override
	public String getActiveAccountName(String accountType, boolean showDialog) {
		Account[] accounts = accountManager.getAccountsByType(accountType);
		if (accounts.length < 1) {
			return null;
		} else if (accounts.length > 1) {
			// check if there is an account setup as current
			SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
			String accountName = preferences.getString(RETROAUTH_ACCOUNTNAME_KEY, null);
			if (accountName != null) {
				for (Account account : accounts) {
					if (accountName.equals(account.name)) return account.name;
				}
			}
		} else {
			return accounts[0].name;
		}
		return showDialog ? showAccountPickerDialog(accountType).toBlocking().first() : null;
	}

	/**
	 * Returns the Token of the currently active user
	 *
	 * @param accountType Account type of the user you want the token from
	 * @param tokenType   Token type of the token you want to retrieve
	 * @return The Token or <code>null</code>
	 */
	@Nullable
	@Override
	public String getTokenFromActiveUser(String accountType, String tokenType) {
		Account activeAccount = getActiveAccount(accountType, false);
		if (activeAccount == null) return null;
		return accountManager.peekAuthToken(activeAccount, tokenType);
	}

	/**
	 * Returns userdata which has to be setup while calling {@link AuthenticationActivity#finalizeAuthentication(String, String, String, Bundle)}
	 *
	 * @param accountType Account type to get the active account
	 * @param key         Key wiht which you want to request the value
	 * @return The Value or <code>null</code> if the account or the key does not exist
	 */
	@Override
	public String getUserData(String accountType, String key) {
		return accountManager.getUserData(getActiveAccount(accountType, false), key);
	}

	/**
	 * Invalidates the Token of the given type for the active user
	 *
	 * @param accountType Account type of the active user
	 * @param tokenType   Token type you want to invalidate
	 */
	@Override
	public void invalidateTokenFromActiveUser(String accountType, String tokenType) {
		String token = getTokenFromActiveUser(accountType, tokenType);
		if (token == null) return;
		accountManager.invalidateAuthToken(accountType, token);
	}

	/**
	 * Sets an active user. If you handle with multiple accounts you can setup an active user.
	 * The token of the active user will be taken for all future requests
	 *
	 * @param accountName name of the account
	 * @param accountType Account type of the active user
	 * @return the active account or <code>null</code> if the account could not be found
	 */
	@SuppressLint("CommitPrefEdits")
	@Override
	public Account setActiveUser(String accountName, String accountType) {
		SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
		preferences.edit().putString(RETROAUTH_ACCOUNTNAME_KEY, accountName).commit();
		return getAccountByName(accountName, accountType);
	}

	/**
	 * Unset the active user.
	 *
	 * @param accountType The account type where you want to unset it's current
	 */
	@SuppressLint("CommitPrefEdits")
	@Override
	public void resetActiveUser(String accountType) {
		SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
		preferences.edit().remove(RETROAUTH_ACCOUNTNAME_KEY).commit();
	}

	/**
	 * Starts the Activity to start the login process which adds the account.
	 *
	 * @param activity    The current active activity
	 * @param accountType The account type you want to create (this account type will be available on {@link AuthenticationActivity#getRequestedAccountType()} then
	 * @param tokenType   The tokentype you want to request. This is an optional parameter and can be <code>null</code> (this token type will be available on {@link AuthenticationActivity#getRequestedTokenType()} then
	 */
	@Override
	public void addAccount(@NonNull Activity activity, @NonNull String accountType, @Nullable String tokenType) {
		accountManager.addAccount(accountType, tokenType, null, null, activity, null, null);
	}


	/**
	 * Shows an account picker for the user to choose an account
	 *
	 * @param accountType     Account type of the accounts the user can choose
	 * @param onItemSelected  a listener to get a callback when the user selects on item
	 * @param onOkClicked     a listener for the click on the ok button
	 * @param onCancelClicked a listener for the click on the cancel button
	 * @param canAddAccount   if <code>true</code> the user has the option to add an account
	 * @return the accounts the user chooses from
	 */
	@Override
	public Account[] showAccountPickerDialog(String accountType, DialogInterface.OnClickListener onItemSelected, DialogInterface.OnClickListener onOkClicked, DialogInterface.OnClickListener onCancelClicked, boolean canAddAccount) {
		final Account[] accounts = accountManager.getAccountsByType(accountType);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final ArrayList<String> accountList = new ArrayList<>();
		for (Account account : accounts) {
			accountList.add(account.name);
		}
		if (canAddAccount)
			accountList.add(context.getString(R.string.add_account_button_label));
		builder.setTitle(context.getString(R.string.choose_account_label));
		builder.setSingleChoiceItems(accountList.toArray(new String[accountList.size()]), 0, onItemSelected);
		builder.setPositiveButton(android.R.string.ok, onOkClicked);
		builder.setNegativeButton(android.R.string.cancel, onCancelClicked);
		builder.show();
		return accounts;
	}

	/**
	 * Shows an account picker dialog to let the user choose an account
	 *
	 * @param accountType Account type of the accounts the user can choose
	 * @return an observable that emmits a string with the name of the account, the user chose or
	 * <code>null</code> if the current context was not an activity
	 */
	private Observable<String> showAccountPickerDialog(final String accountType) {
		final Account[] accounts = accountManager.getAccountsByType(accountType);
		return Observable.create(new Observable.OnSubscribe<String>() {
			int choosenAccount = 0;

			@Override
			public void call(final Subscriber<? super String> subscriber) {
				// make sure the context is an activity. in case of a service
				// this can and should not work
				if (context instanceof Activity) {
					showAccountPickerDialog(accountType,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									choosenAccount = which;
								}
							},
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (choosenAccount >= accounts.length) {
										subscriber.onNext(null);
									} else {
										setActiveUser(accounts[choosenAccount].name, accountType);
										SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
										preferences.edit().putString(RETROAUTH_ACCOUNTNAME_KEY, accounts[choosenAccount].name).apply();
										subscriber.onNext(accounts[choosenAccount].name);
									}
									subscriber.onCompleted();
								}
							},
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									subscriber.onError(new OperationCanceledException());
								}
							}, true);
				} else {
					subscriber.onNext(null);
				}
			}
		}).subscribeOn(AndroidScheduler.mainThread()); // dialogs have to run on the main thread
	}

	@Override
	public String getAuthToken(Account account, String accountType, String tokenType) throws Exception {
		AccountManagerFuture<Bundle> future;
		Activity activity = (context instanceof Activity) ? (Activity) context : null;
		if (account == null) {
			future = accountManager.addAccount(accountType, tokenType, null, null, activity, null, null);
		} else {
			future = accountManager.getAuthToken(account, tokenType, null, activity, null, null);
		}

		Bundle result = future.getResult();
		String token = result.getString(AccountManager.KEY_AUTHTOKEN);
		// even if the AuthenticationActivity set the KEY_AUTHTOKEN in the result bundle,
		// it got stripped out by the AccountManager
		if (token == null) {
			// try using the newly created account to peek the token
			token = accountManager.peekAuthToken(new Account(result.getString(AccountManager.KEY_ACCOUNT_NAME), result.getString(AccountManager.KEY_ACCOUNT_TYPE)), tokenType);
		}
		return token;
	}
}
