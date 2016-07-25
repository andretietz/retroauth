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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.Retrofit2Platform;

/**
 * This is the wrapper builder to create the {@link Retrofit} object.
 */
public final class Retroauth {

    private Retroauth() {
        throw new RuntimeException("no instances allowed");
    }


    public static final class Builder<OWNER, TOKEN_TYPE, TOKEN> {

        private final Retrofit.Builder builder;
        private final AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler;
        private final List<CallAdapter.Factory> callAdapterFactories;
        private OkHttpClient okHttpClient;
        private Executor executor;
        private boolean enableLocking = false;

        public Builder(AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler) {
            builder = new Retrofit.Builder();
            this.authHandler = authHandler;
            this.callAdapterFactories = new LinkedList<>();
        }

        /**
         * {@link retrofit2.Retrofit.Builder#client(OkHttpClient)}
         */
        @SuppressWarnings("unused")
        public Builder<OWNER, TOKEN_TYPE, TOKEN> client(OkHttpClient client) {
            this.okHttpClient = client;
            return this;
        }

        /**
         * {@link retrofit2.Retrofit.Builder#baseUrl(HttpUrl)}
         */
        @SuppressWarnings("unused")
        public Builder<OWNER, TOKEN_TYPE, TOKEN> baseUrl(HttpUrl baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }

        /**
         * {@link retrofit2.Retrofit.Builder#baseUrl(String)}
         */
        @SuppressWarnings("unused")
        public Builder<OWNER, TOKEN_TYPE, TOKEN> baseUrl(String baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }

        /**
         * {@link retrofit2.Retrofit.Builder#addConverterFactory(Converter.Factory)}
         */
        @SuppressWarnings("unused")
        public Builder<OWNER, TOKEN_TYPE, TOKEN> addConverterFactory(Converter.Factory factory) {
            builder.addConverterFactory(factory);
            return this;
        }

        /**
         * {@link retrofit2.Retrofit.Builder#addCallAdapterFactory(CallAdapter.Factory)}
         */
        @SuppressWarnings("unused")
        public Builder<OWNER, TOKEN_TYPE, TOKEN> addCallAdapterFactory(CallAdapter.Factory factory) {
            callAdapterFactories.add(factory);
            return this;
        }

        /**
         * {@link retrofit2.Retrofit.Builder#callbackExecutor(Executor)}
         */
        @SuppressWarnings("unused")
        public Builder<OWNER, TOKEN_TYPE, TOKEN> callbackExecutor(Executor executor) {
            this.executor = executor;
            builder.callbackExecutor(executor);
            return this;
        }

        /**
         * {@link retrofit2.Retrofit.Builder#validateEagerly(boolean)}
         */
        @SuppressWarnings("unused")
        public Builder<OWNER, TOKEN_TYPE, TOKEN> validateEagerly(boolean validateEagerly) {
            builder.validateEagerly(validateEagerly);
            return this;
        }

        /**
         * If this flag is set to <code>true</code> requests will be locked per token. Meaning that only one
         * request will be executed at once. Other requests will queue up. Consider using different threads for each
         * request when using this option, since the thread will be blocked until it can be executed. By default this option
         * is set to <code>false</code>.
         *
         * @param enableLocking locking the request until it's finished executing
         */
        public Builder<OWNER, TOKEN_TYPE, TOKEN> enableLocking(boolean enableLocking) {
            this.enableLocking = enableLocking;
            return this;
        }

        /**
         * @return a {@link Retrofit} instance using the given parameter.
         */
        @SuppressWarnings("unused")
        public Retrofit build() {

            // after adding the retrofit default callAdapter factories
            callAdapterFactories.add(Retrofit2Platform.defaultCallAdapterFactory(executor));

            // creating a custom calladapter to handle authentication
            CallAdapter.Factory callAdapter =
                    new RetroauthCallAdapterFactory<>(callAdapterFactories, authHandler);

            // use this callAdapter to create the retrofit object
            builder.addCallAdapterFactory(callAdapter);

            OkHttpClient.Builder builder = (okHttpClient != null) ? okHttpClient.newBuilder() : new OkHttpClient.Builder();

            // create the okhttp interceptor to intercept requests
            CredentialInterceptor<OWNER, TOKEN_TYPE, TOKEN> interceptor =
                    new CredentialInterceptor<>(authHandler, enableLocking);

            // add it as the first interceptor to be used
            builder.interceptors().add(interceptor);

            // add the newly created okhttpclient as callFactory
            this.builder.callFactory(builder.build());

            // create the retrofit object
            return this.builder.build();
        }
    }
}
