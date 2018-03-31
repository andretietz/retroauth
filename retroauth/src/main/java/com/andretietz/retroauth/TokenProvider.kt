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

import okhttp3.Request
import okhttp3.Response

/**
 * The TokenProvider interface is a very specific provider endpoint dependent implementation,
 * to authenticate your request and defines when or if to retry.
 */
interface TokenProvider<in OWNER : Any, TOKEN_TYPE : Any, TOKEN : Any> {

    /**
     * Creates a token type object.
     *
     * @param annotationValues The values from the [Authenticated] annotation
     * @return a token type.
     */
    fun createTokenType(annotationValues: IntArray): TOKEN_TYPE

    /**
     * Authenticates a [Request].
     *
     * @param request request to authenticate
     * @param token   Token to authenticate
     * @return a modified version of the incoming request, which is authenticated
     */
    fun authenticateRequest(request: Request, token: TOKEN): Request

    /**
     * Checks if the token needs to be refreshed or not.
     *
     * @param count        value contains how many times this request has been executed already
     * @param response     response to check what the result was
     * @return {@code true} if a token refresh is required, {@code false} if not
     */
    fun refreshRequired(count: Int, response: Response): Boolean = (response.code() == 401 && count <= 1)

    /**
     * This method will be called when [isTokenValid] returned false or [refreshRequired] returned true.
     *
     * @param token of the local [TokenStorage]
     */
    fun refreshToken(owner: OWNER, tokenType: TOKEN_TYPE, token: TOKEN): TOKEN? = token

    /**
     * This method is called on each authenticated request, to make sure the current token is still valid.
     *
     * @param token The current token
     */
    fun isTokenValid(token: TOKEN): Boolean = true
}
