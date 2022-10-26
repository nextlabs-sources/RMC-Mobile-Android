package com.skydrm.rmc.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public class DaoMaster {
    private SQLiteOpenHelper mHelper;

    public DaoMaster(Context c) {
        mHelper = new Database(c);
    }

    public DaoSession newSession() {
        return new DaoSession(mHelper);
    }
}
