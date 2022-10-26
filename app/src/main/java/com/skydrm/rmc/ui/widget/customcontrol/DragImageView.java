package com.skydrm.rmc.ui.widget.customcontrol;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.skydrm.rmc.ui.activity.ViewActivity;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;

/**
 * Created by aning on 3/8/2017.
 * -- Control the "Arrow" imageView can move along X or Y axis.
 */

public class DragImageView extends ImageView {

    private Context mContext;
    private int startX = 0;
    private int startY = 0;

    private int mTop;
    private int mLeft;

    private int mScreenWidth;
    private int mScreenHeight;

    private int mViewWidth;
    private int mViewHeight;

    private boolean bFirstClick = false;
    private int mFirstClickY;
    private int mOffFirstOffY;

    // judge the control if is moving along Y axis.
    private boolean bIsSliding = false;
    // judge the control if has been moved along Y axis.
    private boolean bIsSlided = false;

    public DragImageView(Context context) {
        super(context);
        mContext = context;
        mScreenWidth = CommonUtils.getScreenWidth((Activity) mContext);
        mScreenHeight = CommonUtils.getScreenHeight((Activity) mContext);
    }

    public DragImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mScreenWidth = CommonUtils.getScreenWidth((Activity) mContext);
        mScreenHeight = CommonUtils.getScreenHeight((Activity) mContext);
    }

    // this control is moving
    public boolean isbIsSliding() {
        return bIsSliding;
    }

    public void setbIsSliding(boolean isSliding) {
        this.bIsSliding = isSliding;
    }

    public void setbFirstClick(boolean isFirstClick) {
        this.bFirstClick = isFirstClick;
    }

    public boolean isbIsSlided() {
        return bIsSlided;
    }

    public void setbIsSlided(boolean isSlided) {
        this.bIsSlided = isSlided;
    }

    public int getmOffFirstOffY() {
        return mOffFirstOffY;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mViewWidth = right - left;
        mViewHeight = bottom - top;
        Log.d("===", "onLayout");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (((ViewActivity) mContext).isbIsRotateScreen()) {
            mScreenWidth = CommonUtils.getScreenWidth((ViewActivity) mContext);
            mScreenHeight = CommonUtils.getScreenHeight((Activity) mContext);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // init start location
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                mTop = getTop();
                mLeft = getLeft();

                if (!bFirstClick) {
                    // 其实mFirstClickY的只应该是固定的，可以通过 mScreenHeight - mArrow.marginBottom - mArrow.getHeight / 2 大致获取！！
                    mFirstClickY = startY;
                    bFirstClick = true;
                }

                bIsSliding = false;
                Log.d("===", "ACTION_DOWN");
                Log.d("###", "ACTION_DOWN------startX:" + Integer.toString(startX));
                Log.d("###", "ACTION_DOWN------startY:" + Integer.toString(startY));

                break;
            case MotionEvent.ACTION_MOVE:
                // must remove rule, or else can't move
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
                setLayoutParams(layoutParams);

                Log.d("===", "ACTION_MOVE");
                //手势移动的dX和dY为控件的marginLeft和marginTop
                int moveX = (int) event.getRawX();
                int moveY = (int) event.getRawY();
                int dX = moveX - startX;
                int dY = moveY - startY;
                setMargin(dX, dY);

                // slide along Y axis
                if (!bIsSliding && Math.abs(dY) > 20) {
                    bIsSliding = true;
                    Log.d("===", "bIsSlide is true!!!");
                }
                Log.e("===", "onTouchEvent: " + dY);
                if (Math.abs(dY) > 20) {
                    bIsSlided = true;
                }

                break;
            case MotionEvent.ACTION_UP:
                Log.d("===", "ACTION_UP");
                // reset
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                mOffFirstOffY = startY - mFirstClickY;
                mTop = getTop();
                mLeft = getLeft();

                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * set control marginTop and marginLeft dynamically.
     */
    private void setMargin(int dX, int dY) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        int top = mTop + dY;
        int left = mLeft + dX;
        int l = mScreenWidth - mViewWidth;

        int test1 = CommonUtils.getStatusBarHeight(mContext); // 88,  mViewHeight -- 112

        //   int t = mScreenHeight - mViewHeight - CommonUtils.getStatusBarHeight(mContext);
        int marginBottom_px = DensityHelper.dip2px(mContext, 16); //    android:layout_marginBottom="16dp"  -- layout init.
        int t = mScreenHeight - mViewHeight - CommonUtils.getStatusBarHeight(mContext) - marginBottom_px;

        //设置left和top的边界值
        if (left < 0) {
            left = 0;
        } else if (left > l) {
            left = l;
        }

//        if (top < 0) {
//            top = 0;
//        } else if (top > t) {
//            top = t;
//        }

        int titleHeight_px = DensityHelper.dip2px(mContext, 60);  // android:layout_height="60dp"  -- layout init.
        if (top < titleHeight_px) {
            top = titleHeight_px;
        } else if (top > t) {
            top = t;
        }

        Log.d("===", "DX: ----------   " + Integer.toString(dX));
        Log.d("===", "DY: ---------    " + Integer.toString(dY));
        Log.d("===", "setMargin");

        layoutParams.topMargin = top;
//        layoutParams.leftMargin = left;
        layoutParams.leftMargin = getLeft(); // forbid move along X axis.
        setLayoutParams(layoutParams);
    }

}
