package eu.unicate.retroauth.demo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import eu.unicate.retroauth.AuthAccountManager;
import eu.unicate.retroauth.AuthRestAdapter;
import eu.unicate.retroauth.demo.auth.github.model.Email;
import eu.unicate.retroauth.interceptors.TokenInterceptor;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	public static final TokenInterceptor GITHUB_INTERCEPTOR = new TokenInterceptor() {
		@Override
		public void injectToken(RequestFacade facade, String token) {
			// according to the github documentation
			// https://developer.github.com/v3/#authentication
			facade.addHeader("Authorization", "token " + token);
		}
	};

	private GithubService service;
	private AuthAccountManager authAccountManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		authAccountManager = new AuthAccountManager(this);
		showCurrentAccount();
		// create the restadapter like you would do it with retrofit
		AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
				.setEndpoint("https://api.github.com")
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.build();

		// create the service with an activity, a token interceptor and the service interface you want to create
		service = restAdapter.create(this, GITHUB_INTERCEPTOR, GithubService.class);

		/**
		 * RxJava demo
		 * this is an example for the call of an rxjava method
		 */
		findViewById(R.id.buttonRxJavaRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				service.getEmails()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(
								new Action1<List<Email>>() {
									@Override
									public void call(List<Email> emails) {
										showResult(emails);
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

		/**
		 * Blocking Calls demo:
		 * this is an example of a blocking call
		 */
		findViewById(R.id.buttonBlockingRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AsyncTask<Object, Object, List<Email>>() {
					private Throwable error;

					@Override
					protected List<Email> doInBackground(Object... params) {
						try {
							return service.getEmailsBlocking();
						} catch (Throwable e) {
							error = e;
						}
						return null;
					}

					@Override
					protected void onPostExecute(List<Email> o) {
						if (o == null) {
							showError(error);
						} else {
							showResult(o);
						}
					}
				}.execute();
				// since Honeycomb, asynctask is using a threadpool with only one
				// thread (see: http://developer.android.com/reference/android/os/AsyncTask.html#execute(Params...) )
				// remember this, when you need to call more than one request at once using async task!
				// use: .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); instead of ".execute()"
			}
		});


		/**
		 * Demo, using retrofit's async Callback
		 * this is an example of a async request using the Callback Interface from retrofit
		 */
		findViewById(R.id.buttonAsyncRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				service.getEmails(new Callback<List<Email>>() {
					@Override
					public void success(List<Email> emails, Response response) {
						showResult(emails);
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
				// override the current token to force a 401
				AccountManager.get(MainActivity.this)
						.setAuthToken(
								authAccountManager.getActiveAccount(getString(R.string.auth_account_type), false),
								getString(R.string.auth_token_type),
								"some-invalid-token"
						);
			}
		});
		findViewById(R.id.buttonResetPrefAccount).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				authAccountManager.resetActiveAccount(getString(R.string.auth_account_type));
				showCurrentAccount();
			}
		});

		findViewById(R.id.buttonAddAccount).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				authAccountManager.addAccount(MainActivity.this, getString(R.string.auth_account_type), getString(R.string.auth_token_type));
			}
		});
	}

	private void showCurrentAccount() {
		Account activeAccount = authAccountManager.getActiveAccount(getString(R.string.auth_account_type), false);
		if (activeAccount != null)
			setTitle("Active Account: " + activeAccount.name);
		else
			setTitle("No active Account!");
	}

	private void showResult(List<Email> emailList) {
		showCurrentAccount();
		StringBuilder sb = new StringBuilder();
		sb.append("Your protected emails:\n");
		for(Email email : emailList) {
			sb.append(email.email).append('\n');
		}
		Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
	}

	private void showError(Throwable error) {
		Log.e("TAG", "Error", error);
		Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
	}

}
