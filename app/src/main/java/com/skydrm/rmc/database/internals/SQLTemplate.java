package com.skydrm.rmc.database.internals;

/**
 * Created by oye on 12/12/2016.
 */

public class SQLTemplate {
    public static final String DATABASE_DBNAME = "viewer_database.db";
    public static final String DATABASE_TABLE_SERVICE = "bound_service";
    public static final String SQL_CREATE_TABLE_SERVICE =
            "CREATE TABLE " + DATABASE_TABLE_SERVICE +
                    " (" +
                    "service_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "tenant_name TEXT, " +
                    "service_type INTEGER, " +
                    "service_alias TEXT, " +
                    "service_account TEXT, " +
                    "service_account_id TEXT, " +
                    "service_account_token TEXT, " +
                    "selected INTEGER, " +
                    "rms_repo_id TEXT, " +
                    "rms_nick_name TEXT, " +
                    "rms_is_shared INTEGER, " +
                    "rms_token TEXT, " +
                    "rms_is_preference TEXT, " +
                    "rms_creation_time INTEGER64, " +
                    "rms_updated_time INTEGER64" +
                    ");";
}
