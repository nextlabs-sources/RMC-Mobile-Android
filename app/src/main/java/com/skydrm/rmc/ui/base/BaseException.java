package com.skydrm.rmc.ui.base;

public abstract class BaseException extends Exception {
    private static final long serialVersionUID = 267051023294891103L;

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }
}
