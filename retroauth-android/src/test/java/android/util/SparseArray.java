package android.util;

import java.util.HashMap;

public class SparseArray<T> extends HashMap<Integer, T> {

	public T get(int key) {
		return super.get(key);
	}

	public void put(int key, T obj) {
		super.put(key, obj);
	}
}
