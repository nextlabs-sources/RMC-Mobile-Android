package com.skydrm.rmc.ui.project.feature.service.core;

import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;

public class MarkerStatus {
    public static final int STATUS_FAILED_COMMON = 0x01;
    public static final int STATUS_FAILED_UNSUPPORTED_FILE_TYPE = 0x02;
    public static final int STATUS_FAILED_RMS_REST_API_EXCEPTION = 0x03;
    public static final int STATUS_FAILED_IO_EXCEPTION = 0x04;
    public static final int STATUS_FAILED_TOKEN_ACCESS_DENY_EXCEPTION = 0x05;
    public static final int STATUS_FAILED_INVALID_RMC_CLIENT = 0x06;
    public static final int STATUS_FAILED_SESSION_INVALID = 0x07;

    public static final int STATUS_FAILED_WRONG_POLICY_TYPE = 0x08;
    public static final int STATUS_FAILED_UNAUTHORIZED = 0x09;

    public static final int STATUS_FAILED_DECRYPT = 0X10;
    public static final int STATUS_FAILED_ENCRYPT = 0X11;
    public static final int STATUS_FAILED_UPLOAD = 0x12;
    public static final int STATUS_FAILED_SEND_LOG = 0x13;

    public static final int STATUS_MARK_START = 0x100;
    public static final int STATUS_MARK_ALLOW = 0x101;
    public static final int STATUS_MARK_CANCEL = 0x102;
    public static final int STATUS_MARK_FAILED = 0x103;


    private int mStatus;
    private IMarkCallback mCallback;
    private MarkException mException;

    public int getStatus() {
        return mStatus;
    }

    public IMarkCallback getCallback() {
        return mCallback;
    }

    public MarkException getException() {
        return mException;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public void setCallback(IMarkCallback callback) {
        this.mCallback = callback;
    }

    public void setException(MarkException e) {
        this.mException = e;
    }
}
