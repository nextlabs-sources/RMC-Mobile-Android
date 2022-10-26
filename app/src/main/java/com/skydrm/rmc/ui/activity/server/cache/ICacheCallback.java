package com.skydrm.rmc.ui.activity.server.cache;

import java.util.List;

public interface ICacheCallback<T> {
    void onCacheLoad(List<T> caches);
}