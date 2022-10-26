package com.skydrm.sdk.rms.rest.workspace;

import android.support.annotation.StringDef;

public class ListFileParam {
    public static final String ORDER_BY_FILENAME = "name";
    public static final String ORDER_BY_CREATE_TIME = "creationTime";
    // if order by reverse, need to add '-' modifier.
    public static final String ORDER_BY_FILENAME_REVERSE = "-name";
    public static final String ORDER_BY_CREATE_TIME_REVERSE = "-creationTime";

    private int mPage;
    private int mSize;
    private String mOrderBy;
    private String mPathId;
    private String mQ;
    private String mSearchString;

    private ListFileParam() {

    }

    public static ListFileParam newOne(String pathId) {
        ListFileParam param = new ListFileParam();
        param.mPage = -1;
        param.mSize = -1;
        param.mOrderBy = ORDER_BY_CREATE_TIME_REVERSE;
        param.mPathId = pathId;
        param.mQ = "";
        param.mSearchString = "";
        return param;
    }

    public static ListFileParam newOne(int page, int size,
                                       @ORDER_BY String orderBy, String pathId,
                                       String q, String searchString) {
        ListFileParam param = new ListFileParam();
        param.mPage = page;
        param.mSize = size;
        param.mOrderBy = orderBy;
        param.mPathId = pathId;
        param.mQ = q;
        param.mSearchString = searchString;
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

    public String getQ() {
        return mQ;
    }

    public String getSearchString() {
        return mSearchString;
    }

    @StringDef({ORDER_BY_FILENAME,
            ORDER_BY_CREATE_TIME,
            ORDER_BY_FILENAME_REVERSE,
            ORDER_BY_CREATE_TIME_REVERSE})
    @interface ORDER_BY {

    }
}
