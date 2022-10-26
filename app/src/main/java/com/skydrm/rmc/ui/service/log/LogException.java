package com.skydrm.rmc.ui.service.log;

public class LogException extends Exception {
    private static final long serialVersionUID = 4021723605236859772L;
    public static final int EXCEPTION_RMS_REST_API = 0x001;
    public static final int EXCEPTION_SESSION_INVALID = 0x002;
    public static final int EXCEPTION_RMC_CLIENT_INVALID = 0x003;

    private String msg;
    private int statusCode;
    private Exception mCause;

    public LogException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public LogException(String msg, Exception cause) {
        super(msg, cause);
        this.msg = msg;
        this.mCause = cause;
    }

    public LogException(String msg, int code, Exception cause) {
        super(msg, cause);
        this.msg = msg;
        this.statusCode = code;
        this.mCause = cause;
    }

    public String getMsg() {
        return msg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Exception getCause() {
        return mCause;
    }
}

