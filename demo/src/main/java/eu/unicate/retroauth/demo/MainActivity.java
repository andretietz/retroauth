package eu.unicate.retroauth.demo;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import eu.unicate.retroauth.AndroidAuthenticationHandler;
import eu.unicate.retroauth.AuthRestAdapter;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	private AuthenticationService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
//				.setEndpoint("http://api.atekk.de/auth")
//				.setLogLevel(RestAdapter.LogLevel.FULL)
//				.setAuthHandler(new AndroidAuthenticationHandler(this, LoginActivity.ACCOUNT_TYPE, LoginActivity.TOKEN_TYPE, 3))
//				.build();
//		service = restAdapter.create(AuthenticationService.class);
//
		findViewById(R.id.buttonRequest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				service.getProfile().subscribe();
				AccountManager am = AccountManager.get(MainActivity.this);
				am.addAccount(getString(R.string.auth_account_type), LoginActivity.TOKEN_TYPE, null, null, MainActivity.this, new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						waitForResult(future)
								.subscribeOn(Schedulers.io())
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(
										new Action1<Bundle>() {
											@Override
											public void call(Bundle bundle) {
												System.out.println(bundle.toString());
											}
										},
										new Action1<Throwable>() {
											@Override
											public void call(Throwable throwable) {
												Log.e("TAG", "", throwable);
											}
										}
								);
					}
				}, null);


			}
		});


	}

	private Observable<Bundle> waitForResult(final AccountManagerFuture<Bundle> future) {
		return Observable.create(new Observable.OnSubscribe<Bundle>() {
			@Override
			public void call(Subscriber<? super Bundle> subscriber) {
				try {
					subscriber.onNext(future.getResult());
				} catch (Exception e) {
					subscriber.onError(e);
				}
				subscriber.onCompleted();
			}
		});
	}

}
