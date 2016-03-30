package com.andretietz.retroauth;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 *
 */
final class ContextManager implements ActivityLifecycleCallbacks {

    private static ContextManager instance;
    private final Context applicationContext;
    private Activity activity;

    private ContextManager(@NonNull Application application) {
        applicationContext = application.getApplicationContext();
        application.registerActivityLifecycleCallbacks(this);
    }

    public static ContextManager get(@NonNull Application application) {
        if (instance == null) {
            synchronized (ContextManager.class) {
                if (instance == null) {
                    instance = new ContextManager(application);
                }
            }
        }
        return instance;
    }

    @NonNull
    public Context getContext() {
        synchronized (this) {
            if (activity != null) {
                return activity;
            }
        }
        return applicationContext;
    }


    @Nullable
    public Activity getActivity() {
        synchronized (this) {
            return activity;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        synchronized (this) {
            this.activity = activity;
        }
    }


    @Override
    public void onActivityPaused(Activity activity) {
        synchronized (this) {
            this.activity = null;
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}
