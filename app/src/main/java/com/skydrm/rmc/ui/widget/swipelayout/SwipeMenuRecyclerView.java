package com.skydrm.rmc.ui.widget.swipelayout;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.skydrm.rmc.SkyDRMApp;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 11/10/2016.
 */

public class SwipeMenuRecyclerView extends RecyclerView {

    public static final String TAG = "SwipeMenuRecyclerView";
    /**
     * right menu
     */
    public static final int RIGHT_DIRECTION = -1;
    /**
     * left menu
     */
    public static final int LEFT_DIRECTION = 1;

    /**
     * Invalid position.
     */
    private static final int INVALID_POSITION = -1;
    private ViewConfiguration mViewConfiguration;
    private boolean isInterceptTouchEvent = true;
    private int mDownX;
    private int mDownY;
    private int mOldTouchedPosition = INVALID_POSITION;
    private SwipeMenuLayout mOldSwipedLayout;

    @IntDef({LEFT_DIRECTION, RIGHT_DIRECTION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DirectionMode {
    }

    public SwipeMenuRecyclerView(Context context) {
        this(context, null);
    }

    public SwipeMenuRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mViewConfiguration = ViewConfiguration.get(context);
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        boolean isIntercepted = super.onInterceptTouchEvent(e);
        if (!isInterceptTouchEvent) {
            return isIntercepted;
        } else {
            if (e.getPointerCount() > 1) return true;
            int action = e.getAction();
            int x = (int) e.getX();
            int y = (int) e.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = x;
                    mDownY = y;
                    isIntercepted = false;
                    int touchingPosition = getChildAdapterPosition(findChildViewUnder(x, y));
                    if (touchingPosition != mOldTouchedPosition && mOldSwipedLayout != null && mOldSwipedLayout.isMenuOpen()) {
                        mOldSwipedLayout.smoothCloseMenu();
                        isIntercepted = true;
                    }
                    if (isIntercepted) {
                        mOldSwipedLayout = null;
                        mOldTouchedPosition = INVALID_POSITION;
                    } else {
                        ViewHolder vh = findViewHolderForAdapterPosition(touchingPosition);
                        if (vh != null) {
                            View itemView = getSwipeMenuView(vh.itemView);
                            if (itemView != null && itemView instanceof SwipeMenuLayout) {
                                mOldSwipedLayout = (SwipeMenuLayout) itemView;
                                mOldTouchedPosition = touchingPosition;
                            }
                        }
                    }
                    break;
                // They are sensitive to retain sliding and inertia.
                case MotionEvent.ACTION_MOVE:
                    isIntercepted = handleUnDown(x, y, isIntercepted);
                    ViewParent viewParent = getParent();
                    if (viewParent != null) {
                        viewParent.requestDisallowInterceptTouchEvent(!isIntercepted);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isIntercepted = handleUnDown(x, y, isIntercepted);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    isIntercepted = handleUnDown(x, y, isIntercepted);
                    break;
            }
        }
        return isIntercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) e.getX();
                int y = (int) e.getY();
                int touchingPosition = getChildAdapterPosition(findChildViewUnder(x, y));
                ViewHolder vh = findViewHolderForAdapterPosition(touchingPosition);
                if (vh != null) {
                    View itemView = getSwipeMenuView(vh.itemView);
                    if (itemView != null && itemView instanceof SwipeMenuLayout) {
                        mOldSwipedLayout = (SwipeMenuLayout) itemView;
                        mOldTouchedPosition = touchingPosition;
                    }
                }
                if (mOldSwipedLayout != null && mOldSwipedLayout.isMenuOpen()) {
                    mOldSwipedLayout.smoothCloseMenu();
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mOldSwipedLayout != null && mOldSwipedLayout.isMenuOpen()) {
                    mOldSwipedLayout.smoothCloseMenu();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onTouchEvent(e);
    }

    private View getSwipeMenuView(View itemView) {
        if (itemView instanceof SwipeMenuLayout) {
            return itemView;
        }
        List<View> unvisitedViews = new ArrayList<>();
        unvisitedViews.add(itemView);
        while (!unvisitedViews.isEmpty()) {
            View child = unvisitedViews.remove(0);
            if (!(child instanceof ViewGroup)) {
                continue;
            }
            if (child instanceof SwipeMenuLayout) {
                return child;
            }
            ViewGroup viewGroup = (ViewGroup) child;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                unvisitedViews.add(viewGroup.getChildAt(i));
            }
        }
        return itemView;
    }

    private boolean handleUnDown(int x, int y, boolean defaultValue) {
        int distanceX = mDownX - x;
        int distanceY = mDownY - y;
        //swipe
        if (Math.abs(distanceX) > mViewConfiguration.getScaledTouchSlop()
                && Math.abs(distanceX) > Math.abs(distanceY)) {
            defaultValue = false;
        }
        //click
        if (Math.abs(distanceY) < mViewConfiguration.getScaledTouchSlop()
                && Math.abs(distanceX) < mViewConfiguration.getScaledTouchSlop()) {
            defaultValue = false;
        }
        return defaultValue;
    }

    /**
     * open menu.
     *
     * @param position  position.
     * @param direction use {@link #LEFT_DIRECTION}, {@link #RIGHT_DIRECTION}.
     * @param duration  time millis.
     */
    public void smoothOpenMenu(int position, @DirectionMode int direction, int duration) {
        if (mOldSwipedLayout != null) {
            if (mOldSwipedLayout.isMenuOpen()) {
                mOldSwipedLayout.smoothCloseMenu();
            }
        }
        ViewHolder vh = findViewHolderForAdapterPosition(position);
        if (vh != null) {
            View itemView = getSwipeMenuView(vh.itemView);
            if (itemView != null && itemView instanceof SwipeMenuLayout) {
                mOldSwipedLayout = (SwipeMenuLayout) itemView;
                if (direction == RIGHT_DIRECTION) {
                    mOldTouchedPosition = position;
                    mOldSwipedLayout.smoothOpenRightMenu(duration);
                } else if (direction == LEFT_DIRECTION) {
                    mOldTouchedPosition = position;
                    mOldSwipedLayout.smoothOpenLeftMenu(duration);
                }
            }
        }
    }

    /**
     * Close menu.
     */
    public void smoothCloseMenu() {
        if (mOldSwipedLayout != null && mOldSwipedLayout.isMenuOpen()) {
            mOldSwipedLayout.smoothCloseMenu();
        }
    }

    public void configAnimator() {
        setItemAnimator(new DefaultItemAnimator());
        RecyclerView.ItemAnimator animator = getItemAnimator();
        animator.setAddDuration(0);
        animator.setChangeDuration(0);
        animator.setMoveDuration(0);
        animator.setRemoveDuration(0);
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }
}
