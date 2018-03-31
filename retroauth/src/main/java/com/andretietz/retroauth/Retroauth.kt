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
import retrofit2.Retrofit2Platform
import java.util.LinkedList
import java.util.concurrent.Executor

/**
 * This is the wrapper builder to create the [Retrofit] object.
 */
class Retroauth private constructor() {

    class Builder<OWNER : Any, TOKEN_TYPE : Any, TOKEN : Any>(
            private val authHandler: AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN>
    ) {

        private val builder: Retrofit.Builder = Retrofit.Builder()
        private val callAdapterFactories: MutableList<CallAdapter.Factory>
        private var okHttpClient: OkHttpClient? = null
        private var executor: Executor? = null

        init {
            this.callAdapterFactories = LinkedList()
        }

        /**
         * [retrofit2.Retrofit.Builder.client]
         */
        fun client(client: OkHttpClient): Builder<OWNER, TOKEN_TYPE, TOKEN> {
            this.okHttpClient = client
            return this
        }

        /**
         * [retrofit2.Retrofit.Builder.baseUrl]
         */
        fun baseUrl(baseUrl: HttpUrl): Builder<OWNER, TOKEN_TYPE, TOKEN> {
            this.builder.baseUrl(baseUrl)
            return this
        }

        /**
         * [retrofit2.Retrofit.Builder.baseUrl]
         */
        fun baseUrl(baseUrl: String): Builder<OWNER, TOKEN_TYPE, TOKEN> {
            this.builder.baseUrl(baseUrl)
            return this
        }

        /**
         * [retrofit2.Retrofit.Builder.addConverterFactory]
         */
        fun addConverterFactory(factory: Converter.Factory): Builder<OWNER, TOKEN_TYPE, TOKEN> {
            this.builder.addConverterFactory(factory)
            return this
        }

        /**
         * [retrofit2.Retrofit.Builder.addCallAdapterFactory]
         */
        fun addCallAdapterFactory(factory: CallAdapter.Factory): Builder<OWNER, TOKEN_TYPE, TOKEN> {
            this.callAdapterFactories.add(factory)
            return this
        }

        /**
         * [retrofit2.Retrofit.Builder.callbackExecutor]
         */
        fun callbackExecutor(executor: Executor): Builder<OWNER, TOKEN_TYPE, TOKEN> {
            this.executor = executor
            this.builder.callbackExecutor(executor)
            return this
        }

        /**
         * [retrofit2.Retrofit.Builder.validateEagerly]
         */
        fun validateEagerly(validateEagerly: Boolean): Builder<OWNER, TOKEN_TYPE, TOKEN> {
            this.builder.validateEagerly(validateEagerly)
            return this
        }

        /**
         * @return a [Retrofit] instance using the given parameter.
         */
        fun build(): Retrofit {

            // after adding the retrofit default callAdapter factories
            callAdapterFactories.add(Retrofit2Platform.defaultCallAdapterFactory(executor))

            // creating a custom calladapter to handle authentication
            val callAdapter = RetroauthCallAdapterFactory(callAdapterFactories,
                    authHandler.provider,
                    authHandler.methodCache)

            // use this callAdapter to create the retrofit object
            this.builder.addCallAdapterFactory(callAdapter)

            val builder = okHttpClient?.newBuilder() ?: OkHttpClient.Builder()

            // create the okhttp interceptor to intercept requests
            val interceptor = CredentialInterceptor(
                    authHandler.provider,
                    authHandler.ownerManager,
                    authHandler.tokenStorage,
                    authHandler.methodCache
            )

            // add it as the first interceptor to be used
            builder.interceptors().add(interceptor)

            // add the newly created okhttpclient as callFactory
            this.builder.callFactory(builder.build())

            // create the retrofit object
            return this.builder.build()
        }
    }
}
