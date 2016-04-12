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

public class BasicAuthenticationHandler<S, T, U> implements AuthenticationHandler<S, U> {

    private final ExecutorService executorService;
    private final TokenStorage<S, T> storage;
    private final TokenApi<S, T, U> tokenApi;
    private final ReentrantLock lock;
    private U refreshApi;

    public BasicAuthenticationHandler(
          ExecutorService executorService,
          TokenApi<S, T, U> tokenApi,
          TokenStorage<S, T> storage) {
        this.executorService = executorService;
        this.storage = storage;
        this.tokenApi = tokenApi;
        this.lock = new ReentrantLock();
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
            StoreTokenFuture<S, T, U> tokenFuture = new StoreTokenFuture<>(request, lock, tokenApi, storage, type);
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
                try {
                    RefreshFuture<S, T> refreshTask = new RefreshFuture<>(lock, storage, type);
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
    public void setRefreshApi(U refreshApi) {
        this.refreshApi = refreshApi;
    }

    private static class RefreshFuture<S, T> implements Callable<Boolean>, TokenApi.OnTokenReceiveListener<T> {

        private final S type;
        private final Lock lock;
        private final Condition condition;
        private final TokenStorage<S, T> storage;

        private boolean refreshEnabled = false;

        RefreshFuture(Lock lock, TokenStorage<S, T> storage, S type) {
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
        public void onTokenReceive(T token) {
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

    private static class TokenFuture<S, T, U> implements Callable<Request> {
        final Request request;
        final TokenApi<S, T, U> tokenApi;
        T token;

        TokenFuture(Request request, TokenApi<S, T, U> tokenApi) {
            this(request, tokenApi, null);
        }

        TokenFuture(Request request, TokenApi<S, T, U> tokenApi, T token) {
            this.request = request;
            this.tokenApi = tokenApi;
            this.token = token;
        }

        @Override
        public Request call() throws Exception {
            return tokenApi.modifyRequest(token, request);
        }
    }


    private static final class StoreTokenFuture<S, T, U> extends TokenFuture<S, T, U> implements TokenApi
          .OnTokenReceiveListener<T> {
        private final TokenStorage<S, T> storage;
        private final S type;
        private final Lock lock;
        private final Condition condition;

        StoreTokenFuture(Request request, Lock lock, TokenApi<S, T, U> tokenApi, TokenStorage<S, T> storage, S type) {
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
            } finally {
                lock.unlock();
            }
            storage.saveToken(type, token);
            return super.call();
        }

        @Override
        public void onTokenReceive(T token) {
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