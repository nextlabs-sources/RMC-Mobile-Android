package com.skydrm.rmc.database.table.log;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao extends BaseDao<ActivityLogBean> {
    private static final String SQL_CREATE_TABLE_ACTIVITY_LOG = "CREATE TABLE IF NOT EXISTS ActivityLog(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT," +
            "_user_id    integer    NOT NULL," +
            "duid    text    NOT NULL default ''," +
            "operation_id    integer    NOT NULL," +
            "device_type    integer    NOT NULL," +
            "file_name    text    NOT NULL default ''," +
            "file_path    text    NOT NULL default ''," +
            "access_result    integer    NOT NULL," +
            "access_time    long    NOT NULL," +
            "activity_data    text    default ''," +
            "reserved1    text    default ''," +
            "reserved2    text    default ''," +
            "FOREIGN KEY(_user_id) references User(_id) ON DELETE CASCADE)";

    public ActivityLogDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    public void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_ACTIVITY_LOG);
    }

    public void insert(int _user_id, String duid, int operationId,
                       int deviceType, String fileName, String filePath,
                       int accessResult, long accessTime, String activityData) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("INSERT INTO ActivityLog(_user_id,duid,operation_id," +
                        "device_type,file_name,file_path," +
                        "access_result,access_time,activity_data) " +
                        "VALUES(?,?,?,?,?,?,?,?,?)",
                new Object[]{_user_id, duid, operationId,
                        deviceType, fileName, filePath,
                        accessResult, accessTime, activityData});
    }

    public List<ActivityLogBean> queryAll(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM ActivityLog WHERE _user_id=?",
                new String[]{Integer.toString(_user_id)})) {
            List<ActivityLogBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(ActivityLogBean.newByCursor(c));
            }
            return ret;
        }
    }

    public void deleteAll(int _user_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM ActivityLog WHERE _user_id=?", new Object[]{_user_id});
    }
}
