package com.andretietz.retroauth;

import android.app.Activity;

import java.util.concurrent.ExecutorService;

/**
 * Created by andre on 13.04.2016.
 */
public class AndroidAuthenticationHandler extends BaseAuthenticationHandler<AndroidTokenType> {
    public AndroidAuthenticationHandler(Activity activity, ExecutorService executorService, TokenProvider applier) {
        super(executorService,
                new AndroidTokenApi(activity, applier),
                new AndroidTokenStorage(new AuthAccountManager(activity)));
    }
}
