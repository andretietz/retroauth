package com.andretietz.retroauth;

import android.accounts.Account;
import android.app.Activity;
import android.support.annotation.NonNull;

import okhttp3.Request;

/**
 * Created by andre on 13/04/16.
 */
public class BaseAndroidTokenApi implements TokenApi<AndroidTokenType, String, Object> {

    private final AuthAccountManager authAccountManager;
    private final TokenApplier applier;
    private AndroidTokenType type;

    public BaseAndroidTokenApi(@NonNull Activity activity, @NonNull TokenApplier applier) {
        authAccountManager = new AuthAccountManager(ContextManager.get(activity));
        this.applier = applier;
    }

    @Override
    public Request modifyRequest(String token, Request request) {
        return applier.applyToken(token, request);
    }

    @Override
    public AndroidTokenType convert(String[] annotationValues) {
        type = new AndroidTokenType.Builder()
                .accountType(annotationValues[0])
                .tokenType(annotationValues[1])
                .build();
        return type;
    }

    @Override
    public void receiveToken(final OnTokenReceiveListener<String> listener) {
        try {
            String name = authAccountManager.getActiveAccountName(type.accountType, true);
            Account account = authAccountManager.getAccountByName(name, type.accountType);
            String token = authAccountManager.getAuthToken(account, type.accountType, type.tokenType);
            listener.onTokenReceive(token);
        } catch (ChooseAccountCanceledException e) {
            listener.onCancel();
        } catch (AuthenticationCanceledException e) {
            listener.onCancel();
        } catch (Exception e) {
            listener.onCancel();
        }
    }

    @Override
    public void refreshToken(Object refreshApi, OnTokenReceiveListener<String> listener) {
        //listener.onCancel();
    }
}
