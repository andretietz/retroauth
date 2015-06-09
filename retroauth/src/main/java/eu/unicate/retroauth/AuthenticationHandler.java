package eu.unicate.retroauth;

public interface AuthenticationHandler {

	void checkForAccount();

	boolean retry(int count, Throwable error);
}
