package com.skydrm.rmc.engine.exception;

/**
 * Created by aning on 4/9/2017.
 */

public class ProjectException extends Exception {
    private ProjectException.ExceptionType mExceptionType;

    public ProjectException(String detailMessage) {
        super(detailMessage);
        mExceptionType = ProjectException.ExceptionType.Common;
    }

    public ProjectException(String detailMessage, ProjectException.ExceptionType exceptionType) {
        super(detailMessage);
        mExceptionType = exceptionType;
    }

    public ProjectException.ExceptionType getmExceptionType() {
        return mExceptionType;
    }

    public enum ExceptionType {
        Common,
        ParamInvalid,

        InvalidProjectName,
        InvalidProjectDescription,

        OnlyOwnerRemoveMember,
        OwnerCannotBeRemoved,
        ProjectNameAlreadyExist,

        InvitationExpired,
        InvitationAlreadyDeclined,
        EmailNotMatched,
        InvitationAlreadyAccepted,
        InvitationAlreadyRevoked,
        DeclineReasonTooLong
    }
}
