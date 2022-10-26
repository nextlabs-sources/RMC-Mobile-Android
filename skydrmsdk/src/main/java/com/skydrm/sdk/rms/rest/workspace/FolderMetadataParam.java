package com.skydrm.sdk.rms.rest.workspace;

import android.support.annotation.StringDef;

public class FolderMetadataParam {
    public static final String ORDER_BY_FILENAME = "name";
    public static final String ORDER_BY_CREATE_TIME = "creationTime";
    // if order by reverse, need to add '-' modifier.
    public static final String ORDER_BY_FILENAME_REVERSE = "-name";
    public static final String ORDER_BY_CREATE_TIME_REVERSE = "-creationTime";

    private int mPage;
    private int mSize;
    private String mOrderBy;
    private String mPathId;
    private long mLastModified;

    private FolderMetadataParam() {

    }

    public static FolderMetadataParam newOne(String pathId, long lastModified) {
        FolderMetadataParam param = new FolderMetadataParam();
        param.mPage = -1;
        param.mSize = -1;
        param.mOrderBy = ORDER_BY_CREATE_TIME_REVERSE;
        param.mLastModified = lastModified;
        return param;
    }

    public static FolderMetadataParam newOne(int page, int size,
                                             @ORDER_BY String orderBy, String pathId,
                                             long lastModified) {
        FolderMetadataParam param = new FolderMetadataParam();
        param.mPage = page;
        param.mSize = size;
        param.mOrderBy = orderBy;
        param.mPathId = pathId;
        param.mLastModified = lastModified;
        return param;
    }

    public int getPage() {
        return mPage;
    }

    public int getSize() {
        return mSize;
    }

    public String getOrderBy() {
        return mOrderBy;
    }

    public String getPathId() {
        return mPathId;
    }

    public long getLastModified() {
        return mLastModified;
    }

    @StringDef({ORDER_BY_FILENAME,
            ORDER_BY_CREATE_TIME,
            ORDER_BY_FILENAME_REVERSE,
            ORDER_BY_CREATE_TIME_REVERSE})
    @interface ORDER_BY {

    }
}
