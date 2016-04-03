package com.andretietz.retroauth;

import okhttp3.Request;

/**
 *
 * @param <S> Token Type Object (in most cases this is a string) which will be created depending on the annotations in
 * the {@link Authenticated} annotation.
 * @param <T> class type in which the token object is represented.
 */
public interface TokenApi<S, T> {
    Request modifyRequest(T token, Request request);
    S convert(String[] annotationValues);
    void receiveToken(OnTokenReceiveListener<T> listener);

    interface OnTokenReceiveListener<T> {
        void onTokenReceive(T token);
        void onCancel();
    }
}
