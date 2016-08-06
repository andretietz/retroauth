package com.andretietz.retroauth;

import android.accounts.AccountManager;
import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class AuthenticationActivityTest {
    @Test
    public void createLoginIntent() {
        Intent intent = AuthenticationActivity.createLoginIntent("action", "account", "token");
        assertEquals("action", intent.getAction());
        assertEquals("account", intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        assertEquals("token", intent.getStringExtra(AccountAuthenticator.KEY_TOKEN_TYPE));
    }


    @Test
    public void startActivityFailing() {
        Robolectric.setupActivity(LoginTestActivity.class);
    }

    static class LoginTestActivity extends AuthenticationActivity {

    }
}
