package com.skydrm.rmc.utils;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import java.lang.ref.WeakReference;

public class VideoViewEx extends VideoView {

    public VideoViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(new WeakReference<Context>(context).get(), attrs, defStyle);
    }

    public VideoViewEx(Context context, AttributeSet attrs) {
        super(new WeakReference<Context>(context).get(), attrs);
    }

    public VideoViewEx(Context context) {
        super(new WeakReference<Context>(context).get());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

}