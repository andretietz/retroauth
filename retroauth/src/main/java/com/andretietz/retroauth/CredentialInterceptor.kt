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

import okhttp3.Interceptor
import okhttp3.Response
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock


/**
 * This interceptor intercepts the okhttp requests and checks if authentication is required.
 * If so, it tries to get the owner of the token, then tries to get the token and
 * applies the token to the request
 *
 * @param <OWNER>      a type that represents the owner of a token. Since there could be multiple users on one client.
 * @param <TOKEN_TYPE> type of the token that should be added to the request
 */
class CredentialInterceptor<OWNER : Any, TOKEN_TYPE : Any, TOKEN : Any>(
        private val authHandler: AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN>
) : Interceptor {

    companion object {
        private val TOKENTYPE_LOCKERS = HashMap<Any, AccountTokenLock>()
    }

    override fun intercept(chain: Interceptor.Chain): Response? {
        var response: Response? = null
        var request = chain.request()
        // get the token type required by this request
        val type = authHandler.methodCache.getTokenType(Utils.createUniqueIdentifier(request))

        // if the request does require authentication
        if (type != null) {
            var pending = false
            var token: TOKEN
            var owner: OWNER
            var tryCount = 0
            try {
                do {
                    try {
                        pending = lock(type)
                        // get the owner of the token or opens login
                        owner = authHandler.ownerManager.getOwner(type)
                        // get the token
                        val localToken = authHandler.tokenStorage.getToken(owner, type)
                        // check if the token is still valid
                        if (!authHandler.provider.isTokenValid(localToken)) {
                            token = authHandler.provider.refreshToken(localToken)
                            // if the token was refreshed, store it
                            if (token != localToken) {
                                authHandler.tokenStorage.storeToken(owner, type, token)
                            }
                        } else {
                            token = localToken
                        }
                        // modify the request using the token
                        request = authHandler.provider.authenticateRequest(request, token)
                    } finally {
                        unlock(type, pending)
                    }

                    // execute the request
                    response = chain.proceed(request)

                } while (
                        when (authHandler.provider.validateResponse(++tryCount, response!!)) {
                        // if request was ok, no retry
                            TokenProvider.ResponseStatus.TOKEN_VALID -> false
                            TokenProvider.ResponseStatus.TOKEN_INVALID_NO_RETRY -> {
                                authHandler.tokenStorage.removeToken(owner, type, token)
                                false
                            }
                            TokenProvider.ResponseStatus.TOKEN_INVALID_RETRY -> {
                                authHandler.tokenStorage.removeToken(owner, type, token)
                                true
                            }
                        }
                )

            } catch (error: Exception) {
                storeAndThrowError(type, error)
            }
        } else {
            // no authentication required, proceed as usual
            response = chain.proceed(request)
        }
        return response
    }

    private fun storeAndThrowError(type: TOKEN_TYPE, exception: Exception) {
        if (getLock(type).errorContainer.get() == null) {
            getLock(type).errorContainer.set(exception)
        }
        throw exception
    }

    private fun getLock(type: TOKEN_TYPE): AccountTokenLock {
        synchronized(type, {
            val lock: AccountTokenLock = TOKENTYPE_LOCKERS[type] ?: AccountTokenLock()
            TOKENTYPE_LOCKERS[type] = lock
            return lock
        })
    }

    @Throws(Exception::class)
    private fun lock(type: TOKEN_TYPE): Boolean {
        val lock = getLock(type)
        if (!lock.lock.tryLock()) {
            lock.lock.lock()
            val exception = lock.errorContainer.get()
            if (exception != null) {
                throw exception
            }
            return true
        }
        return false
    }

    private fun unlock(type: TOKEN_TYPE, wasWaiting: Boolean) {
        val lock = getLock(type)
        if (wasWaiting && lock.waitCounter.decrementAndGet() <= 0) {
            lock.errorContainer.set(null)
        }
        lock.lock.unlock()
    }

    internal class AccountTokenLock {
        val lock: Lock = ReentrantLock(true)
        val errorContainer: AtomicReference<Exception> = AtomicReference()
        val waitCounter = AtomicInteger()
    }
}