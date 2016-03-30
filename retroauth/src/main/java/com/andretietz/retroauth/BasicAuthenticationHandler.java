package com.andretietz.retroauth;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import okhttp3.Request;
import okhttp3.Response;

public class BasicAuthenticationHandler<S, T> implements AuthenticationHandler<S> {

    private final ExecutorService executorService;
    private final TokenStorage<S, T> storage;
    private final TokenApi<S, T> tokenApi;

    public BasicAuthenticationHandler(
          ExecutorService executorService,
          TokenApi<S, T> tokenApi,
          TokenStorage<S, T> storage) {
        this.executorService = executorService;
        this.storage = storage;
        this.tokenApi = tokenApi;
    }

    @Override
    public S convert(String[] annotationValues) {
        return tokenApi.convert(annotationValues);
    }

    @Override
    public Request handleAuthentication(Request request, S type) throws Exception {
        T token = storage.getToken(type);
        RunnableFuture<Request> future;
        if (token == null) {
            StoreTokenFuture<S, T> tokenFuture = new StoreTokenFuture<>(request, tokenApi, storage, type);
            tokenApi.receiveToken(tokenFuture);
            future = new FutureTask<>(tokenFuture);
        } else {
            future = new FutureTask<>(new TokenFuture<>(request, tokenApi, token));
        }
        executorService.submit(future);
        return future.get();
    }

    @Override
    public boolean retryRequired(int count, Response response, S type) {
        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                storage.removeToken(type);
                return (count < 2);
            }
        }
        return false;
    }

    private static class TokenFuture<S, T> implements Callable<Request> {
        final Request request;
        final TokenApi<S, T> tokenApi;
        T token;

        TokenFuture(Request request, TokenApi<S, T> tokenApi) {
            this(request, tokenApi, null);
        }

        TokenFuture(Request request, TokenApi<S, T> tokenApi, T token) {
            this.request = request;
            this.tokenApi = tokenApi;
            this.token = token;
        }

        @Override
        public Request call() throws Exception {
            return tokenApi.modifyRequest(token, request);
        }
    }

    private static final class StoreTokenFuture<S, T> extends TokenFuture<S, T> implements TokenApi
          .OnTokenReceiveListener<T> {
        private final TokenStorage<S, T> storage;
        private final S type;

        StoreTokenFuture(Request request, TokenApi<S, T> tokenApi, TokenStorage<S, T> storage, S type) {
            super(request, tokenApi);
            this.storage = storage;
            this.type = type;
        }

        @Override
        public Request call() throws Exception {
            while (token == null) {
                Thread.sleep(100);
            }
            storage.saveToken(type, token);
            return super.call();
        }

        @Override
        public void onTokenReceive(T token) {
            this.token = token;
        }

        @Override
        public void onCancel() {
            // TODO: cancel future task
        }
    }
}