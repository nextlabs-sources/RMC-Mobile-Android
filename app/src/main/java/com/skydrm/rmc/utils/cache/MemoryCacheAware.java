package com.skydrm.rmc.utils.cache;

import java.util.Collection;

/**
 * Created by hhu on 3/14/2017.
 */

public interface MemoryCacheAware<K, V> {
    /**
     * put vaule into cache by key
     *
     * @param key
     * @param vaule
     * @return true or false
     */
    boolean put(K key, V vaule);

    V get(K key);

    /**
     * return vaule by key
     *
     * @param key
     * @return vaule
     */
    void remove(K key);

    /**
     * @return return all keys in cache
     */
    Collection<K> keys();

    /**
     * clear all caches
     */
    void clear();
}
