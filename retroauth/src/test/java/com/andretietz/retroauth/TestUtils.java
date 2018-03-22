package com.andretietz.retroauth;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import okhttp3.Request;

@RunWith(MockitoJUnitRunner.class)
public class TestUtils {

    @Test
    public void createUniqueIdentifier() {
        HashMap<Integer, Request> map = new HashMap<>();

        Request request = new Request.Builder()
                .url("http://www.google.com/test/request1")
                .build();
        map.put(Utils.createUniqueIdentifier(request), request);

        request = new Request.Builder()
                .method("GET", null)
                .url("http://www.google.com/test/request1")
                .build();
        map.put(Utils.createUniqueIdentifier(request), request);


        request = new Request.Builder()
                .url("http://www.google.com/test/request2")
                .build();
        map.put(Utils.createUniqueIdentifier(request), request);

        request = new Request.Builder()
                .method("GET", null)
                .url("http://www.google.com/test/request2")
                .build();
        map.put(Utils.createUniqueIdentifier(request), request);

        Assert.assertEquals(2, map.size());

    }
}
