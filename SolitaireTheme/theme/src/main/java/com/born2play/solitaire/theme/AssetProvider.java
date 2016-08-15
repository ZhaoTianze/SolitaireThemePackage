package com.born2play.solitaire.theme;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created on 16/6/27.
 */
public class AssetProvider extends ContentProvider {
    @Override
    public AssetFileDescriptor openAssetFile(Uri paramUri, String paramString)throws FileNotFoundException{
        Object localObject = paramUri.getLastPathSegment();
        if (TextUtils.isEmpty((CharSequence)localObject)){
            throw new FileNotFoundException();
        }
        try{
            localObject = getContext().getAssets().openFd((String)localObject);
            return (AssetFileDescriptor)localObject;
        }catch (IOException e){
            e.printStackTrace();
        }
        return super.openAssetFile(paramUri, paramString);
    }
    @Override
    public boolean onCreate() {
        return true;
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
