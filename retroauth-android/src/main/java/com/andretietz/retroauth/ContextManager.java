/*
 * Copyright (c) 2016 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountManagerCallback;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * The {@link ContextManager} provides an application {@link Context} as well as an {@link Activity} if this was not stopped
 * already. It registers {@link ActivityLifecycleCallbacks} to be able to know if there's an active {@link Activity}
 * or not. The {@link Activity} is required in case the user calls an {@link Authenticated} request
 * and there are not tokens provided, to be able to open the {@link Activity} for login, using the
 * {@link android.accounts.AccountManager#getAuthToken(Account, String, Bundle, Activity, AccountManagerCallback, Handler)}.
 * If you don't provide an {@link Activity} there, the login screen wont open. So in case you're calling an
 * {@link Authenticated} request from a {@link android.app.Service} there will be no Login if required.
 */
final class ContextManager {

    private static ContextManager instance;
    private final Context applicationContext;
    private final LifecycleHandler handler;

    private ContextManager(@NonNull Application application) {
        applicationContext = application;
        handler = new LifecycleHandler(null);
        application.registerActivityLifecycleCallbacks(handler);
    }

    private ContextManager(@NonNull Activity activity) {
        applicationContext = activity.getApplicationContext();
        handler = new LifecycleHandler(activity);
        if (applicationContext instanceof Application) {
            ((Application) applicationContext).registerActivityLifecycleCallbacks(handler);
        }
    }

    /**
     * @param activity some {@link Activity} to be able to create the instance
     * @return a singleton instance of the {@link ContextManager}.
     */
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

    /**
     * @param application some {@link Activity} to be able to create the instance
     * @return a singleton instance of the {@link ContextManager}.
     */
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

    /**
     * @return the Application Context
     */
    @NonNull
    public Context getContext() {
        return applicationContext;
    }


    /**
     * @return an {@link Activity} if there's one available. If not this method returns {@code null}
     */
    @Nullable
    public Activity getActivity() {
        synchronized (this) {
            return handler.activity;
        }
    }

    /**
     * An implementation of {@link ActivityLifecycleCallbacks} which stores a reference to the {@link Activity} as long as
     * it is not stopped. If the {@link Activity} is stopped, the reference will be removed.
     */
    private static class LifecycleHandler implements ActivityLifecycleCallbacks {

        public Activity activity = null;

        LifecycleHandler(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }


        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            synchronized (this) {
                this.activity = activity;
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            synchronized (this) {
                this.activity = null;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }
}
