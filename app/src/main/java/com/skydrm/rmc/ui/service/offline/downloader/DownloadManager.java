package com.skydrm.rmc.ui.service.offline.downloader;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.skydrm.rmc.ui.service.offline.downloader.architecture.IDownloadResponse;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.IDownloader;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.INotifySystem;
import com.skydrm.rmc.ui.service.offline.downloader.config.DefaultConfiguration;
import com.skydrm.rmc.ui.service.offline.downloader.core.DownloadRequest;
import com.skydrm.rmc.ui.service.offline.downloader.core.DownloadResponse;
import com.skydrm.rmc.ui.service.offline.downloader.core.Downloader;
import com.skydrm.rmc.ui.service.offline.downloader.core.NotifySystem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nullable;

public class DownloadManager implements IDownloader.IDownloaderDestroyListener {
    private final Map<String, IDownloader> mDownloaders;
    private final INotifySystem mNotifySystem;
    private Executor mExecutor;

    public static DownloadManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(@Nullable Context context, @Nullable IConfiguration config) {
        IConfiguration configuration = config == null ? new DefaultConfiguration() : config;
        ThreadFactory threadFactory = configuration.getThreadFactory();
        if (threadFactory == null) {
            mExecutor = Executors.newFixedThreadPool(configuration.getThreadNum());
        } else {
            mExecutor = Executors.newFixedThreadPool(configuration.getThreadNum(), threadFactory);
        }
    }

    public void start(@NonNull DownloadRequest request, @NonNull String tag, @NonNull ICallback callback) {
        String key = createKey(tag);
        if (checkKey(key)) {
            IDownloadResponse downloadResponse = new DownloadResponse(mNotifySystem, callback);
            IDownloader downloader = new Downloader(request, downloadResponse, mExecutor, tag, this);
            mDownloaders.put(key, downloader);
            downloader.start();
        }
    }

    public void pause(@NonNull String tag) throws IllegalArgumentException {
        String key = createKey(tag);
        if (mDownloaders.containsKey(key)) {
            IDownloader downloader = mDownloaders.get(key);
            downloader.pause();
        } else {
            throw new IllegalArgumentException("Cannot find key,may be wrong tag delivered.");
        }
    }

    public void cancel(@NonNull String tag) throws IllegalArgumentException {
        String key = createKey(tag);
        if (mDownloaders.containsKey(key)) {
            IDownloader downloader = mDownloaders.get(key);
            if (downloader != null) {
                downloader.cancel();
            }
        } else {
            throw new IllegalArgumentException("Cannot find key,may be wrong tag delivered.");
        }
    }

    private boolean checkKey(String key) throws IllegalArgumentException {
        if (mDownloaders.containsKey(key)) {
            IDownloader downloader = mDownloaders.get(key);
            if (downloader.isRunning()) {
                return false;
            } else {
                throw new IllegalStateException("Downloader instance with the same tag has not been destroyed.");
            }
        }
        return true;
    }

    private String createKey(String tag) throws NullPointerException {
        if (TextUtils.isEmpty(tag)) {
            throw new NullPointerException("Tag must not be null.");
        }
        return String.valueOf(tag.hashCode());
    }

    private DownloadManager() {
        mDownloaders = new HashMap<>();
        mNotifySystem = new NotifySystem(Looper.getMainLooper());
    }

    @Override
    public void onDestroyed(String tag, IDownloader downloader) {
        mDownloaders.remove(createKey(tag));
    }

    private static final class SingletonHolder {
        private static final DownloadManager INSTANCE = new DownloadManager();
    }
}
