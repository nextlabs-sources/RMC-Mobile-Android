package com.skydrm.rmc.utils.cache;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hhu on 3/14/2017.
 */

public abstract class LimitedMemoryCache<K, V> extends BaseMemoryCache<K, V> {
    private final int sizeLimit;
    private final AtomicInteger cacheSize;

    private final List<V> hardCache = Collections.synchronizedList(new LinkedList<V>());

    public LimitedMemoryCache(int sizeLimit) {
        this.sizeLimit = sizeLimit;
        cacheSize = new AtomicInteger();
    }

    @Override
    public boolean put(K key, V vaule) {
        boolean putSuccessful = false;
        //add vaule to hard cache
        int vauleSize = getSize(vaule);
        int sizeLimit = getSizeLimit();
        int currentCacheSize = cacheSize.get();
        if (vauleSize < sizeLimit) {
            while (currentCacheSize + vauleSize > sizeLimit) {
                V removeVaule = removeNext();
                if (hardCache.remove(removeVaule)) {
                    currentCacheSize = cacheSize.addAndGet(-getSize(removeVaule));
                }
            }
            hardCache.add(vaule);
            cacheSize.addAndGet(vauleSize);
            putSuccessful = true;
        }
        //add vaule to soft cache
        super.put(key, vaule);
        return putSuccessful;
    }

    @Override
    public void remove(K key) {
        V value = super.get(key);
        if (value != null) {
            if (hardCache.remove(value)) {
                cacheSize.addAndGet(-getSize(value));
            }
        }
        super.remove(key);
    }

    @Override
    public void clear() {
        hardCache.clear();
        cacheSize.set(0);
        super.clear();
    }

    protected int getSizeLimit() {
        return sizeLimit;
    }

    protected abstract int getSize(V value);

    protected abstract V removeNext();
}
