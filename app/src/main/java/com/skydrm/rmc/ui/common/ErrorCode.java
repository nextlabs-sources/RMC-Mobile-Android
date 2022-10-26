package com.skydrm.rmc.ui.common;

public enum ErrorCode {
    SESSION_INVALID(0),
    INVALID_RMC_CLIENT(1),
    RMS_REST_API_ERROR(2),
    COMMON(3);

    private int value;

    ErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    static ErrorCode valueOf(int value) {
        switch (value) {
            case 0:
                return SESSION_INVALID;
            case 1:
                return INVALID_RMC_CLIENT;
            case 2:
                return RMS_REST_API_ERROR;
            case 3:
                return COMMON;
        }
        throw new IllegalArgumentException("Unrecognized value " + value + " to convert into ErrorCode.");
    }
}
