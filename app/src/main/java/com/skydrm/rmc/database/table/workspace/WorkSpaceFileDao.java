package com.skydrm.rmc.database.table.workspace;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.List;

public class WorkSpaceFileDao extends BaseDao<WorkSpaceFileBean> {
    private static final String SQL_CREATE_TABLE_WORKSPACE_FILE = "CREATE TABLE IF NOT EXISTS WorkSpaceFile(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "_user_id    integer    NOT NULL," +
            "id    text    default ''," +
            "duid    text    default ''," +
            "path_display    text    default ''," +
            "path_id    text    default ''," +
            "name    text    default ''," +
            "file_type    text    default ''," +
            "last_modified    long    default 0," +
            "creation_time    long    default 0," +
            "size    long    default 0," +
            "is_folder    integer    default 0," +
            "uploader_raw_json    text    default '{}'," +
            "last_modified_user_raw_json    text    default '{}'," +
            "is_favorite    integer    default 0," +
            "is_offline    integer    default 0," +
            "modify_rights_status    integer    default 0," +
            "edit_status    integer    default 0," +
            "operation_status    integer    default 0," +
            "local_path    text    default ''," +
            "offline_rights    text    default ''," +
            "offline_obligations    text    default ''," +
            "reserved1    text    default ''," +
            "reserved2    text    default ''," +
            "reserved3    text    default ''," +
            "reserved4    text    default ''," +
            "reserved5    text    default ''," +
            "reserved6    text    default ''," +
            "reserved7    text    default ''," +
            "reserved8    text    default ''," +
            "UNIQUE(_id,duid)," +
            "FOREIGN KEY(_user_id) references User(_id) ON DELETE CASCADE);";

    public WorkSpaceFileDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    public void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_WORKSPACE_FILE);
    }

    public boolean insert(int _user_id, String id, String duid,
                          String pathDisplay, String pathId,
                          String name, String fileType,
                          long lastModified, long creationTime, long size,
                          boolean isFolder, String uploaderRawJson, String lastModifiedUserRawJson) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("INSERT INTO WorkSpaceFile(_user_id,id,duid," +
                        "path_display,path_id," +
                        "name,file_type," +
                        "last_modified,creation_time,size," +
                        "is_folder,uploader_raw_json,last_modified_user_raw_json) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{_user_id, id, duid,
                        pathDisplay, pathId,
                        name, fileType,
                        lastModified, creationTime, size,
                        isFolder ? 1 : 0, uploaderRawJson, lastModifiedUserRawJson});
        return true;
    }

    public boolean batchInsert(int _user_id, List<WorkSpaceFileBean> inserts) {
        if (inserts == null || inserts.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("INSERT INTO WorkSpaceFile(_user_id,id,duid," +
                "path_display,path_id," +
                "name,file_type," +
                "last_modified,creation_time,size," +
                "is_folder,uploader_raw_json,last_modified_user_raw_json) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
        wdb.beginTransaction();
        try {
            int affected = 0;
            for (WorkSpaceFileBean f : inserts) {
                if (f == null) {
                    continue;
                }
                statement.clearBindings();
                statement.bindAllArgsAsStrings(new String[]{String.valueOf(_user_id), f.id, f.duid,
                        f.pathDisplay, f.pathId,
                        f.name, f.fileType,
                        String.valueOf(f.lastModified), String.valueOf(f.creationTime), String.valueOf(f.size),
                        String.valueOf(f.isFolder ? 1 : 0), f.uploaderRawJson, f.lastModifiedUserRawJson});
                if (statement.executeInsert() != -1) {
                    affected++;
                }
            }
            wdb.setTransactionSuccessful();
            return affected > 0;
        } finally {
            wdb.endTransaction();
        }
    }

    public boolean update(int _id, String id, String duid, String pathDisplay, String pathId,
                          String name, String fileType,
                          long lastModified, long creationTime, long size,
                          boolean isFolder, String uploaderRawJson, String lastModifiedUserRawJson) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE WorkSpaceFile SET id=?,duid=?,path_display=?,path_id=?," +
                        "name=?,file_type=?," +
                        "last_modified=?,creation_time=?,size=?," +
                        "is_folder=?,uploader_raw_json=?,last_modified_user_raw_json=?" +
                        " WHERE _id=?",
                new Object[]{id, duid, pathDisplay, pathId,
                        name, fileType,
                        lastModified, creationTime, size,
                        isFolder, uploaderRawJson, lastModifiedUserRawJson, _id});
        return true;
    }

    public boolean batchUpdate(List<WorkSpaceFileBean> updates) {
        if (updates == null || updates.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement(
                "UPDATE WorkSpaceFile SET id=?,duid=?,path_display=?,path_id=?," +
                        "name=?,file_type=?," +
                        "last_modified=?,creation_time=?,size=?," +
                        "is_folder=?,uploader_raw_json=?,last_modified_user_raw_json=? " +
                        "WHERE _id=?");
        wdb.beginTransaction();
        try {
            int updateAffected = 0;
            for (WorkSpaceFileBean f : updates) {
                if (f == null) {
                    continue;
                }
                statement.clearBindings();
                statement.bindAllArgsAsStrings(new String[]{f.id, f.duid, f.pathDisplay, f.pathId,
                        f.name, f.fileType,
                        String.valueOf(f.lastModified), String.valueOf(f.creationTime), String.valueOf(f.size),
                        String.valueOf(f.isFolder ? 1 : 0), f.uploaderRawJson, f.lastModifiedUserRawJson,
                        String.valueOf(f._id)});

                if (statement.executeUpdateDelete() > 0) {
                    updateAffected++;
                }
            }
            wdb.setTransactionSuccessful();
            return updateAffected > 0;
        } finally {
            wdb.endTransaction();
        }
    }

    public void updateResetAllOperationStatus(int _user_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE WorkSpaceFile SET operation_status=-1 WHERE _user_id=?",
                new Object[]{_user_id});
    }

    public void update(int _id, String localPath) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE WorkSpaceFile SET local_path=? WHERE _id=?",
                new Object[]{localPath, _id});
    }

    public void update(int _id, boolean offline) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE WorkSpaceFile SET is_offline=? WHERE _id=?",
                new Object[]{offline ? 1 : 0, _id});
    }

    public void update(int _id, int operationStatus) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE WorkSpaceFile SET operation_status=? WHERE _id=?",
                new Object[]{operationStatus, _id});
    }

    public void update(int _id, int rights, String obligations) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE WorkSpaceFile SET offline_rights=?,offline_obligations=? WHERE _id=?",
                new Object[]{rights, obligations, _id});
    }

    public int queryPrimaryKey(int _user_id, String id) {
        int _id = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT _id FROM WorkSpaceFile WHERE _user_id=? AND id=?",
                new String[]{Integer.toString(_user_id), id})) {
            if (c != null && c.moveToFirst()) {
                _id = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
            return _id;
        }
    }

    public int queryOfflineRights(int _id) {
        SQLiteDatabase rdb = getReadableDatabase();
        int ret = -1;
        try (Cursor c = rdb.rawQuery("SELECT offline_rights FROM WorkSpaceFile WHERE _id=?",
                new String[]{String.valueOf(_id)})) {
            if (c != null && c.moveToFirst()) {
                String rights = c.getString(c.getColumnIndexOrThrow("offline_rights"));
                if (rights == null || rights.isEmpty()) {
                    return ret;
                }
                ret = Integer.valueOf(rights);
            }
        }
        return ret;
    }

    public String queryOfflineObligations(int _id) {
        SQLiteDatabase rdb = getReadableDatabase();
        String ret = "";
        try (Cursor c = rdb.rawQuery("SELECT offline_obligations FROM WorkSpaceFile WHERE _id=?",
                new String[]{String.valueOf(_id)})) {
            if (c != null && c.moveToFirst()) {
                ret = c.getString(c.getColumnIndexOrThrow("offline_obligations"));
            }
        }
        return ret;
    }

    public List<WorkSpaceFileBean> queryAll(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM WorkSpaceFile WHERE _user_id=?",
                new String[]{String.valueOf(_user_id)})) {
            List<WorkSpaceFileBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(WorkSpaceFileBean.newByCursor(c));
            }
            return ret;
        }
    }

    public boolean delete(int _id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM WorkSpaceFile WHERE _id=?",
                new Object[]{_id});
        return true;
    }

    public boolean delete(int _user_id, String pathId) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM WorkSpaceFile WHERE _user_id=? AND path_id=?",
                new Object[]{_user_id, pathId});
        return true;
    }

    public boolean batchDelete(List<Integer> deletes) {
        if (deletes == null || deletes.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("DELETE FROM WorkSpaceFile WHERE _id=?");
        wdb.beginTransaction();
        int deleteAffected = 0;
        try {
            for (Integer id : deletes) {
                statement.clearBindings();
                statement.bindAllArgsAsStrings(new String[]{String.valueOf(id)});
                if (statement.executeUpdateDelete() > 0) {
                    deleteAffected++;
                }
            }
            wdb.setTransactionSuccessful();
            return deleteAffected > 0;
        } finally {
            wdb.endTransaction();
        }
    }

    public void updateLastModifiedTime(int _id, long lastModifiedTime) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE WorkSpaceFile SET last_modified=? WHERE _id=?",
                new Object[]{lastModifiedTime, _id});
    }
}
