package eu.unicate.retroauth;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import eu.unicate.retroauth.annotations.Authenticated;
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

	private final Map<Class<?>, Map<Method, AuthRestMethodInfo>> serviceMethodInfoCache =
			new LinkedHashMap<>();
	private final RestAdapter adapter;
	private final AuthenticationHandler authHandler;


	private AuthRestAdapter(RestAdapter adapter, AuthenticationHandler authHandler) {
		this.adapter = adapter;
		this.authHandler = (null == authHandler) ? new AndroidAuthenticationHandler() : authHandler;
	}

	@SuppressWarnings("unchecked")
	public <T> T create(Class<T> serviceClass) {
		return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class<?>[]{serviceClass},
				new AuthRestHandler<>(adapter.create(serviceClass), authHandler, getMethodInfoCache(serviceClass)));

	}

	private Map<Method, AuthRestMethodInfo> getMethodInfoCache(Class<?> serviceClass) {
		synchronized (serviceMethodInfoCache) {
			Map<Method, AuthRestMethodInfo> methodInfoMap = serviceMethodInfoCache.get(serviceClass);
			if (null == methodInfoMap) {
				serviceMethodInfoCache.put(serviceClass, scanServiceClass(serviceClass));
			}
			return methodInfoMap;
		}
	}

	private Map<Method, AuthRestMethodInfo> scanServiceClass(Class<?> serviceClass) {
		Map<Method, AuthRestMethodInfo> map = new LinkedHashMap<>();
		for (Method method : serviceClass.getMethods()) {
			AuthRestMethodInfo methodInfo = scanServiceMethod(method);
			map.put(method, methodInfo);
		}
		return map;
	}

	private AuthRestMethodInfo scanServiceMethod(Method method) {
		AuthRestMethodInfo info = new AuthRestMethodInfo();
		if (method.isAnnotationPresent(Authenticated.class)) {
			if (method.getReturnType().equals(Observable.class)) {
				info.isAuthenticated = true;
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
		AuthenticationHandler authHandler;

		public Builder() {
			builder = new RestAdapter.Builder();
		}

		public AuthRestAdapter build() {
			return new AuthRestAdapter(builder.build(), authHandler);
		}

		public Builder setEndpoint(String endpoint) {
			builder.setEndpoint(endpoint);
			return this;
		}

		/**
		 * API endpoint.
		 */
		public Builder setEndpoint(Endpoint endpoint) {
			builder.setEndpoint(endpoint);
			return this;
		}

		/**
		 * The HTTP client used for requests.
		 */
		public Builder setClient(final Client client) {
			builder.setClient(client);
			return this;
		}

		/**
		 * The HTTP client used for requests.
		 */
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
		public Builder setExecutors(Executor httpExecutor, Executor callbackExecutor) {
			builder.setExecutors(httpExecutor, callbackExecutor);
			return this;
		}

		/**
		 * A request interceptor for adding data to every request.
		 */
		public Builder setRequestInterceptor(RequestInterceptor requestInterceptor) {
			builder.setRequestInterceptor(requestInterceptor);
			return this;
		}

		/**
		 * The converter used for serialization and deserialization of objects.
		 */
		public Builder setConverter(Converter converter) {
			builder.setConverter(converter);
			return this;
		}

		/**
		 * Set the profiler used to measure requests.
		 */
		public Builder setProfiler(Profiler profiler) {
			builder.setProfiler(profiler);
			return this;
		}

		/**
		 * The error handler allows you to customize the type of exception thrown for errors on
		 * synchronous requests.
		 */
		public Builder setErrorHandler(ErrorHandler errorHandler) {
			builder.setErrorHandler(errorHandler);
			return this;
		}

		/**
		 * Configure debug logging mechanism.
		 */
		public Builder setLog(Log log) {
			builder.setLog(log);
			return this;
		}

		/**
		 * Change the level of logging.
		 */
		public Builder setLogLevel(LogLevel logLevel) {
			builder.setLogLevel(logLevel);
			return this;
		}

		public Builder setAuthHandler(AuthenticationHandler authHandler) {
			this.authHandler = authHandler;
			return this;
		}
	}


}
