package com.skydrm.rmc.ui.project.feature.service;


import android.os.Looper;
import android.text.TextUtils;

import com.skydrm.rmc.ui.service.offline.architecture.IMarker;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerResponse;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatusDelivery;
import com.skydrm.rmc.ui.project.feature.service.share.IShare;
import com.skydrm.rmc.ui.project.feature.service.share.core.ShareMarker;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsFile;
import com.skydrm.rmc.ui.service.modifyrights.core.ModifyRightsMarker;

import java.util.HashMap;
import java.util.Map;

public class MarkerManager implements IMarker.OnDestroyListener {
    private static final Map<String, IMarker> mShareMarkers = new HashMap<>();
    private IMarkerStatusDelivery mStatusDelivery;

    public static MarkerManager getInstance() {
        return SINGLETON.INSTANCE;
    }

    public void shareToProject(IShare file, IMarkCallback callback) {
        String key = createKey(file.getName());
        if (checkKey(key)) {
            IMarkerResponse response = new MarkerResponse(mStatusDelivery, callback);
            IMarker share = new ShareMarker(key, file, ShareMarker.ADD_TO_PROJECT, response, this);
            mShareMarkers.put(key, share);
            share.start();
        }
    }

    public void shareToPerson(IShare file, IMarkCallback callback) {
        String key = createKey(file.getName());
        if (checkKey(key)) {
            IMarkerResponse response = new MarkerResponse(mStatusDelivery, callback);
            IMarker share = new ShareMarker(key, file, ShareMarker.SHARE_TO_PERSON, response, this);
            mShareMarkers.put(key, share);
            share.start();
        }
    }

    public void modifyRights(IModifyRightsFile file, IMarkCallback callback) {
        String key = createKey(file.getName());
        if (checkKey(key)) {
            IMarkerResponse response = new MarkerResponse(mStatusDelivery, callback);
            IMarker share = new ModifyRightsMarker(key, file, response, this);
            mShareMarkers.put(key, share);
            share.start();
        }
    }

    @Override
    public void onDestroy(String key, IMarker marker) {
        mShareMarkers.remove(key);
    }

    private MarkerManager() {
        mStatusDelivery = new MarkerStatusDelivery(Looper.getMainLooper());
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
            if (marker.isRunning()) {
                return false;
            } else {
                throw new IllegalArgumentException("marker instance with the same tag has not been destroyed.");
            }
        }
        return true;
    }

    private static class SINGLETON {
        private static final MarkerManager INSTANCE = new MarkerManager();
    }
}
