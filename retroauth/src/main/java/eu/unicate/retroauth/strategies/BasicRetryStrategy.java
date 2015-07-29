package eu.unicate.retroauth.strategies;

import eu.unicate.retroauth.interfaces.RequestStrategy;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Func2;

/**
 * This {@link RequestStrategy} modifies the actual request to be retried, when
 * the server returns with an 401 (unauthenticated)
 *
 * Extend from this class if you need your custom retry method and override {@link #retry}
 */
public class BasicRetryStrategy implements RequestStrategy {

	private static final int HTTP_UNAUTHORIZED = 401;

	/**
	 * this is adding a retry to the actual request.
	 */
	@Override
	public <T> Observable<T> execute(Observable<T> task) {
		return task.retry(new Func2<Integer, Throwable, Boolean>() {
			@Override
			public Boolean call(Integer count, Throwable error) {
				return retry(count, error);
			}
		});
	}

	/**
	 * Override this method, if you need another retry logic
	 *
	 * @param count the amount of requests (not retries) that were already done
	 * @param error the error that occured and caused the retry
	 * @return {@code true} when this is the first retry and the server returned with 401 {@code false} otherwise
	 */
	protected boolean retry(int count, Throwable error) {
		if (count <= 1) {
			if (error instanceof RetrofitError) {
				Response response = ((RetrofitError) error).getResponse();
				if (response != null && HTTP_UNAUTHORIZED == response.getStatus()) {
					return true;
				}
			}
		}
		return false;
	}
}
