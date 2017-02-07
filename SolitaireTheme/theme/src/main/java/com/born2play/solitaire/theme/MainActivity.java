package com.born2play.solitaire.theme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;


public class MainActivity extends Activity {
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
        int count = analysisInstall();
        if (count == 0){
            createDialog();
        }
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
        Intent intent = new Intent(this, SelectAppActivity.class);
        this.startActivity(intent);
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
}
