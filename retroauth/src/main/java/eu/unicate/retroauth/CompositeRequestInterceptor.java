package eu.unicate.retroauth;

import java.util.List;

import retrofit.RequestInterceptor;

public class CompositeRequestInterceptor implements RequestInterceptor {

	private final List<RequestInterceptor> interceptors;


	public CompositeRequestInterceptor(List<RequestInterceptor> interceptors) {
		this.interceptors = interceptors;
	}

	@Override
	public void intercept(RequestFacade request) {
		for (RequestInterceptor interceptor : interceptors) {
			interceptor.intercept(request);
		}

	}
}
