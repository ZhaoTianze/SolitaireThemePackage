package com.born2play.solitaire.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created on 2017/3/16.
 */

public class LoadingView extends RelativeLayout {

    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        return true;
    }
}
