package com.andretietz.retroauth;

import android.content.Context;
import android.text.TextUtils;

/**
 * This is a simple implementation of an {@link AuthenticationService}. This is to avoid the need for
 * developers to create their own.
 * The only thing required is, that you provide a string {@code retroauth_authentication_action} in you string xml's
 * otherwise this class will crash on creation (runtime)
 */
public final class RetroauthAuthenticationService extends AuthenticationService {

    private String action;

    @Override
    public void onCreate() {
        super.onCreate();
        action = getString(R.string.retroauth_authentication_action);
        if (TextUtils.isEmpty(action)) {
            throw new RuntimeException(String.format(
                  "When using the %s you need to define an action string <string name=\"retroauth_authentication_action\""
                        + " translatable=\"false\">your action</string>",
                  RetroauthAuthenticationService.class.getSimpleName()));
        }
    }

    @Override
    public String getLoginAction(Context context) {
        return action;
    }
}
