package com.skydrm.sdk.exception;


public class TokenAccessDenyException extends Exception {
    public static final int TYPE_TOKEN_EXPIRED = 0x100;
    public static final int TYPE_TOKEN_DENY_PROJECT_SHARE_TO_PERSON = 0x101;
    private int type;
    private String targetPath;
    private int sharedSpaceType;
    private int sharedSpaceId;
    private String sharedSpaceUserMembership;

    public TokenAccessDenyException(String detailMessage) {
        super(detailMessage);
    }

    public TokenAccessDenyException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenAccessDenyException(String message, Throwable cause, int type) {
        super(message, cause);
        this.type = type;
    }

    public TokenAccessDenyException(String message, Throwable cause, int type,
                                    String path) {
        super(message, cause);
        this.type = type;
        this.targetPath = path;
    }

    public TokenAccessDenyException(String message, Throwable cause,
                                    int type, String path,
                                    int sharedSpaceType, int sharedSpaceId,
                                    String sharedSpaceUserMembership) {
        super(message, cause);
        this.type = type;
        this.targetPath = path;
        this.sharedSpaceType = sharedSpaceType;
        this.sharedSpaceId = sharedSpaceId;
        this.sharedSpaceUserMembership = sharedSpaceUserMembership;
    }

    public int getType() {
        return type;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public int getSharedSpaceType() {
        return sharedSpaceType;
    }

    public int getSharedSpaceId() {
        return sharedSpaceId;
    }

    public String getSharedSpaceUserMembership() {
        return sharedSpaceUserMembership;
    }
}
