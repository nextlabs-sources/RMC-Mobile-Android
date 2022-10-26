package com.skydrm.rmc.ui.common;

public enum NxlFileType {
    ALL(0),
    OFFLINE(1),
    FAVORITE(2),
    SHARED_BY_ME(3),
    SHARED_WITH_ME(4),
    PROTECTED(5),
    REVOKED(6),
    DELETED(7),
    RECENT(8);

    private int value;

    NxlFileType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    static NxlFileType valueOf(int value) {
        switch (value) {
            case 0:
                return ALL;
            case 1:
                return OFFLINE;
            case 2:
                return FAVORITE;
            case 3:
                return SHARED_BY_ME;
            case 4:
                return SHARED_WITH_ME;
            case 5:
                return PROTECTED;
            case 6:
                return REVOKED;
            case 7:
                return DELETED;
            case 8:
                return RECENT;
        }
        throw new IllegalArgumentException("Unrecognized value " + value + " to convert into NxlFileType.");
    }
}
