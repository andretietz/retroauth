package com.andretietz.retroauth.demo;

import android.accounts.Account;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Pair;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.andretietz.retroauth.AuthenticationActivity;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.json.JSONObject;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class LoginActivity extends AuthenticationActivity {

    private OAuth20Service helper;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_login);
        // I do trust you here! usually you don't hand out the applicationId or the secret
        // as soon as I feel like it, I'll deactivate the demo on providers.
        helper = new ServiceBuilder()
              .apiKey("405f730d96862da912a8")
              .apiSecret("dce0264a8c9eb94689d4d8ffbe1fadb59c33c4c3")
              .scope("user")
              .callback("http://localhost:8000/accounts/github/login/callback")
              .build(GitHubApi.instance());

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl(helper.getAuthorizationUrl());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                final OAuth2Authorization authorization = helper.extractAuthorization(url);
                String code = authorization.getCode();
                if (code == null) {
                    view.loadUrl(url);
                } else {
                    Observable.fromCallable(new TokenVerifier(helper, code))
                          .subscribeOn(Schedulers.io())
                          .subscribe(new Action1<Pair<OAuth2AccessToken, String>>() {
                                         @Override
                                         public void call(Pair<OAuth2AccessToken, String> pair) {
                                             Account account = createOrGetAccount(pair.second);
                                             storeToken(account, pair.first.getAccessToken());
                                             finalizeAuthentication(account);
                                         }
                                     },
                                new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                });
                }
                return true;
            }
        });
    }

    @Override
    public void finish() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            cookieManager.removeAllCookie();
        } else {
            cookieManager.removeAllCookies(null);
        }
        super.finish();
    }

    private static class TokenVerifier implements Callable<Pair<OAuth2AccessToken, String>> {

        private final OAuth20Service service;
        private final String code;

        TokenVerifier(OAuth20Service service, String code) {
            this.service = service;
            this.code = code;
        }

        @Override
        public Pair<OAuth2AccessToken, String> call() throws Exception {
            OAuth2AccessToken accessToken = service.getAccessToken(code);
            OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user", service);
            service.signRequest(accessToken, request);
            String content = request.send().getBody();
            JSONObject obj = new JSONObject(content);
            return Pair.create(accessToken, obj.getString("login"));
        }
    }
}
