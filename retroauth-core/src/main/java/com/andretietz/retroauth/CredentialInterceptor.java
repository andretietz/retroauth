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
final class CredentialInterceptor<OWNER, TOKEN_TYPE, TOKEN> implements Interceptor {
    private final AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler;
    private final HashMap<TOKEN_TYPE, AccountTokenLock> tokenTypeLockMap = new HashMap<>();
    private final boolean lockable;

    public CredentialInterceptor(AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler, boolean lockPerToken) {
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
            try {
                lock(type);
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
                unlock(type);
            }
        } else {
            // no authentication required, proceed as usual
            response = chain.proceed(request);
        }
        return response;
    }

    private void storeAndThrowError(TOKEN_TYPE type, Exception e) throws IOException {
        if (lockable && getLock(type).errorContainer.get() == null) {
            getLock(type).errorContainer.set(e);
        }
        throw new AuthenticationCanceledException(e);
    }

    private AccountTokenLock getLock(TOKEN_TYPE type) {
        AccountTokenLock lock = tokenTypeLockMap.get(type);
        if (lock == null) {
            lock = new AccountTokenLock();
            tokenTypeLockMap.put(type, lock);
        }
        return lock;
    }

    private void lock(TOKEN_TYPE type) throws Exception {
        if (lockable) {
            AccountTokenLock lock = getLock(type);
            boolean wasWaiting = !lock.lock.tryLock();
            if (wasWaiting) {
                lock.waitCounter.incrementAndGet();
            }
            lock.lock.lock();
            if (wasWaiting) {
                throw lock.errorContainer.get();
            }
        }
    }

    private void unlock(TOKEN_TYPE type) {
        if (lockable) {
            AccountTokenLock lock = getLock(type);
            if (lock.waitCounter.decrementAndGet() <= 0) {
                lock.errorContainer.set(null);
            }
            lock.lock.unlock();
        }
    }

    private static class AccountTokenLock {
        public final Lock lock;
        public final AtomicReference<Exception> errorContainer;
        public final AtomicInteger waitCounter;

        public AccountTokenLock() {
            this.lock = new ReentrantLock();
            this.errorContainer = new AtomicReference<>();
            this.waitCounter = new AtomicInteger();
        }
    }
}
