package com.andretietz.retroauth.demo;

import android.accounts.Account;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.andretietz.retroauth.AuthenticationActivity;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class LoginActivity extends AuthenticationActivity {

    private final OAuth20Service helper = new ServiceBuilder(ProviderGithub.CLIENT_ID)
            .apiSecret(ProviderGithub.CLIENT_SECRET)
            .callback(ProviderGithub.CLIENT_CALLBACK)
            .build(GitHubApi.instance());

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_login);


        WebView webView = findViewById(R.id.webView);
        webView.loadUrl(helper.getAuthorizationUrl());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                final OAuth2Authorization authorization = helper.extractAuthorization(url);
                String code = authorization.getCode();
                if (code == null) {
                    view.loadUrl(url);
                } else {
                    Single.fromCallable(new TokenVerifier(helper, code))

                            .subscribeOn(Schedulers.io())
                            .subscribe(token -> {
                                        Account account = createOrGetAccount(pair.second);
                                        storeToken(
                                                account,
                                                getRequestedTokenType(),
                                                token.getAccessToken());
                                        finalizeAuthentication(account);
                                    },
                                    Throwable::printStackTrace);
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

    private static class TokenVerifier implements Callable<OAuth2AccessToken> {

        private final OAuth20Service service;
        private final String code;

        private final GithubService api = new Retrofit.Builder()
                .baseUrl("http://api.github.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build().create(GithubService.class);

        TokenVerifier(OAuth20Service service, String code) {
            this.service = service;
            this.code = code;
        }

        @Override
        public OAuth2AccessToken call() throws Exception {
            OAuth2AccessToken token = service.getAccessToken(code);
            GithubService.Info info = api.getUserInfo(token.getAccessToken()).blockingGet();


        }
    }
}
