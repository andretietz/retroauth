package com.andretietz.retroauth;

import android.app.Activity;

import java.util.concurrent.ExecutorService;

/**
 * Created by andre on 13.04.2016.
 */
public class AndroidAuthenticationHandler extends BasicAuthenticationHandler<AndroidTokenType, String, Object> {
    public AndroidAuthenticationHandler(Activity activity, ExecutorService executorService, TokenApplier applier) {
        super(executorService,
                new BaseAndroidTokenApi(activity, applier),
                new AndroidTokenStorage(activity));
    }
}
