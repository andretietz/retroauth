package com.andretietz.retroauth;

/**
 * The {@link AuthenticationHandler} is a class that collapses a {@link MethodCache}, an {@link OwnerManager}, a
 * {@link TokenStorage} and a {@link Provider} into one single immutable object.
 */
public class AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> {

    public final MethodCache<TOKEN_TYPE> methodCache;
    public final OwnerManager<OWNER, TOKEN_TYPE> ownerManager;
    public final TokenStorage<OWNER, TOKEN_TYPE, TOKEN> tokenStorage;
    public final Provider<OWNER, TOKEN_TYPE, TOKEN> provider;

    protected AuthenticationHandler(MethodCache<TOKEN_TYPE> methodCache,
                                    OwnerManager<OWNER, TOKEN_TYPE> ownerManager,
                                    TokenStorage<OWNER, TOKEN_TYPE, TOKEN> tokenStorage,
                                    Provider<OWNER, TOKEN_TYPE, TOKEN> provider) {

        this.methodCache = methodCache;
        this.ownerManager = ownerManager;
        this.tokenStorage = tokenStorage;
        this.provider = provider;
    }
}
