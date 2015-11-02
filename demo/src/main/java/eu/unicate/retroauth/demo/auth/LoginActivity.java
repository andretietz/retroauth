package eu.unicate.retroauth.demo.auth;

import android.accounts.Account;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.List;

import eu.unicate.retroauth.AuthenticationActivity;
import eu.unicate.retroauth.demo.R;
import eu.unicate.retroauth.demo.auth.github.GithubHelper;
import eu.unicate.retroauth.demo.auth.github.model.AccessToken;
import eu.unicate.retroauth.demo.auth.github.model.GithubUser;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class LoginActivity extends AuthenticationActivity {

	private GithubHelper helper;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_login);
		// I do trust you here! usually you don't hand out the applicationId or the secret
		// as soon as I feel like it, I'll deactivate the demo on github.
		helper = new GithubHelper(
				// github client id
				"405f730d96862da912a8",
				// github client secret
				"dce0264a8c9eb94689d4d8ffbe1fadb59c33c4c3",
				// callback link you defined, when creating the application on github
				"http://localhost:8000/accounts/github/login/callback"
		);
		WebView webView = (WebView) findViewById(R.id.webView);
		List<String> scopes = new ArrayList<>();
		scopes.add("user");
		String url = helper.getAuthorizationUrl(scopes);
		webView.loadUrl(url);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Uri uri = Uri.parse(url);
				final String code = uri.getQueryParameter("code");
				if (null == code) {
					view.loadUrl(url);
				} else {
					helper.getAccessToken(code)
							.subscribeOn(Schedulers.io())
							.subscribe(new Action1<AccessToken>() {
										   @Override
										   public void call(final AccessToken accessToken) {
											   helper.getUser(accessToken)
													   .subscribeOn(Schedulers.io())
													   .subscribe(new Action1<GithubUser>() {
														   @Override
														   public void call(GithubUser user) {
															   Account account = createOrGetAccount(user.login);
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
	}

	private void removeCookies() {
		CookieManager cookieManager = CookieManager.getInstance();
		if (VERSION.SDK_INT < 21) {
			//noinspection deprecation
			cookieManager.removeAllCookie();
		} else {
			cookieManager.removeAllCookies(null);
		}
	}

	@Override
	public void finish() {
		// remove cookies
		// without that, multiaccount does not work since the browser will relogin the user
		removeCookies();
		super.finish();
	}
}
