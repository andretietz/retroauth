package com.andretietz.retroauth.testhelper;

import android.os.Bundle;
import android.widget.TextView;

import com.andretietz.retroauth.AuthenticationActivity;

public class RetroauthTestLoginActivity extends AuthenticationActivity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTheme(android.support.v7.appcompat.R.style.Base_V7_Theme_AppCompat);
        setContentView(new TextView(this));
    }
}
