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
import java.net.HttpURLConnection

/**
 * The Authenticator interface is a very specific provider endpoint dependent implementation,
 * to authenticate your request and defines when or if to retry.
 * This needs to be an abstract class in order to be java 7 compatible (android).
 */
abstract class Authenticator<OWNER : Any, CREDENTIAL : Any> {

  /**
   * @param credentialType type of the credential reached in from the [Authorize.credentialType]
   * Annotation of the request.
   *
   * @return type of the credential
   */
  abstract fun getCredentialType(credentialType: Int = 0): CredentialType

  /**
   * @param ownerType type of the owner handed in, from the [Authorize.ownerType]
   * Annotation of the request.
   */
  abstract fun getOwnerType(ownerType: Int = 0): String

  /**
   * Authenticates a [Request].
   *
   * @param request request to authenticate
   * @param credential Token to authenticate
   * @return a modified version of the incoming request, which is authenticated
   */
  abstract fun authenticateRequest(request: Request, credential: CREDENTIAL): Request

  /**
   * Checks if the credential needs to be refreshed or not.
   *
   * @param count value contains how many times this request has been executed already
   * @param response response to check what the result was
   * @return {@code true} if a credential refresh is required, {@code false} if not
   */
  open fun refreshRequired(count: Int, response: Response): Boolean =
    response.code == HttpURLConnection.HTTP_UNAUTHORIZED && count <= 1

  /**
   * This method will be called when [isCredentialValid] returned false or [refreshRequired] returned true.
   *
   * @param credential of the local [CredentialStorage]
   */
  @Suppress("UNUSED_PARAMETER")
  open fun refreshCredentials(
    owner: OWNER,
    credentialType: CredentialType,
    credential: CREDENTIAL
  ): CREDENTIAL? = credential

  /**
   * This method is called on each authenticated request, to make sure the current credential is still valid.
   *
   * @param credential The current credential
   */
  @Suppress("UNUSED_PARAMETER")
  open fun isCredentialValid(credential: CREDENTIAL): Boolean = true
}
