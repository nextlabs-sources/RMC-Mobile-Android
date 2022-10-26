package com.skydrm.rmc.reposystem.exception;


public class FileListException extends Exception {
    private ExceptionCode exptCode;

    public FileListException(String detailMessage, ExceptionCode code) {
        super(detailMessage);
        this.exptCode = code;
    }

    public FileListException(String detailMessage) {
        super(detailMessage);
        this.exptCode = ExceptionCode.Common;
    }

    public ExceptionCode getErrorCode() {
        return exptCode;
    }

    public enum ExceptionCode {
        Common,
        ParamInvalid,
        AuthenticationFailed,   //  account invalid, UI must tell user this situation
        IllegalOperation,
        NamingCollided,
        NamingViolation
    }
}
