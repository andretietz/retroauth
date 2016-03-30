package com.andretietz.retroauth;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit2Platform;
import retrofit2.Retrofit;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by andre on 21.03.2016.
 */
public final class Retroauth {

    private Retroauth() {
        throw new RuntimeException("no instances allowed");
    }


    public static final class Builder<T> {

        private final Retrofit.Builder builder;
        private final AuthenticationHandler<T> authHandler;
        private final List<CallAdapter.Factory> callAdapterFactories;
        private MethodCache<T> methodCache;
        private OkHttpClient okHttpClient;
        private Executor executor;

        public Builder(AuthenticationHandler<T> authHandler) {
            builder = new Retrofit.Builder();
            this.authHandler = authHandler;
            this.callAdapterFactories = new LinkedList<>();
        }

        public Builder<T> client(OkHttpClient client) {
            this.okHttpClient = client;
            return this;
        }

        public Builder<T> baseUrl(HttpUrl baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }

        public Builder<T> baseUrl(String baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }


        public Builder<T> addConverterFactory(Converter.Factory factory) {
            builder.addConverterFactory(factory);
            return this;
        }

        public Builder<T> addCallAdapterFactory(CallAdapter.Factory factory) {
            callAdapterFactories.add(factory);
            return this;
        }

        public Builder<T> callbackExecutor(Executor executor) {
            this.executor = executor;
            builder.callbackExecutor(executor);
            return this;
        }

        public Builder<T> validateEagerly(boolean validateEagerly) {
            builder.validateEagerly(validateEagerly);
            return this;
        }

        public Builder<T> methodCache(MethodCache<T> methodCache) {
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
            return this.builder.build();
        }
    }

}
