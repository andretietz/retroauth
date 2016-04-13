package com.andretietz.retroauth;

import okhttp3.Request;

/**
 * Created by andre on 13.04.2016.
 */
public interface TokenApplier {
    Request applyToken(String token, Request request);
}
