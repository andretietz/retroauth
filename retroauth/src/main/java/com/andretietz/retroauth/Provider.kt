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
import retrofit2.Retrofit

/**
 * The Provider interface is a very specific provider endpoint dependent implementation,
 * to authenticate your request and defines when or if to retry.
 */
interface Provider<OWNER, TOKEN_TYPE, TOKEN> {

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
     * @param tokenStorage storage to delete and store tokens if the have been refreshed
     * @param owner        owner of the token used
     * @param type         type of the token used
     * @param token        token used for the last execution of the request
     * @return `true` if a retry is required, `false` if not
     */
    fun retryRequired(count: Int,
                      response: Response,
                      tokenStorage: TokenStorage<OWNER, TOKEN_TYPE, TOKEN>,
                      owner: OWNER,
                      type: TOKEN_TYPE,
                      token: TOKEN): Boolean

    fun onRetrofitCreated(retrofit: Retrofit)
}
