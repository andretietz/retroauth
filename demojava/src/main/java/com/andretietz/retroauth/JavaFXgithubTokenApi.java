package com.andretietz.retroauth;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.oauth.OAuth20Service;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import okhttp3.Request;

/**
 * Created by andre on 23.03.2016.
 */
class JavaFXGithubTokenApi implements TokenApi<String, OAuth2AccessToken, Object> {

    private final OAuth20Service service;
    private final Stage stage;

    public JavaFXGithubTokenApi(Stage stage, OAuth20Service service) {
        this.service = service;
        this.stage = stage;
    }

    @Override
    public Request modifyRequest(OAuth2AccessToken token, Request request) {
        return request.newBuilder()
                .header("Authorization", "token " + token.getAccessToken())
                .build();
    }

    @Override
    public String convert(String[] annotationValues) {
        return annotationValues[0];
    }

    @Override
    public void receiveToken(OnTokenReceiveListener<OAuth2AccessToken> listener) {
        Platform.runLater(() -> {
            stage.setOnCloseRequest(event -> listener.onCancel());
            WebView webView = new WebView();
            WebEngine engine = webView.getEngine();
            engine.load(service.getAuthorizationUrl());
            engine.locationProperty().addListener((observable, oldLocation, newLocation) -> {
                OAuth2Authorization authorization = service.extractAuthorization(newLocation);
                if (authorization.getCode() != null) {
                    OAuth2AccessToken accessToken = service.getAccessToken(authorization.getCode());
                    listener.onTokenReceive(accessToken);
                    stage.setOnCloseRequest(null);
                    stage.close();
                }
            });
            stage.setScene(new Scene(webView, 600, 600));
            stage.show();
        });
    }

    @Override
    public void refreshToken(Object refreshApi, OnTokenReceiveListener<OAuth2AccessToken> listener) {
        listener.onCancel();
    }
}
