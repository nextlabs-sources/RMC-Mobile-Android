package com.skydrm.rmc.reposystem.exception;

/**
 * Created by oye on 4/13/2017.
 */

public class FileUploadException extends Exception {
    private ExceptionCode exptCode;

    public FileUploadException(String detailMessage) {
        super(detailMessage);
        exptCode = ExceptionCode.Common;
    }

    public FileUploadException(String detailMessage, ExceptionCode exptCode) {
        super(detailMessage);
        this.exptCode = exptCode;
    }

    public ExceptionCode getErrorCode() {
        return exptCode;
    }

    public enum ExceptionCode {
        Common,
        ParamInvalid,
        AuthenticationFailed,   // RMS user has logout or changed password, UI must tell user this situation
        NetWorkIOFailed,
        IllegalOperation,
        NamingCollided,
        NameTooLong,
        DriveStorageExceed,
        NamingViolation
    }
}
