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

        public Builder(AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler) {
            builder = new Retrofit.Builder();
            this.authHandler = authHandler;
            this.callAdapterFactories = new LinkedList<>();
        }

        public Builder<OWNER, TOKEN_TYPE, TOKEN> client(OkHttpClient client) {
            this.okHttpClient = client;
            return this;
        }

        public Builder<OWNER, TOKEN_TYPE, TOKEN> baseUrl(HttpUrl baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }

        public Builder<OWNER, TOKEN_TYPE, TOKEN> baseUrl(String baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }


        public Builder<OWNER, TOKEN_TYPE, TOKEN> addConverterFactory(Converter.Factory factory) {
            builder.addConverterFactory(factory);
            return this;
        }

        public Builder<OWNER, TOKEN_TYPE, TOKEN> addCallAdapterFactory(CallAdapter.Factory factory) {
            callAdapterFactories.add(factory);
            return this;
        }

        public Builder<OWNER, TOKEN_TYPE, TOKEN> callbackExecutor(Executor executor) {
            this.executor = executor;
            builder.callbackExecutor(executor);
            return this;
        }

        public Builder<OWNER, TOKEN_TYPE, TOKEN> validateEagerly(boolean validateEagerly) {
            builder.validateEagerly(validateEagerly);
            return this;
        }

        public Retrofit build() {
            callAdapterFactories.add(Retrofit2Platform.defaultCallAdapterFactory(executor));

            CallAdapter.Factory callAdapter =
                    new RetroauthCallAdapterFactory<>(callAdapterFactories, authHandler);

            builder.addCallAdapterFactory(callAdapter);

            if (okHttpClient == null) okHttpClient = new OkHttpClient();
            OkHttpClient.Builder builder = okHttpClient.newBuilder();
            CredentialInterceptor<OWNER, TOKEN_TYPE, TOKEN> interceptor = new CredentialInterceptor<>(authHandler);
            builder.interceptors().add(0, interceptor);

            this.builder.callFactory(builder.build());
            Retrofit retrofit = this.builder.build();
            interceptor.retrofit(retrofit);
            return retrofit;
        }
    }

}
