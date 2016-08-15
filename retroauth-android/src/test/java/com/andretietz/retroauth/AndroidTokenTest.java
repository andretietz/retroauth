package com.andretietz.retroauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;


@RunWith(JUnit4.class)
public class AndroidTokenTest {

    @Test
    public void dataCheck() {
        AndroidToken androidToken = new AndroidToken("token", "refresh");
        assertEquals("token", androidToken.token);
        assertEquals("refresh", androidToken.refreshToken);
    }
}
