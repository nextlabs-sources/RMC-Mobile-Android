package com.skydrm.rmc.reposystem.exception;

/**
 * Created by oye on 4/17/2017.
 */

public class FileDownloadException extends Exception {

    private ExceptionCode exptCode;

    public FileDownloadException(String detailMessage) {
        super(detailMessage);
        exptCode = ExceptionCode.Common;
    }

    public FileDownloadException(String detailMessage, ExceptionCode exptCode) {
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
        UserCanceled,
        NetWorkIOFailed,
        IllegalOperation,
        ExportedFileTooLarge // for google file export.
    }

}
