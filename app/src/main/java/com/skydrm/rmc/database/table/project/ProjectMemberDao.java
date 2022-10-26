package com.skydrm.rmc.database.table.project;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.List;

class ProjectMemberDao extends BaseDao<ProjectMemberBean> {
    private static final String SQL_CREATE_TABLE_PROJECT_MEMBER = "CREATE TABLE IF NOT EXISTS ProjectMember(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "_project_id    integer    NOT NULL," +
            "user_id    integer    NOT NULL," +
            "display_name    text    NOT NULL," +
            "email    text    NOT NULL," +
            "creation_time    long    NOT NULL default 0," +
            "reserved1    text    default ''," +
            "reserved2    text    default ''," +

            "UNIQUE(_project_id,user_id)," +
            "FOREIGN KEY(_project_id) references Project(_id) ON DELETE CASCADE);";

    ProjectMemberDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_PROJECT_MEMBER);
    }

    void insert(int _project_id, int userId, String displayName, String email, long creationTime) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("INSERT INTO ProjectMember(_project_id,user_id,display_name,email,creation_time)" +
                        " VALUES (?,?,?,?,?)",
                new Object[]{_project_id, userId, displayName, email, creationTime});
    }

    void update(int _id, int userId, String displayName, String email, long creationTime) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE ProjectMember SET user_id=?,display_name=?,email=?,creation_time=?" +
                        " WHERE _id=?",
                new Object[]{userId, displayName, email, creationTime, _id});
    }

    int queryPrimaryKey(int _project_id, int userId) {
        int _id = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT _id FROM ProjectMember WHERE _project_id=? AND " +
                "user_id=?", new String[]{Integer.toString(_project_id), Integer.toString(userId)})) {
            if (c != null && c.moveToFirst()) {
                _id = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
            return _id;
        }
    }

    List<ProjectMemberBean> queryAll(int _project_id) {
        List<ProjectMemberBean> ret = new ArrayList<>();
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM ProjectMember WHERE _project_id=?",
                new String[]{Integer.toString(_project_id)})) {
            while (c != null && c.moveToNext()) {
                ret.add(ProjectMemberBean.newByCursor(c));
            }
            return ret;
        }
    }

    void deleteOne(int _id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM ProjectMember WHERE _id=?",
                new Object[]{_id});
    }

    void deleteAll(int _project_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM ProjectMember WHERE _project_id=?",
                new Object[]{_project_id});
    }

    boolean batchInsert(int _project_id, List<ProjectMemberBean> inserts) {
        if (inserts == null || inserts.size() == 0) {
            return false;
        }
        if (_project_id == -1) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("INSERT INTO ProjectMember(_project_id,user_id,display_name,email,creation_time)" +
                " VALUES (?,?,?,?,?)");
        try {
            wdb.beginTransaction();
            int affected = 0;
            for (ProjectMemberBean i : inserts) {
                if (i == null) {
                    continue;
                }
                statement.clearBindings();
                statement.bindAllArgsAsStrings(new String[]{Integer.toString(_project_id),
                        Integer.toString(i.userId), i.displayName, i.email,
                        Long.toString(i.creationTime)});
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

    boolean batchDelete(List<Integer> _ids) {
        if (_ids == null || _ids.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("DELETE FROM ProjectMember WHERE _id=?");
        try {
            wdb.beginTransaction();
            int affected = 0;
            for (Integer _id : _ids) {
                if (_id == -1) {
                    continue;
                }
                statement.clearBindings();
                statement.bindAllArgsAsStrings(new String[]{Integer.toString(_id)});
                if (statement.executeUpdateDelete() != -1) {
                    affected++;
                }
            }
            wdb.setTransactionSuccessful();
            return affected > 0;
        } finally {
            wdb.endTransaction();
        }
    }
}
