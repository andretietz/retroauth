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
 * This is the wrapper builder to create the {@link Retrofit} object
 */
public final class Retroauth {

    private Retroauth() {
        throw new RuntimeException("no instances allowed");
    }


    public static final class Builder<S, T> {

        private final Retrofit.Builder builder;
        private final AuthenticationHandler<S, T> authHandler;
        private final List<CallAdapter.Factory> callAdapterFactories;
        private final Class<T> refreshApi;
        private MethodCache<S> methodCache;
        private OkHttpClient okHttpClient;
        private Executor executor;

        public Builder(AuthenticationHandler<S, T> authHandler) {
            this(authHandler, null);
        }
        public Builder(AuthenticationHandler<S, T> authHandler, Class<T> refreshService) {
            builder = new Retrofit.Builder();
            this.authHandler = authHandler;
            this.callAdapterFactories = new LinkedList<>();
            this.refreshApi = refreshService;
        }

        public Builder<S, T> client(OkHttpClient client) {
            this.okHttpClient = client;
            return this;
        }

        public Builder<S, T> baseUrl(HttpUrl baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }

        public Builder<S, T> baseUrl(String baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }


        public Builder<S, T> addConverterFactory(Converter.Factory factory) {
            builder.addConverterFactory(factory);
            return this;
        }

        public Builder<S, T> addCallAdapterFactory(CallAdapter.Factory factory) {
            callAdapterFactories.add(factory);
            return this;
        }

        public Builder<S, T> callbackExecutor(Executor executor) {
            this.executor = executor;
            builder.callbackExecutor(executor);
            return this;
        }

        public Builder<S, T> validateEagerly(boolean validateEagerly) {
            builder.validateEagerly(validateEagerly);
            return this;
        }

        public Builder<S, T> methodCache(MethodCache<S> methodCache) {
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
            Retrofit retrofit =  this.builder.build();
            if(refreshApi != null)
                authHandler.setRefreshApi(retrofit.create(refreshApi));
            return retrofit;
        }
    }

}
