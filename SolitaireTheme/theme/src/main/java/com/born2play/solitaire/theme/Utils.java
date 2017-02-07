package com.born2play.solitaire.theme;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created on 2017/2/7.
 */

public class Utils {
    public static String[] mPackageNameList = {
            "com.queensgame.collection",
            "com.queensgame.solitaire",
            "com.cardgame.solitaire.full",
            "com.cardgame.solitaire.basic1",
            "com.cardgame.solitaire.basic2",};
    public static ArrayList<String> installList = new ArrayList<>();

    public static void refreshInstallList(Activity activity){
        installList.clear();
        for (int i = 0; i < mPackageNameList.length; i++) {
            try {
                activity.getPackageManager().getPackageInfo(mPackageNameList[i], 0);
                installList.add(mPackageNameList[i]);
            }catch (Exception e){}
        }
    }

    //启动应用
    public static void openMainApp(Activity activity, String mainPackageName){
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(mainPackageName);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_SEND);

        String providerFileName = null;
        try {
            String[] list = activity.getAssets().list("");
            for (int i = 0; i < list.length; i++) {
                if (list[i].endsWith(".aac")){
                    providerFileName = list[i];
                    break;
                }
            }
        }catch (Exception e){}
        if (providerFileName != null){
            String packageName = activity.getPackageName();
            intent.putExtra("themeZipUrl","content://" + packageName + ".AssetProvider/" + providerFileName);
            //以包名最后一个字段作为主题的名字透传给主应用
            int index = packageName.lastIndexOf(".");
            String name = packageName.substring(index+1);
            intent.putExtra("themeName", name);
            intent.putExtra("packageName", packageName);
        }
        activity.startActivity(intent);
    }

    //启动应用商店
    public static void openStore(Activity activity, String mainPackageName){
        String string = activity.getPackageName();
        int index = string.lastIndexOf(".");
        String name = string.substring(index+1);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+mainPackageName+"&referrer=utm_source%3Dthemepromo%26utm_campaign%3D"+name));
        activity.startActivity(intent);
    }
}
