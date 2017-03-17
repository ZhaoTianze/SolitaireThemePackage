package com.born2play.solitaire.theme;

import android.app.Application;

/**
 * Created on 2017/3/16.
 */

public class MyApplication extends Application {
    private FacebookAds mFacebookAds = new FacebookAds();
    public void onCreate(){
        super.onCreate();
        mFacebookAds.loadInterstitial(this);
    }

    public FacebookAds getFacebookAds(){
        return mFacebookAds;
    }
}
