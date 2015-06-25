package eu.unicate.retroauth;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import eu.unicate.retroauth.annotations.Authenticated;
import eu.unicate.retroauth.annotations.Authentication;
import retrofit.Callback;
import retrofit.Endpoint;
import retrofit.ErrorHandler;
import retrofit.Profiler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.Log;
import retrofit.RestAdapter.LogLevel;
import retrofit.client.Client;
import retrofit.converter.Converter;
import rx.Observable;

public class AuthRestAdapter {

	private final Map<Class<?>, Map<Method, AuthRestMethodInfo>> serviceMethodInfoCache = new LinkedHashMap<>();
	private final Map<Class<?>, Pair<Integer, Integer>> serviceAuthTypes = new LinkedHashMap<>();
	private final RestAdapter adapter;


	private AuthRestAdapter(RestAdapter adapter) {
		this.adapter = adapter;
	}

	@SuppressWarnings("unchecked")
	public <T> T create(Activity activity, Class<T> serviceClass) {
		Map<Method, AuthRestMethodInfo> methodInfoCache = getMethodInfoCache(serviceClass);
		Pair<Integer, Integer> authTypeCache = getAuthTypeCache(serviceClass);
		AndroidAuthenticationHandler authenticationHandler;
		if(null != authTypeCache) {
			authenticationHandler = new AndroidAuthenticationHandler(activity, activity.getString(authTypeCache.first), activity.getString(authTypeCache.second));
		} else {
			authenticationHandler = new AndroidAuthenticationHandler(activity, null, null);
		}
		return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class<?>[]{serviceClass},
				new AuthRestHandler<>(adapter.create(serviceClass), authenticationHandler, methodInfoCache));

	}

	private Map<Method, AuthRestMethodInfo> getMethodInfoCache(Class<?> serviceClass) {
		synchronized (serviceMethodInfoCache) {
			Map<Method, AuthRestMethodInfo> methodInfoMap = serviceMethodInfoCache.get(serviceClass);
			if (null == methodInfoMap) {
				methodInfoMap = scanServiceMethods(serviceClass);
				serviceMethodInfoCache.put(serviceClass, methodInfoMap);
			}
			return methodInfoMap;
		}
	}

	private Pair<Integer, Integer> getAuthTypeCache(Class<?> serviceClass) {
		synchronized (serviceAuthTypes) {
			Pair<Integer, Integer> authTypes = serviceAuthTypes.get(serviceClass);
			if (null == authTypes) {
				authTypes = scanAuthTypes(serviceClass);
				serviceAuthTypes.put(serviceClass, authTypes);
			}
			return authTypes;
		}
	}

	private Pair<Integer, Integer> scanAuthTypes(Class<?> serviceClass) {
		Authentication annotation = serviceClass.getAnnotation(Authentication.class);
		if(null != annotation) {
			return new Pair<>(annotation.accountType(), annotation.tokenType());
		}
		return null;
	}

	private Map<Method, AuthRestMethodInfo> scanServiceMethods(Class<?> serviceClass) {
		Map<Method, AuthRestMethodInfo> map = new LinkedHashMap<>();
		for (Method method : serviceClass.getMethods()) {
			AuthRestMethodInfo methodInfo = scanServiceMethod(serviceClass, method);
			map.put(method, methodInfo);
		}
		return map;
	}

	private AuthRestMethodInfo scanServiceMethod(Class<?> serviceClass, Method method) {
		AuthRestMethodInfo info = new AuthRestMethodInfo();
		if (method.isAnnotationPresent(Authenticated.class)) {
			if (method.getReturnType().equals(Observable.class)) {
				if(null != getAuthTypeCache(serviceClass)) {
					info.isAuthenticated = true;
				} else {
					throw methodError(method, "The Method %s contains the %s Annotation, but the interface does not implement the %s Annotation", method.getName(), Authenticated.class.getSimpleName(), Authentication.class.getSimpleName());
				}
			} else {
				throw methodError(method, "Currently only rxjava methods are supported by the %s Annotation", Authenticated.class.getSimpleName());
			}
		}
		return info;
	}


	private RuntimeException methodError(Method method, String message, Object... args) {
		if (args.length > 0) {
			message = String.format(message, args);
		}
		return new IllegalArgumentException(
				method.getDeclaringClass().getSimpleName() + "." + method.getName() + ": " + message);
	}

	public static class Builder {
		RestAdapter.Builder builder;
		RequestInterceptor interceptor;

		public Builder() {
			builder = new RestAdapter.Builder();
		}

		public AuthRestAdapter build() {
			List<RequestInterceptor> interceptorList = new ArrayList<>();
			interceptorList.add(interceptor);
			// TODO add the token interceptor
			CompositeRequestInterceptor interceptor = new CompositeRequestInterceptor(interceptorList);
			builder.setRequestInterceptor(interceptor);
			return new AuthRestAdapter(builder.build());
		}

		public Builder setEndpoint(String endpoint) {
			builder.setEndpoint(endpoint);
			return this;
		}

		/**
		 * API endpoint.
		 */
		@SuppressWarnings("unused")
		public Builder setEndpoint(Endpoint endpoint) {
			builder.setEndpoint(endpoint);
			return this;
		}

		/**
		 * The HTTP client used for requests.
		 */
		@SuppressWarnings("unused")
		public Builder setClient(final Client client) {
			builder.setClient(client);
			return this;
		}

		/**
		 * The HTTP client used for requests.
		 */
		@SuppressWarnings("unused")
		public Builder setClient(Client.Provider clientProvider) {
			builder.setClient(clientProvider);
			return this;
		}

		/**
		 * Executors used for asynchronous HTTP client downloads and callbacks.
		 *
		 * @param httpExecutor     Executor on which HTTP client calls will be made.
		 * @param callbackExecutor Executor on which any {@link Callback} methods will be invoked. If
		 *                         this argument is {@code null} then callback methods will be run on the same thread as the
		 *                         HTTP client.
		 */
		@SuppressWarnings("unused")
		public Builder setExecutors(Executor httpExecutor, Executor callbackExecutor) {
			builder.setExecutors(httpExecutor, callbackExecutor);
			return this;
		}

		/**
		 * A request interceptor for adding data to every request.
		 */
		@SuppressWarnings("unused")
		public Builder setRequestInterceptor(RequestInterceptor requestInterceptor) {
			interceptor = requestInterceptor;
			return this;
		}

		/**
		 * The converter used for serialization and deserialization of objects.
		 */
		@SuppressWarnings("unused")
		public Builder setConverter(Converter converter) {
			builder.setConverter(converter);
			return this;
		}

		/**
		 * Set the profiler used to measure requests.
		 */
		@SuppressWarnings("unused")
		public Builder setProfiler(Profiler profiler) {
			builder.setProfiler(profiler);
			return this;
		}

		/**
		 * The error handler allows you to customize the type of exception thrown for errors on
		 * synchronous requests.
		 */
		@SuppressWarnings("unused")
		public Builder setErrorHandler(ErrorHandler errorHandler) {
			builder.setErrorHandler(errorHandler);
			return this;
		}

		/**
		 * Configure debug logging mechanism.
		 */
		@SuppressWarnings("unused")
		public Builder setLog(Log log) {
			builder.setLog(log);
			return this;
		}

		/**
		 * Change the level of logging.
		 */
		@SuppressWarnings("unused")
		public Builder setLogLevel(LogLevel logLevel) {
			builder.setLogLevel(logLevel);
			return this;
		}
	}


}
