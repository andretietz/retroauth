package eu.unicate.retroauth.demo.auth.github.model;

import com.google.gson.annotations.SerializedName;

public class AccessToken {
	@SerializedName("access_token")
	public String token;
	@SerializedName("token_type")
	public String tokenType;
	public String scope;
}
