package com.andretietz.retroauth;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import okhttp3.HttpUrl;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

@RunWith(MockitoJUnitRunner.class)
public class RetroauthBuilderTest {

    @Mock
    Provider<String, String, String> provider;
    @Mock
    MethodCache<String> methodCache;
    @Mock
    OwnerManager<String, String> ownerManager;
    @Mock
    TokenStorage<String, String, String> tokenStorage;

    @Test
    public void builder() {
        AuthenticationHandler<String, String, String> authHandler =
                new AuthenticationHandler<>(
                        methodCache,
                        ownerManager,
                        tokenStorage,
                        provider
                );

        HttpUrl url = HttpUrl.parse("https://github.com/andretietz/retroauth/");

        Retrofit retrofit = new Retroauth.Builder<>(authHandler)
                .baseUrl(url)
                .build();

        List<CallAdapter.Factory> factories = retrofit.callAdapterFactories();
        // There should be 2 factories. one for retroauth and a retrofit default one
        Assert.assertEquals(2, factories.size());
        // The first one must be the RetroauthCallAdapterFactory
        Assert.assertTrue(factories.get(0) instanceof RetroauthCallAdapterFactory);
        // base url should be the same as defined
        Assert.assertTrue(url.equals(retrofit.baseUrl()));
    }
}
