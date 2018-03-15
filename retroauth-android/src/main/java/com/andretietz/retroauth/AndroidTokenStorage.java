/*
 * Copyright (c) 2016 Andre Tietz
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
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * This is the implementation of a {@link TokenStorage} in Android using the Android {@link AccountManager}
 */
final class AndroidTokenStorage implements TokenStorage<Account, AndroidTokenType, AndroidToken> {

    private final AccountManager accountManager;
    private final ActivityManager activityManager;

    public AndroidTokenStorage(Application application) {
        this.activityManager = ActivityManager.Companion.get(application);
        this.accountManager = AccountManager.get(application);
    }

    @Override
    public AndroidToken getToken(Account account, AndroidTokenType type) throws AuthenticationCanceledException {
        try {
            AndroidToken token;
            Activity activity = activityManager.getActivity();
            if (account == null) {
                token = createAccountAndGetToken(activity, type);
            } else {
                token = getToken(activity, account, type);
            }
            if (token == null) {
                throw new AuthenticationCanceledException("user canceled the login!");
            }
            return token;
        } catch (AuthenticatorException | OperationCanceledException | IOException e) {
            throw new AuthenticationCanceledException(e);
        }
    }

    private AndroidToken createAccountAndGetToken(@Nullable Activity activity, @NonNull AndroidTokenType type)
            throws AuthenticatorException, OperationCanceledException, IOException {

        AccountManagerFuture<Bundle> future = accountManager
                .addAccount(type.getAccountType(), type.getTokenType(), null, null, activity, null, null);
        Bundle result = future.getResult();
        String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
        if (accountName != null) {
            Account account = new Account(result.getString(AccountManager.KEY_ACCOUNT_NAME),
                    result.getString(AccountManager.KEY_ACCOUNT_TYPE));
            String token = accountManager.peekAuthToken(account, type.getTokenType());
            String refreshToken = accountManager.peekAuthToken(account, getRefreshTokenType(type));
            if (token != null) return new AndroidToken(token, refreshToken);
        }
        return null;
    }

    private AndroidToken getToken(@Nullable Activity activity, @NonNull Account account, @NonNull AndroidTokenType type)
            throws AuthenticatorException, OperationCanceledException, IOException {
        // Clear the interrupted flag
        Thread.interrupted();
        AccountManagerFuture<Bundle> future = accountManager
                .getAuthToken(account, type.getTokenType(), null, activity, null, null);
        Bundle result = future.getResult();
        String token = result.getString(AccountManager.KEY_AUTHTOKEN);
        String refreshToken = accountManager.peekAuthToken(account, getRefreshTokenType(type));
        if (token == null) {
            token = accountManager.peekAuthToken(account, type.getTokenType());
        }
        if (token != null) return new AndroidToken(token, refreshToken);
        return null;
    }

    private String getRefreshTokenType(AndroidTokenType type) {
        return String.format("%s_refresh", type.getTokenType());
    }

    @Override
    public void removeToken(Account account, AndroidTokenType type, AndroidToken androidToken) {
        accountManager.invalidateAuthToken(account.type, androidToken.getToken());
        accountManager.invalidateAuthToken(account.type, androidToken.getRefreshToken());
    }

    @Override
    public void storeToken(Account account, AndroidTokenType type, AndroidToken androidToken) {
        accountManager.setAuthToken(account, type.getTokenType(), androidToken.getToken());
        accountManager.setAuthToken(account, getRefreshTokenType(type), androidToken.getRefreshToken());
    }
}
