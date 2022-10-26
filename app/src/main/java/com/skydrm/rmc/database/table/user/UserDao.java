package com.skydrm.rmc.database.table.user;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.skydrm.rmc.database.BaseDao;
import com.skydrm.rmc.database.table.membership.Membership;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UserDao extends BaseDao<User> {
    private static final String SQL_CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS User(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "_server_id    integer    NOT NULL," +
            "name    text    NOT NULL default ''," +
            "email    text    NOT NULL default ''," +
            "rms_user_id    integer    default 0," +
            "rms_idp_type    integer    default 0," +
            "rms_ttl    long    default 0," +
            "rms_ticket    text    default ''," +
            "rms_tenant_id    text    default ''," +
            "rms_token_group_name    text    default ''," +
            "rms_default_tenant    text    default ''," +
            "rms_default_tenant_url    text    default ''," +
            "preferences_raw_json    text    default '{}'," +
            "user_raw_json    text    default '{}'," +
            "is_project_admin    integer    default 0," +
            "is_tenant_admin    integer    default 0," +
            "is_login_active    integer    default 0," +
            "last_login_time_millis    long    default 0," +
            "reserved1    text    default ''," +
            "reserved2    text    default ''," +
            "reserved3    text    default ''," +
            "reserved4    text    default ''," +
            "reserved5    text    default ''," +
            "reserved6    text    default ''," +
            "reserved7    text    default ''," +
            "reserved8    text    default ''," +

            "UNIQUE(_server_id,email)," +
            "FOREIGN KEY(_server_id) references Server(_id) ON DELETE CASCADE);";

    public UserDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    public void creteTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_USER);
    }

    public int insert(int _server_id,
                      String name, String email,
                      int userId, int idpType,
                      long ttl, String ticket,
                      String tenantId, String tokenGroupName,
                      String defaultTenant, String defaultTenantUrl,
                      String preferencesRawJson, String userRawJson) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO User(_server_id,name,email,rms_user_id,rms_idp_type,rms_ttl,rms_ticket,rms_tenant_id,rms_token_group_name,rms_default_tenant,rms_default_tenant_url,preferences_raw_json,user_raw_json,is_login_active,last_login_time_millis)" +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{_server_id, name, email, userId, idpType, ttl, ticket, tenantId, tokenGroupName, defaultTenant, defaultTenantUrl, preferencesRawJson, userRawJson, 1, getCurrentTimeMillis()});

        int _id = -1;
        try (Cursor c = db.rawQuery("SELECT _id FROM User WHERE _server_id = ? AND email = ?", new String[]{Integer.toString(_server_id), email})) {
            if (c != null && c.moveToFirst()) {
                _id = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
        }
        insertMembership(db, _id, userRawJson);
        return _id;
    }

    private long getCurrentTimeMillis() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        return calendar.getTimeInMillis();
    }

    public void update(int _id, int _server_id,
                       String name, String email,
                       int userId, int idpType,
                       long ttl, String ticket,
                       String tenantId, String tokenGroupName,
                       String defaultTenant, String defaultTenantUrl,
                       String preferencesRawJson, String usrRawJson) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE User SET _server_id = ?," +
                        "name=?,email=?," +
                        "rms_user_id=?,rms_idp_type=?," +
                        "rms_ttl=?,rms_ticket=?," +
                        "rms_tenant_id=?,rms_token_group_name=?," +
                        "rms_default_tenant=?,rms_default_tenant_url=?," +
                        "preferences_raw_json=?,user_raw_json=?,is_login_active=1 WHERE _id=?",
                new Object[]{_server_id,
                        name, email,
                        userId, idpType,
                        ttl, ticket,
                        tenantId, tokenGroupName,
                        defaultTenant, defaultTenantUrl,
                        preferencesRawJson, usrRawJson, _id});
    }

    private void insertMembership(SQLiteDatabase db, int _id, String userRawJson) {
        try {
            JSONObject jUser = new JSONObject(userRawJson);
            if (jUser.has("memberships")) {
                JSONArray membershipsArr = jUser.getJSONArray("memberships");
                for (int i = 0; i < membershipsArr.length(); i++) {
                    JSONObject mi = membershipsArr.getJSONObject(i);
                    int type = mi.optInt("type");
                    String id = mi.optString("id");
                    String tokenGroupName = mi.optString("tokenGroupName");
                    if (type == 0) { //tenant
                        String tenantId = mi.optString("tenantId");
                        db.execSQL("INSERT INTO Membership(_user_id,id,type,token_group_name,tenant_id)" +
                                "VALUES(?,?,?,?,?)", new Object[]{
                                _id, id, type, tokenGroupName, tenantId});
                    } else if (type == 1) {
                        int projectId = mi.optInt("projectId");
                        db.execSQL("INSERT INTO Membership(_user_id,id,type,token_group_name,project_id)" +
                                "VALUES(?,?,?,?,?)", new Object[]{
                                _id, id, type, tokenGroupName, projectId});
                    } else if (type == 2) {
                        db.execSQL("INSERT INTO Membership(_user_id,id,type,token_group_name,project_id)" +
                                "VALUES(?,?,?,?,?)", new Object[]{
                                _id, id, type, tokenGroupName, -1});
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int queryPrimaryKey(int _server_id, String email) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT _id FROM User WHERE _server_id = ? AND email = ?",
                new String[]{Integer.toString(_server_id), email})) {
            if (c != null && c.moveToFirst()) {
                return c.getInt(c.getColumnIndex("_id"));
            }
        }
        return -1;
    }

    public int queryPrimaryKey() {
        SQLiteDatabase db = getReadableDatabase();
        int defaultPK = -1;
        try (Cursor c = db.rawQuery("SELECT _id FROM User WHERE is_login_active = 1;",
                null)) {
            if (c != null && c.moveToFirst()) {
                defaultPK = c.getInt(c.getColumnIndex("_id"));
            }
        }
        return defaultPK;
    }

    public String queryPreferenceRawJson(int _id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT preferences_raw_json FROM User WHERE _id=?",
                new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndexOrThrow("preferences_raw_json"));
            }
        }
        return "{}";
    }

    public void updateTenantAdmin(int _id, boolean isTenantAdmin) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE User SET is_tenant_admin=? WHERE _id=?",
                new Object[]{isTenantAdmin ? 1 : 0, _id});
    }

    public void updateProjectAdmin(int _id, boolean isProjectAdmin) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE User SET is_project_admin=? WHERE _id=?",
                new Object[]{isProjectAdmin ? 1 : 0, _id});
    }

    public void updateTenantAndProjectAdmin(int _id, boolean isTenantAdmin, boolean isProjectAdmin) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE User SET is_tenant_admin=? AND is_project_admin=? WHERE _id=?",
                new Object[]{isTenantAdmin ? 1 : 0, isProjectAdmin ? 1 : 0, _id});
    }

    public void clearLoginStatus(int _id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE User SET is_login_active = 0 WHERE _id = ?", new Object[]{_id});
    }

    public User query(int _id) {
        SQLiteDatabase db = getReadableDatabase();
        User user = null;
        try (Cursor c = db.rawQuery("SELECT * FROM User WHERE _id = ? ",
                new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                user = User.newByCursor(c);
            }
        }
        if (user == null) {
            throw new IllegalStateException("Fatal error,Illegal cursor performed.");
        }
        try (Cursor c = db.rawQuery("SELECT * FROM Membership WHERE _user_id = ?",
                new String[]{Integer.toString(_id)})) {
            List<Membership> memberships = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                memberships.add(Membership.newByCursor(c));
            }
            user.paddingMemberships(memberships);
            return user;
        }
    }
}
