package com.andretietz.retroauth;

import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;
import rx.schedulers.Schedulers;


public class Demo extends Application {
    private Google service;
    private Text text;

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("JavaFX Retroauth Demo");
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        OAuth20Service helper = new ServiceBuilder()
                .apiKey("329078189044-q3g29v14uhnrbb5vsaj8d34j26vh4fb4.apps.googleusercontent.com")
                .apiSecret("HOePqkgIemKIcNhfRt8_jpfF")
                .scope("profile")
                .state("secret" + new Random().nextInt(999_999))
                .callback("http://localhost:8000/accounts/google/login/callback/")
                .build(GoogleApi20.instance());

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        XMLTokenStorage xmlTokenStorage = new XMLTokenStorage(helper);

        ProviderGoogle provider = new ProviderGoogle();

        AuthenticationHandler<String, String, OAuth2AccessToken> authHandler = new AuthenticationHandler<>(
                new MethodCache.DefaultMethodCache<>(),
                new SimpleOwnerManager(), xmlTokenStorage, provider
        );

        Retrofit retrofit = new Retroauth.Builder<>(authHandler)
                .baseUrl("https://www.googleapis.com/")
                .client(httpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
        provider.setRetrofit(retrofit);
        this.service = retrofit.create(Google.class);

        text = new Text();
        Button button = new Button("Request name");
        button.setOnAction(event ->
                service.getEmails()
                        .subscribeOn(Schedulers.io())
                        .observeOn(JavaFxScheduler.getInstance())
                        .subscribe(emails -> {
                            text.setText("Hello: " + emails.name);
                        }));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.add(button, 0, 0);
        grid.add(text, 0, 1);
        Scene scene = new Scene(grid, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
