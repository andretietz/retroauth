package com.andretietz.retroauth;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class CredentialInterceptor<TOKEN_TYPE> implements Interceptor {
    private final AuthenticationHandler<TOKEN_TYPE> authHandler;
    private final MethodCache<TOKEN_TYPE> cache;

    public CredentialInterceptor(AuthenticationHandler<TOKEN_TYPE> authHandler, MethodCache<TOKEN_TYPE> cache) {
        this.cache = cache;
        this.authHandler = authHandler;
    }

    @Override
    public synchronized Response intercept(Chain chain) throws IOException {
        Response response;
        Request request = chain.request();
        TOKEN_TYPE type = cache.getTokenType(Utils.createUniqueIdentifier(request));
        if (type != null) {
            int tryCount = 0;
            do {
                try {
                    request = authHandler.handleAuthentication(request, type);
                    // execute the request
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
