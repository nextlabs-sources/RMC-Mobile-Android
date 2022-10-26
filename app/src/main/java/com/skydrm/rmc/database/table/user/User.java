package com.skydrm.rmc.database.table.user;

import android.database.Cursor;

import com.skydrm.rmc.database.table.membership.Membership;
import com.skydrm.rmc.dbbridge.IUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class User {
    //User table primary key.[auto increment&unique]
    public int _id = -1;
    //ServerImpl table primary key.
    public int _server_id = -1;

    public String name;
    public String email;
    public int userId;
    public int idpType;
    public long ttl;
    public String ticket;
    public String tenantId;
    public String tokenGroupName;
    public String defaultTenant;
    public String defaultTenantUrl;
    public List<Membership> memberships;
    public String preferences_raw_json;
    public String user_raw_json;
    public Preference preference;
    public boolean isProjectAdmin;
    public boolean isTenantAdmin;

    private String reserved1;
    private String reserved2;

    private User() {
        preference = new Preference(user_raw_json);
    }

    static User newByCursor(Cursor c) {
        User u = new User();
        u._id = c.getInt(c.getColumnIndexOrThrow("_id"));
        u._server_id = c.getInt(c.getColumnIndexOrThrow("_server_id"));
        u.name = c.getString(c.getColumnIndexOrThrow("name"));
        u.email = c.getString(c.getColumnIndexOrThrow("email"));
        u.userId = c.getInt(c.getColumnIndexOrThrow("rms_user_id"));
        u.idpType = c.getInt(c.getColumnIndexOrThrow("rms_idp_type"));
        u.ttl = c.getLong(c.getColumnIndexOrThrow("rms_ttl"));
        u.ticket = c.getString(c.getColumnIndexOrThrow("rms_ticket"));
        u.tenantId = c.getString(c.getColumnIndexOrThrow("rms_tenant_id"));
        u.tokenGroupName = c.getString(c.getColumnIndexOrThrow("rms_token_group_name"));
        u.defaultTenant = c.getString(c.getColumnIndexOrThrow("rms_default_tenant"));
        u.defaultTenantUrl = c.getString(c.getColumnIndexOrThrow("rms_default_tenant_url"));

        u.preferences_raw_json = c.getString(c.getColumnIndexOrThrow("preferences_raw_json"));
        u.user_raw_json = c.getString(c.getColumnIndexOrThrow("user_raw_json"));
        u.isProjectAdmin = c.getInt(c.getColumnIndexOrThrow("is_project_admin")) == 1;
        u.isTenantAdmin = c.getInt(c.getColumnIndexOrThrow("is_tenant_admin")) == 1;
        return u;
    }

    void paddingMemberships(List<Membership> memberships) {
        this.memberships = memberships;
    }

    static class Preference implements IUser.IPreference {
        private static final String ROOT_NAME = "preferences";
        private static final String SYNC_WITH_RMS_MILLIS = "sync_with_rms_millis";
        private String mRawJson;
        long syncWithRmsMillis;

        Preference(String rawJson) {
            this.mRawJson = rawJson;
            buildFromRawJson(rawJson);
        }

        public void setSyncWithRmsMillis(long syncWithRmsMillis) {
            this.syncWithRmsMillis = syncWithRmsMillis;
        }

        private String toRawJson() {
            JSONObject rootObj = new JSONObject();
            try {
                JSONObject preferenceObj = new JSONObject();
                preferenceObj.put(SYNC_WITH_RMS_MILLIS, syncWithRmsMillis);
                rootObj.put(ROOT_NAME, preferenceObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return rootObj.toString();
        }

        void buildFromRawJson(String rawJson) {
            if (rawJson == null || rawJson.isEmpty()) {
                return;
            }
            if (rawJson.equals("{}")) {
                return;
            }
            try {
                JSONObject rootObj = new JSONObject(rawJson);
                JSONObject preferenceObj = rootObj.optJSONObject(ROOT_NAME);
                this.syncWithRmsMillis = preferenceObj.optLong(SYNC_WITH_RMS_MILLIS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public long getSyncWithRmsTimeMillis() {
            return syncWithRmsMillis;
        }
    }

    public static String toRawJson(long syncRmsTimeMillis) {
        JSONObject rootObj = new JSONObject();
        try {
            JSONObject preferenceObj = new JSONObject();
            preferenceObj.put(Preference.SYNC_WITH_RMS_MILLIS, syncRmsTimeMillis);
            rootObj.put(Preference.ROOT_NAME, preferenceObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rootObj.toString();
    }
}
