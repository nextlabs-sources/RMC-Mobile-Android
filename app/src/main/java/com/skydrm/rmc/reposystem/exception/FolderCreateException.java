package com.skydrm.rmc.reposystem.exception;

/**
 * Created by oye on 4/6/2017.
 */

public class FolderCreateException extends Exception {
    private ExceptionCode exptCode;

    public ExceptionCode getErrorCode() {
        return exptCode;
    }

    public FolderCreateException(String detailMessage, ExceptionCode code) {
        super(detailMessage);
        this.exptCode = code;
    }

    public FolderCreateException(String detailMessage) {
        super(detailMessage);
        this.exptCode = ExceptionCode.Common;
    }

    public enum ExceptionCode {
        Common,
        ParamInvalid,
        AuthenticationFailed,   // RMS user has logout or changed password, UI must tell user this situation
        IllegalOperation,
        NamingCollided,
        NamingViolation
    }
}
