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
 * This is the interface of a token storage.
 */
interface TokenStorage<OWNER : Any, TOKEN_TYPE : Any, TOKEN : Any> {

    /**
     * This method returns an authentication token that is stored locally
     *
     * @param owner The owner type of the token you want to get
     * @param type  the type of the token you want to get
     * @return the token to authenticate your request with or {@code null}, if there's no token locally stored
     */
    fun getToken(owner: OWNER, type: TOKEN_TYPE): TOKEN

    /**
     * Removes the token of a specific type and owner from the token storage.
     *
     * @param owner Owner of the token
     * @param type  Type of the token
     * @param token Token to remove
     */
    fun removeToken(owner: OWNER, type: TOKEN_TYPE, token: TOKEN)

    /**
     * Stores a token of a specific type and owner to the token storage.
     *
     * @param owner Owner of the token
     * @param type  Type of the token
     * @param token Token to store
     */
    fun storeToken(owner: OWNER, type: TOKEN_TYPE, token: TOKEN)
}
