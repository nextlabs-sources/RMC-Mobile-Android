package com.skydrm.rmc.dbbridge.server;

import com.skydrm.rmc.database.table.server.Server;
import com.skydrm.rmc.dbbridge.IServer;

public class ServerImpl implements IServer {
    private Server mRaw;

    public ServerImpl(Server raw) {
        this.mRaw = raw;
    }

    @Override
    public String getRouterURL() {
        return mRaw.routerUrl;
    }

    @Override
    public String getRmsURL() {
        return mRaw.rmsUrl;
    }

    @Override
    public String getTenantId() {
        return mRaw.tenantId;
    }

    @Override
    public boolean isOnPremise() {
        return mRaw.isOnPremise;
    }
}
