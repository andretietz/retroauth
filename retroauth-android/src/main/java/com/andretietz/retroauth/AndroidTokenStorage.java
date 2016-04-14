package com.andretietz.retroauth;

import android.accounts.Account;
import android.support.annotation.NonNull;

/**
 * Created by andre on 13/04/16.
 */
public class AndroidTokenStorage implements TokenStorage<AndroidTokenType, String> {

    private final AuthAccountManager accountManager;

    public AndroidTokenStorage(@NonNull AuthAccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void removeToken(AndroidTokenType type) {
        accountManager.android.invalidateAuthToken(type.accountType, getToken(type));
    }

    @Override
    public void saveToken(AndroidTokenType type, String token) {
        // this has been done in login activity already
    }

    @Override
    public String getToken(AndroidTokenType type) {
        Account account = accountManager.getActiveAccount(type.accountType);
        if (account == null) return null;
        return accountManager.android.peekAuthToken(account, type.tokenType);
    }
}
