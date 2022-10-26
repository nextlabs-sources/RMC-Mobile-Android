package com.skydrm.sdk.rms.rest.project.member;

public class ListMemberParam {
    // note: We can use page and size fields to support pagination on client.
    // page num
    private int mPage;
    // Maximum number of data to return,
    private int mSize;
    // support order by displayName, creationTime; Default is ascending, but may be reversed with the '-' modifier. Example usage: orderBy=-creationTime
    private String mOrderBy;
    // A boolean value which Indicates whether clients need server to return the profile picture of the user or the link to the profile picture
    // default is false.
    private boolean bIsRequestProfilePicture;
    // Search field . Currently only support "email" and "name"
    private String mSearchField;
    // String which the user searches for.
    private String mSearchString;

    public ListMemberParam() {
        mPage = -1;
        mSize = -1;
        mOrderBy = ListMemberParam.OrderBy.ORDER_BY_DISPLAY_NAME;  // default
        bIsRequestProfilePicture = false; // default
        mSearchField = null;
        mSearchString = null;
    }

    /**
     * @param orderBy: order type {@link ListMemberParam.OrderBy}
     * @param bIsRequestProfilePicture:
     * @param searchField: filter condition, {@link ListMemberParam.SearchField}
     * @param searchString: search string
     */
    public ListMemberParam(String orderBy, boolean bIsRequestProfilePicture, String searchField, String searchString) {
        mPage = -1;
        mSize = -1;
        mOrderBy = orderBy;
        this.bIsRequestProfilePicture = bIsRequestProfilePicture;
        mSearchField = searchField;
        mSearchString = searchString;
    }

    /**
     * @param page: page num
     * @param size: maximum number of data to return
     * @param orderBy: order type {@link ListMemberParam.OrderBy}
     * @param bIsRequestProfilePicture:
     * @param searchField: filter condition, {@link ListMemberParam.SearchField}
     * @param searchString: search string
     */
    public ListMemberParam(int page, int size, String orderBy, boolean bIsRequestProfilePicture, String searchField, String searchString) {
        mPage = page;
        mSize = size;
        mOrderBy = orderBy;
        this.bIsRequestProfilePicture = bIsRequestProfilePicture;
        mSearchField = searchField;
        mSearchString = searchString;
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

    public boolean isbIsRequestProfilePicture() {
        return bIsRequestProfilePicture;
    }

    public String getmSearchField() {
        return mSearchField;
    }

    public String getmSearchString() {
        return mSearchString;
    }

    public static class OrderBy {
        public static final String ORDER_BY_DISPLAY_NAME = "displayName";
        public static final String ORDER_BY_CREATE_TIME = "creationTime";
        // if order by reverse, need to add '-' modifier.
        public static final String ORDER_BY_DISPLAY_NAME_REVERSE = "-displayName";
        public static final String ORDER_BY_CREATE_TIME_REVERSE = "-creationTime";
    }

    public static class SearchField {
        public static final String SEARCH_FIELD_NAME = "name";
        public static final String SEARCH_FIELD_EMAIL = "email";
    }

}
