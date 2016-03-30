package com.andretietz.retroauth;


import java.io.IOException;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class LogInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        System.out.println(request.method() + " " + request.url());
        Set<String> names = request.headers().names();
        Headers headers = request.headers();
        for (String name : names) {
            System.out.printf("%s: %s%n", name, headers.get(name));
        }
        return chain.proceed(request);
    }
}
