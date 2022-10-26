package com.skydrm.rmc.ui.widget.customcontrol;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

/**
 * Created by jrzhou on 2/8/2017.
 */

public class AlexaFilesSwipeLayout extends RelativeLayout implements Animator.AnimatorListener {

    public AlexaFilesSwipeLayout(Context context) {
        this(context, null);
    }

    public AlexaFilesSwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlexaFilesSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public static enum SwipeState {
        CLOSE, OPEN, DRAGGING;
    }

    public static interface OnSwipeStateChangeListener {
        void onStateChange(AlexaFilesSwipeLayout view, SwipeState newState);

        Boolean onDown(AlexaFilesSwipeLayout view);
    }

    public static interface OnSwipeTouchListener {
        void onSwipeTouch(AlexaFilesSwipeLayout view);
    }

    public OnSwipeTouchListener mOnSwipeTouchListener;

    public void setOnSwipeTouchListener(OnSwipeTouchListener mOnSwipeTouchListener) {
        this.mOnSwipeTouchListener = mOnSwipeTouchListener;
    }

    protected ViewDragHelper viewDragHelper;
    protected View leftView;
    protected View centerView;
    protected View rightView;
    protected boolean ifCanRightSlip;

    public void setIfCanRightSlip(boolean ifCanRightSlip) {
        this.ifCanRightSlip = ifCanRightSlip;
    }

    protected GestureDetector gestureDetector;

    protected OnSwipeStateChangeListener mOnSwipeStateChangeListener;

    public void setOnSwipeStateChangeListener(OnSwipeStateChangeListener mOnSwipeStateChangeListener) {
        this.mOnSwipeStateChangeListener = mOnSwipeStateChangeListener;
    }

    float cirticalVel;

    private void init() {
        viewDragHelper = ViewDragHelper.create(this, 1.0f, callback);
        gestureDetector = new GestureDetector(getContext(), gestureListener);
        cirticalVel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        initAnimator();
    }

    protected Boolean outProcess;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View leftView = getChildAt(0);
        View rightView = getChildAt(2);
        measureChild(leftView, widthMeasureSpec, heightMeasureSpec);
        measureChild(rightView, widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        View leftView = getChildAt(0);
        View rightView = getChildAt(2);
        leftView.layout(-leftView.getMeasuredWidth(), leftView.getTop(), 0, leftView.getMeasuredHeight());
        rightView.layout(centerView.getMeasuredWidth(), centerView.getTop(), rightView.getMeasuredWidth() + centerView.getMeasuredWidth(), centerView.getMeasuredHeight());
    }

    private boolean forbidListerViewScroll = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mOnSwipeStateChangeListener != null) {
                outProcess = mOnSwipeStateChangeListener.onDown(AlexaFilesSwipeLayout.this);
                Log.d("onInterceptTouchEvent", "" + outProcess);
            }
        }
        if (outProcess != null && outProcess) {
            return true;
        } else {
            if (outProcess == null) {
                forbidListerViewScroll = true;
                requestDisallowInterceptTouchEvent(true);
            } else {
                forbidListerViewScroll = false;
            }
            return viewDragHelper.shouldInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            Log.d("AlexaFilesSwipeLayout", "MotionEvent.ACTION_UP");
//            if (mOnSwipeStateChangeListener != null) {
//                mOnSwipeStateChangeListener.onUp(AlexaFilesSwipeLayout.this);
//            }
//        }
        if (outProcess != null && outProcess) {
            requestDisallowInterceptTouchEvent(true);
            return true;
        }
        viewDragHelper.processTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return pointerId == 0;
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == leftView) {
//                Log.d("onInterceptTouchEvent", "leftWidth:= " + leftView.getWidth());
                if (left > 0) {
                    left = 0;
                } else if (left < -leftView.getWidth()) {
                    left = -leftView.getWidth();
                }
            }
            if (child == centerView) {
                if (left > leftView.getWidth()) {
                    left = leftView.getWidth();
                } else if (left < 0) {
                    left = 0;
                }
            }
            if (child == rightView) {
                if (left > centerView.getWidth()) {
                    left = centerView.getWidth();
                } else if (left < centerView.getWidth() - rightView.getWidth()) {
                    left = centerView.getWidth() - rightView.getWidth();
                }
            }
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return child.getTop();
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView == centerView) {
                leftView.offsetLeftAndRight(dx);
                rightView.offsetLeftAndRight(dx);
            }
            if (changedView == leftView) {
                centerView.offsetLeftAndRight(dx);
                rightView.offsetLeftAndRight(dx);
            }
            if (changedView == rightView) {
                leftView.offsetLeftAndRight(dx);
                centerView.offsetLeftAndRight(dx);
            }
            updateState(SwipeState.DRAGGING);
        }


        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (xvel > cirticalVel) {
                open();
            } else if (xvel < -cirticalVel) {
                close();
            } else {
                if (centerView.getLeft() > leftView.getWidth() / 3) {
                    open();
                } else if (centerView.getLeft() < leftView.getWidth() / 2) {
                    close();
                }
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (state == ViewDragHelper.STATE_IDLE) {
                if (centerView.getLeft() == 0) {
                    updateState(SwipeState.CLOSE);
                } else if (centerView.getLeft() == leftView.getWidth()) {
                    updateState(SwipeState.OPEN);
                }
            }
        }
    };

    protected SwipeState currentState = SwipeState.CLOSE;

    protected void updateState(SwipeState newState) {
        if (currentState == newState) {
            return;
        }
        currentState = newState;
        if (mOnSwipeStateChangeListener != null) {
            mOnSwipeStateChangeListener.onStateChange(this, newState);
        }
    }

    private android.view.GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                AlexaFilesSwipeLayout.this.requestDisallowInterceptTouchEvent(true);
            }
//            requestDisallowInterceptTouchEvent(true);
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };

    protected AnimatorSet animatorSet;
    protected ValueAnimator leftViewAnimator;
    protected ValueAnimator centerViewAnimator;
    protected ValueAnimator rightViewAnimator;
    protected IntEvaluator evaluator = new IntEvaluator();

    protected void initAnimator() {
        if (animatorSet == null) {
            animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(new LinearInterpolator());
            animatorSet.setDuration(130);
            animatorSet.addListener(this);
        }
    }

    public void close() {
//        if(viewDragHelper.smoothSlideViewTo(centerView,0,0)){
//            ViewCompat.postInvalidateOnAnimation(this);
//        }
        animatorSet.cancel();
        if (leftViewAnimator != null && centerViewAnimator != null) {
            leftViewAnimator.removeAllUpdateListeners();
            centerViewAnimator.removeAllUpdateListeners();
        }
        leftViewAnimator = ValueAnimator.ofObject(evaluator, leftView.getLeft(), -leftView.getWidth());
        leftViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int left = (int) animation.getAnimatedValue();
                int right = (int) animation.getAnimatedValue() + leftView.getWidth();
                leftView.setLeft(left);
                leftView.setRight(right);
            }
        });

        centerViewAnimator = ValueAnimator.ofObject(evaluator, centerView.getLeft(), 0);
        centerViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int left = (int) animation.getAnimatedValue();
                int right = (int) animation.getAnimatedValue() + centerView.getWidth();
                centerView.setLeft(left);
                centerView.setRight(right);
            }
        });

        animatorSet.playTogether(leftViewAnimator, centerViewAnimator);
        animatorSet.start();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void open() {
//        if(viewDragHelper.smoothSlideViewTo(centerView,leftView.getWidth(),0)){
//            ViewCompat.postInvalidateOnAnimation(this);
//        }
//        ValueAnimator leftViewAnimator = ValueAnimator.ofFloat(leftView.getLeft(), leftView.getWidth());
//        ValueAnimator centerViewAnimator = ValueAnimator.ofFloat(centerView.getLeft(), leftView.getWidth());
        animatorSet.cancel();
        if (leftViewAnimator != null && centerViewAnimator != null) {
            leftViewAnimator.removeAllUpdateListeners();
            centerViewAnimator.removeAllUpdateListeners();
        }
        leftViewAnimator = ValueAnimator.ofObject(evaluator, leftView.getLeft(), 0);
        leftViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int left = (int) animation.getAnimatedValue();
                int right = (int) animation.getAnimatedValue() + leftView.getWidth();
                Log.d("open", "left:+ " + left);
                leftView.setLeft(left);
                leftView.setRight(right);
            }
        });

        centerViewAnimator = ValueAnimator.ofObject(evaluator, centerView.getLeft(), leftView.getWidth());
        centerViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int left = (int) animation.getAnimatedValue();
                int right = (int) animation.getAnimatedValue() + centerView.getWidth();
                centerView.setLeft(left);
                centerView.setRight(right);
            }
        });

        animatorSet.playTogether(leftViewAnimator, centerViewAnimator);
        animatorSet.start();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void openLeft() {
//        if(viewDragHelper.smoothSlideViewTo(centerView,leftView.getWidth(),0)){
//            ViewCompat.postInvalidateOnAnimation(this);
//        }
//        ValueAnimator leftViewAnimator = ValueAnimator.ofFloat(leftView.getLeft(), leftView.getWidth());
//        ValueAnimator centerViewAnimator = ValueAnimator.ofFloat(centerView.getLeft(), leftView.getWidth());
        animatorSet.cancel();
        if (leftViewAnimator != null && centerViewAnimator != null) {
            leftViewAnimator.removeAllUpdateListeners();
            centerViewAnimator.removeAllUpdateListeners();
        }
        leftViewAnimator = ValueAnimator.ofObject(evaluator, leftView.getLeft(), 0);
        leftViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int left = (int) animation.getAnimatedValue();
                int right = (int) animation.getAnimatedValue() + leftView.getWidth();
                Log.d("open", "left:+ " + left);
                leftView.setLeft(left);
                leftView.setRight(right);
            }
        });

        centerViewAnimator = ValueAnimator.ofObject(evaluator, centerView.getLeft(), leftView.getWidth());
        centerViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int left = (int) animation.getAnimatedValue();
                int right = (int) animation.getAnimatedValue() + centerView.getWidth();
                centerView.setLeft(left);
                centerView.setRight(right);
            }
        });

        rightViewAnimator = ValueAnimator.ofObject(evaluator, centerView.getLeft(), leftView.getWidth());
        rightViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int left = (int) animation.getAnimatedValue();
                int right = (int) animation.getAnimatedValue() + rightView.getWidth();
                rightView.setLeft(left);
                rightView.setRight(right);
            }
        });

        animatorSet.playTogether(leftViewAnimator, centerViewAnimator);
        animatorSet.start();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (centerView.getLeft() == 0 && leftView.getLeft() == -leftView.getWidth() && rightView.getLeft() == centerView.getWidth()) {
            updateState(SwipeState.CLOSE);
            Log.d("onInterceptTouchEvent", "close");
        }
        if (centerView.getLeft() == leftView.getWidth() && leftView.getLeft() == 0) {
            updateState(SwipeState.OPEN);
            Log.d("onInterceptTouchEvent", "open");
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

//    @Override
//    public void computeScroll() {
//        super.computeScroll();
//        if(viewDragHelper.continueSettling(true)){
//            ViewCompat.postInvalidateOnAnimation(this);
//        }
//    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        leftView = getChildAt(0);
        centerView = getChildAt(1);
        rightView = getChildAt(2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlexaFilesSwipeLayout)) return false;

        AlexaFilesSwipeLayout that = (AlexaFilesSwipeLayout) o;

        if (viewDragHelper != null ? !viewDragHelper.equals(that.viewDragHelper) : that.viewDragHelper != null)
            return false;
        if (gestureDetector != null ? !gestureDetector.equals(that.gestureDetector) : that.gestureDetector != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = viewDragHelper != null ? viewDragHelper.hashCode() : 0;
        result = 31 * result + (gestureDetector != null ? gestureDetector.hashCode() : 0);
        result = 31 * result + (mOnSwipeStateChangeListener != null ? mOnSwipeStateChangeListener.hashCode() : 0);
        result = 31 * result + (gestureListener != null ? gestureListener.hashCode() : 0);
        return result;
    }
}
