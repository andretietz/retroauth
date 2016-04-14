package com.andretietz.retroauth;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.Retrofit2Platform;

/**
 * This is the wrapper builder to create the {@link Retrofit} object
 */
public final class Retroauth {

    private Retroauth() {
        throw new RuntimeException("no instances allowed");
    }


    public static final class Builder<S> {

        private final Retrofit.Builder builder;
        private final AuthenticationHandler<S> authHandler;
        private final List<CallAdapter.Factory> callAdapterFactories;
        private MethodCache<S> methodCache;
        private OkHttpClient okHttpClient;
        private Executor executor;

        public Builder(AuthenticationHandler<S> authHandler) {
            builder = new Retrofit.Builder();
            this.authHandler = authHandler;
            this.callAdapterFactories = new LinkedList<>();
        }

        public Builder<S> client(OkHttpClient client) {
            this.okHttpClient = client;
            return this;
        }

        public Builder<S> baseUrl(HttpUrl baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }

        public Builder<S> baseUrl(String baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }


        public Builder<S> addConverterFactory(Converter.Factory factory) {
            builder.addConverterFactory(factory);
            return this;
        }

        public Builder<S> addCallAdapterFactory(CallAdapter.Factory factory) {
            callAdapterFactories.add(factory);
            return this;
        }

        public Builder<S> callbackExecutor(Executor executor) {
            this.executor = executor;
            builder.callbackExecutor(executor);
            return this;
        }

        public Builder<S> validateEagerly(boolean validateEagerly) {
            builder.validateEagerly(validateEagerly);
            return this;
        }

        public Builder<S> methodCache(MethodCache<S> methodCache) {
            this.methodCache = methodCache;
            return this;
        }

        public Retrofit build() {
            if (methodCache == null) methodCache = new DefaultMethodCache<>();
            callAdapterFactories.add(Retrofit2Platform.defaultCallAdapterFactory(executor));
            CallAdapter.Factory callAdapter =
                    new RetroauthCallAdapterFactory<>(
                            callAdapterFactories, authHandler, methodCache);
            builder.addCallAdapterFactory(callAdapter);
            if (okHttpClient == null) okHttpClient = new OkHttpClient();

            List<Interceptor> interceptors = okHttpClient.interceptors();
            OkHttpClient.Builder builder = okHttpClient.newBuilder();
            builder.interceptors().clear();
            builder.addInterceptor(new CredentialInterceptor<>(authHandler, methodCache));
            for (Interceptor interceptor : interceptors) {
                builder.addInterceptor(interceptor);
            }

            this.builder.callFactory(builder.build());
            Retrofit retrofit = this.builder.build();
            authHandler.retrofit(retrofit);
            return retrofit;
        }
    }

}
