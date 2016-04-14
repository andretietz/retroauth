package com.andretietz.retroauth;

/**
 * Created by andre on 14/04/16.
 */
public interface RefreshAuthenticationHandler<S, T> extends AuthenticationHandler<S> {
	void setRefreshApi(T refreshApi);
}
