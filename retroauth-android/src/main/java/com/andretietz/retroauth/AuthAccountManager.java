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
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This class wraps the Android AccountManager and adds some retroauth specific
 * functionality.
 */
public final class AuthAccountManager { //implements BaseAccountManager {

    static final String RETROAUTH_ACCOUNTNAME_KEY = "com.andretietz.retroauth.ACTIVE_ACCOUNT";
    public final AccountManager android;
    private final Context context;

    public AuthAccountManager(@NonNull Context context) {
        this.context = context;
        this.android = AccountManager.get(context);
    }

    /**
     * This method returns the name of the active account of the chosen accountType
     *
     * @param accountType of which you want to get the active accountname of
     * @return the name of the currently active account or {@code null}
     */
    @Nullable
    public String getActiveAccountName(@NonNull String accountType) {
        SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
        return preferences.getString(RETROAUTH_ACCOUNTNAME_KEY, null);
    }

    @Nullable
    public Account getActiveAccount(@NonNull String accountType) {
        String accountName = getActiveAccountName(accountType);
        if (accountName != null) {
            return getAccountByName(accountType, accountName);
        }
        return null;
    }

    @Nullable
    public Account getAccountByName(@NonNull String accountType, @NonNull String accountName) {
        Account[] accounts = android.getAccountsByType(accountType);
        for (Account account : accounts) {
            if (accountName.equals(account.name)) return account;
        }
        return null;
    }

    @Nullable
    public String getActiveUserToken(@NonNull String accountType, @NonNull String tokenType) {
        Account activeAccount = getActiveAccount(accountType);
        if (activeAccount == null) return null;
        return android.peekAuthToken(activeAccount, tokenType);
    }

    @Nullable
    public String getActiveUserData(@NonNull String accountType, @NonNull String key) {
        return android.getUserData(getActiveAccount(accountType), key);
    }

    @Nullable
    public Account setActiveAccount(@NonNull String accountType, @NonNull String accountName) {
        SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
        preferences.edit().putString(RETROAUTH_ACCOUNTNAME_KEY, accountName).apply();
        return getAccountByName(accountType, accountName);
    }

    public void resetActiveAccount(@NonNull String accountType) {
        SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
        preferences.edit().remove(RETROAUTH_ACCOUNTNAME_KEY).apply();
    }


    public AccountManagerFuture<Bundle> addAccount(@NonNull Activity activity, @NonNull String accountType) {
        return addAccount(activity, accountType, null);
    }

    public AccountManagerFuture<Bundle> addAccount(@Nullable Activity activity, @NonNull String accountType, @Nullable String tokenType) {
        return android.addAccount(accountType, tokenType, null, null, activity, null, null);
    }

    public AccountManagerFuture<Bundle> getAuthToken(@Nullable Activity activity, @NonNull Account account, @NonNull String tokenType) {
        return android.getAuthToken(account, tokenType, null, activity, null, null);
    }

}
