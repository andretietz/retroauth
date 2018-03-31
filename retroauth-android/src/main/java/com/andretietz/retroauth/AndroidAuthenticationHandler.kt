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

package com.andretietz.retroauth

import android.accounts.Account
import android.app.Application

/**
 * The [AndroidAuthenticationHandler] wraps all Android specific implementations ([AndroidMethodCache],
 * [AndroidOwnerManager], [AndroidTokenStorage]) together into one [AuthenticationHandler]. This should
 * make your life easier.
 */
class AndroidAuthenticationHandler private constructor(application: Application,
                                                       provider: TokenProvider<Account, AndroidTokenType, AndroidToken>) :

        AuthenticationHandler<Account, AndroidTokenType, AndroidToken>(
                AndroidMethodCache(),
                AndroidOwnerManager(application),
                AndroidTokenStorage(application), provider) {

    companion object {
        @JvmStatic
        fun create(application: Application,
                   provider: TokenProvider<Account, AndroidTokenType, AndroidToken>
        ): AndroidAuthenticationHandler = AndroidAuthenticationHandler(
                application,
                provider)
    }

}
