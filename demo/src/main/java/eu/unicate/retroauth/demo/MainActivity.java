package eu.unicate.retroauth.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import eu.unicate.retroauth.AuthRestAdapter;
import eu.unicate.retroauth.interceptors.TokenInterceptor;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

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
						.subscribe();
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
