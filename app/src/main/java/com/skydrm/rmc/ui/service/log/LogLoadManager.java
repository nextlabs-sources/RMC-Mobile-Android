package com.skydrm.rmc.ui.service.log;

import android.text.TextUtils;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.IVaultFileLog;
import com.skydrm.rmc.utils.sort.SortContext;
import com.skydrm.rmc.utils.sort.SortType;
import com.skydrm.sdk.rms.types.FetchLogRequestParas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogLoadManager {
    private static final Map<String, List<IVaultFileLog>> mCaches = new HashMap<>();
    private List<IVaultFileLog> mTmpData = new ArrayList<>();

    public static LogLoadManager getInstance() {
        return SINGLETON.INSTANCE;
    }

    public List<IVaultFileLog> getSearchItem() {
        return mTmpData;
    }

    public void getSortedLog(final String duid, final SortType sortType, final ILoadCallback<List<IVaultFileLog>, LogException> callback) {
        getLog(duid, new ILoadCallback<List<IVaultFileLog>, LogException>() {
            @Override
            public void onLoading() {
                callback.onLoading();
            }

            @Override
            public void onResult(List<IVaultFileLog> result, int total) {
                //callback.onResult(result);
                sort(new ArrayList<>(result), total, sortType, callback);
                updateCache(duid, result);
            }

            @Override
            public void onError(LogException error) {
                callback.onError(error);
            }
        });
    }

    void getSortedPageLog(String duid, final SortType sortType, int start, int count, final ILoadCallback<List<IVaultFileLog>, LogException> callback) {
        fetchLogByPage(duid, start, count, new ILoadCallback<List<IVaultFileLog>, LogException>() {
            @Override
            public void onLoading() {
                callback.onLoading();
            }

            @Override
            public void onResult(List<IVaultFileLog> result, int total) {
                if (result.size() == 0) {
                    callback.onResult(result, total);
                    return;
                }
                sort(new ArrayList<>(result), total, sortType, callback);
            }

            @Override
            public void onError(LogException error) {
                callback.onError(error);
            }
        });
    }

    private void getLog(final String duid, final ILoadCallback<List<IVaultFileLog>, LogException> callback) {
        final List<IVaultFileLog> logs = getFromMem(duid);
        if (logs == null || logs.isEmpty()) {
            initialize(duid, callback);
        } else {
            callback.onResult(new ArrayList<>(logs), -1);
            fetchAllLog(duid, new ILoadCallback<List<IVaultFileLog>, LogException>() {
                @Override
                public void onLoading() {
                    callback.onLoading();
                }

                @Override
                public void onResult(List<IVaultFileLog> result, int total) {
                    if (logs.containsAll(result) && logs.size() == result.size()) {
                        return;
                    }
                    callback.onResult(result, total);
                    //updateCache(duid, result);
                }

                @Override
                public void onError(LogException error) {
                    callback.onError(error);
                }
            });
        }
    }

    public void sort(List<IVaultFileLog> data, int total, SortType sortType, ILoadCallback<List<IVaultFileLog>, LogException> callback) {
        List<IVaultFileLog> sortedLogs = SortContext.sortLog(data, sortType);
        mTmpData.clear();
        mTmpData.addAll(sortedLogs);
        callback.onResult(sortedLogs, total);
    }

    private void initialize(final String duid, final ILoadCallback<List<IVaultFileLog>, LogException> callback) {
        fetchAllLog(duid, new ILoadCallback<List<IVaultFileLog>, LogException>() {
            @Override
            public void onLoading() {
                callback.onLoading();
            }

            @Override
            public void onResult(List<IVaultFileLog> result, int total) {
                cacheIntoMem(duid, result);
                callback.onResult(result, total);
            }

            @Override
            public void onError(LogException error) {
                callback.onError(error);
            }
        });
    }

    private void cacheIntoMem(String duid, List<IVaultFileLog> result) {
        if (result == null || result.size() == 0) {
            return;
        }
        String key = createKey(duid);
        if (checkKey(key)) {
            mCaches.put(key, result);
        }
    }

    private void updateCache(String duid, List<IVaultFileLog> result) {
        if (result == null || result.size() == 0) {
            return;
        }
        String key = createKey(duid);
        if (containsKey(key)) {
            List<IVaultFileLog> iVaultFileLogs = mCaches.get(key);
            iVaultFileLogs.clear();
            iVaultFileLogs.addAll(result);
        }
    }

    private List<IVaultFileLog> getFromMem(String duid) {
        String key = createKey(duid);
        if (containsKey(key)) {
            return mCaches.get(key);
        }
        return null;
    }

    private void fetchAllLog(String duid, ILoadCallback<List<IVaultFileLog>, LogException> callback) {
        fetchLogByPage(duid, -1, -1, callback);
    }

    private void fetchLogByPage(String duid, int start, int count, ILoadCallback<List<IVaultFileLog>, LogException> callback) {
        FetchLogRequestParas paras = new FetchLogRequestParas();
        paras.setStart(start);
        paras.setCount(count);
        LogTask logTask = new LogTask(duid, paras, callback);
        logTask.executeOnExecutor(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
    }

    private LogLoadManager() {

    }

    private boolean checkKey(String key) {
        return !TextUtils.isEmpty(key) && !mCaches.containsKey(key);
    }

    private boolean containsKey(String key) {
        return !TextUtils.isEmpty(key) && mCaches.containsKey(key);
    }

    private String createKey(String tag) {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("tag must be set correctly.");
        }
        return String.valueOf(tag.hashCode());
    }

    private static final class SINGLETON {
        private static final LogLoadManager INSTANCE = new LogLoadManager();
    }
}
