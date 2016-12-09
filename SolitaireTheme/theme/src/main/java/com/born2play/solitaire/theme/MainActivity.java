package com.born2play.solitaire.theme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

enum AppStatus {
    no_full,
    updata_full,
    ok_full,
    no_basic,
    updata_basic,
    ok_basic,
    no_full1,
    ok_full1,
    no_full2,
    ok_full2,
}
public class MainActivity extends Activity {
    private String basicPackageName = "com.queensgame.solitaire";
    private String fullPackageName = "com.cardgame.solitaire.full";
    private String full1PackageName = "com.cardgame.solitaire.basic1";
    private String full2PackageName = "com.cardgame.solitaire.basic2";
    private String mainPackageName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        float rate = width / 640.f;

        FrameLayout.LayoutParams bgLayoutParams = (FrameLayout.LayoutParams)(findViewById(R.id.view_bg).getLayoutParams());
        bgLayoutParams.width = (int)(bgLayoutParams.width*rate);
        bgLayoutParams.height = (int)(bgLayoutParams.height*rate);
        bgLayoutParams.topMargin = 0;
        bgLayoutParams.leftMargin = (width- bgLayoutParams.width)/2;


        (findViewById(R.id.bind__button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppStatus appStatus = isInstall();
                if (isOK(appStatus)){
                    openMainApp();
                }else {
                    openStore();
                }
            }
        });
        updateView();
        AppStatus appStatus = isInstall();
        if (!isOK(appStatus)){
            createDialog();
        }
    }

    private void updateView(){
        AppStatus appStatus = isInstall();
        if(isOK(appStatus)){
            ((TextView)findViewById(R.id.view_message)).setText(R.string.message1);
            ((TextView)findViewById(R.id.btnTextView)).setText(R.string.apply);
        }else if (isNeedUpdate(appStatus)){
            ((TextView)findViewById(R.id.view_message)).setText(R.string.message_update);
            ((TextView)findViewById(R.id.btnTextView)).setText(R.string.update);
        }else{
            ((TextView)findViewById(R.id.view_message)).setText(R.string.message);
            ((TextView)findViewById(R.id.btnTextView)).setText(R.string.download);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        updateView();
    }

    private void createDialog(){
        AppStatus appStatus = isInstall();
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        if (isNeedDowload(appStatus)){
            localBuilder.setMessage(R.string.dialog_message);
            localBuilder.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openStore();
                }
            });
        }else{
            localBuilder.setMessage(R.string.dialog_message_update);
            localBuilder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openStore();
                }
            });
        }
        localBuilder.setTitle(R.string.dialog_title);
        localBuilder.setCancelable(true);
        localBuilder.create().show();
    }

    private boolean isOK(AppStatus appStatus){
        if (appStatus == AppStatus.ok_basic || appStatus == AppStatus.ok_full ||
                appStatus == AppStatus.ok_full1 || appStatus == AppStatus.ok_full2){
            return true;
        }
        return false;
    }
    private boolean isNeedUpdate(AppStatus appStatus){
        if (appStatus == AppStatus.updata_basic || appStatus == AppStatus.updata_full){
            return true;
        }
        return false;
    }
    private boolean isNeedDowload(AppStatus appStatus){
        if (appStatus == AppStatus.no_basic || appStatus == AppStatus.no_full ||
                appStatus == AppStatus.no_full1 || appStatus == AppStatus.no_full2){
            return true;
        }
        return false;
    }

    private AppStatus isInstall(){
        int limitVersionCode = 12;
        AppStatus appStatus;
        mainPackageName = basicPackageName;
        boolean ok = false;
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(basicPackageName, 0);
            if (pi.versionCode >= limitVersionCode)
                appStatus = AppStatus.ok_basic;
            else
                appStatus = AppStatus.updata_basic;
            ok = true;
        }catch (Exception e){
            appStatus = AppStatus.no_basic;
        }
        if (!ok){
            try {
                PackageInfo pi = getPackageManager().getPackageInfo(fullPackageName, 0);
                if (pi.versionCode >= limitVersionCode)
                    appStatus = AppStatus.ok_full;
                else
                    appStatus = AppStatus.updata_full;
                mainPackageName = fullPackageName;
                ok = true;
            }catch (Exception e){
            }
        }
        if (!ok){
            try {
                getPackageManager().getPackageInfo(full1PackageName, 0);
                appStatus = AppStatus.ok_full1;
                mainPackageName = full1PackageName;
                ok = true;
            }catch (Exception e){
            }
        }
        if (!ok){
            try {
                getPackageManager().getPackageInfo(full2PackageName, 0);
                appStatus = AppStatus.ok_full2;
                mainPackageName = full2PackageName;
                ok = true;
            }catch (Exception e){
            }
        }
        return appStatus;
    }
    //启动应用
    private void openMainApp(){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage(mainPackageName);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_SEND);

        String providerFileName = null;
        try {
            String[] list = getAssets().list("");
            for (int i = 0; i < list.length; i++) {
                if (list[i].endsWith(".aac")){
                    providerFileName = list[i];
                    break;
                }
            }
        }catch (Exception e){}
        if (providerFileName != null){
            String packageName = getPackageName();
            intent.putExtra("themeZipUrl","content://" + packageName + ".AssetProvider/" + providerFileName);
            //以包名最后一个字段作为主题的名字透传给主应用
            int index = packageName.lastIndexOf(".");
            String name = packageName.substring(index+1);
            intent.putExtra("themeName", name);
            intent.putExtra("packageName", packageName);
        }
        startActivity(intent);
    }
    //启动应用商店
    private void openStore(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+mainPackageName+"&referrer=utm_source%3Dthemepromo"));
        startActivity(intent);
    }
}
