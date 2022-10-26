package com.skydrm.rmc.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.skydrm.rmc.database.internals.SQLTemplate;
import com.skydrm.rmc.reposystem.types.BoundService;

import java.util.ArrayList;
import java.util.List;


public class Database extends SQLiteOpenHelper {
    public static final String TAG = "Database";
    private static final String DATABASE_DBNAME = SQLTemplate.DATABASE_DBNAME;
    private static final String DATABASE_TABLE_SERVICE = SQLTemplate.DATABASE_TABLE_SERVICE;
    private static final int DATABASE_VERSION = 3;
    private static final String SQL_SET_ON_FOREIGN_KEY = "PRAGMA foreign_keys=ON;";
    private static final String SQL_CREATE_TABLE_SERVICE = SQLTemplate.SQL_CREATE_TABLE_SERVICE;

    public Database(Context context) {
        super(context, DATABASE_DBNAME, null, DATABASE_VERSION);
    }


    /*
       Callback when first create db, good place to create tables
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_SET_ON_FOREIGN_KEY);
        // create table
        db.execSQL(SQL_CREATE_TABLE_SERVICE);
        //db.execSQL(SQL_CREATE_CACHE_FILE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public List<RepoEntry> queryAll() {
        List<RepoEntry> entries = new ArrayList<>();
        Cursor c = getReadableDatabase().query(DATABASE_TABLE_SERVICE,
                null, null, null, null, null, null);
        try {
            while (c.moveToNext()) {
                RepoEntry e = RepoEntry.Builder(c);
                entries.add(e);
            }
        } finally {
            c.close();
        }
        return entries;
    }

    public boolean addService(int userId, String tenantName, BoundService.ServiceType type,
                              String alias, String account, String accountId,
                              String accountToken, int selected) {
        ContentValues values = new ContentValues();
//        values.put("service_id","null");
        values.put("user_id", userId);
        values.put("tenant_name", tenantName);
        values.put("service_type", type.value());
        values.put("service_alias", alias);
        values.put("service_account", account);
        values.put("service_account_id", accountId);
        values.put("service_account_token", accountToken);
        values.put("selected", selected);
        return -1 != getWritableDatabase().insert(DATABASE_TABLE_SERVICE, null, values);
    }


    public boolean insert(RepoEntry e) {
        return -1 != getWritableDatabase().insert(DATABASE_TABLE_SERVICE, null, e.toContentValues());
    }


    public boolean updateRepoToken(int userId, String tenantName, BoundService s) {
        ContentValues values = new ContentValues();
        values.put("service_account_token", s.accountToken);
        values.put("rms_token", s.rmsToken);

        return -1 != getWritableDatabase().update(DATABASE_TABLE_SERVICE, values,
                "user_id=? and " +
                        "tenant_name =? and " +
                        "service_type = ? and " +
                        "service_account_id =?",
                new String[]{
                        Integer.toString(userId),
                        tenantName,
                        Integer.toString(s.type.value()),
                        s.accountID
                });

    }

    public boolean updateRepoSelected(int userId, String tenantName, BoundService service) {
        ContentValues values = new ContentValues();
        values.put("selected", service.selected);

        return -1 != getWritableDatabase().update(DATABASE_TABLE_SERVICE, values,
                "user_id=? and " +
                        "tenant_name =? and " +
                        "service_type = ? and " +
                        "service_account_token =?",
                new String[]{
                        Integer.toString(userId),
                        tenantName,
                        Integer.toString(service.type.value()),
                        service.accountToken});
    }

    public boolean updateRepoNickName(int userId, String tenantName, BoundService service) {
        ContentValues values = new ContentValues();
        values.put("rms_nick_name", service.rmsNickName);

        return -1 != getWritableDatabase().update(DATABASE_TABLE_SERVICE, values,
                "user_id=? and " +
                        "tenant_name =? and " +
                        "service_type = ? and " +
                        "service_account_token =?",
                new String[]{
                        Integer.toString(userId),
                        tenantName,
                        Integer.toString(service.type.value()),
                        service.accountToken});
    }

    public boolean updateRepoRmsID(int userId, String tenantName, BoundService service) {
        ContentValues values = new ContentValues();
        values.put("rms_repo_id", service.rmsRepoId);
        return -1 != getWritableDatabase().update(DATABASE_TABLE_SERVICE, values,
                "user_id=? and " +
                        "tenant_name =? and " +
                        "service_type = ? and " +
                        "service_account_token =?",
                new String[]{
                        Integer.toString(userId),
                        tenantName,
                        Integer.toString(service.type.value()),
                        service.accountToken});
    }

    public boolean delService(int userId, String tenantName, int repoType, String accountToken) {
        if (accountToken != null) {
            return -1 != getWritableDatabase().delete(DATABASE_TABLE_SERVICE,
                    "user_id=? and " +
                            "tenant_name =? and " +
                            "service_type = ? and " +
                            "service_account_token =?",
                    new String[]{
                            Integer.toString(userId),
                            tenantName,
                            Integer.toString(repoType),
                            accountToken});
        } else {
            return -1 != getWritableDatabase().delete(DATABASE_TABLE_SERVICE,
                    "user_id=? and " +
                            "tenant_name =? and " +
                            "service_type = ? and " +
                            "service_account_token is null",
                    new String[]{
                            Integer.toString(userId),
                            tenantName,
                            Integer.toString(repoType)});
        }
    }
}
