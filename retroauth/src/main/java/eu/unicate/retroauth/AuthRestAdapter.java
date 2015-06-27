package eu.unicate.retroauth;

import android.content.Context;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import eu.unicate.retroauth.annotations.Authenticated;
import eu.unicate.retroauth.annotations.Authentication;
import eu.unicate.retroauth.interceptors.TokenInterceptor;
import eu.unicate.retroauth.interceptors.CompositeRequestInterceptor;
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

public final class AuthRestAdapter {

	private final Map<Class<?>, ServiceInfo> serviceInfoCache = new LinkedHashMap<>();
	private final RestAdapter adapter;
	private final CompositeRequestInterceptor interceptor;


	private AuthRestAdapter(RestAdapter adapter, CompositeRequestInterceptor interceptor) {
		this.adapter = adapter;
		this.interceptor = interceptor;
	}

	@SuppressWarnings("unchecked")
	public <T> T create(Context context, TokenInterceptor tokenInterceptor, Class<T> serviceClass) {
		interceptor.addRequestIntercetor(tokenInterceptor);
		return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class<?>[]{serviceClass},
				new AuthRestHandler<>(adapter.create(serviceClass), context, getServiceInfo(context, serviceClass, tokenInterceptor)));

	}

	private <T> ServiceInfo getServiceInfo(Context context, Class<T> serviceClass, TokenInterceptor interceptor) {
		synchronized (serviceInfoCache) {
			ServiceInfo info = serviceInfoCache.get(serviceClass);
			if (null == info) {
				Authentication annotation = serviceClass.getAnnotation(Authentication.class);
				String accountType = null;
				String tokenType = null;
				if (null != annotation) {
					accountType = context.getString(annotation.accountType());
					tokenType = context.getString(annotation.tokenType());
				}
				Map<Method, AuthRestMethodInfo> methodInfoCache = scanServiceMethods(serviceClass, (null != accountType));
				info = new ServiceInfo(methodInfoCache, accountType, tokenType, interceptor);
				serviceInfoCache.put(serviceClass, info);
			}
			return info;
		}
	}

	private Map<Method, AuthRestMethodInfo> scanServiceMethods(Class<?> serviceClass, boolean containsAuthenticationData) {
		Map<Method, AuthRestMethodInfo> map = new LinkedHashMap<>();
		for (Method method : serviceClass.getMethods()) {
			AuthRestMethodInfo methodInfo = scanServiceMethod(containsAuthenticationData, method);
			map.put(method, methodInfo);
		}
		return map;
	}

	private AuthRestMethodInfo scanServiceMethod(boolean containsAuthenticationData, Method method) {
		AuthRestMethodInfo info = new AuthRestMethodInfo();
		if (method.isAnnotationPresent(Authenticated.class)) {
			if (method.getReturnType().equals(Observable.class)) {
				if (containsAuthenticationData) {
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
			CompositeRequestInterceptor compositeRequestInterceptor = new CompositeRequestInterceptor();
			if (null != interceptor) {
				compositeRequestInterceptor.addRequestIntercetor(interceptor);
			}
			builder.setRequestInterceptor(compositeRequestInterceptor);
			return new AuthRestAdapter(builder.build(), compositeRequestInterceptor);
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
