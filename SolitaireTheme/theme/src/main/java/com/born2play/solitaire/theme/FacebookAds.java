package com.born2play.solitaire.theme;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;


/**
 * Created on 2017/3/16.
 */

class FacebookAds {
    private NativeAd mNativeAd;
    private AdPlayListener mAdPlayListener;
    private boolean mLoading = false;
    private final String mInterstitialId = "217345668743762_217347392076923";
    private final String mNativeId = "217345668743762_217345995410396";
    private InterstitialAd mInterstitialAd = null;

    private boolean isInterstitialReady(){
        return mInterstitialAd != null && mInterstitialAd.isAdLoaded();
    }

    public void loadInterstitial(final Context context){
        if (!Utils.isNetworkAvailable(context)) {
            if (mAdPlayListener != null){
                mAdPlayListener.adEnded();
            }
            return;
        }
        if (mLoading)   return;
        if (mInterstitialAd != null){
            mInterstitialAd.destroy();
            mInterstitialAd = null;
        }
        if (mInterstitialAd == null){
            mInterstitialAd = new InterstitialAd(context, mInterstitialId);
//            AdSettings.addTestDevice("5c30ad399b72a8b07db7fe73b495a4e7");
            mInterstitialAd.setAdListener(new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {
                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    if (mAdPlayListener != null){
                        mAdPlayListener.adEnded();
                    }
                    loadInterstitial(context);
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    mLoading = false;
                    if (mAdPlayListener != null){
                        mAdPlayListener.adEnded();
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    mLoading = false;
                    if (mAdPlayListener != null){
                        mAdPlayListener.adLoaded(mInterstitialAd);
                    }
                }

                @Override
                public void onAdClicked(Ad ad) {

                }
            });
        }
        if (!mLoading) {
            mInterstitialAd.loadAd();
            mLoading = true;
        }
    }

    public void showInterstitial(Context context, @NonNull AdPlayListener adListener){
        mAdPlayListener = adListener;
        if (!isInterstitialReady()) {
            loadInterstitial(context);
        }else{
            mInterstitialAd.show();
        }
    }

    public void clearListener(){
        mAdPlayListener = null;
    }

    public void loadNativeBanner(final Activity context, final ViewGroup view){
        if (view == null)   return;
        view.setVisibility(View.GONE);
        mNativeAd = new NativeAd(context, mNativeId);
        mNativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {

            }

            @Override
            public void onAdLoaded(Ad ad) {
                ImageView nativeAdIcon = (ImageView)view.findViewById(R.id.native_ad_icon);
                TextView nativeAdTitle = (TextView)view.findViewById(R.id.ad_title);
                TextView nativeAdBody = (TextView)view.findViewById(R.id.ad_txt);
                TextView btnTitle = (TextView)view.findViewById(R.id.ad_callAction);

                NativeAd.Image image = mNativeAd.getAdIcon();
                NativeAd.downloadAndDisplayImage(image, nativeAdIcon);
                nativeAdTitle.setText(mNativeAd.getAdTitle());
                nativeAdBody.setText(mNativeAd.getAdBody());
                btnTitle.setText(mNativeAd.getAdCallToAction());

                AdChoicesView adChoicesView = new AdChoicesView(context, mNativeAd, true);
                ((ViewGroup)view.findViewById(R.id.icon_layout)).addView(adChoicesView, 0);
                adChoicesView.bringToFront();
                mNativeAd.registerViewForInteraction(view);

                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }
        });
        mNativeAd.loadAd();
    }
}
