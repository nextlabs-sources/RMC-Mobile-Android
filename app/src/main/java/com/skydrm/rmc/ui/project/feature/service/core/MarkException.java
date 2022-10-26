package com.skydrm.rmc.ui.project.feature.service.core;

import com.skydrm.rmc.ui.base.BaseException;

public class MarkException extends BaseException {
    private static final long serialVersionUID = -7739087478038594803L;

    private int mErrCode;

    public MarkException(int code, String message) {
        super(message);
        this.mErrCode = code;
    }

    public MarkException(int code, String message, Throwable cause) {
        super(message, cause);
        this.mErrCode = code;
    }

    public int getErrorCode() {
        return mErrCode;
    }
}
