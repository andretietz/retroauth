package eu.unicate.retroauth.demo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by a.tietz on 15.06.15.
 */
public class AuthService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		TokenAccountAuthenticator authenticator = new TokenAccountAuthenticator(this, LoginActivity.class);
		return authenticator.getIBinder();
	}
}
