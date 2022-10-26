package com.skydrm.rmc.database.table.project;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.List;

public class ProjectFileDao extends BaseDao<ProjectFileBean> {
    private static final String SQL_CREATE_TABLE_PROJECT_FILE = "CREATE TABLE IF NOT EXISTS ProjectFile(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "_project_id    integer    NOT NULL," +
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
            "owner_raw_json    text    default '{}'," +
            "last_modified_user_raw_json    text    default '{}'," +
            "is_favorite    integer    default 0," +
            "is_offline    integer    default 0," +
            "modify_rights_status    integer    default 0," +
            "edit_status    integer    default 0," +
            "operation_status    integer    default 0," +
            "local_path    text    default ''," +
            "reserved1    text    default ''," +
            "reserved2    text    default ''," +
            "reserved3    text    default ''," +
            "reserved4    text    default ''," +

            "UNIQUE(_project_id,id)," +
            "FOREIGN KEY(_project_id) references Project(_id) ON DELETE CASCADE);";

    public ProjectFileDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    public void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_PROJECT_FILE);
    }

    public void insert(int _project_id, String id, String duid, String pathDisplay, String pathId,
                       String name, String fileType, long lastModified, long creationTime,
                       long size, boolean isFolder, String ownerRawJson, String lastModifiedUserRawJson) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("INSERT INTO ProjectFile(_project_id,id,duid,path_display,path_id,name," +
                        "file_type,last_modified,creation_time,size,is_folder," +
                        "owner_raw_json,last_modified_user_raw_json) " +
                        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{_project_id, id, duid, pathDisplay, pathId,
                        name, fileType, lastModified, creationTime,
                        size, isFolder, ownerRawJson, lastModifiedUserRawJson});
    }

    public boolean batchInsert(int _project_id, List<ProjectFileBean> inserts) {
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("INSERT INTO " +
                "ProjectFile(_project_id,id,duid,path_display,path_id,name," +
                "file_type,last_modified,creation_time,size,is_folder," +
                "owner_raw_json,last_modified_user_raw_json)" +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
        try {
            wdb.beginTransaction();
            int affected = 0;
            for (ProjectFileBean i : inserts) {
                //Folder contains no DUID.
                statement.clearBindings();
                if (i.isFolder) {
                    statement.bindAllArgsAsStrings(new String[]{Integer.toString(_project_id),
                            i.id, "", i.pathDisplay, i.pathId, i.name, "",
                            Long.toString(i.lastModified),
                            Long.toString(i.creationTime),
                            Long.toString(i.size), Integer.toString(1),
                            i.ownerRawJson, i.lastModifiedUserRawJson});
                } else {
                    statement.bindAllArgsAsStrings(new String[]{Integer.toString(_project_id),
                            i.id, i.duid, i.pathDisplay, i.pathId, i.name, i.fileType,
                            Long.toString(i.lastModified), Long.toString(i.creationTime),
                            Long.toString(i.size), Integer.toString(0),
                            i.ownerRawJson, i.lastModifiedUserRawJson});
                }

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

    public void update(int _id, String id, String duid, String pathDisplay, String pathId,
                       String name, String fileType, long lastModified, long creationTime,
                       long size, boolean isFolder, String ownerRawJson, String lastModifiedUserRawJson) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFile SET id=?,duid=?,path_display=?,path_id=?,name=?," +
                        "file_type=?,last_modified=?,creation_time=?,size=?," +
                        "is_folder=?,owner_raw_json=?,last_modified_user_raw_json=?" +
                        " WHERE _id=?",
                new Object[]{id, duid, pathDisplay, pathId,
                        name, fileType, lastModified, creationTime,
                        size, isFolder, ownerRawJson, lastModifiedUserRawJson, _id});
    }

    public void updateLocalPath(int _id, String localPath) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFile SET local_path=? WHERE _id=?",
                new Object[]{localPath, _id});
    }

    public void updateOperationStatus(int _id, int status) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFile SET operation_status=? WHERE _id=?",
                new Object[]{status, _id});
    }

    public void update(int _id, int rights, String obligationRaw) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFile SET reserved1=?,reserved2=? WHERE _id=?",
                new Object[]{rights, obligationRaw, _id});
    }

    public void updateResetAllLocalPath(int _project_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFile SET local_path=? WHERE _project_id=?",
                new Object[]{"", _project_id});
    }

    public void updateResetAllOperationStatus(int _project_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFile SET operation_status=-1 WHERE _project_id=?",
                new Object[]{_project_id});
    }

    public void updateOffline(int _id, boolean offline) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFile SET is_offline=? WHERE _id=?",
                new Object[]{offline ? 1 : 0, _id});
    }

    public void updateLastModifiedTime(int _id, long lastModifiedTime) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFile SET last_modified=? WHERE _id=?",
                new Object[]{lastModifiedTime, _id});
    }

    public void updateFavorite(int _id, boolean favorite) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFile SET is_favorite=? WHERE _id=?",
                new Object[]{favorite ? 1 : 0});
    }

    public void batchUpdate(List<ProjectFileBean> updates) {
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("UPDATE ProjectFile SET " +
                "id=?,duid=?,path_display=?,path_id=?,name=?," +
                "file_type=?,last_modified=?,creation_time=?,size=?," +
                "is_folder=?,owner_raw_json=?,last_modified_user_raw_json=?" +
                " WHERE _id=?");
        try {
            wdb.beginTransaction();
            for (ProjectFileBean i : updates) {
                if (i._id == -1) {
                    continue;
                }
                statement.clearBindings();
                if (i.isFolder) {
                    statement.bindAllArgsAsStrings(new String[]{i.id, "", i.pathDisplay, i.pathId,
                            i.name, "", Long.toString(i.lastModified), Long.toString(i.creationTime),
                            Long.toString(i.size), Integer.toString(i.isFolder ? 1 : 0),
                            i.ownerRawJson, i.lastModifiedUserRawJson, Integer.toString(i._id)});
                } else {
                    statement.bindAllArgsAsStrings(new String[]{i.id, i.duid, i.pathDisplay, i.pathId,
                            i.name, i.fileType, Long.toString(i.lastModified), Long.toString(i.creationTime),
                            Long.toString(i.size), Integer.toString(i.isFolder ? 1 : 0),
                            i.ownerRawJson, i.lastModifiedUserRawJson, Integer.toString(i._id)});
                }
                statement.executeUpdateDelete();
            }
            wdb.setTransactionSuccessful();
        } finally {
            wdb.endTransaction();
        }
    }

    public int queryPrimaryKey(int _project_id, String id) {
        int _id = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT _id FROM ProjectFile WHERE _project_id=? AND id=?",
                new String[]{Integer.toString(_project_id), id})) {
            if (c != null && c.moveToFirst()) {
                _id = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
            return _id;
        }
    }

    public List<ProjectFileBean> queryAll(int _project_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM ProjectFile WHERE _project_id=?",
                new String[]{Integer.toString(_project_id)})) {
            List<ProjectFileBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(ProjectFileBean.newByCursor(c));
            }
            return ret;
        }
    }

    public List<ProjectFileBean> queryRecent(int _project_id, int limit) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM ProjectFile WHERE _project_id=? AND is_folder=0 ORDER BY last_modified DESC LIMIT ?",
                new String[]{Integer.toString(_project_id), Integer.toString(limit)})) {
            List<ProjectFileBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(ProjectFileBean.newByCursor(c));
            }
            return ret;
        }
    }

    public int queryRights(int _id) {
        int rights = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT reserved1 FROM ProjectFile WHERE _id=?",
                new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                String reserved1 = c.getString(c.getColumnIndexOrThrow("reserved1"));
                if (reserved1 != null && !reserved1.isEmpty()) {
                    rights = Integer.valueOf(reserved1);
                }
            }
            return rights;
        }
    }

    public String queryObligation(int _id) {
        String obligation = "";
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT reserved2 FROM ProjectFile WHERE _id=?",
                new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                obligation = c.getString(c.getColumnIndexOrThrow("reserved2"));
            }
            return obligation;
        }
    }

    public void deleteOne(int _id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM ProjectFile WHERE _id=?",
                new Object[]{_id});
    }

    public void deleteOne(int _project_id, String pathId) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM ProjectFile WHERE _project_id=? AND path_id=?",
                new Object[]{_project_id, pathId});
    }

    public boolean batchDelete(List<Integer> _ids) {
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("DELETE FROM ProjectFile WHERE _id=?");
        try {
            wdb.beginTransaction();
            int affected = 0;
            for (int _id : _ids) {
                if (_id == -1) {
                    continue;
                }
                statement.clearBindings();
                statement.bindAllArgsAsStrings(new String[]{Integer.toString(_id)});
                if (statement.executeUpdateDelete() > 0) {
                    affected++;
                }
            }
            wdb.setTransactionSuccessful();
            return affected > 0;
        } finally {
            wdb.endTransaction();
        }
    }

    public void deleteAll(int _project_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM ProjectFile WHERE _project_id=?",
                new Object[]{_project_id});
    }
}
