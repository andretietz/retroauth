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

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * This AccountAuthenticator is a very basic implementation of Android's
 * {@link AbstractAccountAuthenticator}.
 */
public final class AccountAuthenticator extends AbstractAccountAuthenticator {

    public static final String KEY_TOKEN_TYPE = "account_token_type";

    /**
     * The Action string to open the implementation of the {@link AuthenticationActivity},
     * to show the user a login.
     */
    private final String action;

    /**
     * @param context The context (needed by the {@link AbstractAccountAuthenticator}
     * @param action  The Action String to open the Activity to login
     */
    public AccountAuthenticator(Context context, String action) {
        super(context);
        this.action = action;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
                             String accountType, String authTokenType,
                             String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        return createAuthBundle(response, accountType, authTokenType, null);
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                               String authTokenType, Bundle options) throws NetworkErrorException {
        return createAuthBundle(response, account.type, authTokenType, account.name);
    }

    /**
     * Creates an Intent to open the Activity to login.
     *
     * @param response    needed parameter
     * @param accountType The account Type
     * @param tokenType   The requested token type
     * @param accountName The name of the account
     * @return a bundle to open the activity
     */
    private Bundle createAuthBundle(AccountAuthenticatorResponse response, String accountType,
                                    String tokenType, String accountName) {
        Intent intent = new Intent(action);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(KEY_TOKEN_TYPE, tokenType);
        if (null != accountName) {
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName);
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                                     Account account, Bundle options) throws NetworkErrorException {
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
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                    String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                              String[] features) throws NetworkErrorException {
        return null;
    }
}
