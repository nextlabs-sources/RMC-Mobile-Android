package com.skydrm.rmc.ui.service.share;

import android.os.Looper;
import android.text.TextUtils;

import com.skydrm.rmc.ui.service.offline.architecture.IMarker;
import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.rmc.ui.project.feature.service.IMarkerResponse;
import com.skydrm.rmc.ui.project.feature.service.IMarkerStatusDelivery;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerResponse;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatusDelivery;
import com.skydrm.rmc.ui.service.share.core.ShareMarker;

import java.util.HashMap;
import java.util.Map;

public class ShareManager implements IMarker.OnDestroyListener {
    private static final Map<String, IMarker> mShareMarkers = new HashMap<>();
    private IMarkerStatusDelivery mStatusDelivery;

    private ShareManager() {
        mStatusDelivery = new MarkerStatusDelivery(Looper.getMainLooper());
    }

    public static ShareManager getInstance() {
        return SINGLETON.INSTANCE;
    }

    public void share(ISharingFile file, IMarkCallback callback) {
        String key = createKey(file.getName());
        if (checkKey(key)) {
            IMarkerResponse response = new MarkerResponse(mStatusDelivery, callback);
            IMarker share = new ShareMarker(key, file, response, this);
            mShareMarkers.put(key, share);
            share.start();
        }
    }

    @Override
    public void onDestroy(String key, IMarker marker) {
        mShareMarkers.remove(key);
    }

    private String createKey(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new NullPointerException("path must be set correctly first.");
        }
        return String.valueOf(path.hashCode());
    }

    private boolean checkKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        if (mShareMarkers.containsKey(key)) {
            IMarker marker = mShareMarkers.get(key);
            if (marker != null && marker.isRunning()) {
                return false;
            } else {
                throw new IllegalArgumentException("marker instance with the same tag has not been destroyed.");
            }
        }
        return true;
    }

    private static class SINGLETON {
        private static final ShareManager INSTANCE = new ShareManager();
    }

}
