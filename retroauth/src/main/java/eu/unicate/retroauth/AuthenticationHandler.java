package eu.unicate.retroauth;

import android.accounts.Account;

public interface AuthenticationHandler {

	boolean retry(int count, Throwable error);

	Account getAccount();
}
