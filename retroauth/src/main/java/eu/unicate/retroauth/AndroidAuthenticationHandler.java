package eu.unicate.retroauth;

import retrofit.RetrofitError;

public class AndroidAuthenticationHandler implements AuthenticationHandler {


	private static final int MAX_RETRIES = 3;
	private static final int HTTP_UNAUTHORIZED = 401;

	private final int maxRetries;

	public AndroidAuthenticationHandler() {
		this(MAX_RETRIES);
	}

	public AndroidAuthenticationHandler(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Override
	public void checkForAccount() {
		// TODO some account check
	}

	@Override
	public boolean retry(int count, Throwable error) {
		if (error instanceof RetrofitError) {
			int status = ((RetrofitError) error).getResponse().getStatus();
			if (HTTP_UNAUTHORIZED == status
					&& count < maxRetries) {
				// TODO: some re-authentication work
				return true;
			}
		}
		return false;
	}
}
