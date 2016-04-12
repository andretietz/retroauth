package com.andretietz.retroauth;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class CredentialInterceptor<S, T> implements Interceptor {
    private final AuthenticationHandler<S, T> authHandler;
    private final MethodCache<S> cache;

    public CredentialInterceptor(AuthenticationHandler<S, T> authHandler, MethodCache<S> cache) {
        this.cache = cache;
        this.authHandler = authHandler;
    }

    @Override
    public synchronized Response intercept(Chain chain) throws IOException {
        Response response;
        Request request = chain.request();
        S type = cache.getTokenType(Utils.createUniqueIdentifier(request));
        if (type != null) {
            int tryCount = 0;
            do {
                try {
                    // execute the request
                    request = authHandler.handleAuthentication(request, type);
                    response = chain.proceed(request);
                } catch (Exception e) {
                    throw new AuthenticationCanceledException(e);
                }
            } while (authHandler.retryRequired(++tryCount, response, type));
        } else {
            response = chain.proceed(request);
        }
        return response;
    }
}
