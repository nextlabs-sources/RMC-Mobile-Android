package com.skydrm.rmc.datalayer.heartbeat;

public interface IHeartBeatPolicy {
    int TYPE_NEW_USER_LOGIN = 0x00;
    int TYPE_USER_RECOVER = 0x01;

    int getType();

    long getInterval();
}
