package com.skydrm.sdk.rms.types;

/**
 * Created by aning on 1/13/2017.
 */

public class FetchLogRequestParas {
    // mStart: the number of records to skip
    // mCount: number of records to be returned
    // we can use mStart and mCount fields to support pagination on client.
    private int mStart;
    private int mCount;
    // the field on which search is being done
    private String mSearchField;
    // this text will be used to filter on searchField
    private String mSearchText;
    // results will be ordered by this field. Default is accessTime.
    private String mOrderBy;
    // true for descending Order, false for ascending Order. Default is true
    private boolean mOrderByReverse;

    /**
     *  fetch all logs for a nxl file.
     */
    public FetchLogRequestParas() {
        // -1: means we don't pass the two parameters
        mStart = -1;
        mCount = -1;
        mSearchField = null;
        mSearchText = null;
        mOrderBy = OrderBy.ORDER_BY_ACCESSTIME;
        mOrderByReverse = true;
    }

    /**
     *  fetch relative logs by search.
     *  @param searchField: {@link SearchField}
     *  @param searchText: search text
     *  @param orderBy: {@link OrderBy}
     *  @param orderByReverse: true for descending Order, false for ascending Order.
     */
    public FetchLogRequestParas(String searchField, String searchText, String orderBy, boolean orderByReverse) {
        // -1: means we don't pass the two parameters
        mStart = -1;
        mCount = -1;
        mSearchField = searchField;
        mSearchText = searchText;
        mOrderBy = orderBy;
        mOrderByReverse = orderByReverse;
    }

    /**
     *   fetch relative logs by search and pagination
     *  @param start: the number of records to skip
     *  @param count: number of records to be returned
     *  @param searchField: {@link SearchField}
     *  @param searchText: search text
     *  @param orderBy: {@link OrderBy}
     *  @param orderByReverse: true for descending Order, false for ascending Order.
     */
    public FetchLogRequestParas(int start, int count, String searchField, String searchText, String orderBy, boolean orderByReverse) {
        mStart = start;
        mCount = count;
        mSearchField = searchField;
        mSearchText = searchText;
        mOrderBy = orderBy;
        mOrderByReverse = orderByReverse;
    }

    public void setStart(int mStart) {
        this.mStart = mStart;
    }

    public void setCount(int mCount) {
        this.mCount = mCount;
    }

    public int getStart() {
        return mStart;
    }

    public int getCount() {
        return mCount;
    }

    public String getSearchField() {
        return mSearchField;
    }

    public String getSearchText() {
        return mSearchText;
    }

    public String getOrderBy() {
        return mOrderBy;
    }

    public boolean isOrderByReverse() {
        return mOrderByReverse;
    }

    public static class SearchField {
        private static final String SEARCH_FIELD_EMAIL = "email";
        private static final String SEARCH_FIELD_OPERATION = "operation";
        private static final String SEARCH_FIELD_DEVICEID = "deviceId";
    }

    public static class OrderBy {
        private static final String ORDER_BY_ACCESSTIME = "accessTime"; // default
        private static final String ORDER_BY_ACCESSRESULT = "accessResult";
    }
}
