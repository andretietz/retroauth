package com.andretietz.retroauth;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

final class WeakActivityStack {

    private final SparseArray<WeakReference<Activity>> map = new SparseArray<>();

    private final LinkedList<Integer> stack = new LinkedList<>();


    public void push(@NonNull Activity item) {
        Integer identifier = getIdentifier(item);
        synchronized (this) {
            stack.push(identifier);
            map.put(identifier, new WeakReference<>(item));
        }
    }

    @Nullable
    public Activity pop() {
        synchronized (this) {
            if (!stack.isEmpty()) {
                Integer identifier = stack.removeFirst();
                Activity item = map.get(identifier).get();
                map.remove(identifier);
                return item;
            }
            return null;
        }
    }

    public void remove(@NonNull Activity item) {
        Integer identifier = getIdentifier(item);
        synchronized (this) {
            stack.remove(identifier);
            map.remove(identifier);
        }
    }

    @Nullable
    public Activity peek() {
        synchronized (this) {
            if (!stack.isEmpty()) {
                return map.get(stack.getFirst()).get();
            }
        }
        return null;
    }


    protected Integer getIdentifier(Activity item) {
        return item.hashCode();
    }

}
