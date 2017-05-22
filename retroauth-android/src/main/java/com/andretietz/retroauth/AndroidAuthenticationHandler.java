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

/**
 * The {@link AndroidAuthenticationHandler} wraps all Android specific implementations ({@link AndroidMethodCache},
 * {@link AndroidOwnerManager}, {@link AndroidTokenStorage}) together into one {@link AuthenticationHandler}. This should
 * make your life easier.
 */
public final class AndroidAuthenticationHandler extends AuthenticationHandler<Account, AndroidTokenType, AndroidToken> {

    private AndroidAuthenticationHandler(Provider<Account, AndroidTokenType, AndroidToken> provider,
                                         TokenTypeFactory<AndroidTokenType> typeFactory) {
        super(new AndroidMethodCache(), new AndroidOwnerManager(new AuthAccountManager()),
                new AndroidTokenStorage(), provider, typeFactory);
    }

    public static AndroidAuthenticationHandler create(Provider<Account, AndroidTokenType, AndroidToken> provider,
                                                      TokenTypeFactory<AndroidTokenType> typeFactory) {
        return new AndroidAuthenticationHandler(provider, typeFactory);
    }

}
