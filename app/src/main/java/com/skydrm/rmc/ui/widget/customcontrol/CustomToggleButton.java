package com.skydrm.rmc.ui.widget.customcontrol;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.skydrm.rmc.R;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by aning on 5/5/2017.
 */

public class CustomToggleButton extends View implements View.OnClickListener {
    private static final int sMSG_TOGGLE_STATUS_CHANGE = 0x01;
    private static final int DEFAULT_BUTTON_WIDTH = 38;
    private static final int DEFAULT_BUTTON_HEIGHT = 30;
    private static final int DEFAULT_CIRCLE_RADIUS = 10;
    private static final int DEFAULT_LINE_WIDTH = 14;
    // control color
    private int mOnLineColor;
    private int mOnCircleColor;
    private int mOffLineColor;
    private int mOffCircleColor;
    // dimens
    private int mBtnWidth;
    private int mBtnHeight;
    private int mCircleRadius;
    private int mLineWidth;
    // X axis of the circle
    private int mCircleX;

    // paint
    private Paint mLinePaint = new Paint();
    private Paint mCirclePaint = new Paint();

    private boolean bChangeComplete = true;
    // in default, is toggle on
    private boolean bToggleOn = true;
    // toggle status change listener
    private OnToggleChanged mListener;

    private Resources r;
    private Timer mTimer;
    private Handler mHandler;

    public CustomToggleButton(Context context) {
        this(context, null, 0);
    }

    public CustomToggleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHandler = new ToggleChangeHandler(this);
        init(context, attrs, defStyleAttr);
    }

    public int getOnLineColor() {
        return mOnLineColor;
    }

    public void setOnLineColor(int onLineColor) {
        this.mOnLineColor = onLineColor;
        invalidate();
    }

    public int getOnCircleColor() {
        return mOnCircleColor;
    }

    public void setOnCircleColor(int onCircleColor) {
        this.mOnCircleColor = onCircleColor;
        invalidate();
    }

    public int getOffLineColor() {
        return mOffLineColor;
    }

    public void setOffLineColor(int offLineColor) {
        this.mOffLineColor = offLineColor;
        invalidate();
    }

    public int getOffCircleColor() {
        return mOffCircleColor;
    }

    public void setOffCircleColor(int offCircleColor) {
        this.mOffCircleColor = offCircleColor;
        invalidate();
    }

    public void setOnToggleChanged(OnToggleChanged onToggleChanged) {
        mListener = onToggleChanged;
    }

    public boolean isToggleOn() {
        return bToggleOn;
    }

    public void setToggleOn(boolean toggleOn) {
        this.bToggleOn = toggleOn;
        if (toggleOn) {
            mCircleX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBtnWidth - mCircleRadius, r.getDisplayMetrics());
        } else {
            mCircleX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mCircleRadius, r.getDisplayMetrics());
        }
        invalidate();
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        // get custom attrs
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomToggleButton, defStyleAttr, 0);
        mOnLineColor = typedArray.getColor(R.styleable.CustomToggleButton_onLineColor, context.getResources().getColor(R.color.onLineColor_default));
        mOnCircleColor = typedArray.getColor(R.styleable.CustomToggleButton_onCircleColor, context.getResources().getColor(R.color.onCircleColor_default));
        mOffLineColor = typedArray.getColor(R.styleable.CustomToggleButton_offLineColor, context.getResources().getColor(R.color.offLineColor_default));
        mOffCircleColor = typedArray.getColor(R.styleable.CustomToggleButton_offCircleColor, context.getResources().getColor(R.color.offCircleColor_default));
        mBtnWidth = typedArray.getInteger(R.styleable.CustomToggleButton_btnWidth, DEFAULT_BUTTON_WIDTH);
        mBtnHeight = typedArray.getInteger(R.styleable.CustomToggleButton_btnHeight, DEFAULT_BUTTON_HEIGHT);
        mCircleRadius = typedArray.getInteger(R.styleable.CustomToggleButton_circleRadius, DEFAULT_CIRCLE_RADIUS);
        mLineWidth = typedArray.getInteger(R.styleable.CustomToggleButton_lineWidth, DEFAULT_LINE_WIDTH);
        typedArray.recycle();

        r = Resources.getSystem();
        setOnClickListener(this);
        mCircleX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBtnWidth - mCircleRadius, r.getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bToggleOn) {
            // draw line
            mLinePaint.setColor(mOnLineColor);
            mLinePaint.setAntiAlias(true);
            mLinePaint.setStyle(Paint.Style.FILL);
            mLinePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLineWidth, r.getDisplayMetrics()));
            canvas.drawRoundRect(0,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (mBtnHeight - mLineWidth) / 2, r.getDisplayMetrics()),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBtnWidth, r.getDisplayMetrics()),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (mBtnHeight + mLineWidth) / 2, r.getDisplayMetrics()),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLineWidth / 2, r.getDisplayMetrics()),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLineWidth / 2, r.getDisplayMetrics()),
                    mLinePaint);

            // draw circle
            mCirclePaint.setColor(mOnCircleColor);
            mCirclePaint.setAntiAlias(true);
            canvas.drawCircle(mCircleX,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBtnHeight / 2, r.getDisplayMetrics()),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mCircleRadius, r.getDisplayMetrics()),
                    mCirclePaint);
        } else {

            // draw line
            mLinePaint.setColor(mOffLineColor);
            mLinePaint.setAntiAlias(true);
            mLinePaint.setStyle(Paint.Style.FILL);
            mLinePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLineWidth, r.getDisplayMetrics()));
            canvas.drawRoundRect(0,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (mBtnHeight - mLineWidth) / 2, r.getDisplayMetrics()),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBtnWidth, r.getDisplayMetrics()),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (mBtnHeight + mLineWidth) / 2, r.getDisplayMetrics()),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLineWidth / 2, r.getDisplayMetrics()),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLineWidth / 2, r.getDisplayMetrics()),
                    mLinePaint);

            // draw circle
            mCirclePaint.setColor(mOffCircleColor);
            mCirclePaint.setAntiAlias(true);
            canvas.drawCircle(mCircleX,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBtnHeight / 2, r.getDisplayMetrics()),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mCircleRadius, r.getDisplayMetrics()),
                    mCirclePaint);

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST) {
            widthSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBtnWidth, r.getDisplayMetrics());
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }
        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
            heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBtnHeight, r.getDisplayMetrics());
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onClick(View v) {
        if (bChangeComplete) {

            bToggleOn = !bToggleOn;

            bChangeComplete = false;
            // make the circle to slid along the line by changing the circleX
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (bToggleOn) {
                        mCircleX++;
                    } else {
                        mCircleX--;
                    }

                    mHandler.sendEmptyMessage(sMSG_TOGGLE_STATUS_CHANGE);
                    if (mCircleX == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBtnWidth - mCircleRadius, r.getDisplayMetrics()) // toggleOn
                            || mCircleX == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mCircleRadius, r.getDisplayMetrics())) { // toggleOff
                        bChangeComplete = true;
                        mTimer.cancel();
                    }
                }
            };
            // period is 2 milliseconds
            mTimer = new Timer();
            mTimer.schedule(task, 0, 2);

            if (mListener != null)
                mListener.onToggle(bToggleOn);
        }
    }

    public interface OnToggleChanged {
        void onToggle(boolean on);
    }

    private static class ToggleChangeHandler extends Handler {
        private WeakReference<CustomToggleButton> mWeakRef;

        public ToggleChangeHandler(CustomToggleButton customToggleButton) {
            mWeakRef = new WeakReference<CustomToggleButton>(customToggleButton);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == sMSG_TOGGLE_STATUS_CHANGE) {
                if (mWeakRef.get() != null)
                    mWeakRef.get().invalidate();
            }
        }
    }

}
