package com.skydrm.rmc.utils.cache;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by hhu on 3/14/2017.
 */

public abstract class BaseMemoryCache<K, V> implements MemoryCacheAware<K, V> {
    private final Map<K, Reference<V>> softMap = Collections.synchronizedMap(new HashMap<K, Reference<V>>());

    @Override
    public boolean put(K key, V vaule) {
        softMap.put(key, createReference(vaule));
        return true;
    }

    @Override
    public V get(K key) {
        V result = null;
        Reference<V> reference = softMap.get(key);
        if (reference != null) {
            result = reference.get();
        }
        return result;
    }

    @Override
    public void remove(K key) {
        softMap.remove(key);
    }

    @Override
    public Collection<K> keys() {
        synchronized (softMap) {
            return new HashSet<>(softMap.keySet());
        }
    }

    @Override
    public void clear() {
        softMap.clear();
    }

    public abstract Reference<V> createReference(V vaule);
}
