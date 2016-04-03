package com.andretietz.retroauth;

import okhttp3.Request;

/**
 * Created by andre on 23.03.2016.
 */
public class Utils {
    public static int createUniqueIdentifier(Request request) {
        return (request.url().toString() + request.method()).hashCode();
    }
}
