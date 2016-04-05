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

package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


/**
 * Activity that creates the Account.
 */
public abstract class AuthenticationActivity extends AppCompatActivity {

    private String accountType;
    private String tokenType;
    private String accountName;
    private AccountAuthenticatorResponse accountAuthenticatorResponse = null;
    private Bundle resultBundle = null;

    private AccountManager accountManager;

    /**
     * Retrieves the AccountAuthenticatorResponse from either the intent of the icicle, if the
     * icicle is non-zero.
     *
     * @param icicle the save instance data of this Activity, may be null
     */
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        accountAuthenticatorResponse =
                intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (accountAuthenticatorResponse != null) {
            accountAuthenticatorResponse.onRequestContinued();
        }
        accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        if (accountType == null)
            throw new RuntimeException(
                    String.format(
                            "This Activity cannot be started without the \"%s\" extra in the intent!",
                            AccountManager.KEY_ACCOUNT_TYPE));
        accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        tokenType = intent.getStringExtra(AccountAuthenticator.KEY_TOKEN_TYPE);
        accountManager = AccountManager.get(this);


        resultBundle = new Bundle();
        resultBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
    }

    /**
     * This method stores an authentication Token to a specific account.
     *
     * @param account Account you want to store the token for
     * @param token   Token itself
     */
    @SuppressWarnings("unused")
    protected void storeToken(@NonNull Account account, @NonNull String token) {
        storeToken(account, token, null);
    }

    /**
     * This method stores an authentication Token to a specific account.
     *
     * @param account      Account you want to store the token for
     * @param token        Token itself
     * @param refreshToken a refresh token if present
     */
    @SuppressWarnings("unused")
    protected void storeToken(@NonNull Account account, @NonNull String token, @Nullable String refreshToken) {
        accountManager.setAuthToken(account, tokenType, token);
        if (refreshToken != null) {
            accountManager.setAuthToken(account, String.format("%s_refresh", tokenType), refreshToken);
        }
    }

    /**
     * With this you can store some additional userdata in key-value-pairs to the account.
     *
     * @param account Account you want to store information for
     * @param key     the key for the data
     * @param value   the actual data you want to store
     */
    @SuppressWarnings("unused")
    protected void storeUserData(@NonNull Account account, @NonNull String key, @NonNull String value) {
        accountManager.setUserData(account, key, value);
    }

    /**
     * This method will finish the login process, close the login activity.
     * The account which is reached into this method will be set as
     * "current-active" account. Use {@link AuthAccountManager#resetActiveAccount(String)} to
     * reset this if necessary
     *
     * @param account Account you want to set as current active
     */
    protected void finalizeAuthentication(@NonNull Account account) {
        resultBundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        SharedPreferences preferences = getSharedPreferences(accountType, Context.MODE_PRIVATE);
        preferences.edit().putString(AuthAccountManager.RETROAUTH_ACCOUNTNAME_KEY, account.name).apply();
        finish();
    }


    /**
     * Tries finding an existing account with the given name.
     * It creates a new Account if it couldn't find it
     *
     * @return The account if found, or a newly created one
     */
    @NonNull
    @SuppressWarnings("unused")
    protected Account createOrGetAccount(@NonNull String accountName) {
        // if this is a relogin
        Account[] accountList = accountManager.getAccountsByType(accountType);
        for (Account account : accountList) {
            if (account.name.equals(accountName))
                return account;
        }
        Account account = new Account(accountName, accountType);
        accountManager.addAccountExplicitly(account, null, null);
        return account;
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    public void finish() {
        if (accountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (resultBundle != null) {
                accountAuthenticatorResponse.onResult(resultBundle);
            } else {
                accountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled");
            }
            accountAuthenticatorResponse = null;
        } else {
            if (resultBundle != null && resultBundle.containsKey(AccountManager.KEY_ACCOUNT_NAME)) {
                Intent intent = new Intent();
                intent.putExtras(resultBundle);
                setResult(RESULT_OK, intent);
            } else {
                setResult(RESULT_CANCELED);
            }
        }
        super.finish();
    }

    /**
     * When the login token is not valid anymore, but the account already exists
     * this will return the account name of the user.
     *
     * @return account name of the user
     */
    @Nullable
    @SuppressWarnings("unused")
    protected String getAccountName() {
        return accountName;
    }

    /**
     * @return The requested account type if available. otherwise <code>null</code>
     */
    @NonNull
    protected String getRequestedAccountType() {
        return accountType;
    }

    /**
     * @return The requested token type if available. otherwise <code>null</code>
     */
    @Nullable
    protected String getRequestedTokenType() {
        return tokenType;
    }


}
