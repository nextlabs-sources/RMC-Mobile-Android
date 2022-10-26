package com.skydrm.rmc.ui.service.offline.downloader.config;

import android.support.annotation.NonNull;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.service.offline.downloader.IConfiguration;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;

public class DefaultConfiguration implements IConfiguration {
    @Override
    public ThreadFactory getThreadFactory() {
        return new DefaultThreadFactory();
    }

    @Override
    public int getThreadNum() {
        return 3;
    }

    /**
     * The default thread factory.
     */
    private static class DefaultThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "DThread-";
        }

        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    public static OkHttpClient getHttpClient() {
        try {
            return SkyDRMApp.getInstance().getSession().getRmsRestAPI().getHttpClient();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return null;
    }
}
