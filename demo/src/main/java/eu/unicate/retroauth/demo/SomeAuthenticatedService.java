package eu.unicate.retroauth.demo;

import eu.unicate.retroauth.annotations.Authenticated;
import eu.unicate.retroauth.annotations.Authentication;
import eu.unicate.retroauth.demo.models.Token;
import eu.unicate.retroauth.demo.models.User;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import rx.Observable;

@Authentication(accountType = R.string.auth_account_type, tokenType = R.string.auth_token_type)
public interface SomeAuthenticatedService {



	@Authenticated
	@GET("/user/")
	Observable<User> getProfile();

}
