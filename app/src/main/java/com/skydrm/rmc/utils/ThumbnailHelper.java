package com.skydrm.rmc.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.skydrm.rmc.DevLog;

/**
 * Created by hhu on 4/26/2018.
 */

public class ThumbnailHelper {
    private static DevLog log = new DevLog(ThumbnailHelper.class.getSimpleName());

    public static Bitmap generatePreviewThumbnail(final View v, final ImageView subview) {
        if (null == v) {
            return null;
        }
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
//        v.measure(View.MeasureSpec.makeMeasureSpec(v.getWidth(),
//                View.MeasureSpec.AT_MOST), 0);
        v.layout((int) v.getX(), (int) v.getY(),
                (int) v.getX() + v.getMeasuredWidth(),
                (int) v.getY() + v.getMeasuredHeight());
        v.post(new Runnable() {
            @Override
            public void run() {
                log.e("v.getMeasuredWidth" + v.getMeasuredWidth());
                log.e("v.getWidth" + v.getWidth());
                log.e("v.getMeasuredHeight" + v.getMeasuredHeight());
                log.e("v.getHeight" + v.getHeight());
                if (v.getMeasuredWidth() != 0 && v.getMeasuredHeight() != 0) {
                    Bitmap b = Bitmap.createBitmap(v.getDrawingCache(), 0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
                    Canvas canvas = new Canvas(b);
                    v.draw(canvas);
                    v.setDrawingCacheEnabled(false);
                    v.destroyDrawingCache();

                    subview.setImageBitmap(b);
                }
            }
        });

        return null;
    }

    static int measuredWidth;
    static int measuredHeight;

    public static Bitmap generateWebViewThumbnail(final WebView webView) {
        webView.measure(View.MeasureSpec.makeMeasureSpec(webView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(webView.getHeight(), View.MeasureSpec.AT_MOST));

        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();

        float scale = webView.getScale();

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.measure(View.MeasureSpec.makeMeasureSpec(webView.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(webView.getHeight(), View.MeasureSpec.AT_MOST));
                measuredWidth = webView.getMeasuredWidth();
                measuredHeight = webView.getMeasuredHeight();
                log.d("measuredWidth:" + measuredWidth);
                log.d("measuredHeight:" + measuredHeight);
            }
        });
        log.d("mw:" + webView.getMeasuredWidth());
        log.d("mh:" + webView.getMeasuredHeight());
        int contentHeight = (int) (webView.getContentHeight() * scale);
        if (webView.getWidth() > 0 && contentHeight > 0) {
            Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(),
                    webView.getHeight(), Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            webView.draw(canvas);
            webView.setDrawingCacheEnabled(false);
            webView.destroyDrawingCache();
            return bitmap;
        }
        return null;
    }

    public static Bitmap captureWebView(WebView webView) {
        webView.measure(View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(webView.getMeasuredWidth(),
                (int) (webView.getContentHeight() * webView.getScale()), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        int iHeight = bitmap.getHeight();
        canvas.drawBitmap(bitmap, 0, iHeight, paint);
        webView.draw(canvas);
        webView.setDrawingCacheEnabled(false);
        webView.destroyDrawingCache();
        return bitmap;
    }

    public static Bitmap captureSurfaceView(SurfaceView view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(
                0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        if (view.getMeasuredHeight() > 0 && view.getMeasuredWidth() > 0) {
            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                    view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            int iHeight = bitmap.getHeight();
            canvas.drawBitmap(bitmap, 0, iHeight, paint);
            view.draw(canvas);
            view.setDrawingCacheEnabled(false);
            view.destroyDrawingCache();
            return bitmap;
        }
        return null;
    }

    public interface IThumbnailLoadCallback {
        void onThumbnailLoad(Bitmap cache);
    }
}
