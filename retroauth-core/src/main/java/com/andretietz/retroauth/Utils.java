package com.andretietz.retroauth;

import okhttp3.Request;

/**
 * This util class is only used to create a
 */
public class Utils {
    public static int createUniqueIdentifier(Request request) {
        return (request.url().toString() + request.method()).hashCode();
    }
}
