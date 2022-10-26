package com.skydrm.rmc.ui.service.offline.downloader.exception;

public class DownloadException extends Exception {
    private static final long serialVersionUID = 225852391037939359L;
    private int errCode;
    private String errMsg;
    private int statusCode;

    public DownloadException() {
    }

    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(int code, String message) {
        super(message);
        this.errCode = code;
        this.errMsg = message;
    }

    public DownloadException(int code, int statusCode, String message) {
        super(message);
        this.errCode = code;
        this.statusCode = statusCode;
        this.errMsg = message;
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DownloadException(int code, String message, Throwable cause) {
        super(message, cause);
        this.errCode = code;
        this.errMsg = message;
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
