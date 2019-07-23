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
 * If so, it tries to get the owner of the credential, then tries to get the credential and
 * applies the credential to the request
 *
 * @param <OWNER> a type that represents the owner of a credential. Since there could be multiple users on one client.
 * @param <CREDENTIAL_TYPE> type of the credential that should be added to the request
 */
internal class CredentialInterceptor<out OWNER_TYPE : Any, OWNER : Any, CREDENTIAL_TYPE : Any, CREDENTIAL : Any>(
  private val authenticator: Authenticator<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL>,
  private val ownerManager: OwnerStorage<OWNER_TYPE, OWNER, CREDENTIAL_TYPE>,
  private val credentialStorage: CredentialStorage<OWNER, CREDENTIAL_TYPE, CREDENTIAL>,
  private val methodCache: MethodCache<OWNER_TYPE, CREDENTIAL_TYPE> = MethodCache.DefaultMethodCache()
) : Interceptor {

  companion object {
    private val TOKEN_TYPE_LOCKERS = HashMap<Any, AccountTokenLock>()
  }

  override fun intercept(chain: Interceptor.Chain): Response? {
    var response: Response? = null
    var request = chain.request()
    // get the credential type required by this request
    val authRequestType = methodCache.getCredentialType(Utils.createUniqueIdentifier(request))
      ?: return chain.proceed(request)

    // if the request does require authentication
    var pending = false
    var refreshRequested = false
    var credential: CREDENTIAL
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
            // get the credential of the owner
            val localToken = credentialStorage.getCredentials(owner, authRequestType.credentialType).get()
            // if the credential is still valid and no refresh has been requested
            if (authenticator.isCredentialValid(localToken) && !refreshRequested) {
              credential = localToken
            } else {
              // otherwise remove the current credential from the storage
              credentialStorage.removeCredentials(owner, authRequestType.credentialType, localToken)
              // try to refresh the credential
              val refreshedToken = authenticator.refreshCredentials(owner, authRequestType.credentialType, localToken)
              credential = if (refreshedToken != null) {
                // if the credential was refreshed, store it
                credentialStorage.storeCredentials(owner, authRequestType.credentialType, refreshedToken)
                refreshedToken
              } else {
                // otherwise use the "old" credential
                localToken
              }
            }
            // authenticate the request using the credential
            request = authenticator.authenticateRequest(request, credential)
          } else {
            ownerManager.createOwner(authRequestType.ownerType, authRequestType.credentialType)
            throw AuthenticationRequiredException()
          }
        } finally {
          // release type lock
          unlock(authRequestType, pending)
        }
        // execute the request
        response = chain.proceed(request)
        refreshRequested = authenticator.refreshRequired(++tryCount, response)
        // https://github.com/square/okhttp/blob/master/CHANGELOG.md#version-3141
        if (refreshRequested) response.close()
      } while (refreshRequested)
    } catch (error: Exception) {
      storeAndThrowError(authRequestType, error)
    }
    return response
  }

  private fun storeAndThrowError(type: RequestType<OWNER_TYPE, CREDENTIAL_TYPE>, exception: Exception) {
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

  private fun getLock(type: RequestType<OWNER_TYPE, CREDENTIAL_TYPE>): AccountTokenLock {
    synchronized(type) {
      val lock: AccountTokenLock = TOKEN_TYPE_LOCKERS[type] ?: AccountTokenLock()
      TOKEN_TYPE_LOCKERS[type] = lock
      return lock
    }
  }

  @Throws(Exception::class)
  private fun lock(type: RequestType<OWNER_TYPE, CREDENTIAL_TYPE>): Boolean {
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

  private fun unlock(type: RequestType<OWNER_TYPE, CREDENTIAL_TYPE>, wasWaiting: Boolean) {
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
