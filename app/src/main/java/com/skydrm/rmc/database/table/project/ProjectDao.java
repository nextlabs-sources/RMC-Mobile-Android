package com.skydrm.rmc.database.table.project;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProjectDao extends BaseDao<ProjectBean> {
    private ProjectMemberDao mMemberDao;
    private static final String SQL_CREATE_TABLE_PROJECT = "CREATE TABLE IF NOT EXISTS Project(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "_user_id    integer    NOT NULL," +
            "id    integer    default 0," +
            "parent_tenant_id    text    default ''," +
            "parent_tenant_name    text    default ''," +
            "token_group_name    text    default ''," +
            "name    text    default ''," +
            "description    text    default ''," +
            "display_name    text    default ''," +
            "creation_time    long    default 0," +
            "configuration_modified    long    default 0," +
            "total_members    integer    default 0," +
            "total_files    integer    default 0," +
            "is_owned_by_me    integer    default 0," +
            "owner_raw_json    text    default '{}'," +
            "account_type    text    default ''," +
            "trial_end_time    long    default 0," +
            "expiry    text    default ''," +
            "watermark    text    default ''," +
            "classification_raw_json    text    default '{}'," +
            "usage    long    default 0," +
            "quota    long    default 0," +
            "user_access_count    long    default 0," +
            "last_access_time    long    default 0," +
            "last_refresh_millis    long    default 0," +
            "reserved1    text    default ''," +
            "reserved2    text    default ''," +
            "reserved3    text    default ''," +
            "reserved4    text    default ''," +
            "reserved5    text    default ''," +
            "reserved6    text    default ''," +
            "reserved7    text    default ''," +
            "reserved8    text    default ''," +

            "UNIQUE(_user_id,id)," +
            "FOREIGN KEY(_user_id) references User(_id) ON DELETE CASCADE);";

    public ProjectDao(SQLiteOpenHelper helper) {
        super(helper);
        mMemberDao = new ProjectMemberDao(helper);
    }

    public void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_PROJECT);
        mMemberDao.createTable();
    }

    public int insert(int _user_id, int id, String parentTenantId, String parentTenantName, String tokenGroupName,
                      String name, String description, String displayName, long creationTime,
                      long configurationModified, int totalMembers, int totalFiles,
                      boolean isOwnedByMe, String ownerRawJson, String accountType,
                      long trialEndTime, String expiry, String watermark) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("INSERT INTO Project(_user_id,id,parent_tenant_id,parent_tenant_name," +
                        "token_group_name,name,description," +
                        "display_name,creation_time,configuration_modified," +
                        "total_members,total_files,is_owned_by_me," +
                        "owner_raw_json,account_type,trial_end_time," +
                        "expiry,watermark) " +
                        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{_user_id, id, parentTenantId, parentTenantName, tokenGroupName,
                        name, description, displayName, creationTime,
                        configurationModified, totalMembers, totalFiles,
                        isOwnedByMe ? 1 : 0, ownerRawJson, accountType,
                        trialEndTime, expiry, watermark});

        return queryPrimaryKey(_user_id, id);
    }

    public void update(int _id, int id, String parentTenantId, String parentTenantName, String tokenGroupName,
                       String name, String description, String displayName, long creationTime,
                       long configurationModified, int totalMembers, int totalFiles,
                       boolean isOwnedByMe, String ownerRawJson, String accountType,
                       long trialEndTime, String expiry, String watermark) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE Project SET id=?,parent_tenant_id=?,parent_tenant_name=?," +
                        "token_group_name=?,name=?,description=?," +
                        "display_name=?,creation_time=?,configuration_modified=?," +
                        "total_members=?,total_files=?,is_owned_by_me=?,owner_raw_json=?,account_type=?," +
                        "trial_end_time=?,expiry=?,watermark=? " +
                        " WHERE _id=?",
                new Object[]{id, parentTenantId, parentTenantName, tokenGroupName,
                        name, description, displayName, creationTime,
                        configurationModified, totalMembers, totalFiles,
                        isOwnedByMe ? 1 : 0, ownerRawJson, accountType,
                        trialEndTime, expiry, watermark, _id});
    }

    public void update(int _id, int id, String name, String description, String displayName,
                       long creationTime, int totalMembers, int totalFiles, boolean isOwnedByMe,
                       String accountType, long trialEndTime) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE Project SET id=?,name=?," +
                        "description=?,display_name=?,creation_time=?," +
                        "total_members=?,total_files=?,is_owned_by_me=?,account_type=?," +
                        "trial_end_time=? WHERE _id=?",
                new Object[]{id, name, description, displayName, creationTime,
                        totalMembers, totalFiles, isOwnedByMe ? 1 : 0,
                        accountType, trialEndTime, _id});
    }

    public void update(int _id, String classificationRaw) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE Project SET classification_raw_json=? WHERE _id=?",
                new Object[]{classificationRaw, _id});
    }


    public void update(int _id, long trialEndTime) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE Project SET trial_end_time=? WHERE _id=?",
                new Object[]{trialEndTime, _id});
    }


    public void update(int _id, long usage, long quota) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE Project SET usage=?,quota=? WHERE _id=?",
                new Object[]{usage, quota, _id});
    }

    public void updateTotalFiles(int _id, int total) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE Project SET total_files=? WHERE _id=?",
                new Object[]{total, _id});
    }

    public void updateUserAccessCount(int _id) {
        SQLiteDatabase wdb = getWritableDatabase();
        long count = -1;
        try (Cursor c = wdb.rawQuery("SELECT user_access_count FROM Project WHERE _id=?",
                new String[]{String.valueOf(_id)})) {
            if (c != null && c.moveToFirst()) {
                count = c.getLong(c.getColumnIndexOrThrow("user_access_count"));
            }
        }
        wdb.execSQL("UPDATE Project SET user_access_count=? WHERE _id=?",
                new Object[]{++count, _id});
    }

    public void updateLastRefreshTime(int _id) {
        SQLiteDatabase wdb = getWritableDatabase();
        Calendar now = Calendar.getInstance(Locale.getDefault());
        wdb.execSQL("UPDATE Project SET last_refresh_millis=? WHERE _id=?",
                new Object[]{now.getTimeInMillis(), _id});
    }

    public void updateLastAccessTime(int _id, long lastAccessTime) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("UPDATE Project SET last_access_time=? WHERE _id=?",
                new Object[]{lastAccessTime, _id});
    }

    public void upsertMember(int _project_id, int userId, String displayName,
                             String email, long creationTime) {
        int _member_id = mMemberDao.queryPrimaryKey(_project_id, userId);
        if (_member_id == -1) {
            //Insert a new member item.
            mMemberDao.insert(_project_id, userId, displayName, email, creationTime);
        } else {
            //Update member item.
            mMemberDao.update(_member_id, userId, displayName, email, creationTime);
        }
    }

    public boolean batchInsertMember(int _project_id, List<ProjectMemberBean> inserts) {
        return mMemberDao.batchInsert(_project_id, inserts);
    }

    public int queryPrimaryKey(int _user_id, int id) {
        int _id = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT _id FROM Project WHERE _user_id=? AND id=?",
                new String[]{Integer.toString(_user_id), Integer.toString(id)})) {
            if (c != null && c.moveToFirst()) {
                _id = c.getInt(c.getColumnIndexOrThrow("_id"));
            }
            return _id;
        }
    }

    public List<ProjectBean> queryAll(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        List<ProjectBean> ret = new ArrayList<>();
        try (Cursor c = rdb.rawQuery("SELECT * FROM Project WHERE _user_id=? ORDER BY trial_end_time DESC",
                new String[]{Integer.toString(_user_id)})) {
            while (c != null && c.moveToNext()) {
                ret.add(ProjectBean.newByCursor(c));
            }
        }
        //Query all project members.
        for (ProjectBean i : ret) {
            i.members = mMemberDao.queryAll(i._id);
        }
        return ret;
    }

    public List<ProjectBean> queryRecent(int _user_id) {
        SQLiteDatabase rdb = getReadableDatabase();
        List<ProjectBean> ret = new ArrayList<>();
        try (Cursor c = rdb.rawQuery("SELECT * FROM Project WHERE _user_id=? ORDER BY user_access_count DESC LIMIT 5",
                new String[]{Integer.toString(_user_id)})) {
            while (c != null && c.moveToNext()) {
                ret.add(ProjectBean.newByCursor(c));
            }
        }
        return ret;
    }

    public ProjectBean queryOne(int _id) {
        SQLiteDatabase rdb = getReadableDatabase();
        ProjectBean ret = null;
        try (Cursor c = rdb.rawQuery("SELECT * FROM Project WHERE _id=?",
                new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                ret = ProjectBean.newByCursor(c);
            }
            return ret;
        }
    }

    public ProjectBean queryOne(int _user_id, int id) {
        SQLiteDatabase rdb = getReadableDatabase();
        ProjectBean ret = null;
        try (Cursor c = rdb.rawQuery("SELECT * FROM Project WHERE _user_id=? AND id=?",
                new String[]{Integer.toString(_user_id), Integer.toString(id)})) {
            if (c != null && c.moveToFirst()) {
                ret = ProjectBean.newByCursor(c);
            }
            return ret;
        }
    }

    public long queryUsage(int _id) {
        long usage = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT usage FROM Project WHERE _id=?",
                new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                usage = c.getLong(c.getColumnIndexOrThrow("usage"));
            }
            return usage;
        }
    }

    public long queryQuota(int _id) {
        long quota = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT quota FROM Project WHERE _id=?",
                new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                quota = c.getLong(c.getColumnIndexOrThrow("quota"));
            }
            return quota;
        }
    }

    public long queryLastRefreshMillis(int _id) {
        long lstRefreshMillis = -1;
        SQLiteDatabase rdb = getReadableDatabase();
        try (Cursor c = rdb.rawQuery("SELECT last_refresh_millis FROM Project WHERE _id=?",
                new String[]{Integer.toString(_id)})) {
            if (c != null && c.moveToFirst()) {
                lstRefreshMillis = c.getLong(c.getColumnIndexOrThrow("last_refresh_millis"));
            }
            return lstRefreshMillis;
        }
    }

    public List<ProjectMemberBean> queryAllMember(int _project_id) {
        return mMemberDao.queryAll(_project_id);
    }

    public void deleteOne(int _id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM Project WHERE _id=?",
                new Object[]{_id});
    }

    public void deleteAll(int _user_id) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.execSQL("DELETE FROM Project WHERE _user_id=?",
                new Object[]{_user_id});
    }

    public void deleteMember(int _project_member_id) {
        mMemberDao.deleteOne(_project_member_id);
    }

    public boolean batchDeleteMember(List<Integer> _ids) {
        return mMemberDao.batchDelete(_ids);
    }
}
