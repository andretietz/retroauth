package com.andretietz.retroauth;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationHandlerTest {

    @Mock
    MethodCache<String> methodCache;
    @Mock
    OwnerManager<String, String> ownerManager;
    @Mock
    TokenStorage<String, String, String> tokenStorage;
    @Mock
    TokenProvider<String, String, String> provider;
    @Mock
    TokenTypeFactory<String> typeFactory;

    @Test
    public void allocate() {
        AuthenticationHandler<String, String, String> authHandler =
                new AuthenticationHandler<>(methodCache, ownerManager, tokenStorage, provider, typeFactory);
        Assert.assertNotNull(authHandler.getMethodCache());
        Assert.assertNotNull(authHandler.getOwnerManager());
        Assert.assertNotNull(authHandler.getTokenStorage());
        Assert.assertNotNull(authHandler.getProvider());
        Assert.assertNotNull(authHandler.getTypeFactory());
    }
}
