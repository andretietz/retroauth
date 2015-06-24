package eu.unicate.android.auth;

public class AuthToken {

	public final String token;
	public final String type;

	public AuthToken(String token, String type) {
		this.token = token;
		this.type = type;
	}
}
