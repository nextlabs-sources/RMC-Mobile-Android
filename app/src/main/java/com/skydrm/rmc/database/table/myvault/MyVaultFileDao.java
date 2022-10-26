package com.skydrm.rmc.database.table.myvault;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyVaultFileDao extends BaseDao<MyVaultFileBean> {
    private static final String SQL_CREATE_TABLE_MYVAULT_FILE = "CREATE TABLE IF NOT EXISTS MyVaultFile(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "_user_id    integer    NOT NULL," +
            "path_id    text    NOT NULL default ''," +
            "path_display    text    default ''," +
            "repo_id    text    default ''," +
            "shared_on    long    default 0," +
            "shared_with    text    default ''," +
            "rights    text    default ''," +
            "name    text    default ''," +
            "file_type    text    default ''," +
            "duid    text    default ''," +
            "is_revoked    integer    default 0," +
            "is_deleted    integer    default 0," +
            "is_shared    integer    default 0," +
            "size    long    default 0," +
            "custom_meta_data_raw_json    text    default '{}'," +
            "is_favorite    integer    default 0," +
            "is_offline    integer    default 0," +
            "operation_status    integer    default 0," +
            "modify_rights_status    integer    default 0," +
            "edit_status    integer    default 0," +
            "local_path    text    default ''," +
            "reserved1    text    default ''," +
            "reserved2    text    default ''," +
            "reserved3    text    default ''," +
            "reserved4    text    default ''," +

            "UNIQUE(_user_id,duid)," +
            "FOREIGN KEY(_user_id) references User(_id) ON DELETE CASCADE);";

    public MyVaultFileDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    public void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_MYVAULT_FILE);
    }

    public int insert(int _user_id, String pathId, String pathDisplay, String repoId,
                      long sharedOn, String sharedWith, String rights, String name, String fileType, String duid,
                      boolean isRevoked, boolean isDeleted, boolean isShared,
                      long size, String rawMetadata, boolean isFavorite) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("INSERT INTO MyVaultFile(_user_id,path_id,path_display,repo_id,shared_on,shared_with," +
                        "rights,name,file_type,duid,is_revoked," +
                        "is_deleted,is_shared,size,custom_meta_data_raw_json,is_favorite) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{_user_id, pathId, pathDisplay, repoId,
                        sharedOn, sharedWith, rights, name, fileType, duid,
                        isRevoked ? 1 : 0, isDeleted ? 1 : 0, isShared ? 1 : 0,
                        size, rawMetadata, isFavorite ? 1 : 0});
        int _id = -1;
        try (Cursor c = wdb.rawQuery("SELECT _id FROM MyVaultFile where _user_id=? AND path_id=?",
                new String[]{Integer.toString(_user_id), pathId})) {
            if (c != null && c.moveToFirst()) {
                _id = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
        }
        return _id;
    }

    public void update(int _id, String pathId, String pathDisplay, String repoId,
                       long sharedOn, String sharedWith, String rights, String name, String fileType, String duid,
                       boolean isRevoked, boolean isDeleted, boolean isShared,
                       long size, String rawMetadata,
                       boolean isFavorite) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE MyVaultFile SET path_id=?,path_display=?,repo_id=?,shared_on=?,shared_with=?," +
                        "rights=?,name=?,file_type=?,duid=?,is_revoked=?," +
                        "is_deleted=?,is_shared=?,size=?,custom_meta_data_raw_json=?,is_favorite=? WHERE _id=?",
                new Object[]{pathId, pathDisplay, repoId,
                        sharedOn, sharedWith, rights, name, fileType, duid,
                        isRevoked ? 1 : 0, isDeleted ? 1 : 0, isShared ? 1 : 0,
                        size, rawMetadata, isFavorite ? 1 : 0, _id});
    }

    public int queryPrimaryKey(int _user_id, String duid) {
        int _id = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT _id FROM MyVaultFile WHERE _user_id=? AND duid=?",
                new String[]{Integer.toString(_user_id), duid})) {
            if (c != null && c.moveToFirst()) {
                _id = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
            return _id;
        }
    }

    public MyVaultFileBean queryOne(int _id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM MyVaultFile WHERE _id=?",
                new String[]{Integer.toString(_id)})) {
            MyVaultFileBean ret = null;
            if (c != null && c.moveToFirst()) {
                ret = MyVaultFileBean.newByCursor(c);
            }
            return ret;
        }
    }

    public List<MyVaultFileBean> queryAll(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM MyVaultFile WHERE _user_id=?",
                new String[]{Integer.toString(_user_id)})) {
            List<MyVaultFileBean> ret = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(MyVaultFileBean.newByCursor(c));
                }
            }
            return ret;
        }
    }

    public List<MyVaultFileBean> queryActiveShared(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM MyVaultFile WHERE _user_id=? AND is_shared=1 AND is_revoked=0" +
                        " AND is_revoked=0 AND is_deleted=0",
                new String[]{Integer.toString(_user_id)})) {
            List<MyVaultFileBean> ret = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(MyVaultFileBean.newByCursor(c));
                }
            }
            return ret;
        }
    }

    public List<MyVaultFileBean> queryProtected(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM MyVaultFile WHERE _user_id=? AND is_shared=0",
                new String[]{Integer.toString(_user_id)})) {
            List<MyVaultFileBean> ret = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(MyVaultFileBean.newByCursor(c));
                }
            }
            return ret;
        }
    }

    public List<MyVaultFileBean> queryRevoked(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM MyVaultFile WHERE _user_id=? AND is_revoked=1",
                new String[]{Integer.toString(_user_id)})) {
            List<MyVaultFileBean> ret = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(MyVaultFileBean.newByCursor(c));
                }
            }
            return ret;
        }
    }

    public List<MyVaultFileBean> queryDeleted(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM MyVaultFile WHERE _user_id=? AND is_revoked=1 AND is_deleted=1",
                new String[]{Integer.toString(_user_id)})) {
            List<MyVaultFileBean> ret = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(MyVaultFileBean.newByCursor(c));
                }
            }
            return ret;
        }
    }

    public List<MyVaultFileBean> queryFavorite(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM MyVaultFile WHERE _user_id=? AND is_favorite=1 AND is_deleted = 0",
                new String[]{Integer.toString(_user_id)})) {
            List<MyVaultFileBean> ret = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(MyVaultFileBean.newByCursor(c));
                }
            }
            return ret;
        }
    }

    public List<MyVaultFileBean> queryOffline(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT * FROM MyVaultFile WHERE _user_id=? AND is_offline=1 AND is_deleted = 0",
                new String[]{Integer.toString(_user_id)})) {
            List<MyVaultFileBean> ret = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(MyVaultFileBean.newByCursor(c));
                }
            }
            return ret;
        }
    }

    public void updateLocalPath(int _id, String localPath) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE MyVaultFile SET local_path=? WHERE _id=?",
                new Object[]{localPath, _id});
    }

    public void updateSharedWith(int _id, String sharedWith) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE MyVaultFile SET shared_with=? WHERE _id=?",
                new Object[]{sharedWith, _id});
    }

    public void updateRevoked(int _id, boolean revoked) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE MyVaultFile SET is_revoked=? WHERE _id=?",
                new Object[]{revoked ? 1 : 0, _id});
    }

    public void updateDeleted(int _id, boolean deleted) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE MyVaultFile SET is_revoked=?,is_deleted=?,is_favorite=0 WHERE _id=?",
                new Object[]{1, deleted ? 1 : 0, _id});
    }

    public void updateOperationStatus(int _id, int status) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE MyVaultFile SET operation_status=? WHERE _id=?",
                new Object[]{status, _id});
    }

    public void updateResetAllOperationStatus(int _user_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE MyVaultFile SET operation_status=-1 WHERE _user_id=?",
                new Object[]{_user_id});
    }

    public void updateFavorite(int _id, boolean favorite) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE MyVaultFile SET is_favorite=? WHERE _id=?",
                new Object[]{favorite ? 1 : 0, _id});
    }

    public void updateFavorite(int _user_id, String pathId, boolean favorite) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE MyVaultFile SET is_favorite=? WHERE _user_id=? AND path_id=?",
                new Object[]{favorite ? 1 : 0, _user_id, pathId});
    }

    public void updateOffline(int _id, boolean offline) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE MyVaultFile SET is_offline=? WHERE _id=?",
                new Object[]{offline ? 1 : 0, _id});
    }

    public void deleteOne(int _id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM MyVaultFile WHERE _id=?", new Object[]{_id});
    }

    public void deleteAll(int _user_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM MyVaultFile WHERE _user_id=?", new Object[]{_user_id});
    }

    public boolean batchInsert(int _user_id, List<MyVaultFileBean> inserts) {
        if (inserts == null || inserts.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("INSERT INTO MyVaultFile(_user_id,path_id,path_display," +
                "repo_id,shared_on,shared_with," +
                "rights,name,file_type,duid," +
                "is_revoked,is_deleted,is_shared,size," +
                "custom_meta_data_raw_json,is_favorite) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        try {
            int insertAffected = 0;
            wdb.beginTransaction();
            for (MyVaultFileBean i : inserts) {
                statement.clearBindings();
                statement.bindAllArgsAsStrings(new String[]{Integer.toString(_user_id),
                        i.pathId, i.pathDisplay, i.repoId,
                        Long.toString(i.sharedOn), i.sharedWith, i.rights,
                        i.name, i.fileType, i.duid,
                        Integer.toString(i.isRevoked ? 1 : 0),
                        Integer.toString(i.isDeleted ? 1 : 0),
                        Integer.toString(i.isShared ? 1 : 0),
                        Long.toString(i.size),
                        i.metadata == null ? "{}" : i.metadata.toRawJson(),
                        Integer.toString(i.isFavorite ? 1 : 0)});
                if (statement.executeInsert() != -1) {
                    insertAffected++;
                }
            }
            wdb.setTransactionSuccessful();
            return insertAffected > 0;
        } finally {
            wdb.endTransaction();
        }
    }

    public boolean batchUpdate(int _user_id, Map<String, Boolean> updates) {
        if (_user_id == -1) {
            return false;
        }
        if (updates == null || updates.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("UPDATE MyVaultFile SET is_favorite=? WHERE _user_id=? AND path_id=?");
        try {
            wdb.beginTransaction();
            int updateAffected = 0;
            for (Map.Entry<String, Boolean> e : updates.entrySet()) {
                statement.clearBindings();
                String pathId = e.getKey();
                Boolean favorite = e.getValue();
                statement.bindAllArgsAsStrings(new String[]{Integer.toString(favorite ? 1 : 0),
                        Integer.toString(_user_id),
                        pathId});
                if (statement.executeUpdateDelete() != -1) {
                    updateAffected++;
                }
            }
            wdb.setTransactionSuccessful();
            return updateAffected > 0;
        } finally {
            wdb.endTransaction();
        }
    }

    public boolean batchDelete(List<Integer> _ids) {
        if (_ids == null || _ids.size() == 0) {
            return false;
        }
        SQLiteDatabase wdb = getWritableDatabase();
        SQLiteStatement statement = wdb.compileStatement("DELETE FROM MyVaultFile WHERE _id=?");
        try {
            wdb.beginTransaction();
            int affected = 0;
            for (Integer _id : _ids) {
                if (_id == -1) {
                    return false;
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
