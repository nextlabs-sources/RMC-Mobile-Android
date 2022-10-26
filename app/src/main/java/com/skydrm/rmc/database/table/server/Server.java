package com.skydrm.rmc.database.table.server;

import android.database.Cursor;

public class Server {
    //ServerImpl table primary key.[auto increment&unique]
    public int _id = -1;

    public String routerUrl;
    public String rmsUrl;
    public String tenantId;
    public boolean isOnPremise;

    private String reserved1;
    private String reserved2;

    private Server(int _id, String routerUrl, String rmsUrl, String tenantId, boolean isOnPremise) {
        this._id = _id;
        this.routerUrl = routerUrl;
        this.rmsUrl = rmsUrl;
        this.tenantId = tenantId;
        this.isOnPremise = isOnPremise;
        this.reserved1 = "";
        this.reserved2 = "";
    }

    static Server newByCursor(Cursor c) {
        int _id = c.getInt(c.getColumnIndexOrThrow("_id"));
        String routerUrl = c.getString(c.getColumnIndexOrThrow("router_url"));
        String rmsUrl = c.getString(c.getColumnIndexOrThrow("rms_url"));
        String tenantId = c.getString(c.getColumnIndexOrThrow("tenant_id"));
        boolean isOnPremise = c.getInt(c.getColumnIndexOrThrow("is_onpremise")) == 1;
        return new Server(_id, routerUrl, rmsUrl, tenantId, isOnPremise);
    }
}
