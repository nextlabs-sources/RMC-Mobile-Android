package com.skydrm.sdk.rms.rest.sharedwithspace;

public class ListFileParams {
    private int page;
    private int size;
    private String orderBy;
    private String q;
    private String searchStr;
    //possible values: 0(default, shared to person files) /1(shared to project files) /2(shared to enterprise workspace files, not implemented now).
    private int fromSpaceType;
    //Used when listing shared with project/shared with tenant files. projectId for projects, tenantId for tenants
    private String spaceId;

    //default constructor
    public ListFileParams(int spaceType, String spaceId) {
        this.page = -1;
        this.size = -1;
        this.orderBy = OrderBy.SHARED_BY + "," + OrderBy.SHARED_DATE_REVERSE;
        this.q = "name";
        this.searchStr = "";
        this.fromSpaceType = spaceType;
        this.spaceId = spaceId;
    }

    public ListFileParams(int page, int size,
                          String orderBy, String searchText,
                          int spaceType, String spaceId) {
        this.page = page;
        this.size = size;
        this.orderBy = orderBy;
        this.q = "name";
        this.searchStr = searchText;
        this.fromSpaceType = spaceType;
        this.spaceId = spaceId;
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
        return q;
    }

    public String getSearchStr() {
        return searchStr;
    }

    public int getFromSpaceType() {
        return fromSpaceType;
    }

    public String getSpaceId() {
        return spaceId;
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
