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
import okhttp3.Request
import okhttp3.Response
import retrofit2.Invocation
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
class CredentialInterceptor<out OWNER_TYPE : Any, OWNER : Any, CREDENTIAL : Any>(
  private val authenticator: Authenticator<OWNER_TYPE, OWNER, CREDENTIAL>,
  private val ownerManager: OwnerStorage<OWNER_TYPE, OWNER>,
  private val credentialStorage: CredentialStorage<OWNER, CREDENTIAL>
) : Interceptor {

  companion object {
    private val refreshLock = AccountTokenLock()
    private const val HASH_PRIME = 31
  }

  private val registration = mutableMapOf<Int, RequestType<OWNER_TYPE>>()

  @Suppress("Detekt.RethrowCaughtException")
  override fun intercept(chain: Interceptor.Chain): Response {
    var response: Response
    var request = chain.request()
    // get the credential type required by this request
    val authRequestType = findRequestType(request) ?: return chain.proceed(request)
    var refreshRequested = false
    var credential: CREDENTIAL
    var owner: OWNER?
    var tryCount = 0
    do {
      try {
        lock()
        try {
          owner = ownerManager.getActiveOwner(authRequestType.ownerType)
          if (owner == null) {
            val owners = ownerManager.getOwners(authRequestType.ownerType)
            if (owners.isNotEmpty()) {
              owner = authenticator.chooseOwner(owners).get()
              ownerManager.switchActiveOwner(authRequestType.ownerType, owner)
            }
          }
          if (owner != null) {
            // get the credential of the owner
            val localToken =
              credentialStorage.getCredentials(owner, authRequestType.credentialType).get()
            // if the credential is still valid and no refresh has been requested
            if (authenticator.isCredentialValid(localToken) && !refreshRequested) {
              credential = localToken
            } else {
              // try to refreshing the credentials
              val refreshedToken =
                authenticator.refreshCredentials(owner, authRequestType.credentialType, localToken)
              credential = if (refreshedToken != null) {
                // if the credential was refreshed, store it
                credentialStorage
                  .storeCredentials(owner, authRequestType.credentialType, refreshedToken)
                refreshedToken
              } else {
                // otherwise remove the current credential from the storage
                credentialStorage
                  .removeCredentials(owner, authRequestType.credentialType, localToken)
                // and use the "old" credential
                localToken
              }
            }
            // authenticate the request using the credential
            request = authenticator.authenticateRequest(request, credential)
          } else {
            ownerManager.createOwner(authRequestType.ownerType, authRequestType.credentialType)
            throw AuthenticationRequiredException()
          }
        } catch (error: Throwable) {
          refreshLock.errorContainer.set(error)
          throw error
        }
      } finally {
        unlock()
      }
      // execute the request
      response = chain.proceed(request)
      refreshRequested = authenticator.refreshRequired(++tryCount, response)
      if (refreshRequested) response.close()
    } while (refreshRequested)
    return response
  }

  @Throws(Throwable::class)
  private fun lock() {
    if (!refreshLock.lock.tryLock()) {
      refreshLock.waitCounter.incrementAndGet()
      refreshLock.lock.lock()
      val exception = refreshLock.errorContainer.get()
      if (exception != null) {
        throw exception
      }
    } else {
      refreshLock.waitCounter.incrementAndGet()
    }
  }

  private fun unlock() {
    if (refreshLock.waitCounter.getAndDecrement() <= 0) {
      refreshLock.errorContainer.set(null)
    }
    refreshLock.lock.unlock()
  }

  private fun findRequestType(
    request: Request
  ): RequestType<OWNER_TYPE>? {
    val key = request.url.hashCode() + HASH_PRIME * request.method.hashCode()
    return registration[key] ?: request.tag(Invocation::class.java)
      ?.method()
      ?.annotations
      ?.filterIsInstance<Authenticated>()
      ?.firstOrNull()
      ?.let {
        RequestType(
          authenticator.getCredentialType(it.credentialType),
          authenticator.getOwnerType(it.ownerType)
        )
      }
      ?.also { registration[key] = it }
  }

  internal data class AccountTokenLock(
    val lock: Lock = ReentrantLock(true),
    val errorContainer: AtomicReference<Throwable> = AtomicReference(),
    val waitCounter: AtomicInteger = AtomicInteger()
  )
}
