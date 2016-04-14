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
final class ContextManager {

    private static ContextManager instance;
    private final Context applicationContext;
    private final LifecycleHandler handler;

    private ContextManager(@NonNull Activity activity) {
        applicationContext = activity.getApplicationContext();
        handler = new LifecycleHandler(activity);
        if (applicationContext instanceof Application) {
            ((Application) applicationContext).registerActivityLifecycleCallbacks(handler);

        }
    }

    public static ContextManager get(@NonNull Activity activity) {
        if (instance == null) {
            synchronized (ContextManager.class) {
                if (instance == null) {
                    instance = new ContextManager(activity);
                }
            }
        }
        return instance;
    }

    @NonNull
    public Context getContext() {
        return applicationContext;
    }

    @Nullable
    public Activity getActivity() {
        synchronized (this) {
            return handler.getActivity();
        }
    }

    private static class LifecycleHandler implements ActivityLifecycleCallbacks {

        private Activity activity = null;

        LifecycleHandler(Activity activity) {
            this.activity = activity;
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
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        @Nullable
        public Activity getActivity() {
            return activity;
        }
    }


}
