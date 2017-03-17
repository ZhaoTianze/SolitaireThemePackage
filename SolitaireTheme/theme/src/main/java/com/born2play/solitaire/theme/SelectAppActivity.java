package com.born2play.solitaire.theme;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.ads.InterstitialAd;

/**
 * Created on 2017/2/7.
 */

public class SelectAppActivity extends Activity {
    private FacebookAds mFacebookAds;
    private LoadingView mLoadingView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_app_view);
        mLoadingView = (LoadingView)findViewById(R.id.loadingView);
        mLoadingView.setVisibility(View.GONE);
        mFacebookAds = ((MyApplication)getApplication()).getFacebookAds();
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        Utils.refreshInstallList(this);
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(new AppListAdapter());
    }

    @Override
    public boolean onKeyDown(int code, KeyEvent event){
        if (code == KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(code, event);
    }

    public void openMainApp(final String pkg){
        mLoadingView.setVisibility(View.VISIBLE);
        mFacebookAds.showInterstitial(SelectAppActivity.this, new AdPlayListener() {
            @Override
            public void adLoaded(InterstitialAd interstitialAd) {
                interstitialAd.show();
            }

            @Override
            public void adEnded() {
                mLoadingView.setVisibility(View.GONE);
                mFacebookAds.clearListener();
                Utils.openMainApp(SelectAppActivity.this.getApplicationContext(), pkg);
                SelectAppActivity.this.finish();
            }
        });
    }

    public class AppListAdapter extends BaseAdapter {
        View[] itemViews;

        public AppListAdapter() {
            itemViews = new View[getCount()];
        }

        @Override
        public int getCount() {
            return Utils.installList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(SelectAppActivity.this);
                convertView = inflater.inflate(R.layout.select_app_item, null);
            }
            ImageView icon = (ImageView)convertView.findViewById(R.id.app_icon);
            TextView name = (TextView)convertView.findViewById(R.id.app_name);
            try {
                PackageManager manager = SelectAppActivity.this.getPackageManager();
                ApplicationInfo info = manager.getApplicationInfo(Utils.installList.get(position), 0);
                name.setText(info.loadLabel(manager).toString());
                icon.setImageDrawable(info.loadIcon(manager));
            }catch (Exception e){}
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFacebookAds.showInterstitial(SelectAppActivity.this, new AdPlayListener() {
                        @Override
                        public void adLoaded(InterstitialAd interstitialAd) {

                        }

                        @Override
                        public void adEnded() {

                        }
                    });
                    SelectAppActivity.this.openMainApp(Utils.installList.get(position));
                }
            });

            return convertView;
        }
    }
}
