package com.andretietz.retroauth;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

public class BaseAuthenticationHandler<TOKEN_TYPE> implements AuthenticationHandler<TOKEN_TYPE> {

    private final ExecutorService executorService;
    private final TokenStorage<TOKEN_TYPE> storage;
    private final TokenApi<TOKEN_TYPE> tokenApi;
    private final ReentrantLock lock;
    private Retrofit retrofit;

    public BaseAuthenticationHandler(
            ExecutorService executorService,
            TokenApi<TOKEN_TYPE> tokenApi,
            TokenStorage<TOKEN_TYPE> storage) {
        this.executorService = executorService;
        this.storage = storage;
        this.tokenApi = tokenApi;
        this.lock = new ReentrantLock();
    }

    @Override
    public TOKEN_TYPE convert(String[] annotationValues) {
        return tokenApi.convert(annotationValues);
    }

    @Override
    public Request handleAuthentication(Request request, TOKEN_TYPE type) throws Exception {
        String token = storage.getToken(type);
        RunnableFuture<Request> future;
        if (token == null) {
            StoreTokenFuture<TOKEN_TYPE> tokenFuture = new StoreTokenFuture<>(request, lock, tokenApi, storage, type);
            future = new FutureTask<>(tokenFuture);
            executorService.submit(future);
            try {
                tokenApi.receiveToken(tokenFuture);
            } catch (Exception e) {
                future.cancel(true);
                throw e;
            }
        } else {
            future = new FutureTask<>(new TokenFuture<>(request, tokenApi, token));
            executorService.submit(future);
        }
        return future.get();
    }

    @Override
    public boolean retryRequired(int count, Response response, TOKEN_TYPE type) {
        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                // get the current token
                String token = storage.getToken(type);
                // remove the token from storage
                storage.removeToken(type, token);

                String refreshToken = storage.getRefreshToken(type);
                if(refreshToken != null) {
                    // try to refresh the token
                    token = tokenApi.refreshToken(retrofit, refreshToken);
                    // if successfull
                    if (token != null) {
                        // store token
                        storage.saveToken(type, token);
                        // and retry request
                        return true;
                    } else {
                        storage.removeRefreshToken(type, refreshToken);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void retrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }


    private static class TokenFuture<TOKEN_TYPE> implements Callable<Request> {
        final Request request;
        final TokenApi<TOKEN_TYPE> tokenApi;
        String token;

        TokenFuture(Request request, TokenApi<TOKEN_TYPE> tokenApi) {
            this(request, tokenApi, null);
        }

        TokenFuture(Request request, TokenApi<TOKEN_TYPE> tokenApi, String token) {
            this.request = request;
            this.tokenApi = tokenApi;
            this.token = token;
        }

        @Override
        public Request call() throws Exception {
            if (token != null)
                return tokenApi.modifyRequest(token, request);
            else
                return request;
        }
    }


    private static final class StoreTokenFuture<TOKEN_TYPE> extends TokenFuture<TOKEN_TYPE>
            implements TokenApi.OnTokenReceiveListener {
        private final TokenStorage<TOKEN_TYPE> storage;
        private final TOKEN_TYPE type;
        private final Lock lock;
        private final Condition condition;

        StoreTokenFuture(Request request, Lock lock, TokenApi<TOKEN_TYPE> tokenApi, TokenStorage<TOKEN_TYPE> storage, TOKEN_TYPE type) {
            super(request, tokenApi);
            this.storage = storage;
            this.type = type;
            this.lock = lock;
            this.condition = lock.newCondition();
        }

        @Override
        public Request call() throws Exception {
            lock.lock();
            try {
                condition.await();
                storage.saveToken(type, token);
            } finally {
                lock.unlock();
            }
            return super.call();
        }

        @Override
        public void onTokenReceive(String token) {
            this.token = token;
            lock.lock();
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
        }
    }
}