/*
 * Copyright (c) 2015 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.unicate.retroauth;

import android.support.v4.util.Pair;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import eu.unicate.retroauth.ServiceInfo.AuthRequestType;
import eu.unicate.retroauth.interfaces.BaseAccountManager;
import eu.unicate.retroauth.strategies.RequestStrategy;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

final class AuthRestHandler<T> implements InvocationHandler {

	private final ServiceInfo serviceInfo;
	private final T retrofitService;
	private final AuthInvoker authInvoker;

	public AuthRestHandler(T retrofitService, ServiceInfo serviceInfo, BaseAccountManager authAccountManager, RequestStrategy requestStrategy) {
		this.retrofitService = retrofitService;
		this.serviceInfo = serviceInfo;
		authInvoker = new AuthInvoker(serviceInfo, authAccountManager, requestStrategy);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		ServiceInfo.AuthRequestType methodInfo = serviceInfo.methodInfoCache.get(method);
		if (serviceInfo.tokenInterceptor != null)
			serviceInfo.tokenInterceptor.setIgnore(AuthRequestType.NONE.equals(methodInfo));
		switch (methodInfo) {
			case RXJAVA:
				return authInvoker
						.invoke(observableRequest(method, args));
			case BLOCKING:
				return authInvoker
						.invoke(blockingRequest(method, args))
						.toBlocking().single();
			case ASYNC:
				// store original callback
				@SuppressWarnings("unchecked")
				final Callback<Object> originalCallback = (Callback<Object>) args[args.length - 1];
				authInvoker
						.invoke(asyncRequest(method, args))
						.observeOn(AndroidScheduler.mainThread())
						.subscribe(new Action1<Pair<Object, Response>>() {
									   @Override
									   public void call(Pair<Object, Response> result) {
										   originalCallback.success(result.first, result.second);
									   }
								   },
								new Action1<Throwable>() {
									@Override
									public void call(Throwable throwable) {
										if (throwable instanceof RetrofitError) {
											originalCallback.failure((RetrofitError) throwable);
										} else {
											originalCallback.failure(RetrofitError.unexpectedError(null, throwable));
										}
									}
								});
				return null;
			case NONE:
			default:
				return method.invoke(retrofitService, args);
		}
	}

	/**
	 * This method wraps a request that returns an observable typed request. It has to be wrapped,
	 * since calling invoke on the method could lead to some exceptions
	 *
	 * @param method request method that is going to  be executed
	 * @param args   arguments for that request
	 * @return the response from the originally created retrofit observable request
	 */
	private Observable<?> observableRequest(Method method, Object[] args) {
		//noinspection TryWithIdenticalCatches
		try {
			return (Observable<?>) method.invoke(retrofitService, args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method wraps a blocking request into an observable request, that emits the expected
	 * result type
	 *
	 * @param method request method that is going to  be executed
	 * @param args   arguments for that request
	 * @return an observable that emits exactly one item, once someone subscribes to it
	 */
	private Observable<Object> blockingRequest(final Method method, final Object[] args) {
		return Observable.create(new Observable.OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {
				//noinspection TryWithIdenticalCatches
				try {
					subscriber.onNext(method.invoke(retrofitService, args));
					subscriber.onCompleted();
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	/**
	 * This method overrides the Callback argument of an async request type with an observable one
	 * and invokes the original request method. Once the callback is called by retrofit the subscriber
	 * can catch the result
	 *
	 * @param method request method that is going to  be executed
	 * @param args   arguments for that request
	 * @return an observable that emits an item with the retrofit response object and the actual response object
	 */
	private Observable<Pair<Object, Response>> asyncRequest(final Method method, final Object[] args) {
		return Observable.create(new Observable.OnSubscribe<Pair<Object, Response>>() {
			@Override
			public void call(final Subscriber<? super Pair<Object, Response>> subscriber) {
				// override the callback which was here before
				args[args.length - 1] = new Callback<Object>() {
					@Override
					public void success(Object o, Response response) {
						subscriber.onNext(Pair.create(o, response));
						subscriber.onCompleted();
					}

					@Override
					public void failure(RetrofitError error) {
						subscriber.onError(error);
					}
				};
				observableRequest(method, args);
			}
		});

	}


}
