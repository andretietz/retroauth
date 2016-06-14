package com.andretietz.retroauth;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * This is a very optimistic and completely unsecured implementation of
 * storing a token! It's only supposed to show the effect of the retroauth library
 */
public class XMLTokenStorage implements TokenStorage<String, String, OAuth2AccessToken> {

    private final OAuth20Service service;

    public XMLTokenStorage(OAuth20Service service) {
        this.service = service;
    }

    private static File createFile(String owner) {
        File file = new File(owner + ".xml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    @Override
    public String createType(String[] annotationValues) {
        return annotationValues[0];
    }

    @Override
    public OAuth2AccessToken getToken(String owner, String tokenType) throws AuthenticationCanceledException {
        File file = createFile(owner);
        Serializer serializer = new Persister();
        try {
            XMLAuthToken xmlAuthToken = serializer.read(XMLAuthToken.class, file);
            return new OAuth2AccessToken(xmlAuthToken.token, tokenType, 0, xmlAuthToken.refreshToken, "", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LoginHelper(owner, tokenType, this, service).call();
    }

    @Override
    public void removeToken(String owner, String tokenType, OAuth2AccessToken token) {
        createFile(owner).delete();
    }

    @Override
    public void storeToken(String owner, String tokenType, OAuth2AccessToken token) {
        File file = createFile(owner);
        Serializer serializer = new Persister();
        XMLAuthToken xmlAuthToken = new XMLAuthToken(token.getAccessToken(), token.getRefreshToken());
        try {
            serializer.write(xmlAuthToken, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static class LoginHelper implements Callable<OAuth2AccessToken> {
        private final TokenStorage<String, String, OAuth2AccessToken> storage;
        private final OAuth20Service service;
        private final String owner;
        private final String tokenType;
        private final Lock lock = new ReentrantLock();
        OAuth2AccessToken accessToken;

        LoginHelper(String owner, String type, TokenStorage<String, String, OAuth2AccessToken> storage, OAuth20Service service) {
            this.storage = storage;
            this.service = service;
            this.owner = owner;
            this.tokenType = type;
        }

        @Override
        public OAuth2AccessToken call() {
            Condition condition = lock.newCondition();

            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setOnCloseRequest(event -> {
                    stage.close();
                    lock.lock();
                    try {
                        condition.signal();
                    } finally {
                        lock.unlock();
                    }
                });
                final Map<String, String> additionalParams = new HashMap<>();
                additionalParams.put("access_type", "offline");
                additionalParams.put("prompt", "consent");

                WebView webView = new WebView();
                WebEngine engine = webView.getEngine();
                engine.load(service.getAuthorizationUrl(additionalParams));
                engine.locationProperty().addListener((observable, oldLocation, newLocation) -> {
                    OAuth2Authorization authorization = service.extractAuthorization(newLocation);
                    if (authorization.getCode() != null) {
                        accessToken = service.getAccessToken(authorization.getCode());
                        storage.storeToken(owner, tokenType, accessToken);
                        lock.lock();
                        try {
                            condition.signal();
                        } finally {
                            lock.unlock();
                        }
                        stage.setOnCloseRequest(null);
                        stage.close();
                    }
                });
                stage.setScene(new Scene(webView, 600, 600));
                stage.show();
            });
            lock.lock();
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
            return accessToken;
        }
    }
}
