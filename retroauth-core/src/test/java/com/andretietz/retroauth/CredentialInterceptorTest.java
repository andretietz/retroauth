package com.andretietz.retroauth;

import com.andretietz.retroauth.testimpl.TestTokenStorage;
import com.andretietz.retroauth.testimpl.TestTokenTypeFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

@RunWith(MockitoJUnitRunner.class)
public class CredentialInterceptorTest {


    public static final String AUTHENTICATION_HEADER_KEY = "auth";

    @Test
    public void intercept() throws IOException {
        Request request1 = new Request.Builder().url("http://www.google.de/test1").build();
        Request request2 = new Request.Builder().url("http://www.google.de/test2").build();

        // only request2 is authenticated
        AuthenticationHandler<String, String, String> authHandler = getAuthenticationHandler(request2);
        CredentialInterceptor<String, String, String> interceptor = new CredentialInterceptor<>(authHandler, true);

        TestInterceptorChain interceptorChain = new TestInterceptorChain();

        // testing unauthenticated request
        interceptorChain.setupRequest(request1);
        Response response = interceptor.intercept(interceptorChain);
        // should not contain any headers
        Assert.assertEquals(0, response.headers().size());

        // testing authenticated request
        interceptorChain.setupRequest(request2);
        response = interceptor.intercept(interceptorChain);
        // should contain the token in the header
        Assert.assertTrue(TestTokenStorage.TEST_TOKEN.equals(response.request().header(AUTHENTICATION_HEADER_KEY)));

    }

    private AuthenticationHandler<String, String, String> getAuthenticationHandler(Request request) {
        MethodCache.DefaultMethodCache<String> methodCache = new MethodCache.DefaultMethodCache<>();
        methodCache.register(Utils.createUniqueIdentifier(request), "token-type");
        return new AuthenticationHandler<>(
                methodCache,
                Mockito.mock(OwnerManager.class),
                new TestTokenStorage(),
                new Provider<String, String, String>() {
                    @Override
                    public Request authenticateRequest(Request request, String token) {
                        return request.newBuilder().addHeader(AUTHENTICATION_HEADER_KEY, token).build();
                    }

                    @Override
                    public boolean retryRequired(int count,
                                                 Response response, TokenStorage<String, String, String> tokenStorage,
                                                 String s, String s2, String s3) {
                        return false;
                    }
                },
                new TestTokenTypeFactory()
        );
    }

    static class TestInterceptorChain implements Interceptor.Chain {
        private Request request;

        public void setupRequest(Request request) {
            this.request = request;
        }

        @Override
        public Request request() {
            return request;
        }

        @Override
        public Response proceed(Request request) throws IOException {
            return new Response.Builder()
                    .request(request)
                    .code(200)
                    .protocol(Protocol.HTTP_1_1)
                    .message("required message")
                    .build();
        }

        @Override
        public Connection connection() {
            return null;
        }
    }

}
