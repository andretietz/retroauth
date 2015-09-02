package eu.unicate.retroauth.demo;

import android.accounts.Account;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.unicate.retroauth.AuthenticationActivity;
import eu.unicate.retroauth.demo.github.AccessToken;
import eu.unicate.retroauth.demo.github.GithubHelper;
import eu.unicate.retroauth.demo.github.GithubUser;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class LoginActivity extends AuthenticationActivity {

	private TextView textUser;
	private TextView textPass;

	private AlertDialog dialog;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_login);
		textUser = (TextView) findViewById(R.id.textUser);
		textPass = (TextView) findViewById(R.id.textPassword);


		final GithubHelper helper = new GithubHelper("applications-client-id", "applications-client-secret", "http://localhost:8000/accounts/github/login/callback");


		findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
				WebView wv = new WebView(LoginActivity.this);
				List<String> scopes = new ArrayList<>();
				scopes.add("user");
				String url = helper.getAuthorizationUrl(scopes);
				wv.loadUrl(url);
				wv.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						Uri uri = Uri.parse(url);
						final String code = uri.getQueryParameter("code");
						if (null == code)
							view.loadUrl(url);
						else {
							helper.getAccessTokenUrl(code)
									.subscribeOn(Schedulers.io())
									.subscribe(new Action1<AccessToken>() {
												   @Override
												   public void call(final AccessToken accessToken) {
													   dialog.dismiss();
													   helper.getUser(accessToken)
															   .subscribeOn(Schedulers.io())
															   .subscribe(new Action1<GithubUser>() {
																   @Override
																   public void call(GithubUser githubUser) {
																	   Account account = createOrGetAccount(githubUser.login);
																	   storeToken(account, getString(R.string.auth_token_type), accessToken.token);
																	   finalizeAuthentication(account);
																   }
															   });
												   }
											   },
											new Action1<Throwable>() {
												@Override
												public void call(Throwable throwable) {
													throwable.printStackTrace();
												}
											}

									);
						}
						return true;
					}
				});

				alert.setView(wv);
//				alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int id) {
//						dialog.dismiss();
//					}
//				});
				dialog = alert.show();
				// do the login
//				String token = demoLogin(textUser.getText().toString(), textPass.getText().toString());
//				if (null != token) {
//					String accountName = textUser.getText().toString();
//					// create the account if necessary
//					Account account = createOrGetAccount(accountName);
//					storeToken(account, getString(R.string.auth_token_type), token);
//					finalizeAuthentication(account);
//				}
			}
		});

		String username = getAccountName();
		if (null != username) {
			textUser.setEnabled(false);
			textUser.setText(username);
		}
	}

	private void closeDialog() {

	}

	private String demoLogin(String username, String password) {
		if (errorCheck(username, textUser, "Don't leave the username empty!"))
			return null;
		if (errorCheck(password, textPass, "Don't leave the password empty!"))
			return null;
		if ("test".equalsIgnoreCase(password))
			return "this-is-a-demo-token-from-user: " + username;
		return null;
	}

	private boolean errorCheck(String s, TextView tv, String message) {
		boolean error = TextUtils.isEmpty(s);
		tv.setError(error ? message : null);
		tv.requestFocus();
		return error;
	}

}
