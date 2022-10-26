package com.skydrm.rmc.engine.watermark;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.activity.ViewActivity;
import com.skydrm.rmc.utils.NxCommonUtils;
import com.skydrm.sdk.policy.Watermark;


/**
 * Created by aning on 12/2/2016.
 */
public class Overlay {
    private Context mContext;
    // overlay  container
    private FrameLayout mOverlayFrameLayout;
    // display overlay layout
    private RelativeLayout mOverlayLayout;
    // overlay control
    private TextView mTvOverlay;
    // overlay info
    private Watermark mWatermarkInfo;
    // used to record whether is displaying project overlay, which don't support edit and handling new line is different from others.
    private boolean bIsProjectOverlay = false;


    public Overlay(Context context, FrameLayout mOverlayFrameLayout, Watermark watermarkInfo) {
        this.mContext = context;
        this.mOverlayFrameLayout = mOverlayFrameLayout;
        this.mWatermarkInfo = watermarkInfo;
        initOverlay();
    }

    private void initOverlay() {
        mOverlayFrameLayout.setVisibility(View.VISIBLE);
        mOverlayLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.overlay, null);
        mOverlayFrameLayout.removeAllViews();
        mOverlayFrameLayout.addView(mOverlayLayout);

        mTvOverlay = (TextView) mOverlayLayout.findViewById(R.id.ovtxt);
//        setOverlayParameters(mTvOverlay);
        mOverlayLayout.removeAllViews();
        // in order to calculate related paras about overlay text.
        mOverlayLayout.addView(mTvOverlay);

        if (((Activity) mContext) instanceof ViewActivity) {
            bIsProjectOverlay = ((ViewActivity) mContext).isFromProject();
        }
    }

    private void setOverlayParameters(TextView tvOverlay) {
        try {
            String ovText = mWatermarkInfo.getText();
            // adjust overlay margin
            tvOverlay.setPadding(20, 5, 20, 5);

            StringBuilder sb = new StringBuilder();
            EditWatermarkHelper.replacePresetValue(ovText, sb, bIsProjectOverlay);
            ovText = sb.toString();

            tvOverlay.setText(ovText);
            float alpha = mWatermarkInfo.getTransparentRatio() / 100.0f >= 1.0f ? 1.0f : mWatermarkInfo.getTransparentRatio() / 100.0f;
            tvOverlay.setAlpha(alpha);
            tvOverlay.setTextSize(mWatermarkInfo.getFontSize());
            // #008000 --> 0x008000
            tvOverlay.setTextColor(TextUtils.isEmpty(mWatermarkInfo.getFontColor()) ? mContext.getResources().getColor(R.color.Blue) : Color.parseColor(mWatermarkInfo.getFontColor()));
            if (!TextUtils.isEmpty(mWatermarkInfo.getRotation()) && mWatermarkInfo.getRotation().equals("Anticlockwise")) {
                tvOverlay.setRotation(-45);
            } else {
                tvOverlay.setRotation(45);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showOverlay() {
        setOverlayParameters(mTvOverlay);
        // get the overlay textView's measure: width and height.
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mTvOverlay.measure(w, h);
        // init location paras
        int hypotenuse = mTvOverlay.getMeasuredWidth();
        int tvWidth = (int) (hypotenuse / Math.sqrt(2));
        int tvHeight = mTvOverlay.getMeasuredHeight();
        int halfHeight = tvWidth / 2;
        mTvOverlay.setX(0);
        mTvOverlay.setY(halfHeight);
        int tx = (int) mTvOverlay.getX();
        int ty = (int) mTvOverlay.getY();
        // remove it after calculating the paras.
        mOverlayLayout.removeView(mTvOverlay);
        // add overlays
        TextView tv;
        DisplayMetrics screenMetrics = NxCommonUtils.getScreenMetrics((Activity) mContext);
        for (int x = tx; x < screenMetrics.widthPixels; x = x + hypotenuse) {
            for (int y = ty; y < screenMetrics.heightPixels; y = y + tvWidth + tvHeight) {
                tv = new TextView((Activity) mContext);
                setOverlayParameters(tv);
                tv.setX(x);
                tv.setY(y);
                tv.setGravity(Gravity.CENTER);

                mOverlayLayout.addView(tv);
            }
        }
    }
}
