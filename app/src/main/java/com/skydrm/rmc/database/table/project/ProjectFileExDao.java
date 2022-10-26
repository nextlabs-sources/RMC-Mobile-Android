package com.skydrm.rmc.database.table.project;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.List;

public class ProjectFileExDao extends BaseDao<ProjectFileExBean> {
    private static final String SQL_CREATE_TABLE_PROJECT_FILE_EX = "CREATE TABLE IF NOT EXISTS ProjectFileEX(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "_project_file_id    integer    NOT NULL," +
            "id    text    default ''," +
            "is_shared    integer    default 0," +
            "is_revoked    integer    default 0," +
            "share_with_project_raw_json    text    default '{}'," +
            "share_with_person_raw_json    text    default '{}'," +
            "reserved1    text    default ''," +
            "reserved2    text    default ''," +
            "reserved3    text    default ''," +
            "reserved4    text    default ''," +
            "reserved5    text    default ''," +
            "reserved6    text    default ''," +
            "reserved7    text    default ''," +
            "reserved8    text    default ''," +
            "reserved9    text    default ''," +
            "reserved10    text    default ''," +
            "reserved11    text    default ''," +
            "reserved12    text    default ''," +

            "UNIQUE(_project_file_id,id)," +
            "FOREIGN KEY(_project_file_id) references ProjectFile(_id) ON DELETE CASCADE);";

    public ProjectFileExDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    public void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_PROJECT_FILE_EX);
    }

    public void insert(int _project_id, String id, boolean isShared, boolean isRevoked,
                       String shareWithProjectRawJson, String shareWithPersonRawJson) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("INSERT INTO ProjectFileEX(_project_file_id,id,is_shared,is_revoked," +
                        "share_with_project_raw_json,share_with_person_raw_json) " +
                        "VALUES((SELECT _id FROM ProjectFile WHERE _project_id=? AND id=?),?,?,?,?,?)",
                new Object[]{_project_id, id, id, isShared ? 1 : 0, isRevoked ? 1 : 0,
                        shareWithProjectRawJson, shareWithPersonRawJson});
    }

    public boolean batchInsert(int _project_id, List<ProjectFileExBean> inserts) {
        if (inserts == null || inserts.isEmpty()) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("INSERT INTO " +
                "ProjectFileEX(_project_file_id,id,is_shared,is_revoked," +
                "share_with_project_raw_json,share_with_person_raw_json) " +
                "VALUES((SELECT _id FROM ProjectFile WHERE _project_id=? AND id=?),?,?,?,?,?)");

        try {
            wdb.beginTransaction();

            int affected = 0;
            for (ProjectFileExBean i : inserts) {
                statement.clearBindings();

                statement.bindAllArgsAsStrings(new String[]{String.valueOf(_project_id), i.id, i.id,
                        Integer.toString(i.isShared ? 1 : 0),
                        Integer.toString(i.isRevoked ? 1 : 0),
                        i.shareWithProjectRawJson, i.shareWithPersonRawJson});

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

    public void update(int _id, boolean isShared, boolean isRevoked,
                       String shareWithProjectRawJson, String shareWithPersonRawJson) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFileEX SET is_shared=?,is_revoked=?,share_with_project_raw_json=?," +
                        "share_with_person_raw_json=? WHERE _id=?",
                new Object[]{isShared ? 1 : 0, isRevoked ? 1 : 0,
                        shareWithProjectRawJson, shareWithPersonRawJson, _id});
    }

    public void updateShareStatus(int _project_file_id, boolean isShared) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFileEX SET is_shared=? WHERE _project_file_id=?",
                new Object[]{isShared ? 1 : 0, _project_file_id});
    }

    public void updateRevokeStatus(int _project_file_id, boolean isRevoked) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFileEX SET is_revoked=? WHERE _project_file_id=?",
                new Object[]{isRevoked ? 1 : 0, _project_file_id});
    }

    public void updateShareWithProject(int _project_file_id, String shareWithProjectRawJson) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectFileEX SET share_with_project_raw_json=? WHERE _project_file_id=?",
                new Object[]{shareWithProjectRawJson, _project_file_id});
    }

    public int queryPrimaryKey(int _project_id, String id) {
        int _id = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT _id FROM ProjectFileEX WHERE id=? AND _project_file_id=((SELECT _id FROM ProjectFile WHERE _project_id=? AND id=?))",
                new String[]{id, String.valueOf(_project_id), id})) {
            if (c != null && c.moveToFirst()) {
                _id = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
            return _id;
        }
    }

    public List<ProjectFileExBean> queryAll(int _project_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT ProjectFile._id,ProjectFile._project_id," +
                        "ProjectFile.id,ProjectFile.duid,ProjectFile.path_display,ProjectFile.path_id," +
                        "ProjectFile.name,ProjectFile.file_type,ProjectFile.last_modified,ProjectFile.creation_time," +
                        "ProjectFile.size,ProjectFile.is_folder,ProjectFile.owner_raw_json,ProjectFile.last_modified_user_raw_json," +
                        "ProjectFile.is_favorite,ProjectFile.is_offline,ProjectFile.modify_rights_status," +
                        "ProjectFile.edit_status,ProjectFile.operation_status,ProjectFile.local_path," +
                        "ProjectFileEX._project_file_id,ProjectFileEX.is_shared,ProjectFileEX.is_revoked," +
                        "ProjectFileEX.share_with_project_raw_json,ProjectFileEX.share_with_person_raw_json " +

                        "FROM ProjectFile,ProjectFileEX WHERE _project_id=? " +
                        "AND ProjectFileEX._project_file_id = ProjectFile._id",
                new String[]{Integer.toString(_project_id)})) {
            List<ProjectFileExBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(ProjectFileExBean.newByCursor(c));
            }
            return ret;
        }
    }

    public List<ProjectFileExBean> queryRecent(int _project_id, int limit) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT ProjectFile._id,ProjectFile._project_id," +
                        "ProjectFile.id,ProjectFile.duid,ProjectFile.path_display,ProjectFile.path_id," +
                        "ProjectFile.name,ProjectFile.file_type,ProjectFile.last_modified,ProjectFile.creation_time," +
                        "ProjectFile.size,ProjectFile.is_folder,ProjectFile.owner_raw_json,ProjectFile.last_modified_user_raw_json," +
                        "ProjectFile.is_favorite,ProjectFile.is_offline,ProjectFile.modify_rights_status," +
                        "ProjectFile.edit_status,ProjectFile.operation_status,ProjectFile.local_path," +
                        "ProjectFileEX._project_file_id,ProjectFileEX.is_shared,ProjectFileEX.is_revoked," +
                        "ProjectFileEX.share_with_project_raw_json,ProjectFileEX.share_with_person_raw_json " +

                        "FROM ProjectFile,ProjectFileEX WHERE _project_id=? " +
                        "AND ProjectFileEX._project_file_id = ProjectFile._id " +
                        "AND is_folder=0 ORDER BY last_modified DESC LIMIT ?",
                new String[]{Integer.toString(_project_id), Integer.toString(limit)})) {
            List<ProjectFileExBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(ProjectFileExBean.newByCursor(c));
            }
            return ret;
        }
    }

    public List<ProjectFileExBean> queryAllShared(int _project_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT ProjectFile._id,ProjectFile._project_id," +
                        "ProjectFile.id,ProjectFile.duid,ProjectFile.path_display,ProjectFile.path_id," +
                        "ProjectFile.name,ProjectFile.file_type,ProjectFile.last_modified,ProjectFile.creation_time," +
                        "ProjectFile.size,ProjectFile.is_folder,ProjectFile.owner_raw_json,ProjectFile.last_modified_user_raw_json," +
                        "ProjectFile.is_favorite,ProjectFile.is_offline,ProjectFile.modify_rights_status," +
                        "ProjectFile.edit_status,ProjectFile.operation_status,ProjectFile.local_path," +
                        "ProjectFileEX._project_file_id,ProjectFileEX.is_shared,ProjectFileEX.is_revoked," +
                        "ProjectFileEX.share_with_project_raw_json,ProjectFileEX.share_with_person_raw_json " +

                        "FROM ProjectFile,ProjectFileEX WHERE _project_id=? " +
                        "AND ProjectFileEX._project_file_id = ProjectFile._id " +
                        "AND is_shared=1",
                new String[]{Integer.toString(_project_id)})) {
            List<ProjectFileExBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(ProjectFileExBean.newByCursor(c));
            }
            return ret;
        }
    }

    public List<ProjectFileExBean> queryActiveShared(int _project_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT ProjectFile._id,ProjectFile._project_id," +
                        "ProjectFile.id,ProjectFile.duid,ProjectFile.path_display,ProjectFile.path_id," +
                        "ProjectFile.name,ProjectFile.file_type,ProjectFile.last_modified,ProjectFile.creation_time," +
                        "ProjectFile.size,ProjectFile.is_folder,ProjectFile.owner_raw_json,ProjectFile.last_modified_user_raw_json," +
                        "ProjectFile.is_favorite,ProjectFile.is_offline,ProjectFile.modify_rights_status," +
                        "ProjectFile.edit_status,ProjectFile.operation_status,ProjectFile.local_path," +
                        "ProjectFileEX._project_file_id,ProjectFileEX.is_shared,ProjectFileEX.is_revoked," +
                        "ProjectFileEX.share_with_project_raw_json,ProjectFileEX.share_with_person_raw_json " +

                        "FROM ProjectFile,ProjectFileEX WHERE _project_id=? " +
                        "AND ProjectFileEX._project_file_id = ProjectFile._id " +
                        "AND is_shared=1 " +
                        "AND is_revoked=0",
                new String[]{Integer.toString(_project_id)})) {
            List<ProjectFileExBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(ProjectFileExBean.newByCursor(c));
            }
            return ret;
        }
    }

}
