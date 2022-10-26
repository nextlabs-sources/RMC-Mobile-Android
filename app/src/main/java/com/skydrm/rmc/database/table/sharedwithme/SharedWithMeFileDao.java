package com.skydrm.rmc.database.table.sharedwithme;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.List;

public class SharedWithMeFileDao extends BaseDao<SharedWithMeFileBean> {
    private static final String TABLE_NAME = "SharedWithMeFile";
    private static final String SQL_CREATE_TABLE_SHAREDWITHME_File = "CREATE TABLE IF NOT EXISTS SharedWithMeFile(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "_user_id    integer    NOT NULL," +
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
            "comment    text    default ''," +
            "is_owner    integer    default 0," +
            "protection_type    integer    default 0," +
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

            "UNIQUE(_user_id,duid)," +
            "FOREIGN KEY(_user_id) references User(_id) ON DELETE CASCADE);";

    public SharedWithMeFileDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    public void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_SHAREDWITHME_File);
    }

    public boolean insert(int _user_id, String duid, String name, long size,
                          String fileType, long sharedDate, String sharedBy,
                          String transactionId, String transactionCode, String sharedLink,
                          String rights, String comment, boolean isOwner,
                          int protectionType) {
        SQLiteDatabase wdb = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("_user_id", _user_id);
        cv.put("duid", duid);
        cv.put("name", name);
        cv.put("size", size);
        cv.put("file_type", fileType);
        cv.put("shared_date", sharedDate);
        cv.put("shared_by", sharedBy);
        cv.put("transaction_id", transactionId);
        cv.put("transaction_code", transactionCode);
        cv.put("shared_link", sharedLink);
        cv.put("rights", rights);
        cv.put("comment", comment);
        cv.put("is_owner", isOwner ? 1 : 0);
        cv.put("protection_type", protectionType);
        return wdb.insertOrThrow(TABLE_NAME, null, cv) != -1;
    }

    public boolean update(int _id, String duid, String name, long size,
                          String fileType, long sharedDate, String sharedBy,
                          String transactionId, String transactionCode, String sharedLink,
                          String rights, String comment, boolean isOwner,
                          int protectionType) {
        SQLiteDatabase wdb = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("duid", duid);
        cv.put("name", name);
        cv.put("size", size);
        cv.put("file_type", fileType);
        cv.put("shared_date", sharedDate);
        cv.put("shared_by", sharedBy);
        cv.put("transaction_id", transactionId);
        cv.put("transaction_code", transactionCode);
        cv.put("shared_link", sharedLink);
        cv.put("rights", rights);
        cv.put("comment", comment);
        cv.put("is_owner", isOwner ? 1 : 0);
        cv.put("protection_type", protectionType);
        return wdb.update(TABLE_NAME, cv, "_id=?",
                new String[]{Integer.toString(_id)}) != 0;
    }

    public int queryPrimaryKey(int _user_id, String duid) {
        SQLiteDatabase rdb = getReadableDatabase();
        int _id = -1;
        try (Cursor c = rdb.rawQuery("SELECT _id FROM SharedWithMeFile WHERE _user_id=? AND duid=?",
                new String[]{Integer.toString(_user_id), duid})) {
            if (c != null && c.moveToFirst()) {
                _id = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
            return _id;
        }
    }

    public List<SharedWithMeFileBean> queryAll(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM SharedWithMeFile WHERE _user_id=?",
                new String[]{Integer.toString(_user_id)})) {
            List<SharedWithMeFileBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(SharedWithMeFileBean.newByCursor(c));
            }
            return ret;
        }
    }

    public List<SharedWithMeFileBean> queryOffline(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM SharedWithMeFile WHERE _user_id=? AND is_offline=1",
                new String[]{Integer.toString(_user_id)})) {
            List<SharedWithMeFileBean> ret = new ArrayList<>();
            while (c != null && c.moveToNext()) {
                ret.add(SharedWithMeFileBean.newByCursor(c));
            }
            return ret;
        }
    }

    public int queryOfflineRights(int _id) {
        SQLiteDatabase rdb = getReadableDatabase();
        int ret = -1;
        try (Cursor c = rdb.rawQuery("SELECT reserved1 FROM SharedWithMeFile WHERE _id=?",
                new String[]{String.valueOf(_id)})) {
            if (c != null && c.moveToFirst()) {
                String rights = c.getString(c.getColumnIndexOrThrow("reserved1"));
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
        try (Cursor c = rdb.rawQuery("SELECT reserved2 FROM SharedWithMeFile WHERE _id=?",
                new String[]{String.valueOf(_id)})) {
            if (c != null && c.moveToFirst()) {
                ret = c.getString(c.getColumnIndexOrThrow("reserved2"));
            }
        }
        return ret;
    }

    public void updateLocalPath(int _id, String localPath) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE SharedWithMeFile SET local_path=? WHERE _id=?",
                new Object[]{localPath, _id});
    }

    public void updateOfflineStatus(int _id, boolean offline) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE SharedWithMeFile SET is_offline=? WHERE _id=?",
                new Object[]{offline ? 1 : 0, _id});
    }

    public void updateOperationStatus(int _id, int status) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE SharedWithMeFile SET operation_status=? WHERE _id=?",
                new Object[]{status, _id});
    }

    public void updateResetAllOperationStatus(int _user_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE SharedWithMeFile SET operation_status=-1 WHERE _user_id=?",
                new Object[]{_user_id});
    }

    public void updateRightsAndObligation(int _id, int rights, String obligationRaw) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE SharedWithMeFile SET reserved1=?,reserved2=? WHERE _id=?",
                new Object[]{rights, obligationRaw, _id});
    }

    public void deleteOne(int _id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM SharedWithMeFile WHERE _id=?", new Object[]{_id});
    }

    public void deleteAll(int _user_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM SharedWithMeFile WHERE _user_id=?", new Object[]{_user_id});
    }

    public boolean batchInsert(int _user_id, List<SharedWithMeFileBean> inserts) {
        if (inserts == null || inserts.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("INSERT INTO SharedWithMeFile(_user_id,duid,name,size," +
                "file_type,shared_date,shared_by,transaction_id,transaction_code," +
                "shared_link,rights,comment,is_owner,protection_type) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        try {
            wdb.beginTransaction();
            int affected = 0;
            for (SharedWithMeFileBean i : inserts) {
                if (i == null) {
                    continue;
                }
                statement.clearBindings();
                statement.bindAllArgsAsStrings(new String[]{Integer.toString(_user_id),
                        i.duid, i.name, Long.toString(i.size),
                        i.fileType == null ? "" : i.fileType,
                        Long.toString(i.sharedDate), i.sharedBy,
                        i.transactionId == null ? "" : i.transactionId,
                        i.transactionCode == null ? "" : i.transactionCode,
                        i.sharedLink, i.rights,
                        i.comment == null ? "" : i.comment,
                        Integer.toString(i.isOwner ? 1 : 0), Integer.toString(i.protectionType)});
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

    public boolean batchDelete(List<Integer> _ids) {
        if (_ids == null || _ids.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("DELETE FROM SharedWithMeFile WHERE _id=?");
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
