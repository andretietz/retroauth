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
        if (response.isSuccessful) return ResponseStatus.OK
        if (response.code() == 401) return ResponseStatus.RETRY_TOKEN_INVALID
        return ResponseStatus.NO_RETRY_TOKEN_INVALID
    }

    enum class ResponseStatus {
        /** Token was valid, request was successful */
        OK,
        /** Token was invalid, retry */
        RETRY_TOKEN_INVALID,
        /** Token was invalid, do not retry */
        NO_RETRY_TOKEN_INVALID,
    }
}
