package eu.unicate.retroauth;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * You have to extend this service in order to use this library
 */
public abstract class AuthenticationService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		AccountAuthenticator authenticator = new AccountAuthenticator(this, getLoginAction(this));
		return authenticator.getIBinder();
	}

	/**
	 * @param context a valid context
	 * @return An Action String to open the activity to login
	 */
	public abstract String getLoginAction(Context context);
}
