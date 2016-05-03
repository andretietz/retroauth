package com.andretietz.retroauth;

import com.andretietz.retroauth.stub.TestTokenStorage;
import com.andretietz.retroauth.stub.TestProvider;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.http.GET;

@RunWith(MockitoJUnitRunner.class)
public class RetroauthCallAdapterFactoryTest {

    @Mock
    Type type;

    @Mock
    CallAdapter callAdapter;

    @Test
    public void adapterFactory() {
        List<CallAdapter.Factory> factories = new ArrayList<>();
        factories.add(new CallAdapter.Factory() {
            @Override
            public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
                return callAdapter;
            }
        });
        MethodCache.DefaultMethodCache<String> methodCache = new MethodCache.DefaultMethodCache<>();
        AuthenticationHandler<String, String, String> authHandler =
                new AuthenticationHandler<>(
                        methodCache,
                        Mockito.mock(OwnerManager.class),
                        new TestTokenStorage(),
                        new TestProvider()
                );

        RetroauthCallAdapterFactory adapterFactory =
                new RetroauthCallAdapterFactory<>(factories, authHandler);

        Method[] methods = TestInterface.class.getMethods();

        for (Method method : methods) {
            CallAdapter callAdapter = adapterFactory.get(type, method.getAnnotations(), null);
            if (method.getName().equals("authenticatedMethod")) {
                // the authenticated method should use the RetroauthCallAdapter
                Assert.assertTrue(callAdapter instanceof RetroauthCallAdapterFactory.RetroauthCallAdapter);
            } else {
                // the unauthenticated method should not use the one
                Assert.assertFalse(callAdapter instanceof RetroauthCallAdapterFactory.RetroauthCallAdapter);
            }
        }
    }

    interface TestInterface {
        @Authenticated({"some", "token", "type"})
        @GET("some/path")
        void authenticatedMethod();

        @GET("some/path")
        void unauthenticatedMethod();
    }
}
