package com.skydrm.rmc.utils.commonUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.utils.cache.LRULimitedMemoryCache;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;
import static com.skydrm.rmc.ExecutorPools.Select_Type.REGULAR_BACK_GROUND;

/**
 * Created by hhu on 4/13/2017.
 */

public class ImageLoader {
    public static final String TAG = "ImageLoader";
    private static final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
    private static ImageLoader mDefault;
    private Handler mUIhandler;
    private Handler mThreadHandler;

    //this is used to avoid mThreadHandler initialize failed.
    private volatile Semaphore mSemaphore = new Semaphore(0);

    private LRULimitedMemoryCache mLruCache;
    //task container
    private static LinkedList<Runnable> mTasks;

    public static ImageLoader getDefault() {
        if (mDefault == null) {
            synchronized (ImageLoader.class) {
                if (mDefault == null) {
                    mDefault = new ImageLoader();
                }
            }
        }
        return mDefault;
    }

    private ImageLoader() {
        Thread mPoolthread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mThreadHandler = new ThreadHandler();
                mSemaphore.release();
                Looper.loop();
            }
        });
        mPoolthread.start();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 16;
        if (DEBUG) {
            Log.e(TAG, "ImageLoader: " + cacheSize / 1024 / 1024);
        }
        mLruCache = new LRULimitedMemoryCache(cacheSize);
        mTasks = new LinkedList<>();
    }

    public void loadImage(final String path, final ImageView imageView) {
        imageView.setTag(path);
        //init ui handler
        mUIhandler = new UIHandler(path);
        Bitmap cache = getBitmapFromLruCache(path);
        if (cache != null) {
            ImageHolder holder = new ImageHolder();
            holder.bitmap = cache;
            holder.imageView = imageView;
            holder.path = path;
            Message message = Message.obtain();
            message.obj = holder;
            mUIhandler.sendMessage(message);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    ImageSize imageSize = getImageViewWidth(imageView);
                    int reqWidth = imageSize.width;
                    int reqHeight = imageSize.height;
                    Bitmap cache = decodeSampledBitmapFromResource(path, reqWidth, reqHeight);
                    addBitmapToLruCache(path, cache);
                    ImageHolder holder = new ImageHolder();
                    holder.path = path;
                    holder.imageView = imageView;
                    holder.bitmap = getBitmapFromLruCache(path);
                    Message message = Message.obtain();
                    message.obj = holder;
                    mUIhandler.sendMessage(message);
                }
            });
        }
    }

    private Bitmap decodeSampledBitmapFromResource(String pathName,
                                                   int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (width > reqWidth && height > reqHeight) {
            int widthRatio = Math.round((float) width / (float) reqWidth);
            int heightRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.max(widthRatio, heightRatio);
        }
        return inSampleSize;
    }

    private ImageSize getImageViewWidth(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        final DisplayMetrics displayMetrics = imageView.getContext()
                .getResources().getDisplayMetrics();
        final ViewGroup.LayoutParams params = imageView.getLayoutParams();

        int width = params.width == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getWidth(); // Get actual image width
        if (width <= 0)
            width = params.width; // Get layout width parameter
        if (width <= 0)
            width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check
        // maxWidth parameter
        if (width <= 0)
            width = displayMetrics.widthPixels;
        int height = params.height == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getHeight(); // Get actual image height
        if (height <= 0)
            height = params.height; // Get layout height parameter
        if (height <= 0)
            height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check
        // maxHeight parameter
        if (height <= 0)
            height = displayMetrics.heightPixels;
        imageSize.width = width;
        imageSize.height = height;
        return imageSize;

    }

    private void addBitmapToLruCache(String key, Bitmap value) {
        if (getBitmapFromLruCache(key) == null) {
            if (value != null) {
                mLruCache.put(key, value);
            }
        }
    }

    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    private synchronized void addTask(Runnable task) {
        if (mThreadHandler == null) {
            try {
                mSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mTasks.add(task);
        mThreadHandler.sendEmptyMessage(0x110);
    }

    private synchronized static Runnable getTask() {
        return mTasks.removeLast();
    }

    private int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private class ImageHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

    private class ImageSize {
        int width;
        int height;
    }

    private static class ThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(getTask());
        }
    }

    private static class UIHandler extends Handler {
        private String path;

        UIHandler(String path) {
            this.path = path;
        }

        @Override
        public void handleMessage(Message msg) {
            ImageHolder holder = (ImageHolder) msg.obj;
            ImageView imageView = holder.imageView;
            Bitmap bitmap = holder.bitmap;
            int degree = AvatarUtil.getInstance().readPictureDegree(path);
            bitmap = AvatarUtil.getInstance().rotateBitmap(bitmap, degree);
            String path = holder.path;
            if (imageView.getTag().equals(path)) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
