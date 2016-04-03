package com.andretietz.retroauth;


import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.Future;

/**
 * The implementation of this class should handle the authentication. Since this is
 * operating system dependent (i.e. Android), there can be more implementations
 *
 * @param <T> Type of the token you want to use
 */
public interface AuthenticationHandler<T> {

    /**
     * Converts the String you get from the annotation into your Token Type
     *
     * @param annotationValues the strings read out of the {@link Authenticated} annotation
     * @return the Token Type you want to use
     */
    T convert(String[] annotationValues);

    /**
     * This method must authenticate the user and modify the request if necessary
     * to achieve the {@link Request} being authenticated
     *
     * @param request to modify
     * @param type    of token required
     * @return the modified {@link Request}
     */
    Request handleAuthentication(Request request, T type) throws Exception;

    /**
     * This method tells if there's a retry required.
     *
     * @param count    number of requests already sent, starting with 1
     * @param response to check if the response was ok or not
     * @param type     token type used within this request
     * @return {@code true} if there should be a retry, {@code false} if not
     */
    boolean retryRequired(int count, Response response, T type);
}
