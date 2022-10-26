package com.skydrm.rmc.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseDao<T> {
    protected SQLiteOpenHelper mHelper;

    public BaseDao(SQLiteOpenHelper helper) {
        mHelper = helper;
    }

    protected SQLiteDatabase getWritableDatabase() {
        return mHelper.getWritableDatabase();
    }

    protected SQLiteDatabase getReadableDatabase() {
        return mHelper.getReadableDatabase();
    }

    protected void close() {
        mHelper.close();
    }
}
