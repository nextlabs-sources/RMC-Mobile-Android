package com.skydrm.rmc.ui.widget.customcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.skydrm.rmc.ui.activity.ViewActivity;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;

/**
 * Created by aning on 11/30/2016.
 */

public class DragViewGroup extends LinearLayout {
    private int lastX, lastY, screenHeight;
    private int originalX;

    // the max value that the layout can slide towards left.
    private int thresholdValue;

    private int mScreenWidth;
    private Context mContext;

    private int mViewWidth;
    // flag, used to label if shortcut is move,if yes, we should forbid trigger any button click event(include: offline, favorite, protect and share button)
    private boolean bIsSlide = false;

    public DragViewGroup(Context context) {
        this(context, null);
    }

    public DragViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mScreenWidth = CommonUtils.getScreenWidth((ViewActivity) mContext);
    }

    public boolean isbIsSlide() {
        return bIsSlide;
    }

    public void setbIsSlide(boolean isSlide) {
        this.bIsSlide = isSlide;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mViewWidth = r - l;

        if (((ViewActivity) mContext).isbIsRotateScreen()) {
            mScreenWidth = CommonUtils.getScreenWidth((ViewActivity) mContext);
        }
        // 设置往左滑动的阈值，当滑动至(mScreenWidth - mViewWidth)时，不能再往左滑动了。
        thresholdValue = mScreenWidth - mViewWidth;
    }

    // intercept the touch event of the layout.
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (((ViewActivity) mContext).isbIsRotateScreen()) {
            mScreenWidth = CommonUtils.getScreenWidth((ViewActivity) mContext);
        }

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) ev.getRawX();
                lastY = (int) ev.getRawY();
                // note: must record the original touch down X point ---- originalX
                originalX = lastX;

                bIsSlide = false;
                Log.d("===", "ACTION_DOWN");

                break;
            case MotionEvent.ACTION_MOVE:

                Log.d("===", "ACTION_MOVE");

                int dx = (int) ev.getRawX() - lastX;

//                if (!bIsSlide && dx != 0 ) { // 注意：不能通过dx != 0 去说明是否沿着x轴水平滑动，因为点击控件的时候也会出现x轴的小波动(特别在高灵敏的手机上)
                if (!bIsSlide && Math.abs(dx) > 20) {
                    bIsSlide = true;
                    Log.d("===", "bIsSlide is true!!!");
                }

                Log.d("===", "dx:  " + Integer.toString(dx));
                //  dx < 0 means sliding towards left
                if (dx < 0) {
                    // 说明已经移至最左端，不能再往左移动了。
                    if (this.getLeft() == mScreenWidth - mViewWidth) {
                        break;
                    }
                    // 往左移动
                    if (ev.getRawX() < originalX) {
                        break;
                    }
                }

                // 计算移动后的左边和右边的距离
                int left = this.getLeft() + dx;
                int right = this.getRight() + dx;

                // 即控件往右滑动至左边距到右边框的距离为100时，若继续滑动控件则会自动隐藏。
                if (left >= mScreenWidth - 100) {
                    //this.setVisibility(GONE);
                    this.setLeft(mScreenWidth); // for test!!!
                    this.setVisibility(INVISIBLE);
                    if (((ViewActivity) mContext).getArrow() != null) {
                        ((ViewActivity) mContext).getArrow().setVisibility(VISIBLE);
                    }
                }

                //  int top = this.getTop() + dy;
                //  int bottom = this.getBottom() + dy;

                if (left < 0) { // 控件即将滑出最左端(本项目不会出现这个情况)
                    left = 0;
                    right = left + this.getWidth();
                }

                if (right > 2 * mScreenWidth) {  // 滑动至右边距>2倍的mScreenWidth(本项目不会出现这个情况)
                    right = 2 * mScreenWidth;
                    left = right - this.getWidth();
                }

                // set the new location of the control
                this.layout(left, this.getTop(), right, this.getBottom());

                // again reset the lastX after layout
                lastX = (int) ev.getRawX();
                lastY = (int) ev.getRawY();

                break;
            case MotionEvent.ACTION_UP:

                Log.d("===", "ACTION_UP");

                // 在控件完全显示的情况下，只要往右滑动一点松手，则该控件自动隐藏
                if (this.getLeft() > thresholdValue && ev.getRawX() > originalX) {
                    //this.setVisibility(GONE);
                    this.setLeft(mScreenWidth); // for test!!!
                    this.setVisibility(INVISIBLE);
                    if (((ViewActivity) mContext).getArrow() != null) {
                        ((ViewActivity) mContext).getArrow().setVisibility(VISIBLE);
                    }
                }

                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

}
