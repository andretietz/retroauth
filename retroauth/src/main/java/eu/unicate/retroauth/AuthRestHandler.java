package eu.unicate.retroauth;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AuthRestHandler<T> implements InvocationHandler {


	private final ServiceInfoCache serviceInfo;
	private final T retrofitService;

	public AuthRestHandler(T retrofitService, ServiceInfoCache serviceInfo) {
		this.retrofitService = retrofitService;
		this.serviceInfo = serviceInfo;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		AuthRestMethodInfo methodInfo = serviceInfo.methodInfoCache.get(method);
		if (!methodInfo.isAuthenticated) {
			return method.invoke(retrofitService, args);
		}
		return RxAuthInvoker.invoke(retrofitService, serviceInfo, method, args);
	}


}
