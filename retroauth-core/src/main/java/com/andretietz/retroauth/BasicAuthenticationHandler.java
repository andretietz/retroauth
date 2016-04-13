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

public class BasicAuthenticationHandler<TOKEN_TYPE, TOKEN, REFRESH_API> implements AuthenticationHandler<TOKEN_TYPE, REFRESH_API> {

    private final ExecutorService executorService;
    private final TokenStorage<TOKEN_TYPE, TOKEN> storage;
    private final TokenApi<TOKEN_TYPE, TOKEN, REFRESH_API> tokenApi;
    private final ReentrantLock lock;
    private REFRESH_API refreshApi;

    public BasicAuthenticationHandler(
          ExecutorService executorService,
          TokenApi<TOKEN_TYPE, TOKEN, REFRESH_API> tokenApi,
          TokenStorage<TOKEN_TYPE, TOKEN> storage) {
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
        TOKEN token = storage.getToken(type);
        RunnableFuture<Request> future;
        if (token == null) {
            StoreTokenFuture<TOKEN_TYPE, TOKEN, REFRESH_API> tokenFuture = new StoreTokenFuture<>(request, lock, tokenApi, storage, type);
            future = new FutureTask<>(tokenFuture);
            executorService.submit(future);
            tokenApi.receiveToken(tokenFuture);
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
                storage.removeToken(type);
                try {
                    RefreshFuture<TOKEN_TYPE, TOKEN> refreshTask = new RefreshFuture<>(lock, storage, type);
                    FutureTask<Boolean> future = new FutureTask<>(refreshTask);
                    executorService.submit(future);
                    tokenApi.refreshToken(refreshApi, refreshTask);
                    return future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

            }
        }
        return false;
    }

    @Override
    public void setRefreshApi(REFRESH_API refreshApi) {
        this.refreshApi = refreshApi;
    }

    private static class RefreshFuture<TOKEN_TYPE, TOKEN> implements Callable<Boolean>, TokenApi.OnTokenReceiveListener<TOKEN> {

        private final TOKEN_TYPE type;
        private final Lock lock;
        private final Condition condition;
        private final TokenStorage<TOKEN_TYPE, TOKEN> storage;

        private boolean refreshEnabled = false;

        RefreshFuture(Lock lock, TokenStorage<TOKEN_TYPE, TOKEN> storage, TOKEN_TYPE type) {
            this.type = type;
            this.lock = lock;
            this.storage = storage;
            this.condition = lock.newCondition();
        }

        @Override
        public Boolean call() throws Exception {
            lock.lock();
            try {
                condition.await();
            } finally {
                lock.unlock();
            }
            return refreshEnabled;
        }

        @Override
        public void onTokenReceive(TOKEN token) {
            storage.saveToken(type, token);
            lock.lock();
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void onCancel() {
            refreshEnabled = false;
            lock.lock();
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    private static class TokenFuture<TOKEN_TYPE, TOKEN, U> implements Callable<Request> {
        final Request request;
        final TokenApi<TOKEN_TYPE, TOKEN, U> tokenApi;
        TOKEN token;

        TokenFuture(Request request, TokenApi<TOKEN_TYPE, TOKEN, U> tokenApi) {
            this(request, tokenApi, null);
        }

        TokenFuture(Request request, TokenApi<TOKEN_TYPE, TOKEN, U> tokenApi, TOKEN token) {
            this.request = request;
            this.tokenApi = tokenApi;
            this.token = token;
        }

        @Override
        public Request call() throws Exception {
            return tokenApi.modifyRequest(token, request);
        }
    }


    private static final class StoreTokenFuture<TOKEN_TYPE, TOKEN, U> extends TokenFuture<TOKEN_TYPE, TOKEN, U> implements TokenApi
          .OnTokenReceiveListener<TOKEN> {
        private final TokenStorage<TOKEN_TYPE, TOKEN> storage;
        private final TOKEN_TYPE type;
        private final Lock lock;
        private final Condition condition;

        StoreTokenFuture(Request request, Lock lock, TokenApi<TOKEN_TYPE, TOKEN, U> tokenApi, TokenStorage<TOKEN_TYPE, TOKEN> storage, TOKEN_TYPE type) {
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
        public void onTokenReceive(TOKEN token) {
            this.token = token;
            lock.lock();
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void onCancel() {
            lock.lock();
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
        }
    }
}