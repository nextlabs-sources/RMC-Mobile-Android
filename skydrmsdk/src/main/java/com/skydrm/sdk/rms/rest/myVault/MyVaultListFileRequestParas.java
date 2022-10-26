package com.skydrm.sdk.rms.rest.myVault;

/**
 * Created by aning on 1/16/2017.
 */

public class MyVaultListFileRequestParas {
    // note: We can use page and size fields to support pagination on client.
    // page num
    private int mPage;
    // Maximum number of data to return,
    private int mSize;
    // support order by fileName, creationTime, size; Default is ascending, but may be reversed with the '-' modifier. Example usage: orderBy=-creationTime
    private String mOrderBy;
    // Filter transaction type. Valid keys are activeTransaction, allShared, allFiles, protected. Default is
    private String mFilter;
    // Search by filename. such as q.fileName=document
    private String mSearchText;


    public MyVaultListFileRequestParas() {
        mPage = -1;
        mSize = -1;
        mOrderBy = OrderBy.ORDER_BY_CREATE_TIME;  // default
        mFilter = FilterType.FILTER_TYPE_ALL_FILES; // default
        mSearchText = null;
    }

    /**
     * @param orderBy:    order type {@link OrderBy}
     * @param filter:     filter condition, {@link FilterType}
     * @param searchText: search by file name.
     */
    public MyVaultListFileRequestParas(String orderBy, String filter, String searchText) {
        mPage = -1;
        mSize = -1;
        mOrderBy = orderBy;
        mFilter = filter;
        mSearchText = searchText;
    }

    /**
     * @param page:       page num
     * @param size:       maximum number of data to return
     * @param orderBy:    order type {@link OrderBy}
     * @param filter:     filter condition, {@link FilterType}
     * @param searchText: search by file name.
     */
    public MyVaultListFileRequestParas(int page, int size, String orderBy, String filter, String searchText) {
        mPage = page;
        mSize = size;
        mOrderBy = orderBy;
        mFilter = filter;
        mSearchText = searchText;
    }

    public int getmPage() {
        return mPage;
    }

    public int getmSize() {
        return mSize;
    }

    public String getmOrderBy() {
        return mOrderBy;
    }

    public String getmFilter() {
        return mFilter;
    }

    public String getmSearchText() {
        return mSearchText;
    }

    public static class OrderBy {
        public static final String ORDER_BY_FILENAME = "fileName";
        public static final String ORDER_BY_CREATE_TIME = "creationTime";
        public static final String ORDER_BY_SIZE = "size";
        // if order by reverse, need to add '-' modifier.
        public static final String ORDER_BY_FILENAME_REVERSE = "-fileName";
        public static final String ORDER_BY_CREATE_TIME_REVERSE = "-creationTime";
        public static final String ORDER_BY_SIZE_REVERSE = "-size";
    }

    public static class FilterType {
        public static final String FILTER_TYPE_ACTIVE = "activeTransaction";
        public static final String FILTER_TYPE_ALL_SHARED = "allShared";
        public static final String FILTER_TYPE_ALL_FILES = "allFiles";
        public static final String FILTER_TYPE_PROTECTED = "protected";
        public static final String FILTER_TYPE_DELETED = "deleted";
        public static final String FILTER_TYPE_REVOKED = "revoked";
    }
}
