package com.andretietz.retroauth;

import okhttp3.Request;

/**
 * This util class is only used to create a unique identifier for a request.
 */
class Utils {

    private Utils() {
        throw new RuntimeException("no instance allowed!");
    }

    public static int createUniqueIdentifier(Request request) {
        return (request.url().toString() + request.method()).hashCode();
    }
}
