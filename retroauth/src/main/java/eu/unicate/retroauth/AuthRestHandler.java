package eu.unicate.retroauth;

import android.content.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AuthRestHandler<T> implements InvocationHandler {


	private final ServiceInfo serviceInfo;
	private final T retrofitService;
	private final Context context;
	private final AuthInvoker<T> authInvoker;

	public AuthRestHandler(T retrofitService, Context context, ServiceInfo serviceInfo) {
		this.context = context;
		this.retrofitService = retrofitService;
		this.serviceInfo = serviceInfo;
		authInvoker = new AuthInvoker<>(context, retrofitService, serviceInfo);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		AuthRequestType methodInfo = serviceInfo.methodInfoCache.get(method);
		serviceInfo.authenticationInterceptor.setIgnore(false);
		switch (methodInfo) {
			case RXJAVA:
				return authInvoker.invokeRxJavaCall(method, args);
			case SYNC:
				return authInvoker.invokeBlockingCall(method, args);
			case ASYNC:
				authInvoker.invokeAsyncCall(method, args);
				return null;
			case NONE:
			default:
				serviceInfo.authenticationInterceptor.setIgnore(true);
				return method.invoke(retrofitService, args);
		}
	}


}
