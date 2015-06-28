package eu.unicate.retroauth.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.List;

import eu.unicate.retroauth.AuthRestAdapter;
import eu.unicate.retroauth.interceptors.TokenInterceptor;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
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

		findViewById(R.id.buttonRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				service.listRepos("Unic8")
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
									}
								}
						);
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
