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
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
final class CredentialInterceptor<OWNER, TOKEN_TYPE extends TokenType, TOKEN> implements Interceptor {
    private final AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler;
    private final static HashMap<TokenType, AccountTokenLock> TOKENTYPE_LOCKERS = new HashMap<>();
    private final boolean lockable;

    CredentialInterceptor(AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler, boolean lockPerToken) {
        this.authHandler = authHandler;
        this.lockable = lockPerToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = null;
        Request request = chain.request();
        // get the token type required by this request
        TOKEN_TYPE type = authHandler.methodCache.getTokenType(Utils.createUniqueIdentifier(request));

        // if the request does require authentication
        if (type != null) {
            boolean wasWaiting = false;
            try {
                wasWaiting = lock(type);
                TOKEN token;
                OWNER owner;
                int tryCount = 0;
                do {
                    // get the owner of the token
                    owner = authHandler.ownerManager.getOwner(type);
                    // get the token
                    token = authHandler.tokenStorage.getToken(owner, type);
                    // modify the request using the token
                    request = authHandler.provider.authenticateRequest(request, token);
                    // execute the request
                    response = chain.proceed(request);
                } while (authHandler.provider
                        .retryRequired(++tryCount, response, authHandler.tokenStorage, owner, type, token));
            } catch (Exception e) {
                storeAndThrowError(type, e);
            } finally {
                unlock(type, wasWaiting);
            }
        } else {
            // no authentication required, proceed as usual
            response = chain.proceed(request);
        }
        return response;
    }

    private void storeAndThrowError(TOKEN_TYPE type, Exception exception) throws IOException {
        //noinspection ThrowableResultOfMethodCallIgnored
        if (lockable && getLock(type).errorContainer.get() == null) {
            getLock(type).errorContainer.set(exception);
        }
        throw new AuthenticationCanceledException(exception);
    }

    private synchronized AccountTokenLock getLock(TOKEN_TYPE type) {
        AccountTokenLock lock = TOKENTYPE_LOCKERS.get(type);
        if (lock == null) {
            lock = new AccountTokenLock();
            TOKENTYPE_LOCKERS.put(type, lock);
        }
        return lock;
    }

    private boolean lock(TOKEN_TYPE type) throws Exception {
        if (lockable) {
            AccountTokenLock lock = getLock(type);
            if (!lock.lock.tryLock()) {
                lock.lock.lock();
                Exception exception = lock.errorContainer.get();
                if (exception != null) {
                    throw exception;
                }
                return true;
            }
        }
        return false;
    }

    private void unlock(TOKEN_TYPE type, boolean wasWaiting) {
        if (lockable) {
            AccountTokenLock lock = getLock(type);
            if (wasWaiting && lock.waitCounter.decrementAndGet() <= 0) {
                lock.errorContainer.set(null);
            }
            lock.lock.unlock();
        }
    }

    private static class AccountTokenLock {
        final Lock lock;
        final AtomicReference<Exception> errorContainer;
        final AtomicInteger waitCounter;

        AccountTokenLock() {
            this.lock = new ReentrantLock(true);
            this.errorContainer = new AtomicReference<>();
            this.waitCounter = new AtomicInteger();
        }
    }
}
