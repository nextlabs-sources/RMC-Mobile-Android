package com.skydrm.pdf.core.cache;

import java.util.Objects;

public class CacheKey {
    private int idx;

    private CacheKey(int idx) {
        this.idx = idx;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey cacheKey = (CacheKey) o;
        return idx == cacheKey.idx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idx);
    }

    @Override
    public String toString() {
        return String.valueOf(hashCode());
    }

    static CacheKey makeKey(int idx) {
        return new CacheKey(idx);
    }

    public void recycle() {
        idx = -1;
    }
}
