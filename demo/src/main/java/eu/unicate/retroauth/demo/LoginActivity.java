package eu.unicate.retroauth.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import eu.unicate.retroauth.AuthRestAdapter;
import eu.unicate.retroauth.AuthenticationActivity;
import eu.unicate.retroauth.demo.models.Token;
import eu.unicate.retroauth.demo.models.User;
import retrofit.RestAdapter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LoginActivity extends AuthenticationActivity {

	private TextView textUser;
	private TextView textPass;
	private AuthenticationService service;
	private String token;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_login);

		textUser = (TextView) findViewById(R.id.textUser);
		textPass = (TextView) findViewById(R.id.textPassword);

		AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
				.setEndpoint("http://api.atekk.de/auth")
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.build();
		service = restAdapter.create(this, AuthenticationService.class);


		findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// do the login
				service.login(textUser.getText().toString(), textPass.getText().toString())
						// get some additional userdata
						.flatMap(new Func1<Token, Observable<User>>() {
							@Override
							public Observable<User> call(Token result) {
								token = result.key;
								return service.getProfile("Token " + token);
							}
						})
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(new Action1<User>() {
									   @Override
									   public void call(User user) {
										   finalizeAuthentication(user.name, getString(R.string.auth_token_type), token, null);
									   }
								   },
								new Action1<Throwable>() {
									@Override
									public void call(Throwable throwable) {
										Log.e("ERROR", "An error occured while login", throwable);
									}
								}
						);


			}
		});

		String username = getAccountName();
		if (null != username) {
			textUser.setEnabled(false);
			textUser.setText(username);
		}

	}

	public interface AuthenticationService {
		@FormUrlEncoded
		@POST("/login/")
		@Headers("Accept: application/json")
		Observable<Token> login(@Field("username") String username, @Field("password") String password);

		@GET("/user/")
		@Headers("Accept: application/json")
		Observable<User> getProfile(@Header("Authorization") String token);
	}
}
