package com.skydrm.rmc.ui.service.offline.exception;

public class OfflineException extends Exception {
    private static final long serialVersionUID = -261381771106446094L;
    private int errCode;
    private String errMsg;
    private int statusCode;

    public OfflineException() {
    }

    public OfflineException(int code, String message) {
        super(message);
        this.errCode = code;
        this.errMsg = message;
    }

    public OfflineException(String message, Throwable cause) {
        super(message, cause);
    }

    public OfflineException(Throwable cause) {
        super(cause);
    }

    public OfflineException(int code, String message, Throwable cause) {
        super(message, cause);
        this.errCode = code;
        this.errMsg = message;
    }

    public OfflineException(int code, Throwable cause) {
        super(cause);
        this.errCode = code;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
