package com.andretietz.retroauth.testhelper;

import java.lang.reflect.Field;

public final class Helper {

    private Helper() {
    }

    public static void setMember(Object object, String memberName, Object value)
            throws IllegalAccessException, NoSuchFieldException {
        Field declaredField = getField(object.getClass(), memberName);
        declaredField.setAccessible(true);
        declaredField.set(object, value);
    }

    public static <T> T getMember(Object object, String member, Class<T> klass)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(object.getClass(), member);
        field.setAccessible(true);
        return (T)field.get(object);
    }


    public static Field getField(Class<?> klass, String member) throws NoSuchFieldException {
        try {
            return klass.getDeclaredField(member);
        } catch (NoSuchFieldException e) {
            if (klass.getSuperclass() != null)
                return getField(klass.getSuperclass(), member);
            else
                throw new NoSuchFieldException(String.format("Class does not contain member %s!", member));
        }
    }
}
