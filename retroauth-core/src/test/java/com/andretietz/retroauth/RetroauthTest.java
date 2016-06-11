package com.andretietz.retroauth;

import com.andretietz.retroauth.testimpl.TestInterface;
import com.andretietz.retroauth.testimpl.TestProvider;
import com.andretietz.retroauth.testimpl.TestResponse;
import com.andretietz.retroauth.testimpl.TestTokenStorage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;


/**
 * Created by andre on 11.06.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class RetroauthTest {


    private MockWebServer server;
    private TestInterface service;

    @Before
    public void prepare() throws IOException {
        server = new MockWebServer();
        server.start();
        TestTokenStorage tokenStorage = new TestTokenStorage();
        MethodCache.DefaultMethodCache<String> methodCache = new MethodCache.DefaultMethodCache<>();
        AuthenticationHandler<String, String, String> authHandler =
                new AuthenticationHandler<>(
                        methodCache,
                        Mockito.mock(OwnerManager.class),
                        tokenStorage,
                        new TestProvider()
                );

        Retrofit retrofit = new Retroauth.Builder<>(authHandler)
                .baseUrl(server.url("/"))
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

        service = retrofit.create(TestInterface.class);
    }

    @After
    public void shutdown() throws IOException {
        server.shutdown();
    }

    @Test
    public void simpleHappyCase() throws IOException {
        // test very simple case
        server.enqueue(new MockResponse().setBody("{ \"value\" : 1 }"));
        Response<TestResponse> response = service.authenticatedMethod().execute();
        Assert.assertEquals(1, response.body().value);
    }

    @Test
    public void simpleErrorCase() throws IOException {
        // test simple error case
        server.enqueue(new MockResponse().setResponseCode(400));
        Response<TestResponse> response = service.authenticatedMethod().execute();
        Assert.assertEquals(400, response.code());
    }
}
