package com.skydrm.rmc.ui.widget.popupwindow;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.annotation.ColorInt;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Created by aning on 5/17/2017.
 */

public class OperateCompleteWindow3 extends PopupWindow {
    private Activity mActivity;
    // root view
    private View mRootView;
    // window standing time, default time is 3s.
    private long mStandingTme = 3000;
    // set alpha
    private float mAlpha = 0.4f; // default value
    // timer controls window standing time.
    private CountDownTimer mTimer = new CountDownTimer(mStandingTme, 10) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            OperateCompleteWindow3.this.dismiss();
            recoverBgTransparency();
            if(mActivity != null && !mActivity.isFinishing()) {
                mActivity.finish();
            }
        }
    };

    /**
     * @param rootView  root view
     * @param windowContent window layout view
     * @param width window width
     * @param height window height
     */
    public OperateCompleteWindow3(Activity activity, View rootView, View windowContent, int width, int height) {
        mActivity = activity;
        mRootView = rootView;
        this.setContentView(windowContent);
        this.setWidth(width);
        this.setHeight(height);

        setBgTransparency();
    }

    public OperateCompleteWindow3(final Activity activity, View rootView, View windowContent, int width, int height, boolean focusable) {
        mActivity = activity;
        mRootView = rootView;
        this.setContentView(windowContent);
        this.setWidth(width);
        this.setHeight(height);

        setBgTransparency();

        // set popup window getting focus in order to can capture the event of click backKey.
        setFocusable(focusable);
        // init do something when popup window dismiss.
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (activity != null) {
                    activity.finish();
                }
                recoverBgTransparency();
            }
        });
    }

    // set the window standing time.
    public void setStandingTme(long time) {
        this.mStandingTme = time;
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
    }

    // set bg color
    public void setBackGroundColor(@ColorInt int color) {
        ColorDrawable mDrawable = new ColorDrawable(color);
        this.setBackgroundDrawable(mDrawable);
    }

    private void setBgTransparency() {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = mAlpha;
        mActivity.getWindow().setAttributes(lp);
    }

    private void recoverBgTransparency() {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        // default value
        lp.alpha = 1.0f;
        mActivity.getWindow().setAttributes(lp);
    }

    // show window in center.
    public void showWindow() {
        if (mActivity != null && !mActivity.isFinishing()) {
            showAtLocation(mRootView, Gravity.CENTER, 0, 0);
            mTimer.start();
        }
    }

}
