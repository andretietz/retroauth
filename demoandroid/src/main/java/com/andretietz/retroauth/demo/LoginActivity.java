package com.andretietz.retroauth.demo;

import android.accounts.Account;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Pair;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.andretietz.retroauth.AuthenticationActivity;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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

        // Go to:
        // https://console.developers.google.com/
        // create a Web client id and provide the client id and it's secret here:
        // ProviderGoogle.GOOGLE_CLIENT_ID
        // ProviderGoogle.GOOGLE_CLIENT_SECRET
        // ProviderGoogle.GOOGLE_CLIENT_CALLBACK

        helper = new ServiceBuilder()
                .apiKey(ProviderGoogle.GOOGLE_CLIENT_ID)
                .apiSecret(ProviderGoogle.GOOGLE_CLIENT_SECRET)
                .scope("profile")
                .state("secret" + new Random().nextInt(999_999))
                .callback(ProviderGoogle.GOOGLE_CLIENT_CALLBACK)
                .build(GoogleApi20.instance());

        final Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("access_type", "offline");
        //force to reget refresh token (if usera are asked not the first time)
        additionalParams.put("prompt", "consent");


        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(helper.getAuthorizationUrl(additionalParams));
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
                                               storeToken(account, getRequestedTokenType(), pair.first.getAccessToken(), pair.first.getRefreshToken());
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
            OAuthRequest request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/oauth2/v1/userinfo", service);
            service.signRequest(accessToken, request);
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<Profile> jsonAdapter = moshi.adapter(Profile.class);
            String body = request.send().getBody();
            System.out.println(body);
            Profile profile = jsonAdapter.fromJson(body);
            return Pair.create(accessToken, profile.name);
        }

        static class Profile {
            String name;
        }
    }
}
