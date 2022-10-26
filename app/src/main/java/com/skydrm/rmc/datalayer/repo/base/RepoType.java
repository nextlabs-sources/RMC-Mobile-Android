package com.skydrm.rmc.datalayer.repo.base;

public enum RepoType {
    TYPE_MYVAULT(0),
    TYPE_SHARED_WITH_ME(1),
    TYPE_PROJECT(2),
    TYPE_WORKSPACE(3),
    TYPE_LIBRARY(4);

    private int value;

    RepoType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RepoType valueOf(int value) {
        switch (value) {
            case 0:
                return TYPE_MYVAULT;
            case 1:
                return TYPE_SHARED_WITH_ME;
            case 2:
                return TYPE_PROJECT;
            case 3:
                return TYPE_WORKSPACE;
            case 4:
                return TYPE_LIBRARY;
        }
        throw new IllegalArgumentException("Unrecognized value " + value + " to convert into RepoType");
    }
}
