package com.skydrm.rmc.ui.service.offline.architecture;

import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public class OfflineStatus {
    public static final int STATUS_FILTER_FAILED_INVALID_NXL_FILE = 0x1000;
    public static final int STATUS_FILTER_FAILED_NOT_SUPPORTED_TYPE = 0x1001;
    public static final int STATUS_FILTER_FAILED_UNKNOWN_TYPE = 0x1002;
    public static final int STATUS_DOWNLOAD_NXL_FILE_HEADER = 0x1003;
    public static final int STATUS_DOWNLOAD_NXL_FILE_CONTENT = 0x1004;
    public static final int STATUS_SESSION_INVALID = 0x1005;

    public static final int STATUS_STARTED = 0x100;
    public static final int STATUS_PROGRESS = 0x101;
    public static final int STATUS_MARK_SUCCESS = 0x102;
    public static final int STATUS_FAILED = 0x103;
    public static final int STATUS_INVALID_NXL_FILE = 0x104;
    public static final int STATUS_FILE_NOT_FOUND = 0x105;
    public static final int STATUS_UNAUTHORIZED = 0x106;

    public static final int STATUS_ACCEPTED = 0x200;
    public static final int STATUS_CHECK_POLICY = 0x201;
    public static final int STATUS_CACHE_RIGHTS = 0x202;
    public static final int STATUS_DOWNLOAD_START = 0x203;
    public static final int STATUS_DOWNLOAD_PROGRESS = 0x204;
    public static final int STATUS_DOWNLOAD_CANCEL = 0x205;
    public static final int STATUS_DOWNLOAD_COMPLETE = 0x206;
    public static final int STATUS_DOWNLOAD_FAILED = 0x207;
    public static final int STATUS_TOKEN_PROCESSED = 0x208;
    public static final int STATUS_TOKEN_PROCESS_FAILED = 0x209;
    public static final int STATUS_TOKEN_DEACTIVE_FAILED = 0x210;
    public static final int STATUS_REST_API_EXCEPTION = 0x211;

    public static final int STATUS_TOKEN_INVALID_SETTING_TIME = 0x300;
    public static final int STATUS_TOKEN_EXPIRED = 0x301;
    public static final int STATUS_TOKEN_ACCESS_DENY = 0x302;
    public static final int STATUS_UNMARK_DONE = 0x303;
    private int status;
    private long start;
    private long end;
    private IOfflineCallback callback;
    private OfflineException exception;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public OfflineException getException() {
        return exception;
    }

    public void setException(OfflineException mException) {
        this.exception = mException;
    }

    public IOfflineCallback getCallback() {
        return callback;
    }

    public void setCallback(IOfflineCallback callback) {
        this.callback = callback;
    }

    public long getStartTime() {
        return start;
    }

    public void setStartTime(long timillis) {
        this.start = timillis;
    }

    public long getEndTime() {
        return end;
    }

    public void setEndTime(long end) {
        this.end = end;
    }

    public long getDuring() {
        return end - start;
    }
}
