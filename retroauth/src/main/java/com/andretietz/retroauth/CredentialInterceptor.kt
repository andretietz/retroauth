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

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Invocation
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * This interceptor intercepts the okhttp requests and checks if authentication is required.
 * If so, it tries to get the owner of the credential, then tries to get the credential and
 * applies the credential to the request
 *
 * @param <OWNER> a type that represents the owner of a credential. Since there could be multiple users on one client.
 * @param <CREDENTIAL> credential that should be added to the request
 */
class CredentialInterceptor<OWNER : Any>(
  private val authenticator: Authenticator<OWNER>,
  private val ownerManager: OwnerStorage<OWNER>,
  private val credentialStorage: CredentialStorage<OWNER>,
  private val scope: CoroutineScope = CoroutineScope(CoroutineName("InterceptorScope"))
) : Interceptor {

  companion object {
    private val refreshLock = AtomicReference(AccountTokenLock())
    private const val HASH_PRIME = 31
  }

  private val registration = mutableMapOf<Int, RequestType>()

  @Suppress("Detekt.RethrowCaughtException")
  override fun intercept(chain: Interceptor.Chain): Response {
    var response: Response
    var request = chain.request()
    // get the credential type required by this request
    val authRequestType = findRequestType(request) ?: return chain.proceed(request)
    var refreshRequested = false
    var credential: Credentials
    var owner: OWNER?
    var tryCount = 0
    do {
      try {
        lock()
        owner = ownerManager.getActiveOwner(authRequestType.ownerType)
          ?: ownerManager.getOwners(authRequestType.ownerType).firstOrNull()
            ?.also { ownerManager.switchActiveOwner(authRequestType.ownerType, it) }
        if (owner != null) {
          // get the credential of the owner
          val localToken =
            credentialStorage.getCredentials(owner, authRequestType.credentialType)
              ?: throw AuthenticationRequiredException()
          // if the credential is still valid and no refresh has been requested
          credential = if (authenticator.isCredentialValid(localToken) && !refreshRequested) {
            localToken
          } else {
            // try to refreshing the credentials
            val refreshedToken =
              authenticator.refreshCredentials(owner, authRequestType.credentialType, localToken)
            if (refreshedToken != null) {
              // if the credential was refreshed, store it
              credentialStorage
                .storeCredentials(owner, authRequestType.credentialType, refreshedToken)
              refreshedToken
            } else {
              // otherwise remove the current credential from the storage
              credentialStorage
                .removeCredentials(owner, authRequestType.credentialType)
              // and use the "old" credential
              localToken
            }
          }
          // authenticate the request using the credential
          request = authenticator.authenticateRequest(request, credential)
        } else {
          scope.launch {
            // async creation of an owner
            ownerManager.createOwner(authRequestType.ownerType, authRequestType.credentialType)
          }
          // cannot authorize request -> cancel running request
          throw AuthenticationRequiredException()
        }
      } catch (error: Throwable) {
        // store the error for the other requests that might be queued
        refreshLock.get().error = error
        throw error
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
  private fun lock() = runBlocking {
    if (!refreshLock.get().lock.tryLock()) {
      refreshLock.get().count.incrementAndGet()
      refreshLock.get().lock.lock()
      refreshLock.get().error?.let { throw it }
    } else {
      refreshLock.get().count.incrementAndGet()
    }
  }

  private fun unlock() {
    if (refreshLock.get().count.decrementAndGet() <= 0) {
      refreshLock.get().error = null
    }
    refreshLock.get().lock.unlock()
  }

  private fun findRequestType(
    request: Request
  ): RequestType? {
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
    val lock: Mutex = Mutex(),
    var error: Throwable? = null,
    var count: AtomicInteger = AtomicInteger(0)
  )
}
