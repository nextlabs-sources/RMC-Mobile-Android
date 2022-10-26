package com.skydrm.rmc.ui.service.offline;

import android.os.Looper;
import android.text.TextUtils;

import com.skydrm.rmc.ui.service.offline.architecture.IMarker;
import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineResponse;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineStatusDelivery;
import com.skydrm.rmc.ui.service.offline.architecture.IUnMarker;
import com.skydrm.rmc.ui.service.offline.core.OfflineMarker;
import com.skydrm.rmc.ui.service.offline.core.OfflineResponse;
import com.skydrm.rmc.ui.service.offline.core.OfflineStatusDelivery;
import com.skydrm.rmc.ui.service.offline.core.OfflineUnMarker;
import com.skydrm.rmc.ui.service.offline.filter.OfflineFileFilter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class OfflineManager implements IMarker.OnDestroyListener, IUnMarker.OnDestroyListener {
    private static final Map<String, IMarker> mMarkers = new HashMap<>();
    private static final Map<String, IUnMarker> mUnMarkers = new HashMap<>();
    private OfflineFileFilter mFileFilter;
    private IOfflineStatusDelivery mDelivery;

    public static OfflineManager getInstance() {
        return SINGLETON.INSTANCE;
    }

    /**
     * This method is used to mark file from project&myvault as offline file.
     * here are exact flows as make as offline need:filter file->check policy->download file->cache token
     * each step of the concurrent flow is a handler,all handlers are connected as chain.
     *
     * @param file target file need to be operate
     */
    public void markAsOffline(IOffline file, IOfflineCallback callback) {
        String key = createKey(file.getName());
        if (checkKey(key)) {
            IOfflineResponse response = new OfflineResponse(mDelivery, callback);
            IMarker marker = new OfflineMarker(mFileFilter, file, key, response, this);
            mMarkers.put(key, marker);
            marker.start();
        }
    }

    /**
     * This method is used to remove target file offline mark.
     * include remove [token&rights,etc.] if necessary.
     *
     * @param file target file need to be operated.
     */
    public void unMarkAsOffline(IOffline file) {
        String key = createKey(file.getName());
        //means file is in the download list.
        if (containsKey(key)) {
            IMarker marker = mMarkers.get(key);
            if (marker != null) {
                marker.cancel();
            }
        } else {
            if (checkUnMarkKey(key)) {
                //file already downloaded just clear all caches have been stored.
                IUnMarker unMarker = new OfflineUnMarker(file, key, this);
                unMarker.start();
            }
        }
    }

    public boolean isTaskRunning() {
        if (mMarkers.size() == 0) {
            return false;
        }
        Collection<IMarker> markers = mMarkers.values();
        for (IMarker m : markers) {
            if (m != null && m.isRunning()) {
                return true;
            }
        }
        return false;
    }

    public void cancelAll() {
        if (mMarkers.size() == 0) {
            return;
        }
        Collection<IMarker> markers = mMarkers.values();
        for (IMarker m : markers) {
            if (m != null) {
                m.cancel();
            }
        }
    }

    private OfflineManager() {
        mFileFilter = new OfflineFileFilter();
        mDelivery = new OfflineStatusDelivery(Looper.getMainLooper());
    }

    private String createKey(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new NullPointerException("path must be set correctly first.");
        }
        return String.valueOf(path.hashCode());
    }

    private boolean checkKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        if (mMarkers.containsKey(key)) {
            IMarker marker = mMarkers.get(key);
            if (marker != null && marker.isRunning()) {
                return false;
            } else {
                throw new IllegalArgumentException("marker instance with the same tag has not been destroyed.");
            }
        }
        return true;
    }

    private boolean checkUnMarkKey(String key) {
        return !TextUtils.isEmpty(key) && !mUnMarkers.containsKey(key);
    }

    private boolean containsKey(String key) {
        return !TextUtils.isEmpty(key) && mMarkers.containsKey(key);
    }

    @Override
    public void onDestroy(String key, IMarker marker) {
        mMarkers.remove(key);
    }

    @Override
    public void onDestroy(String key, IUnMarker unMarker) {
        mUnMarkers.remove(key);
    }

    private static final class SINGLETON {
        private static final OfflineManager INSTANCE = new OfflineManager();
    }
}
