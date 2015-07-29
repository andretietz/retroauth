package eu.unicate.retroauth.interfaces;

import rx.Observable;

/**
 * This interface is used to extend the observable chain of an authenticated request.
 */
public interface RequestStrategy {
	/**
	 * This is to modify the request observable. In the easiest case you just return the request
	 * that comes in.
	 * This is used to extend the requests as it is done i.e. in the {@link eu.unicate.retroauth.strategies.BasicRetryStrategy}
	 * or in the {@link eu.unicate.retroauth.strategies.LockingStrategy}
	 *
	 * @param request the request as it is generated in the {@link eu.unicate.retroauth.AuthInvoker#invoke(Observable)}
	 * @return The request as you want it to be
	 */
	<T> Observable<T> execute(Observable<T> request);
}
