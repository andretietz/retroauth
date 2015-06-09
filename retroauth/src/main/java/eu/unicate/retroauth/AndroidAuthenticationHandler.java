package eu.unicate.retroauth;

import eu.unicate.retroauth.account.ServiceAccountProvider;
import retrofit.RetrofitError;

public class AndroidAuthenticationHandler implements AuthenticationHandler {


	private static final int MAX_RETRIES = 3;
	private static final int HTTP_UNAUTHORIZED = 401;

	private final int maxRetries;
	private final ServiceAccountProvider provider;

	public AndroidAuthenticationHandler(ServiceAccountProvider provider) {
		this(provider, MAX_RETRIES);
	}

	public AndroidAuthenticationHandler(ServiceAccountProvider provider, int maxRetries) {
		this.maxRetries = maxRetries;
		this.provider = provider;
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
