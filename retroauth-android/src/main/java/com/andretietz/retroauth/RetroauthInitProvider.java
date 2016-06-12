package com.andretietz.retroauth;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * This does the trick of registering a lifecycle callback on application start without having the developer
 * do this manually. I knew that I would need the firebase sources for something ^^.
 */
public class RetroauthInitProvider extends ContentProvider {

    public void attachInfo(Context context, ProviderInfo providerInfo) {
        if (providerInfo == null) {
            throw new NullPointerException("RetroauthInitProvider ProviderInfo cannot be null.");
        }
        super.attachInfo(context, providerInfo);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext().getApplicationContext();
        if (context instanceof Application) {
            ContextManager.get((Application) context);
            return false;
        }
        throw new IllegalStateException("Retroauth could not get initialized!");
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
