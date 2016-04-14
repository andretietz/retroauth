package com.andretietz.retroauth;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by andre.tietz on 22/03/16.
 */
public final class RetroauthCallAdapterFactory<TOKEN_TYPE> extends CallAdapter.Factory {

    private final List<CallAdapter.Factory> callAdapterFactories;
    private final MethodCache<TOKEN_TYPE> registration;
    private final AuthenticationHandler<TOKEN_TYPE> authHandler;

    RetroauthCallAdapterFactory(List<CallAdapter.Factory> callAdapterFactories, AuthenticationHandler<TOKEN_TYPE> authHandler, MethodCache<TOKEN_TYPE> registration) {
        this.callAdapterFactories = callAdapterFactories;
        this.registration = registration;
        this.authHandler = authHandler;
    }

    public void addCallAdapterFactory(CallAdapter.Factory factory) {
        callAdapterFactories.add(factory);
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Authenticated auth = isAuthenticated(annotations);
        for (int i = 0; i < callAdapterFactories.size(); i++) {
            CallAdapter<?> adapter = callAdapterFactories.get(i).get(returnType, annotations, retrofit);
            if (adapter != null)
                return (auth != null) ? new RetroauthCallAdapter<>(adapter, authHandler.convert(auth.value()), registration) : adapter;
        }
        return null;
    }

    private Authenticated isAuthenticated(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (Authenticated.class == annotation.annotationType()) {
                return (Authenticated) annotation;
            }
        }
        return null;
    }

    private static class RetroauthCallAdapter<S, T> implements CallAdapter<T> {

        private final CallAdapter<T> adapter;
        private final S type;
        private final MethodCache<S> registration;

        RetroauthCallAdapter(CallAdapter<T> adapter, S type, MethodCache<S> reg) {
            this.adapter = adapter;
            this.type = type;
            this.registration = reg;
        }

        @Override
        public Type responseType() {
            return adapter.responseType();
        }

        @Override
        public <R> T adapt(Call<R> call) {
            Request request = call.request();
            registration.register(Utils.createUniqueIdentifier(request), type);
            return adapter.adapt(call);
        }
    }
}
