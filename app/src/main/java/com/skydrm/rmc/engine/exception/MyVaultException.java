package com.skydrm.rmc.engine.exception;

/**
 * Created by aning on 4/9/2017.
 */

public class MyVaultException extends Exception {
    private ExceptionType mExceptionType;

    public MyVaultException(String detailMessage) {
        super(detailMessage);
        mExceptionType = ExceptionType.Common;
    }

    public MyVaultException(String detailMessage, ExceptionType exceptionType) {
        super(detailMessage);
        mExceptionType = exceptionType;
    }

    public ExceptionType getmExceptionType() {
        return mExceptionType;
    }

    public enum ExceptionType {
        Common,
        ParamInvalid,
        AuthenticationFailed,
        InvalidNxlFormat,
        InvalidRepoMetadata,
        InvalidFileName,
        InvalidFileExtension
    }

}
