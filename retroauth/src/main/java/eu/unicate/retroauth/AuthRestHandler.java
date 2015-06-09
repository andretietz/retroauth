package eu.unicate.retroauth;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class AuthRestHandler<T> implements InvocationHandler {


	private final Map<Method, AuthRestMethodInfo> methodInfoCache;
	private final T retrofitService;
	private final AuthenticationHandler authHandler;

	public AuthRestHandler(T retrofitService, AuthenticationHandler authHandler, Map<Method, AuthRestMethodInfo> methodInfoCache) {
		this.retrofitService = retrofitService;
		this.methodInfoCache = methodInfoCache;
		this.authHandler = authHandler;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		AuthRestMethodInfo methodInfo = methodInfoCache.get(method);
		if (!methodInfo.isAuthenticated) {
			return method.invoke(retrofitService, args);
		}
		return RxAuthInvoker.invoke(retrofitService, authHandler, method, args);
	}


}
