package eu.unicate.retroauth.interceptors;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import retrofit.RequestInterceptor;

public class CompositeRequestInterceptor implements RequestInterceptor {

	private final List<RequestInterceptor> interceptors;


	public CompositeRequestInterceptor() {
		this.interceptors = new ArrayList<>();
	}

	public void addRequestIntercetor(@NonNull RequestInterceptor interceptor) {
		interceptors.add(interceptor);
	}

	@Override
	public void intercept(RequestFacade request) {
		for (RequestInterceptor interceptor : interceptors) {
			interceptor.intercept(request);
		}

	}
}
