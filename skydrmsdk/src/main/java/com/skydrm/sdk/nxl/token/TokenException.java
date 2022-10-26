package com.skydrm.sdk.nxl.token;

public class TokenException extends Exception {
    private static final long serialVersionUID = -6043092266642760464L;
    public static final int FAILED_COMMON = 0x01;
    public static final int FAILED_TOKEN_EXPIRED = 0x100;
    public static final int FAILED_TOKEN_ACCESS_DENY = 0x101;
    public static final int FAILED_RMS_REST_API_EXCEPTION = 0x102;

    private int mStatus;

    public TokenException() {
    }

    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenException(Throwable cause) {
        super(cause);
    }

    public TokenException(int status, String message) {
        super(message);
        this.mStatus = status;
    }

    public TokenException(int status, String message, Throwable cause) {
        super(message, cause);
        this.mStatus = status;
    }

    public int getStatus() {
        return mStatus;
    }
}
