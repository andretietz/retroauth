package com.andretietz.retroauth;

import android.accounts.Account;
import android.support.annotation.NonNull;

/**
 * Created by andre on 13/04/16.
 */
public class AndroidTokenStorage implements TokenStorage<AndroidTokenType> {

    private final AuthAccountManager accountManager;

    public AndroidTokenStorage(@NonNull AuthAccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void removeToken(AndroidTokenType type, String token) {
        accountManager.android.invalidateAuthToken(type.accountType, token);
    }

    @Override
    public void saveToken(AndroidTokenType type, String token) {
        Account account = accountManager.getActiveAccount(type.accountType);
        if (account == null) return;
        accountManager.android.setAuthToken(account, type.tokenType, token);
    }

    @Override
    public String getToken(AndroidTokenType type) {
        Account account = accountManager.getActiveAccount(type.accountType);
        if (account == null) return null;
        return accountManager.android.peekAuthToken(account, type.tokenType);
    }

    @Override
    public String getRefreshToken(AndroidTokenType type) {
        Account account = accountManager.getActiveAccount(type.accountType);
        if (account == null) return null;
        return accountManager.android.peekAuthToken(account, String.format("%s_refresh", type.tokenType));
    }
}
