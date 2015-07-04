package eu.unicate.retroauth.demo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonElement;

import eu.unicate.retroauth.AuthAccountManager;
import eu.unicate.retroauth.AuthRestAdapter;
import eu.unicate.retroauth.interceptors.TokenInterceptor;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	private SomeAuthenticatedService service;
	private AuthAccountManager authAccountManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		authAccountManager = AuthAccountManager.get(this);

		// create the restadapter like you would do it with retrofit
		AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
				.setEndpoint("https://api.github.com")
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.build();

		// create the service with an activity, a token interceptor and the service interface you want to create
		service = restAdapter.create(this, new SomeFakeAuthenticationToken(), SomeAuthenticatedService.class);

		// this is an example for the call of an rxjava method
		findViewById(R.id.buttonRxJavaRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				service.listReposRxJava("Unic8")
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(
								new Action1<JsonElement>() {
									@Override
									public void call(JsonElement jsonElements) {
										showResult(jsonElements);
									}
								},
								new Action1<Throwable>() {
									@Override
									public void call(Throwable throwable) {
										showError(throwable);
									}
								}
						);
			}
		});

		// this is an example of a blocking call
		findViewById(R.id.buttonBlockingRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// I had to wrap this into an async task
				// cause its a network request
				new AsyncTask<Object, Object, JsonElement>() {
					private Throwable error;

					@Override
					protected JsonElement doInBackground(Object... params) {
						try {
							return service.listReposBlocking("Unic8");
						} catch (Throwable e) {
							error = e;
						}
						return null;
					}

					@Override
					protected void onPostExecute(JsonElement o) {
						if (o == null) {
							showError(error);
						} else {
							showResult(o);
						}
					}
				}.execute();
			}
		});

		// this is an example of a async request using the Callable Interface from retrofit
		findViewById(R.id.buttonAsyncRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				service.listReposAsync("Unic8", new Callback<JsonElement>() {
					@Override
					public void success(JsonElement jsonElements, Response response) {
						showResult(jsonElements);
					}

					@Override
					public void failure(RetrofitError error) {
						showError(error);
					}
				});
			}
		});


		findViewById(R.id.buttonInvalidateToken).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				authAccountManager.invalidateTokenFromActiveUser(getString(R.string.auth_account_type), getString(R.string.auth_token_type));
			}
		});
		findViewById(R.id.buttonResetPrefAccount).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				authAccountManager.resetActiveUser(getString(R.string.auth_account_type));
			}
		});

		findViewById(R.id.buttonAddAccount).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				authAccountManager.addAccount(MainActivity.this, getString(R.string.auth_account_type), getString(R.string.auth_token_type));
			}
		});
	}

	private void showResult(JsonElement jsonElement) {
		Toast.makeText(MainActivity.this, jsonElement.toString(), Toast.LENGTH_SHORT).show();
	}

	private void showError(Throwable error) {
		Log.e("TAG", "Error", error);
		Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
	}

	public static class SomeFakeAuthenticationToken extends TokenInterceptor {
		@Override
		public void injectToken(RequestFacade facade, String token) {
			facade.addHeader("Token", token);
		}
	}

}
