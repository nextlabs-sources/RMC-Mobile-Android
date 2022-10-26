package com.skydrm.rmc.ui.service.offline.downloader.utils;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {
    public static void closeSilently(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
