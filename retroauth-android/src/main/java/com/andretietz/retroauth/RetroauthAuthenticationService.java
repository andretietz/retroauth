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
        action = getString(R.string.com_andretietz_retroauth_authentication_ACTION);
        if (TextUtils.isEmpty(action)) {
            throw new RuntimeException(String.format(
                    "When using the %s you need to define an action string <string "
                            + "name=\"com.andretietz.retroauth.authentication.ACTION\" "
                            + "translatable=\"false\">your action</string>",
                    RetroauthAuthenticationService.class.getSimpleName()));
        }
    }

    @Override
    public String getLoginAction(Context context) {
        return action;
    }
}
