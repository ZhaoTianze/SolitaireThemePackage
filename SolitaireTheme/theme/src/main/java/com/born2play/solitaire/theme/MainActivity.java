package com.born2play.solitaire.theme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.InterstitialAd;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {
    private String mainPackageName = "";
    private LoadingView mLoadingView;
    private FacebookAds mFacebookAds;
    private boolean showingAlert = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mFacebookAds = ((MyApplication)getApplication()).getFacebookAds();
        mFacebookAds.loadNativeBanner(this, (ViewGroup)findViewById(R.id.ad_view));

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        float rate = width / 640.f;

        FrameLayout.LayoutParams bgLayoutParams = (FrameLayout.LayoutParams)(findViewById(R.id.view_bg).getLayoutParams());
        bgLayoutParams.width = (int)(bgLayoutParams.width*rate);
        bgLayoutParams.height = (int)(bgLayoutParams.height*rate);

        mLoadingView = (LoadingView)findViewById(R.id.loadingView);

        (findViewById(R.id.btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = analysisInstall();
                if (count == 1) {
                    Utils.openMainApp(MainActivity.this, mainPackageName);
                }else if (count > 1){
                    openSelectList();
                }else {
                    Utils.openStore(MainActivity.this, mainPackageName);
                }
            }
        });
        updateView();

        mLoadingView.setVisibility(View.VISIBLE);
        mFacebookAds.showInterstitial(this, new AdPlayListener() {
            @Override
            public void adLoaded(InterstitialAd interstitialAd) {
                interstitialAd.show();
            }

            @Override
            public void adEnded() {
                mLoadingView.setVisibility(View.GONE);
                mFacebookAds.clearListener();
                int count = analysisInstall();
                if (count == 0){
                    createDialog();
                }
            }
        });

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingView.setVisibility(View.GONE);
                        int count = analysisInstall();
                        if (count == 0){
                            createDialog();
                        }
                    }
                });
            }
        };
        timer.schedule(task, 5*1000);
    }

    private void updateView(){
        int count = analysisInstall();
        if(count > 0){
            ((TextView)findViewById(R.id.view_message)).setText(R.string.message1);
            ((TextView)findViewById(R.id.btnTextView)).setText(R.string.apply);
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
        if (showingAlert)   return;
        showingAlert = true;
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setMessage(R.string.dialog_message);
        localBuilder.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.openStore(MainActivity.this, mainPackageName);
            }
        });
        localBuilder.setTitle(R.string.dialog_title);
        localBuilder.setCancelable(true);
        localBuilder.create().show();
    }

    private void openSelectList(){
        Intent intent = new Intent(MainActivity.this, SelectAppActivity.class);
        MainActivity.this.startActivity(intent);
    }

    private int analysisInstall(){
        Utils.refreshInstallList(this);
        if (Utils.installList.size() > 0){
            mainPackageName = Utils.installList.get(0);
        }else{
            mainPackageName = Utils.mPackageNameList[1];
        }
        return Utils.installList.size();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK && mLoadingView.getVisibility() == View.VISIBLE){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
