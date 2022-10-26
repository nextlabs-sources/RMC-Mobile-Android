package com.skydrm.rmc.database.table.project;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.List;

public class SharedWithProjectFileDao extends BaseDao<SharedWithProjectFileBean> {
    private static final String SQL_CREATE_TABLE_SHARE_WITH_PROJECT_FILE = "CREATE TABLE IF NOT EXISTS SharedWithProjectFile(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "_project_id    integer    NOT NULL," +
            "duid    text    default ''," +
            "name    text    default ''," +
            "size    long    default 0," +
            "file_type    text    default ''," +
            "shared_date    long    default 0," +
            "shared_by    text    default ''," +
            "transaction_id    text    default ''," +
            "transaction_code    text    default ''," +
            "shared_link    text    default ''," +
            "rights    text    default ''," +
            "is_owner    integer    default 0," +
            "protection_type    integer    default 0," +
            "shared_by_space    text    default ''," +
            "is_favorite    integer    default 0," +
            "is_offline    integer    default 0," +
            "offline_rights    text    default ''," +
            "offline_obligations    text    default ''," +
            "modify_rights_status    integer    default 0," +
            "edit_status    integer    default 0," +
            "operation_status    integer    default 0," +
            "comment    text    default ''," +
            "local_path    text    default ''," +
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

            "UNIQUE(_project_id,duid)," +
            "FOREIGN KEY(_project_id) references Project(_id) ON DELETE CASCADE);";

    public SharedWithProjectFileDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    public void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_SHARE_WITH_PROJECT_FILE);
    }

    public void insert(int _project_id, String duid, String name, long size,
                       String fileType, long sharedDate, String sharedBy,
                       String transactionId, String transactionCode,
                       String sharedLink, String rights, boolean isOwner,
                       int protectionType, String sharedBySpace) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("INSERT INTO SharedWithProjectFile(_project_id,duid,name,size," +
                        "file_type,shared_date,shared_by," +
                        "transaction_id,transaction_code," +
                        "shared_link,rights,is_owner," +
                        "protection_type,shared_by_space) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{_project_id, duid, name, size,
                        fileType, sharedDate, sharedBy,
                        transactionId, transactionCode,
                        sharedLink, rights, isOwner ? 1 : 0,
                        protectionType, sharedBySpace});
    }

    public boolean batchInsert(int _project_id, List<SharedWithProjectFileBean> inserts) {
        if (inserts == null || inserts.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("INSERT INTO " +
                "SharedWithProjectFile(_project_id,duid,name,size," +
                "file_type,shared_date,shared_by," +
                "transaction_id,transaction_code," +
                "shared_link,rights,is_owner," +
                "protection_type,shared_by_space) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        try {
            wdb.beginTransaction();

            int affected = 0;
            for (SharedWithProjectFileBean i : inserts) {
                statement.clearBindings();
                statement.bindAllArgsAsStrings(new String[]{String.valueOf(_project_id),
                        i.duid == null ? "" : i.duid,
                        i.name == null ? "" : i.name,
                        String.valueOf(i.size),
                        i.fileType == null ? "" : i.fileType,
                        String.valueOf(i.sharedDate),
                        i.sharedBy == null ? "" : i.sharedBy,
                        i.transactionId == null ? "" : i.transactionId,
                        i.transactionCode == null ? "" : i.transactionCode,
                        i.sharedLink == null ? "" : i.sharedLink,
                        i.rights == null ? "" : i.rights,
                        String.valueOf(i.isOwner ? 1 : 0),
                        String.valueOf(i.protectionType),
                        i.shareBySpace == null ? "" : i.shareBySpace});
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

    public void update(int _id, String duid, String name, long size,
                       String fileType, long sharedDate, String sharedBy,
                       String transactionId, String transactionCode,
                       String sharedLink, String rights, boolean isOwner,
                       int protectionType, String sharedBySpace) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE SharedWithProjectFile SET duid=?,name=?,size=?," +
                        "file_type=?,shared_date=?,shared_by=?," +
                        "transaction_id=?,transaction_code=?," +
                        "shared_link=?,rights=?,is_owner=?," +
                        "protection_type=?,shared_by_space=? " +
                        "WHERE _id=?",
                new Object[]{duid, name, size,
                        fileType, sharedDate, sharedBy,
                        transactionId, transactionCode,
                        sharedLink, rights, isOwner ? 1 : 0,
                        protectionType, sharedBySpace, _id});
    }

    public void update(int _id, String path) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE SharedWithProjectFile SET local_path=? WHERE _id=?",
                new Object[]{path, _id});
    }

    public void update(int _id, int status) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE SharedWithProjectFile SET operation_status=? WHERE _id=?",
                new Object[]{status, _id});
    }

    public void update(int _id, int rights, String obligationRaw) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE SharedWithProjectFile SET offline_rights=?,offline_obligations=? WHERE _id=?",
                new Object[]{String.valueOf(rights), obligationRaw, _id});
    }

    public void update(int _id, boolean offline) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE SharedWithProjectFile SET is_offline=? WHERE _id=?",
                new Object[]{offline ? 1 : 0, _id});
    }

    public List<SharedWithProjectFileBean> queryAll(int _project_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM SharedWithProjectFile WHERE _project_id=?",
                new String[]{String.valueOf(_project_id)})) {
            List<SharedWithProjectFileBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(SharedWithProjectFileBean.newByCursor(c));
            }
            return ret;
        }
    }

    public int queryPrimaryKey(int _project_id, String duid) {
        int _id = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT _id FROM SharedWithProjectFile " +
                        "WHERE _project_id=? AND duid=?",
                new String[]{String.valueOf(_project_id), duid})) {
            if (c != null && c.moveToFirst()) {
                _id = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
            return _id;
        }
    }

    public int queryRights(int _id) {
        int rightsRet = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT offline_rights FROM SharedWithProjectFile WHERE _id=?",
                new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                String rights = c.getString(c.getColumnIndexOrThrow("offline_rights"));
                if (rights != null && !rights.isEmpty()) {
                    rightsRet = Integer.valueOf(rights);
                }
            }
            return rightsRet;
        }
    }

    public String queryObligations(int _id) {
        String obligation = "";
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT offline_obligations FROM SharedWithProjectFile WHERE _id=?",
                new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                obligation = c.getString(c.getColumnIndexOrThrow("offline_obligations"));
            }
            return obligation;
        }
    }

    public void delete(int _id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM SharedWithProjectFile WHERE _id=?",
                new Object[]{_id});
    }

    public boolean batchDelete(List<Integer> _ids) {
        if (_ids == null || _ids.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("DELETE FROM SharedWithProjectFile WHERE _id=?");
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

    public boolean deleteAll(int _project_id) {
        if (_project_id == -1) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM SharedWithProjectFile WHERE _project_id=?",
                new Object[]{_project_id});
        return true;
    }

}
