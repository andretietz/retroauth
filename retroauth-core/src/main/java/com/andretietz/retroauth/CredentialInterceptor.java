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

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This interceptor intercepts the okhttp requests and checks if authentication is required.
 * If so, it tries to get the owner of the token, then tries to get the token and
 * applies the token to the request
 *
 * @param <OWNER>      a type that represents the owner of a token. Since there could be multiple users on one client.
 * @param <TOKEN_TYPE> type of the token that should be added to the request
 */
final class CredentialInterceptor<OWNER, TOKEN_TYPE, TOKEN> implements Interceptor {
    private final AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler;

    public CredentialInterceptor(AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler) {
        this.authHandler = authHandler;
    }

    @Override
    public synchronized Response intercept(Chain chain) throws IOException {
        Response response;
        Request request = chain.request();
        // get the token type required by this request
        TOKEN_TYPE type = authHandler.methodCache.getTokenType(Utils.createUniqueIdentifier(request));
        // if the request does require authentication
        if (type != null) {
            TOKEN token;
            OWNER owner;
            int tryCount = 0;
            do {
                try {
                    // get the owner of the token
                    owner = authHandler.ownerManager.getOwner(type);
                    // get the token
                    token = authHandler.tokenStorage.getToken(owner, type);
                    // modify the request using the token
                    request = authHandler.provider.authenticateRequest(request, token);
                    // execute the request
                    response = chain.proceed(request);
                } catch (Exception e) {
                    throw new AuthenticationCanceledException(e);
                }
            } while (authHandler.provider
                    .retryRequired(++tryCount, response, authHandler.tokenStorage, owner, type, token));
        } else {
            // no authentication required, proceed as usual
            response = chain.proceed(request);
        }
        return response;
    }
}
