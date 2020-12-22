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

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * This is a [retrofit2.CallAdapter.Factory] implementation for handling annotated
 * requests using retrofit2.
 */
class RetroauthCallAdapterFactory<out OWNER_TYPE : Any, OWNER : Any, CREDENTIAL_TYPE : Any, CREDENTIAL : Any>(
  private val authenticator: Authenticator<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL>,
  private val methodCache: MethodCache<OWNER_TYPE, CREDENTIAL_TYPE> = MethodCache.DefaultMethodCache()
) : CallAdapter.Factory() {

  /**
   * checks if an [Authenticated] annotation exists on this request.
   *
   * @param annotations annotations to check
   * @return if the [Authenticated] annotation exists it returns it, otherwise `null`
   */
  private fun isAuthenticated(annotations: Array<Annotation>): Authenticated? {
    for (annotation in annotations) {
      if (Authenticated::class == annotation.annotationClass) return annotation as Authenticated
    }
    return null
  }

  @Suppress("UNCHECKED_CAST")
  override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
    val auth = isAuthenticated(annotations)
    val callAdapterFactories = retrofit.callAdapterFactories()
      .filterNot { it is RetroauthCallAdapterFactory<*, *, *, *> }
    for (i in callAdapterFactories.indices) {
      val adapter = callAdapterFactories[i].get(returnType, annotations, retrofit)
      adapter?.let {
        auth?.let {
          return RetroauthCallAdapter(
            adapter as CallAdapter<Any, Any>,
            authenticator.getCredentialType(auth.credentialType),
            authenticator.getOwnerType(auth.ownerType),
            methodCache)
        }
        return adapter
      }
    }
    return null
  }

  /**
   * This [CallAdapter] is a wrapper adapter. After registering the request as an
   * authenticated request, it executes the given [CallAdapter.adapt] call
   *
   * @param <CREDENTIAL_TYPE>  Type of Token to use
   * @param <RETURN_TYPE> Return type of the call
   */
  internal class RetroauthCallAdapter<OWNER_TYPE : Any, CREDENTIAL_TYPE : Any, RETURN_TYPE : Any>(
    private val adapter: CallAdapter<Any, RETURN_TYPE>,
    private val credentialType: CREDENTIAL_TYPE,
    private val ownerType: OWNER_TYPE,
    private val registration: MethodCache<OWNER_TYPE, CREDENTIAL_TYPE>
  ) : CallAdapter<Any, RETURN_TYPE> {

    override fun responseType(): Type = adapter.responseType()

    override fun adapt(call: Call<Any>): RETURN_TYPE {
      registration.register(
        Utils.createUniqueIdentifier(
          call.request()
        ), RequestType(credentialType, ownerType))
      return adapter.adapt(call)
    }
  }
}
