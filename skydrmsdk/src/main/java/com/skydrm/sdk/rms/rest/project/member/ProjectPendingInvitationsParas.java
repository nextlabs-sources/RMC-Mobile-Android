package com.skydrm.sdk.rms.rest.project.member;


public class ProjectPendingInvitationsParas {
    // note: We can use page and size fields to support pagination on client.
    // page num
    private int mPage;
    // Maximum number of data to return,
    private int mSize;
    // support order by displayName, creationTime; Default is ascending, but may be reversed with the '-' modifier. Example usage: orderBy=-creationTime
    // displayName here represents for invitee's email.
    private String mOrderBy;
    // Search field . Currently only support "email"
    private String mSearchField;
    // String which the user searches for.
    private String mSearchString;

    public ProjectPendingInvitationsParas() {
        mPage = -1;
        mSize = -1;
        mOrderBy = ProjectPendingInvitationsParas.OrderBy.ORDER_BY_CREATE_TIME;  // default
        mSearchField = null;
        mSearchString = null;
    }

    /**
     * @param orderBy: order type {@link ProjectPendingInvitationsParas.OrderBy}
     * @param searchString: search string
     */
    public ProjectPendingInvitationsParas(String orderBy, String searchString) {
        mPage = -1;
        mSize = -1;
        mOrderBy = orderBy;
        mSearchField = SearchField.SEARCH_FIELD_EMAIL;
        mSearchString = searchString;
    }

    /**
     * @param page: page num
     * @param size: maximum number of data to return
     * @param orderBy: order type {@link ProjectPendingInvitationsParas.OrderBy}
     * @param searchString: search string
     */
    public ProjectPendingInvitationsParas(int page, int size, String orderBy, String searchString) {
        mPage = page;
        mSize = size;
        mOrderBy = orderBy;
        mSearchField = SearchField.SEARCH_FIELD_EMAIL;
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

    // here only support email search
    public static class SearchField {
        public static final String SEARCH_FIELD_EMAIL = "email";
    }

}
