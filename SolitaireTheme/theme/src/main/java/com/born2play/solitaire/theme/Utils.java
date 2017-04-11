package com.born2play.solitaire.theme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created on 2017/2/7.
 */

class Utils {
    static String[] mPackageNameList = {
            "com.queensgame.collection",
            "com.queensgame.solitaire",
            "com.cardgame.solitaire.full",
            "com.cardgame.solitaire.basic1",
            "com.cardgame.solitaire.basic2",
            "com.queensgame.freecell",
            "com.queensgame.spider",
            "com.queensgame.spider2",
            "com.hapogames.FreeCell",
            "com.queensgame.tripeaks",
    };
    static ArrayList<String> installList = new ArrayList<>();

    static void refreshInstallList(Context context){
        installList.clear();
        for (int i = 0; i < mPackageNameList.length; i++) {
            try {
                context.getPackageManager().getPackageInfo(mPackageNameList[i], 0);
                installList.add(mPackageNameList[i]);
            }catch (Exception e){}
        }
    }

    //启动应用
    static void openMainApp(Context context, String mainPackageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(mainPackageName);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_SEND);

        String providerFileName = null;
        try {
            String[] list = context.getAssets().list("");
            for (int i = 0; i < list.length; i++) {
                if (list[i].endsWith(".aac")){
                    providerFileName = list[i];
                    break;
                }
            }
        }catch (Exception e){}
        if (providerFileName != null){
            String packageName = context.getPackageName();
            intent.putExtra("themeZipUrl","content://" + packageName + ".AssetProvider/" + providerFileName);
            //以包名最后一个字段作为主题的名字透传给主应用
            int index = packageName.lastIndexOf(".");
            String name = packageName.substring(index+1);
            intent.putExtra("themeName", name);
            intent.putExtra("packageName", packageName);
        }
        context.startActivity(intent);
    }

    //启动应用商店
    static void openStore(Context context, String mainPackageName){
        String string = context.getPackageName();
        int index = string.lastIndexOf(".");
        String name = string.substring(index+1);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+mainPackageName+"&referrer=utm_source%3Dthemepromo%26utm_campaign%3D"+name));
        context.startActivity(intent);
    }

    /**
     * 网络是否可用
     */
    static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)    return false;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }
}
