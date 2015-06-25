package eu.unicate.retroauth.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import eu.unicate.retroauth.AuthRestAdapter;
import retrofit.RestAdapter;

public class MainActivity extends AppCompatActivity {

	private AuthenticationService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
				.setEndpoint("http://api.atekk.de/auth")
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.build();
		service = restAdapter.create(this, AuthenticationService.class);
//
		findViewById(R.id.buttonRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				service.getProfile().subscribe();

			}
		});


	}

}
