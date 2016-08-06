package com.andretietz.retroauth.testhelper;

import java.lang.reflect.Field;

/**
 * Created by andre on 06.08.2016.
 */

public class Helper {
    public static void setMember(Object object, String memberName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = object.getClass().getDeclaredField(memberName);
        declaredField.setAccessible(true);
        declaredField.set(object, value);
    }
}
