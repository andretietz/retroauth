package com.andretietz.retroauth.providers;

import com.andretietz.retroauth.Authenticated;
import com.andretietz.retroauth.providers.model.Email;

import java.util.List;

import retrofit2.http.GET;
import rx.Observable;

public interface Github {
    @Authenticated("providers")
    @GET("/user/emails")
    Observable<List<Email>> getEmails();
}
