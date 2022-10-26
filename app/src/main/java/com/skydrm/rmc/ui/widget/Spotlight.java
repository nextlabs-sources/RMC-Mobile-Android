package com.skydrm.rmc.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;

import com.skydrm.rmc.R;


/**
 * Created by hhu on 10/25/2016.
 */

public class Spotlight extends View {
    private static final int SPOT_HEIGHT = 100;
    public static final String TAG = "Spotlight";
    private int mSpotlightColor;
    private float mAlphaColor;
    private int mReacWidth;
    private int mReacHeight;
    private float mDensity;
    private float mDownX;
    private float mDownY;
    private Bitmap backBitmap;
    private RectF rectF;
    private RectF rectF2;
    private Animation mAlphaAnimation;
    private Activity activity;

    public Spotlight(Context context) {
        this(context, null);
    }

    public Spotlight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Spotlight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setContext(context);
        init(context);
    }

    private void init(Context context) {
        mDensity = context.getResources().getDisplayMetrics().density;//4 SAMSUNG S6
        backBitmap = decodeSampledBitmapFromResource(context.getResources(), R.drawable.spot, getScreenWidth(), getScreenHeight());
        mReacWidth = getScreenWidth();
        mReacHeight = getSpotlightHeight();
    }

    public void setContext(Context context) {
        if (context == null) {
            return;
        }
        activity = (Activity) context;
    }

    private int dp(int dp) {
        return (int) (dp * mDensity + 0.5f);
    }

    private ClickArea mClickArea;
    private boolean mAnimationIsCancel;
    private boolean mIsAnimating = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean superResult = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && this.isEnabled()) {
            mDownX = event.getX();
            mDownY = event.getY();
            if (mDownY <= dp(SPOT_HEIGHT)) {
                mClickArea = ClickArea.TOP;
            } else if (mDownY >= getSpotlightHeight() - dp(SPOT_HEIGHT)) {
                mClickArea = ClickArea.END;
            } else {
                mClickArea = ClickArea.MIDDLE;
            }

//            mAlphaAnimation = new AlphaAnimation(0.1f, 1.0f);
//            mAlphaAnimation.setDuration(400);
//            startAnimation(mAlphaAnimation);
            BitmapDrawable mBitmapDrawable = new BitmapDrawable(tailorSpotlight(backBitmap, (int) mDownY, dp(SPOT_HEIGHT), mClickArea));
            setBackground(mBitmapDrawable);
            if (!superResult) {
                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE && this.isEnabled()) {
            mDownX = event.getX();
            mDownY = event.getY();
            if (mDownY <= dp(SPOT_HEIGHT)) {
                mClickArea = ClickArea.TOP;
            } else if (mDownY >= getSpotlightHeight() - dp(SPOT_HEIGHT)) {
                mClickArea = ClickArea.END;
            } else {
                mClickArea = ClickArea.MIDDLE;
            }
            BitmapDrawable mBitmapDrawable = new BitmapDrawable(tailorSpotlight(backBitmap, (int) mDownY, dp(SPOT_HEIGHT), mClickArea));
            setBackground(mBitmapDrawable);
            invalidate();
            if (!superResult) {
                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            setBackgroundResource(R.drawable.spot);
        }
        setBackgroundResource(R.drawable.spot);
        return superResult;
    }

    /**
     * @param bitmap
     * @param mDownY
     * @param pixels
     * @param mClickArea
     */
    public Bitmap tailorSpotlight(Bitmap bitmap, int mDownY, int pixels, ClickArea mClickArea) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.e(TAG, "tailorSpotlight: " + width + "--" + height);
        Bitmap creBitmap = Bitmap.createBitmap(mReacWidth, mReacHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(creBitmap);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#028103"));
        paint.setAntiAlias(true);

        switch (mClickArea) {
            case TOP:
                rectF = new RectF(0, pixels, mReacWidth,
                        mReacHeight);
                canvas.drawRect(rectF, paint);
                break;
            case MIDDLE:
                rectF = new RectF(0, 0, mReacWidth, mDownY - pixels / 2);
                rectF2 = new RectF(0, mDownY + pixels / 2, mReacWidth, mReacHeight);
                canvas.drawRect(rectF, paint);
                canvas.drawRect(rectF2, paint);
                break;
            case END:
                rectF = new RectF(0, 0, mReacWidth, mReacHeight - pixels);
                canvas.drawRect(rectF, paint);
                break;
        }
        canvas.drawARGB(0, 0, 0, 0);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return creBitmap;
    }

    public int getSpotlightHeight() {
        View view = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        int contentHeight = view.getHeight();
        if (contentHeight != 0) {
            return contentHeight;
        } else {
            return getScreenHeight() - getStatusBarHeight();
        }
    }

    public int getScreenHeight() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusBarHeight = getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    private enum ClickArea {
        TOP, MIDDLE, END;
    }
}
