package eu.unicate.retroauth;

import android.app.Activity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AuthRestHandler<T> implements InvocationHandler {


	private final ServiceInfo serviceInfo;
	private final T retrofitService;
	private final Activity activity;

	public AuthRestHandler(T retrofitService, Activity activity, ServiceInfo serviceInfo) {
		this.activity = activity;
		this.retrofitService = retrofitService;
		this.serviceInfo = serviceInfo;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		AuthRestMethodInfo methodInfo = serviceInfo.methodInfoCache.get(method);
		if (!methodInfo.isAuthenticated) {
			return method.invoke(retrofitService, args);
		}
		return RxAuthInvoker.invoke(retrofitService, activity, serviceInfo, method, args);
	}


}
