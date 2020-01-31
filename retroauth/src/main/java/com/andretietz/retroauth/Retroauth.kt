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

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.LinkedList
import java.util.concurrent.Executor

/**
 * This is the wrapper builder to create the [Retrofit] object.
 */
class Retroauth private constructor() {

  class Builder<out OWNER_TYPE : Any, OWNER : Any, CREDENTIAL_TYPE : Any, CREDENTIAL : Any> @JvmOverloads constructor(
    private val authenticator: Authenticator<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL>,
    private val ownerManager: OwnerStorage<OWNER_TYPE, OWNER, CREDENTIAL_TYPE>,
    private val credentialStorage: CredentialStorage<OWNER, CREDENTIAL_TYPE, CREDENTIAL>,
    private val methodCache: MethodCache<OWNER_TYPE, CREDENTIAL_TYPE> = MethodCache.DefaultMethodCache()
  ) {

    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
    private val callAdapterFactories: MutableList<CallAdapter.Factory> = LinkedList()
    private var okHttpClient: OkHttpClient? = null
    private var executor: Executor? = null

    /**
     * [retrofit2.Retrofit.Builder.client]
     */
    @Suppress("unused")
    fun client(client: OkHttpClient): Builder<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL> {
      this.okHttpClient = client
      return this
    }

    /**
     * [retrofit2.Retrofit.Builder.baseUrl]
     */
    @Suppress("unused")
    fun baseUrl(baseUrl: HttpUrl): Builder<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL> {
      this.retrofitBuilder.baseUrl(baseUrl)
      return this
    }

    /**
     * [retrofit2.Retrofit.Builder.baseUrl]
     */
    @Suppress("unused")
    fun baseUrl(baseUrl: String): Builder<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL> {
      this.retrofitBuilder.baseUrl(baseUrl)
      return this
    }

    /**
     * [retrofit2.Retrofit.Builder.addConverterFactory]
     */
    @Suppress("unused")
    fun addConverterFactory(factory: Converter.Factory): Builder<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL> {
      this.retrofitBuilder.addConverterFactory(factory)
      return this
    }

    /**
     * [retrofit2.Retrofit.Builder.addCallAdapterFactory]
     */
    @Suppress("unused")
    fun addCallAdapterFactory(factory: CallAdapter.Factory): Builder<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL> {
      this.callAdapterFactories.add(factory)
      return this
    }

    /**
     * [retrofit2.Retrofit.Builder.callbackExecutor]
     */
    @Suppress("unused")
    fun callbackExecutor(executor: Executor): Builder<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL> {
      this.executor = executor
      this.retrofitBuilder.callbackExecutor(executor)
      return this
    }

    /**
     * [retrofit2.Retrofit.Builder.validateEagerly]
     */
    @Suppress("unused")
    fun validateEagerly(validateEagerly: Boolean): Builder<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL> {
      this.retrofitBuilder.validateEagerly(validateEagerly)
      return this
    }

    /**
     * @return a [Retrofit] instance using the given parameter.
     */
    fun build(): Retrofit {

      // creating a custom calladapter to handle authentication
      val callAdapter = RetroauthCallAdapterFactory(
        authenticator,
        methodCache)

      // use this callAdapter to create the retrofit object
      retrofitBuilder.addCallAdapterFactory(callAdapter)

      callAdapterFactories.forEach { retrofitBuilder.addCallAdapterFactory(it) }

      val builder = okHttpClient?.newBuilder() ?: OkHttpClient.Builder()

      // create the okhttp interceptor to intercept requests
      val interceptor = CredentialInterceptor(
        authenticator,
        ownerManager,
        credentialStorage,
        methodCache
      )

      // add it as the first interceptor to be used
      builder.interceptors().add(interceptor)

      // add the newly created okhttpclient as callFactory
      this.retrofitBuilder.callFactory(builder.build())

      // create the retrofit object
      return this.retrofitBuilder.build()
    }
  }
}
