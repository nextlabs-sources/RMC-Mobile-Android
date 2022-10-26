package com.skydrm.sdk;

import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.Map;

//  todo: osm, change it as if containning all adHoc's obls
public interface INxlObligations {
    @Nullable
    Iterator<Map.Entry<String, String>> getIterator();

    boolean hasWatermark();

    String getDisplayWatermark();
}
