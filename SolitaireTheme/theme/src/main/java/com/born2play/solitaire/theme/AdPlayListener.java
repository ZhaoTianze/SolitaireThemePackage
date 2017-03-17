package com.born2play.solitaire.theme;

import com.facebook.ads.InterstitialAd;

/**
 * Created on 2017/3/16.
 */

public interface AdPlayListener {
    void adLoaded(InterstitialAd interstitialAd);
    //包含加载失败和广告关闭
    void adEnded();
}
