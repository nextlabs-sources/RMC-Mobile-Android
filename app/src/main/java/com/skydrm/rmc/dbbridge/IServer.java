package com.skydrm.rmc.dbbridge;

public interface IServer {
    String getRouterURL();

    String getRmsURL();

    String getTenantId();

    boolean isOnPremise();
}
