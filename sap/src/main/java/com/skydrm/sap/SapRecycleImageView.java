package com.skydrm.sap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class SapRecycleImageView extends AppCompatImageView {
    private int mColor;
    private int mBorderWidth;
    private Paint mPaint;

    public SapRecycleImageView(Context context) {
        super(context);
    }

    public SapRecycleImageView(Context context, AttributeSet attrs,
                               int defStyle) {
        super(context, attrs, defStyle);
    }

    public SapRecycleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public void setBorderWidth(int width) {
        this.mBorderWidth = width;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rec = canvas.getClipBounds();
        rec.bottom--;
        rec.right--;

        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderWidth);
        canvas.drawRect(rec, mPaint);
    }
}