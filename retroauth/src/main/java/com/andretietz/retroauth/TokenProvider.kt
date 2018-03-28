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
interface TokenProvider<TOKEN : Any> {

    /**
     * Authenticates a [Request].
     *
     * @param request request to authenticate
     * @param token   Token to authenticate
     * @return a modified version of the incoming request, which is authenticated
     */
    fun authenticateRequest(request: Request, token: TOKEN): Request

    /**
     * Checks if the retry of an request is required or not. If your provider provides a refresh token
     * mechanism, you can do it in here.
     *
     * @param count        value contains how many times this request has been executed already
     * @param response     response to check what the result was
     * @return `true` if a retry is required, `false` if not
     */
    fun validateResponse(count: Int, response: Response): ResponseStatus {
        if (response.code() == 401) {
            if (count <= 1) return ResponseStatus.TOKEN_INVALID_RETRY
            return ResponseStatus.TOKEN_INVALID_NO_RETRY
        }
        return ResponseStatus.TOKEN_VALID
    }

    /**
     * This method will be called right after a token was successfully loaded from the local [TokenStorage]. Check if
     * it is still valid. If not, refresh the token and return it.
     *
     * @param token of the local [TokenStorage]
     */
    fun refreshToken(token: TOKEN): TOKEN = token

    /**
     * This method is called on each authenticated request, to make sure the current token is still valid.
     *
     * @param token The current token
     */
    fun isTokenValid(token: TOKEN): Boolean = true

    enum class ResponseStatus {
        /** Token was valid, request was successful */
        TOKEN_VALID,
        /** Token was invalid, retry */
        TOKEN_INVALID_RETRY,
        /** Token was invalid, do not retry */
        TOKEN_INVALID_NO_RETRY,
    }
}
