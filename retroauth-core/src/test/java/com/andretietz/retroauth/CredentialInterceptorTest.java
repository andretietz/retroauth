package com.andretietz.retroauth;

import com.andretietz.retroauth.dummy.DummyInterceptorChain;
import com.andretietz.retroauth.dummy.DummyOwnerManager;
import com.andretietz.retroauth.dummy.DummyTokenStorage;
import com.andretietz.retroauth.dummy.TestProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

@RunWith(MockitoJUnitRunner.class)
public class CredentialInterceptorTest {

    private Request request1 = new Request.Builder().url("http://www.google.de/test1").build();
    private Request request2 = new Request.Builder().url("http://www.google.de/test2").build();

    @Mock
    Interceptor.Chain interceptorChain;

    @Test
    public void intercept() throws IOException {
        MethodCache.DefaultMethodCache<String> methodCache = new MethodCache.DefaultMethodCache<>();
        methodCache.register(Utils.createUniqueIdentifier(request2), "token-type");
        AuthenticationHandler<String, String, String> authHandler =
                new AuthenticationHandler<>(
                        methodCache,
                        new DummyOwnerManager(),
                        new DummyTokenStorage(),
                        new TestProvider()
                );
        CredentialInterceptor<String, String, String> interceptor = new CredentialInterceptor<>(authHandler);

        Mockito.when(interceptorChain.request()).thenReturn(request1);
        Mockito.when(interceptorChain.request()).thenReturn(request2);

        Response response1 = new Response.Builder().request(request1).code(200).protocol(Protocol.HTTP_1_1).build();
        Mockito.when(interceptorChain.proceed(request1)).thenReturn(response1);


        Response response = interceptor.intercept(interceptorChain);
        response = interceptor.intercept(interceptorChain);
    }

}
