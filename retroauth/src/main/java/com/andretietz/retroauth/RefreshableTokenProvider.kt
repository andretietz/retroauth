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

/**
 * If the token of your provider can be refreshed, implement this interface instead of [TokenProvider]
 */
interface RefreshableTokenProvider<TOKEN : Any> : TokenProvider<TOKEN> {
    /**
     * This method will be called right after a token was successfully loaded from the local [TokenStorage]. Check if
     * it is still valid. If not, refresh the token and return it.
     *
     * @param token of the local [TokenStorage]
     */
    fun checkForTokenRefresh(token: TOKEN): TOKEN
}