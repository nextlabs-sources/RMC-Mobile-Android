package com.skydrm.rmc.database.table.membership;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.skydrm.rmc.database.BaseDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MembershipDao extends BaseDao<Membership> {
    private static final String SQL_CREATE_TABLE_MEMBERSHIP = "CREATE TABLE IF NOT EXISTS Membership(" +
            "_id    integer    NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            "_user_id    integer    NOT NULL," +
            "id    text    NOT NULL default ''," +
            "type    integer    NOT NULL," +
            "token_group_name,    text    NOT NULL default ''," +
            "tenant_id    text    default ''," +
            "project_id    integer    default -1," +
            "reserved1    text    default ''," +
            "reserved2    text    default ''," +

            "UNIQUE(_user_id,id)," +
            "FOREIGN KEY(_user_id) references User(_id) ON DELETE CASCADE);";

    public MembershipDao(SQLiteOpenHelper helper) {
        super(helper);
    }

    public void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE_MEMBERSHIP);
    }

    public void insert(int _user_id, int id, int type, String tokenGroupName, String tenantId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Membership(_user_id,id,type,token_group_name,tenant_id)" +
                "VALUES(?,?,?,?,?)", new Object[]{
                _user_id, id, type, tokenGroupName, tenantId});
    }

    public void insert(int _user_id, int id, int type, String tokenGroupName, int projectId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Membership(_user_id,id,type,token_group_name,project_id)" +
                "VALUES(?,?,?,?,?)", new Object[]{
                _user_id, id, type, tokenGroupName, projectId});
    }

    public List<Membership> querySpecific(int _user_id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM Membership WHERE _user_id = ?", new String[]{Integer.toString(_user_id)})) {
            List<Membership> ret = new ArrayList<>();
            while (c.moveToNext()) {
                ret.add(Membership.newByCursor(c));
            }
            return ret;
        }
    }
}
