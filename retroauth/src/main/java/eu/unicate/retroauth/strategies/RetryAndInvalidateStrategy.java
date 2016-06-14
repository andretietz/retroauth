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

package eu.unicate.retroauth.strategies;


import eu.unicate.retroauth.interfaces.BaseAccountManager;
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
public class RetryAndInvalidateStrategy extends RequestStrategy {

	private static final int HTTP_UNAUTHORIZED = 401;
	private final BaseAccountManager accountManager;
	private final String accountType;
	private final String tokenType;


	/**
	 * @param accountType    Type of account you are using for your API
	 * @param tokenType      Type of Token your API is using
	 * @param accountManager an AccountManager to invalidate Tokens if necessary
	 */
	public RetryAndInvalidateStrategy(String accountType, String tokenType, BaseAccountManager accountManager) {
		this.accountManager = accountManager;
		this.accountType = accountType;
		this.tokenType = tokenType;
	}

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
	 * @param throwable the error that occured and caused the retry
	 * @return {@code true} when this is the first retry and the server returned with 401 {@code false} otherwise
	 */
	protected boolean retry(int count, Throwable throwable) {
		if (count <= 1) {
			@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
			RetrofitError error = isRetrofitError(throwable);
			if (error != null) {
				Response response = error.getResponse();
				if (response != null && HTTP_UNAUTHORIZED == response.getStatus()) {
					accountManager.invalidateTokenFromActiveUser(accountType, tokenType);
					return true;
				}
			}
		}
		return false;
	}

	private RetrofitError isRetrofitError(Throwable error) {
		if (error instanceof RetrofitError) {
			return (RetrofitError) error;
		}
		Throwable cause = error.getCause();
		if(cause != null) {
			return isRetrofitError(cause);
		}
		return null;
	}
}
