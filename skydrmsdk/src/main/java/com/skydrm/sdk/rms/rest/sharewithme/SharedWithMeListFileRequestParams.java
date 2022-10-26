package com.skydrm.sdk.rms.rest.sharewithme;

/**
 * Created by hhu on 7/24/2017.
 */

public class SharedWithMeListFileRequestParams {
    private int page;
    private int size;
    private String orderBy;
    private String q;
    private String searchStr;

    //default constructor
    public SharedWithMeListFileRequestParams() {
        this.page = -1;
        this.size = -1;
        this.orderBy = OrderBy.SHARED_BY + "," + OrderBy.SHARED_DATE_REVERSE;
        this.searchStr = "";
    }

    public SharedWithMeListFileRequestParams(int page, int size, String orderBy, String searchText) {
        this.page = page;
        this.size = size;
        this.orderBy = orderBy;
        this.searchStr = searchText;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getQ() {
        return "name";
    }

    public String getSearchStr() {
        return searchStr;
    }

    public static class OrderBy {
        public static final String NAME = "name";
        public static final String NAME_REVERSE = "-name";
        public static final String SIZE = "size";
        public static final String SIZE_REVERSE = "-size";
        public static final String SHARED_BY = "sharedBy";
        public static final String SHARED_BY_REVERSE = "-sharedBy";
        public static final String SHARED_DATE = "sharedDate";
        public static final String SHARED_DATE_REVERSE = "-sharedDate";
    }
}
