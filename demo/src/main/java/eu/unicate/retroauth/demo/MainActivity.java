package eu.unicate.retroauth.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.List;

import eu.unicate.retroauth.AndroidScheduler;
import eu.unicate.retroauth.AuthRestAdapter;
import eu.unicate.retroauth.interceptors.TokenInterceptor;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	private SomeAuthenticatedService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
				.setEndpoint("https://api.github.com")
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.build();
		service = restAdapter.create(this, new SomeFakeAuthenticationToken(), SomeAuthenticatedService.class);

		findViewById(R.id.buttonRxJavaRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				service.listReposRxJava("Unic8")
						.subscribeOn(Schedulers.computation())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(
								new Action1<List<JsonElement>>() {
									@Override
									public void call(List<JsonElement> jsonElements) {
										Toast.makeText(MainActivity.this, jsonElements.toString(), Toast.LENGTH_SHORT).show();
									}
								},
								new Action1<Throwable>() {
									@Override
									public void call(Throwable throwable) {
										Toast.makeText(MainActivity.this, "An error occured: " + throwable.getClass().getName(), Toast.LENGTH_SHORT).show();
										throwable.printStackTrace();
									}
								}
						);
			}
		});



		findViewById(R.id.buttonBlockingRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// wrapping the blocking call with an rxjava construct
				// (since you cannot do network requests in the main thread)
				// you could use an async task as well
				Observable.create(new Observable.OnSubscribe<List<JsonElement>>() {
					@Override
					public void call(Subscriber<? super List<JsonElement>> subscriber) {
						try {
							subscriber.onNext(service.listReposBlocking("Unic8"));
							subscriber.onCompleted();
						} catch (Exception e) {
							subscriber.onError(e);
						}
					}
				})
						.subscribeOn(Schedulers.computation())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(
								new Action1<List<JsonElement>>() {
									@Override
									public void call(List<JsonElement> jsonElements) {
										Toast.makeText(MainActivity.this, jsonElements.toString(), Toast.LENGTH_SHORT).show();
									}
								},
								new Action1<Throwable>() {
									@Override
									public void call(Throwable throwable) {
										Log.e("TAG", "Error", throwable);
										Toast.makeText(MainActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
									}
								}
						);
			}
		});

		findViewById(R.id.buttonAsyncRequest).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				service.listReposAsync("Unic8", new Callback<List<JsonElement>>() {
					@Override
					public void success(List<JsonElement> jsonElements, Response response) {
						Toast.makeText(MainActivity.this, jsonElements.toString(), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void failure(RetrofitError error) {
						Log.e("TAG", "Error", error);
						Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	public static class SomeFakeAuthenticationToken extends TokenInterceptor {

		@Override
		public void injectToken(RequestFacade facade, String token) {
			facade.addHeader("Token", token);
		}
	}

}
