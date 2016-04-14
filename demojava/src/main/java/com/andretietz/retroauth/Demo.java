package com.andretietz.retroauth;

import com.andretietz.retroauth.providers.Github;
import com.andretietz.retroauth.providers.model.Email;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.schedulers.JavaFxScheduler;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.concurrent.Executors;


public class Demo extends Application {
    private Github githubService;
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
        OAuth20Service github = new ServiceBuilder()
                .apiKey("405f730d96862da912a8")
                .apiSecret("dce0264a8c9eb94689d4d8ffbe1fadb59c33c4c3")
                .scope("user")
                .callback("http://localhost:8000/accounts/github/login/callback")
                .build(GitHubApi.instance());


        BaseAuthenticationHandler<String, OAuth2AccessToken, Object> authHandler = new BaseAuthenticationHandler<>(
                Executors.newSingleThreadExecutor(),
                new JavaFXGithubTokenApi(new Stage(), github),
                new CredentialStorage()
        );

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retroauth.Builder<>(authHandler)
                .baseUrl("https://api.github.com")
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
        this.githubService = retrofit.create(Github.class);

        text = new Text();
        Button button = new Button("Request Emails");
        button.setOnAction(event ->
                githubService.getEmails()
                        .subscribeOn(Schedulers.io())
                        .observeOn(JavaFxScheduler.getInstance())
                        .subscribe(emails -> {
                            StringBuilder sb = new StringBuilder();
                            for (Email email : emails) {
                                sb.append(email.email).append('\n');
                            }
                            text.setText(sb.toString());
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
