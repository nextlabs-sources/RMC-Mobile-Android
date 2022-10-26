package com.skydrm.rmc.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.skydrm.rmc.reposystem.types.BoundService;

/**
 * Created by oye on 12/13/2016.
 */

public class RepoEntry {
    //public int id;     // INTEGER PRIMARY KEY AUTOINCREMENT
    // login user
    public String tenant;
    public int userId;
    // local repo info
    public int repo_type;
    public String repo_alias;
    public String repo_account;
    public String repo_account_id;
    public String repo_account_token;
    public boolean repo_select_status;         // true: selected  flase: not-select
    // rms repo info
    public String rms_repo_id;     // GUID
    public String rms_nick_name;
    public String rms_repo_token;
    public boolean rms_is_shared;
    public String rms_is_perference;
    public long rms_creation_time;
    public long rms_updated_time;

    public int getUserId() {
        return userId;
    }

    public String getTenant() {
        return tenant;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append("Repo:[")
                .append(tenant + "/" + userId + " ")
                .append(repo_type + ":" + repo_alias + " ")
                .append(repo_account + " ")
                .append("select:" + repo_select_status + " ")
                .append("rmsID:" + rms_repo_id + " ")
                .append("rmsName:" + rms_nick_name + " ")
                .append("]");
        return sb.toString();

    }

    public BoundService convert() {
        BoundService service = new BoundService(
                BoundService.ServiceType.valueOf(repo_type),
                repo_alias,
                repo_account,
                repo_account_id,
                repo_account_token,
                repo_select_status ? 1 : 0,
                rms_repo_id,
                rms_nick_name,
                rms_is_shared,
                rms_repo_token,
                rms_is_perference,
                rms_creation_time
        );
        return service;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put("user_id", this.userId);
        values.put("tenant_name", this.tenant);
        values.put("service_type", this.repo_type);
        values.put("service_alias", this.repo_alias);
        values.put("service_account", this.repo_account);
        values.put("service_account_id", this.repo_account_id);
        values.put("service_account_token", this.repo_account_token);
        values.put("selected", this.repo_select_status ? 1 : 0);
        values.put("rms_repo_id", this.rms_repo_id);
        values.put("rms_nick_name", this.rms_nick_name);
        values.put("rms_is_shared", this.rms_is_shared);
        values.put("rms_token", this.rms_repo_token);
        values.put("rms_is_preference", this.rms_is_perference);
        values.put("rms_creation_time", this.rms_creation_time);
        values.put("rms_updated_time", this.rms_updated_time);
        return values;
    }


    public static RepoEntry Builder(Cursor c) {
        try {
            RepoEntry entry = new RepoEntry();
            // id
//            entry.id = c.getInt(c.getColumnIndex("service_id"));
            // user info
            entry.tenant = c.getString(c.getColumnIndex("tenant_name"));
            entry.userId = c.getInt(c.getColumnIndex("user_id"));
            // local repo
            entry.repo_type = c.getInt(c.getColumnIndex("service_type"));
            entry.repo_alias = c.getString(c.getColumnIndex("service_alias"));
            entry.repo_account = c.getString(c.getColumnIndex("service_account"));
            entry.repo_account_id = c.getString(c.getColumnIndex("service_account_id"));
            entry.repo_account_token = c.getString(c.getColumnIndex("service_account_token"));
            entry.repo_select_status = c.getInt(c.getColumnIndex("selected")) == 1;
            // rms repo
            entry.rms_repo_id = c.getString(c.getColumnIndex("rms_repo_id"));
            entry.rms_nick_name = c.getString(c.getColumnIndex("rms_nick_name"));
            entry.rms_repo_token = c.getString(c.getColumnIndex("rms_token"));
            entry.rms_is_shared = c.getInt(c.getColumnIndex("rms_is_shared")) == 1;
            entry.rms_is_perference = c.getString(c.getColumnIndex("rms_is_preference"));
            entry.rms_creation_time = c.getLong(c.getColumnIndex("rms_creation_time"));
            entry.rms_updated_time = c.getLong(c.getColumnIndex("rms_updated_time"));
            return entry;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("failed to build repo entry", e);
        }

    }

}
