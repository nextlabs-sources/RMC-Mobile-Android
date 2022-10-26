package com.skydrm.rmc.ui.service.offline.downloader;

import java.util.concurrent.ThreadFactory;

public interface IConfiguration {
    ThreadFactory getThreadFactory();

    int getThreadNum();
}
