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
internal class CredentialInterceptor<out OWNER_TYPE : Any, OWNER : Any, TOKEN_TYPE : Any, TOKEN : Any>(
  private val authenticator: Authenticator<OWNER_TYPE, OWNER, TOKEN_TYPE, TOKEN>,
  private val ownerManager: OwnerManager<OWNER_TYPE, OWNER, TOKEN_TYPE>,
  private val tokenStorage: TokenStorage<OWNER, TOKEN_TYPE, TOKEN>,
  private val methodCache: MethodCache<OWNER_TYPE, TOKEN_TYPE> = MethodCache.DefaultMethodCache()
) : Interceptor {

  companion object {
    private val TOKEN_TYPE_LOCKERS = HashMap<Any, AccountTokenLock>()
  }

  override fun intercept(chain: Interceptor.Chain): Response? {
    var response: Response? = null
    var request = chain.request()
    // get the token type required by this request
    val authRequestType = methodCache.getTokenType(Utils.createUniqueIdentifier(request))
      ?: return chain.proceed(request)

    // if the request does require authentication
    var pending = false
    var refreshRequested = false
    var token: TOKEN
    var owner: OWNER?
    var tryCount = 0
    try {
      do {
        try {
          // Lock foreach type
          pending = lock(authRequestType)

          owner = ownerManager.getActiveOwner(authRequestType.ownerType)
            ?: ownerManager.openOwnerPicker(authRequestType.ownerType).get()
          if (owner != null) {
            // get the token of the owner
            val localToken = tokenStorage.getToken(owner, authRequestType.tokenType).get()
            // if the token is still valid and no refresh has been requested
            if (authenticator.isTokenValid(localToken) && !refreshRequested) {
              token = localToken
            } else {
              // otherwise remove the current token from the storage
              tokenStorage.removeToken(owner, authRequestType.tokenType, localToken)
              // try to refresh the token
              val refreshedToken = authenticator.refreshToken(owner, authRequestType.tokenType, localToken)
              if (refreshedToken != null) {
                // if the token was refreshed, store it
                tokenStorage.storeToken(owner, authRequestType.tokenType, refreshedToken)
                token = refreshedToken
              } else {
                // otherwise use the "old" token
                token = localToken
              }
            }
            // authenticate the request using the token
            request = authenticator.authenticateRequest(request, token)
          } else {
            ownerManager.createOwner(authRequestType.ownerType, authRequestType.tokenType)
            throw AuthenticationRequiredException()
          }
        } finally {
          // release type lock
          unlock(authRequestType, pending)
        }
        // execute the request
        response = chain.proceed(request)
        refreshRequested = authenticator.refreshRequired(++tryCount, requireNotNull(response))
        if (refreshRequested) response?.close()
      } while (refreshRequested)
    } catch (error: Exception) {
      storeAndThrowError(authRequestType, error)
    }
    return response
  }

  private fun storeAndThrowError(type: RequestType<OWNER_TYPE, TOKEN_TYPE>, exception: Exception) {
    val unwrappedException = unwrapThrowable(exception)
    if (getLock(type).errorContainer.get() == null) {
      getLock(type).errorContainer.set(unwrappedException)
    }
    throw unwrappedException
  }

  private fun unwrapThrowable(throwable: Throwable): Throwable {
    if (
      throwable is AuthenticationCanceledException ||
      throwable is AuthenticationRequiredException
    ) return throwable
    throwable.cause?.let {
      return unwrapThrowable(it)
    }
    return throwable
  }

  private fun getLock(type: RequestType<OWNER_TYPE, TOKEN_TYPE>): AccountTokenLock {
    synchronized(type) {
      val lock: AccountTokenLock = TOKEN_TYPE_LOCKERS[type] ?: AccountTokenLock()
      TOKEN_TYPE_LOCKERS[type] = lock
      return lock
    }
  }

  @Throws(Exception::class)
  private fun lock(type: RequestType<OWNER_TYPE, TOKEN_TYPE>): Boolean {
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

  private fun unlock(type: RequestType<OWNER_TYPE, TOKEN_TYPE>, wasWaiting: Boolean) {
    val lock = getLock(type)
    if (wasWaiting && lock.waitCounter.decrementAndGet() <= 0) {
      lock.errorContainer.set(null)
    }
    lock.lock.unlock()
  }

  internal data class AccountTokenLock(
    val lock: Lock = ReentrantLock(true),
    val errorContainer: AtomicReference<Throwable> = AtomicReference(),
    val waitCounter: AtomicInteger = AtomicInteger()
  )
}
