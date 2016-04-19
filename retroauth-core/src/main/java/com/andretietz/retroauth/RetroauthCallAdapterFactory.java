package com.andretietz.retroauth;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * This is a {@link retrofit2.CallAdapter.Factory} implementation for handling annotated
 * requests using retrofit2.
 */
final class RetroauthCallAdapterFactory<OWNER, TOKEN_TYPE, TOKEN> extends CallAdapter.Factory {

    /**
     * registered {@link retrofit2.CallAdapter.Factory}s.
     */
    private final List<CallAdapter.Factory> callAdapterFactories;

    /**
     *
     */
    private final AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler;

    RetroauthCallAdapterFactory(List<CallAdapter.Factory> callAdapterFactories,
                                AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler) {
        this.callAdapterFactories = callAdapterFactories;
        this.authHandler = authHandler;
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Authenticated auth = isAuthenticated(annotations);
        for (int i = 0; i < callAdapterFactories.size(); i++) {
            CallAdapter<?> adapter = callAdapterFactories.get(i).get(returnType, annotations, retrofit);
            if (adapter != null)
                return (auth != null)
                        ? new RetroauthCallAdapter<>(
                        adapter,
                        authHandler.tokenStorage.createType(auth.value()),
                        authHandler.methodCache)
                        : adapter;
        }
        return null;
    }

    /**
     * checks if an {@link Authenticated} annotation exists on this request.
     *
     * @param annotations annotations to check
     * @return if the {@link Authenticated} annotation exists it returns it, otherwise {@code null}
     */
    private Authenticated isAuthenticated(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (Authenticated.class == annotation.annotationType()) {
                return (Authenticated) annotation;
            }
        }
        return null;
    }

    /**
     * This {@link CallAdapter} is a wrapper adapter. After registering the request as an
     * authenticated request, it executes the given {@link CallAdapter#adapt(Call)} call
     *
     * @param <TOKEN_TYPE> Type of Token to use
     * @param <RETURN_TYPE> Return type of the call
     */
    private static final class RetroauthCallAdapter<TOKEN_TYPE, RETURN_TYPE>
            implements CallAdapter<RETURN_TYPE> {

        private final CallAdapter<RETURN_TYPE> adapter;
        private final TOKEN_TYPE type;
        private final MethodCache<TOKEN_TYPE> registration;

        RetroauthCallAdapter(CallAdapter<RETURN_TYPE> adapter, TOKEN_TYPE type, MethodCache<TOKEN_TYPE> reg) {
            this.adapter = adapter;
            this.type = type;
            this.registration = reg;
        }

        @Override
        public Type responseType() {
            return adapter.responseType();
        }

        @Override
        public <R> RETURN_TYPE adapt(Call<R> call) {
            Request request = call.request();
            registration.register(Utils.createUniqueIdentifier(request), type);
            return adapter.adapt(call);
        }
    }
}
