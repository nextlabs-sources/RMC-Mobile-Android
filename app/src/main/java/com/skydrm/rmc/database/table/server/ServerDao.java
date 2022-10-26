package com.skydrm.rmc.database.table.server;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.List;

public class ServerDao extends BaseDao<Server> {
    private static final String SQL_CREATE_TABLE_SERVER = "CREATE TABLE IF NOT EXISTS Server(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "router_url    text    NOT NULL default ''," +
            "rms_url    text    NOT NULL default ''," +
            "tenant_id    text    NOT NULL default ''," +
            "is_onpremise    integer    default 0," +
            "reserved1    text    default ''," +
            "reserved2    text    default ''," +

            "UNIQUE(router_url,tenant_id) );";


    public ServerDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    public void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_SERVER);
    }

    /**
     * Insert or update a item
     *
     * @param routerUrl
     * @param rmsUrl
     * @param tenantId
     * @param isOnPremise
     * @return primary key value of current item.
     */
    public int insert(String routerUrl, String rmsUrl,
                      String tenantId, boolean isOnPremise) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Server(router_url,rms_url,tenant_id,is_onpremise,reserved1) VALUES(?,?,?,?,?);",
                new Object[]{routerUrl, rmsUrl, tenantId, isOnPremise ? 1 : 0, 1});

        try (Cursor c = db.rawQuery("SELECT _id FROM Server WHERE router_url = ? AND tenant_id = ?;", new String[]{routerUrl, tenantId})) {
            if (c != null && c.moveToFirst()) {
                return c.getInt(c.getColumnIndexOrThrow("_id"));
            }
        }
        return -1;
    }

    public void update(int _id, String routerUrl, String rmsUrl, String tenantId, boolean isOnPremise) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE Server SET router_url=?,rms_url=?,tenant_id=?,is_onpremise=?,reserved1=1 WHERE _id = ?;",
                new Object[]{routerUrl, rmsUrl, tenantId, isOnPremise, _id});
    }

    public int queryPrimaryKey() {
        SQLiteDatabase db = getReadableDatabase();
        int defaultPK = -1;
        try (Cursor c = db.rawQuery("SELECT _id FROM Server WHERE reserved1 = ?;", new String[]{Integer.toString(1)})) {
            if (c != null && c.moveToFirst()) {
                defaultPK = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
        }
        return defaultPK;
    }

    public void clearLoginStatus(int _id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE Server SET reserved1 = 0 WHERE _id = ?",
                new Object[]{_id});
    }

    public int queryPrimaryKey(String routerUrl, String tenantId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT _id FROM Server WHERE router_url = ? AND tenant_id = ?", new String[]{routerUrl, tenantId})) {
            if (c != null && c.moveToFirst()) {
                return c.getInt(c.getColumnIndexOrThrow("_id"));
            }
        }
        return -1;
    }

    public Server queryServerItem(int _id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM Server WHERE _id = ?", new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                return Server.newByCursor(c);
            }
        }
        throw new IllegalStateException("Fatal error,Illegal cursor performed.");
    }

    public List<Server> queryAll() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM Server", null)) {
            List<Server> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(Server.newByCursor(c));
            }
            return ret;
        }
    }
}
