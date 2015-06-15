package eu.unicate.retroauth.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import eu.unicate.retroauth.AndroidAuthenticationHandler;
import eu.unicate.retroauth.AuthRestAdapter;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
				.setEndpoint("https://api.github.com")
				.setAuthHandler(new AndroidAuthenticationHandler(this, "eu.unicate.example", "", 3))
				.build();
		GitHubService service = restAdapter.create(GitHubService.class);

		service.listRepos("Unic8").subscribe();

	}
}
