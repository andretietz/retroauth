package com.andretietz.retroauth;

/**
 * Created by andre on 15/04/16.
 */
public class AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> {
    public final MethodCache<TOKEN_TYPE> methodCache;
    public final OwnerManager<OWNER, TOKEN_TYPE> ownerManager;
    public final TokenStorage<OWNER, TOKEN_TYPE, TOKEN> tokenStorage;
    public final Provider<TOKEN> provider;

    protected AuthenticationHandler(MethodCache<TOKEN_TYPE> methodCache,
                                    OwnerManager<OWNER, TOKEN_TYPE> ownerManager,
                                    TokenStorage<OWNER, TOKEN_TYPE, TOKEN> tokenStorage,
                                    Provider<TOKEN> provider) {

        this.methodCache = methodCache;
        this.ownerManager = ownerManager;
        this.tokenStorage = tokenStorage;
        this.provider = provider;
    }

    public static class Builder<OWNER, TOKEN_TYPE, TOKEN> {
        private MethodCache<TOKEN_TYPE> methodCache;
        private OwnerManager<OWNER, TOKEN_TYPE> ownerManager;
        private TokenStorage<OWNER, TOKEN_TYPE, TOKEN> tokenStorage;
        private Provider<TOKEN> provider;

        public Builder(TokenStorage<OWNER, TOKEN_TYPE, TOKEN> storage) {
            this.tokenStorage = storage;
        }

        public Builder<OWNER, TOKEN_TYPE, TOKEN> methodCache(MethodCache<TOKEN_TYPE> methodCache) {
            this.methodCache = methodCache;
            return this;
        }

        public Builder<OWNER, TOKEN_TYPE, TOKEN> ownerManager(OwnerManager<OWNER, TOKEN_TYPE> ownerManager) {
            this.ownerManager = ownerManager;
            return this;
        }

        public Builder<OWNER, TOKEN_TYPE, TOKEN> provider(Provider<TOKEN> provider) {
            this.provider = provider;
            return this;
        }

        public AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> build() {
            if (methodCache == null) methodCache = new MethodCache.DefaultMethodCache<>();
            return new AuthenticationHandler<>(this.methodCache, this.ownerManager, this.tokenStorage, this.provider);
        }
    }
}
