package com.skydrm.rmc.database.table.membership;

import android.database.Cursor;

public class Membership {
    //Membership table primary key.[auto increment&unique]
    private int _id = -1;
    //User table primary key.[unique]
    private int _user_id = -1;

    public String id;
    public int type;
    public String tokenGroupName;
    public String tenantId;
    public int projectId;

    private String reserved1;
    private String reserved2;

    private Membership() {

    }

    public static Membership newByCursor(Cursor c) {
        Membership m = new Membership();
        m._user_id = c.getInt(c.getColumnIndexOrThrow("_user_id"));
        m.id = c.getString(c.getColumnIndexOrThrow("id"));
        m.type = c.getInt(c.getColumnIndexOrThrow("type"));
        m.tokenGroupName = c.getString(c.getColumnIndexOrThrow("token_group_name"));
        m.tenantId = c.getString(c.getColumnIndexOrThrow("tenant_id"));
        m.projectId = c.getInt(c.getColumnIndexOrThrow("project_id"));
        return m;
    }
}

