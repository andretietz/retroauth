package eu.unicate.retroauth.demo;

import eu.unicate.retroauth.annotations.Authenticated;
import eu.unicate.retroauth.demo.models.Token;
import eu.unicate.retroauth.demo.models.User;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import rx.Observable;

public interface AuthenticationService {
	@FormUrlEncoded
	@POST("/registration/")
	User register(@Field("username") String username, @Field("password1") String password1, @Field("password2") String password2, @Field("email") String email);


	@FormUrlEncoded
	@POST("/login/")
	Token login(@Field("username") String username, @Field("password") String password);

	@Authenticated
	@GET("/user/")
	Observable<User> getProfile();


	@POST("/logout/")
	Void logout();

	@FormUrlEncoded
	@POST("/password/change/")
	Void changePassword(@Field("new_password1") String password1, @Field("new_password2") String password2);

	@FormUrlEncoded
	@POST("/password/reset/")
	Void resetPasswort(@Field("email") String email);


	@FormUrlEncoded
	@PUT("/user/")
	User updateUser(@Field("username") String username, @Field("first_name") String firstName, @Field("last_name") String lastName);

}
