package com.skydrm.rmc.ui.common;

import com.skydrm.sdk.exception.RmsRestAPIException;

public class LoadNxlException extends Exception {
    private static final long serialVersionUID = 8308068187923627901L;
    private ErrorCode mErrorCode;
    private RmsRestAPIException.ExceptionDomain mExceptionDomain;

    public LoadNxlException() {
    }

    public LoadNxlException(String message) {
        super(message);
    }

    public LoadNxlException(String message, ErrorCode code, Throwable cause) {
        super(message, cause);
        this.mErrorCode = code;
    }

    public LoadNxlException(Throwable cause) {
        super(cause);
    }

    public LoadNxlException(String message, ErrorCode code,
                            RmsRestAPIException.ExceptionDomain domain, Throwable e) {
        super(message, e);
        this.mErrorCode = code;
        this.mExceptionDomain = domain;
    }

    public ErrorCode getErrorCode() {
        return mErrorCode;
    }

    public RmsRestAPIException.ExceptionDomain getExceptionDomain() {
        return mExceptionDomain;
    }
}
