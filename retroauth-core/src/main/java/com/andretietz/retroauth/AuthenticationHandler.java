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
