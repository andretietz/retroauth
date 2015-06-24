package eu.unicate.android.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import eu.unicate.retroauth.AccountAuthenticator;

/**
 * Created by a.tietz on 15.06.15.
 */
public class AuthService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		AccountAuthenticator authenticator = new AccountAuthenticator(this);
		return authenticator.getIBinder();
	}
}
