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
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;


/**
 * Created by andre on 11.06.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class RetroauthTest {

    private MockWebServer server;
    private TestInterface service;
    private TestTokenStorage tokenStorage;

    @Before
    public void prepare() throws IOException {
        server = new MockWebServer();
        server.start();
        tokenStorage = new TestTokenStorage();
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
                .enableLocking(true)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
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
        server.enqueue(new MockResponse().setBody(TestInterface.TEST_BODY));
        TestResponse response = service.authenticatedMethod().toBlocking().single();
        Assert.assertEquals(1, response.value);

        server.enqueue(new MockResponse().setBody(TestInterface.TEST_BODY));
        response = service.unauthenticatedMethod().toBlocking().single();
        Assert.assertEquals(1, response.value);

    }

    @Test
    public void simpleErrorCase() throws IOException {
        TestSubscriber<TestResponse> subscriber = TestSubscriber.create();
        // test simple error case
        server.enqueue(new MockResponse().setResponseCode(400));
        service.authenticatedMethod().subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoValues();
        subscriber.assertError(HttpException.class);

        subscriber = TestSubscriber.create();
        server.enqueue(new MockResponse().setResponseCode(400));
        service.unauthenticatedMethod().subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoValues();
        subscriber.assertError(HttpException.class);
    }

    @Test
    public void userCanceledAuthentication() throws IOException, InterruptedException {
        tokenStorage.setTestBehaviour(new TestTokenStorage.TestBehaviour() {
            @Override
            public String getToken(String owner, String tokenType) throws AuthenticationCanceledException {
                throw new AuthenticationCanceledException("userCanceledAuthentication");
            }
        });

        TestSubscriber<TestResponse> subscriber = TestSubscriber.create();

        service.authenticatedMethod().subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoValues();
        subscriber.assertError(AuthenticationCanceledException.class);

        subscriber = TestSubscriber.create();
        server.enqueue(new MockResponse().setBody(TestInterface.TEST_BODY));
        service.unauthenticatedMethod().subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);

        tokenStorage.setTestBehaviour(null);
    }

    /**
     * This test should fail, if you're not using {@link Retroauth.Builder#enableLocking(boolean)}
     * with the value <code>true</code>
     */
    @Test
    public void blockingErrorCaseTest() {
        int requestCount = 100;
        TestSubscriber<TestResponse>[] subscribers = new TestSubscriber[requestCount];
        // create a lot of requests
        for (int i = 0; i < requestCount; i++) {
            subscribers[i] = TestSubscriber.create();
            service.authenticatedMethod()
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(subscribers[i]);
        }
        // create possible failing responses
        for (int i = 0; i < requestCount; i++) {
            server.enqueue(new MockResponse().setResponseCode(401));
        }

        // catch responses
        for (int i = 0; i < requestCount; i++) {
            subscribers[i].awaitTerminalEvent();
            subscribers[i].assertNoValues();
            subscribers[i].assertError(Exception.class);
        }
        // ensure that only one request has been executed
        Assert.assertEquals(1, server.getRequestCount());

    }
}
