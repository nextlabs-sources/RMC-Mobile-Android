package com.skydrm.rmc.utils.cache;

import android.graphics.Bitmap;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by hhu on 3/14/2017.
 */

public class LRULimitedMemoryCache extends LimitedMemoryCache<String, Bitmap> {
    private static final int INITIAL_CAPACITY = 10;
    private static final float LOAD_FACTOR = 0.75f;
    /**
     * Cache providing Least-Recently-Used logic
     */
    private final Map<String, Bitmap> lruCache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(INITIAL_CAPACITY, LOAD_FACTOR, true));

    public LRULimitedMemoryCache(int maxSize) {
        super(maxSize);
    }

    @Override
    public Reference<Bitmap> createReference(Bitmap vaule) {
        return new WeakReference<>(vaule);
    }

    @Override
    protected int getSize(Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    public boolean put(String key, Bitmap vaule) {
        if (super.put(key, vaule)) {
            lruCache.put(key, vaule);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void remove(String key) {
        lruCache.remove(key);
        super.remove(key);
    }

    @Override
    public void clear() {
        lruCache.clear();
        super.clear();
    }

    @Override
    protected Bitmap removeNext() {
        Bitmap mostLongUsedValue = null;
        synchronized (lruCache) {
            Iterator<Map.Entry<String, Bitmap>> it = lruCache.entrySet().iterator();
            if (it.hasNext()) {
                Map.Entry<String, Bitmap> entry = it.next();
                mostLongUsedValue = entry.getValue();
                it.remove();
            }
        }
        return mostLongUsedValue;
    }
}
