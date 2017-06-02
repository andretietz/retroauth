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
final class RetroauthCallAdapterFactory<OWNER, TOKEN_TYPE extends TokenType, TOKEN> extends CallAdapter.Factory {

    /**
     * registered {@link retrofit2.CallAdapter.Factory}s.
     */
    private final List<CallAdapter.Factory> callAdapterFactories;
    private final AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler;

    RetroauthCallAdapterFactory(List<CallAdapter.Factory> callAdapterFactories,
                                AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler) {
        this.callAdapterFactories = callAdapterFactories;
        this.authHandler = authHandler;
    }

    /**
     * checks if an {@link Authenticated} annotation exists on this request.
     *
     * @param annotations annotations to check
     * @return if the {@link Authenticated} annotation exists it returns it, otherwise {@code null}
     */
    private static Authenticated isAuthenticated(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (Authenticated.class == annotation.annotationType()) {
                return (Authenticated) annotation;
            }
        }
        return null;
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Authenticated auth = isAuthenticated(annotations);
        for (int i = 0; i < callAdapterFactories.size(); i++) {
            CallAdapter<?, ?> adapter = callAdapterFactories.get(i).get(returnType, annotations, retrofit);
            if (adapter != null) {
                if (auth != null) {
                    TOKEN_TYPE tokenType = authHandler.typeFactory.create(auth.value());
                    return new RetroauthCallAdapter<>((CallAdapter<Object, Object>) adapter,
                            tokenType, authHandler.methodCache);
                }
                return adapter;
            }
        }
        return null;
    }

    /**
     * This {@link CallAdapter} is a wrapper adapter. After registering the request as an
     * authenticated request, it executes the given {@link CallAdapter#adapt(Call)} call
     *
     * @param <TOKEN_TYPE>  Type of Token to use
     * @param <RETURN_TYPE> Return type of the call
     */
    static final class RetroauthCallAdapter<TOKEN_TYPE extends TokenType, RETURN_TYPE>
            implements CallAdapter<Object, RETURN_TYPE> {

        private final CallAdapter<Object, RETURN_TYPE> adapter;
        private final TOKEN_TYPE type;
        private final MethodCache<TOKEN_TYPE> registration;

        RetroauthCallAdapter(CallAdapter<Object, RETURN_TYPE> adapter, TOKEN_TYPE type, MethodCache<TOKEN_TYPE> reg) {
            this.adapter = adapter;
            this.type = type;
            this.registration = reg;
        }

        @Override
        public Type responseType() {
            return adapter.responseType();
        }

        @Override
        public RETURN_TYPE adapt(Call<Object> call) {
            Request request = call.request();
            registration.register(Utils.createUniqueIdentifier(request), type);
            return adapter.adapt(call);
        }
    }
}
