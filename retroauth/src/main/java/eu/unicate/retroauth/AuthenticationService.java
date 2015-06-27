package eu.unicate.retroauth;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import eu.unicate.retroauth.AccountAuthenticator;

public abstract class AuthenticationService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		AccountAuthenticator authenticator = new AccountAuthenticator(this, getLoginAction(this));
		return authenticator.getIBinder();
	}

	public abstract String getLoginAction(Context context);
}
