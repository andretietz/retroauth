package com.andretietz.retroauth;

import com.andretietz.retroauth.dummy.DummyOwnerManager;
import com.andretietz.retroauth.dummy.DummyTokenStorage;
import com.andretietz.retroauth.dummy.TestProvider;

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
    Provider<String, String, String> provider;

    @Test
    public void allocate() {
        AuthenticationHandler<String, String, String> authHandler =
                new AuthenticationHandler<>(methodCache, ownerManager, tokenStorage, provider);
        Assert.assertNotNull(authHandler.methodCache);
        Assert.assertNotNull(authHandler.ownerManager);
        Assert.assertNotNull(authHandler.tokenStorage);
        Assert.assertNotNull(authHandler.provider);
    }
}
