package com.andretietz.retroauth;

import okhttp3.Request;

/**
 * Created by andre.tietz on 30/03/16.
 */
public interface TokenInjector {
    Request inject(Request originalRequest, String token);
}
