package eu.unicate.retroauth;

import android.content.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AuthRestHandler<T> implements InvocationHandler {


	private final ServiceInfo serviceInfo;
	private final T retrofitService;
	private final Context context;

	public AuthRestHandler(T retrofitService, Context context, ServiceInfo serviceInfo) {
		this.context = context;
		this.retrofitService = retrofitService;
		this.serviceInfo = serviceInfo;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		AuthRestMethodInfo methodInfo = serviceInfo.methodInfoCache.get(method);
		if (methodInfo.isAuthenticated) {
			// activate the TokenInterceptor
			serviceInfo.authenticationInterceptor.setIgnore(false);
			return RxAuthInvoker.invoke(retrofitService, context, serviceInfo, method, args);
		}
		// ignore the TokenInterceptor
		serviceInfo.authenticationInterceptor.setIgnore(true);
		return method.invoke(retrofitService, args);
	}


}
