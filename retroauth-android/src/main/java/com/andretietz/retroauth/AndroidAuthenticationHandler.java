package com.andretietz.retroauth;

import android.accounts.Account;
import android.app.Activity;

/**
 * Created by andre on 13.04.2016.
 */
public class AndroidAuthenticationHandler extends AuthenticationHandler<Account, AndroidTokenType, AndroidToken> {
    public AndroidAuthenticationHandler(Activity activity, Provider<Account, AndroidTokenType, AndroidToken> provider) {
        super(
                new AndroidMethodCache(),
                new AndroidOwnerManager(
                        new AuthAccountManager(activity.getApplicationContext()),
                        ContextManager.get(activity)),
                new AndroidTokenStorage(ContextManager.get(activity)), provider);
    }
}
