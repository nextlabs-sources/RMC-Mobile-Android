package com.skydrm.rmc.database.table.log;

import android.database.Cursor;

public class ActivityLogBean {
    int _id;
    int _user_id;
    public String duid;
    public int operationId;
    public int deviceType;
    public String fileName;
    public String filePath;
    public int accessResult;
    public long accessTime;
    public String activityData;//""

    private String reserved1;
    private String reserved2;

    static ActivityLogBean newByCursor(Cursor c) {
        ActivityLogBean ret = new ActivityLogBean();
        if (c == null) {
            return ret;
        }
        ret._id = c.getInt(c.getColumnIndexOrThrow("_id"));
        ret._user_id = c.getInt(c.getColumnIndexOrThrow("_user_id"));
        ret.duid = c.getString(c.getColumnIndexOrThrow("duid"));
        ret.operationId = c.getInt(c.getColumnIndexOrThrow("operation_id"));
        ret.deviceType = c.getInt(c.getColumnIndexOrThrow("device_type"));
        ret.fileName = c.getString(c.getColumnIndexOrThrow("file_name"));
        ret.filePath = c.getString(c.getColumnIndexOrThrow("file_path"));
        ret.accessResult = c.getInt(c.getColumnIndexOrThrow("access_result"));
        ret.accessTime = c.getLong(c.getColumnIndexOrThrow("access_time"));
        ret.activityData = c.getString(c.getColumnIndexOrThrow("activity_data"));
        return ret;
    }
}
