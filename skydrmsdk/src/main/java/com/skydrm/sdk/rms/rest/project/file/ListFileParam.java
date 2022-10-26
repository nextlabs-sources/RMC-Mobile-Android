package com.skydrm.sdk.rms.rest.project.file;

/**
 * Created by aning on 2/9/2017.
 */

public class ListFileParam {
    // note: We can use page and size fields to support pagination on client.
    // page num
    private int mPage;
    // Maximum number of data to return,
    private int mSize;
    // support order by fileName, creationTime; Default is ascending, but may be reversed with the '-' modifier. Example usage: orderBy=-creationTime
    private String mOrderBy;
    // The path where you want to list the file. You can use:
    //   '/' --  for root folder. If you want to go inside a folder name then put it as ---  /folderName/
    private String mPathId;
    //The value will always hold "fileName" as searching is done based on the file name
    private String mP;
    //Enter any string here. Search will be done based on fileName
    private String mSearchString;
    //Filter transaction type. Valid keys are allShared, allFiles, revoked.
    private String mFilter;

    public ListFileParam() {
        this.mFilter = Filter.FILTER_ALL_FILE;
    }

    public static ListFileParam newProjectRecentFilesRequestParas() {
        ListFileParam paras = new ListFileParam();
        paras.setmPage(1);
        paras.setmSize(6);
        paras.setmOrderBy(OrderBy.ORDER_BY_LAST_MODIFIER_REVERSE);
        return paras;
    }

    /**
     * Support for project share feature.
     *
     * @return All project shared files.
     */
    public static ListFileParam newProjectAllSharedFilesRequestParas() {
        ListFileParam param = new ListFileParam();
        param.setmPage(-1);
        param.setmSize(-1);
        param.setFilter(Filter.FILTER_ALL_SHARED);
        return param;
    }

    /**
     * Support for project share feature.
     *
     * @return All project revoked files.
     */
    public static ListFileParam newProjectRevokedFilesRequestParas() {
        ListFileParam param = new ListFileParam();
        param.setmPage(-1);
        param.setmSize(-1);
        param.setFilter(Filter.FILTER_REVOKED);
        return param;
    }

    public ListFileParam(String pathId) {
        mPage = -1;
        mSize = -1;
        mOrderBy = ListFileParam.OrderBy.ORDER_BY_CREATE_TIME_REVERSE;  // default
        mPathId = pathId;
        mP = "";
        mSearchString = "";
        mFilter = Filter.FILTER_ALL_FILE;
    }

    public ListFileParam(String orderBy, String pathId) {
        mPage = -1;
        mSize = -1;
        mOrderBy = orderBy;
        mPathId = pathId;
        mP = "";
        mSearchString = "";
        mFilter = Filter.FILTER_ALL_FILE;
    }

    public ListFileParam(int page, int size, String orderBy, String pathId, String p,
                         String searchString,String filter) {
        mPage = page;
        mSize = size;
        mOrderBy = orderBy;
        mPathId = pathId;
        mP = p;
        mSearchString = searchString;
        mFilter = filter;
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

    public String getmPathId() {
        return mPathId;
    }

    public String getmP() {
        return mP;
    }

    public String getmSearchString() {
        return mSearchString;
    }

    public void setmPage(int mPage) {
        this.mPage = mPage;
    }

    public void setmSize(int mSize) {
        this.mSize = mSize;
    }

    public void setmOrderBy(String mOrderBy) {
        this.mOrderBy = mOrderBy;
    }

    public void setmPathId(String mPathId) {
        this.mPathId = mPathId;
    }

    public void setmP(String mP) {
        this.mP = mP;
    }

    public void setmSearchString(String mSearchString) {
        this.mSearchString = mSearchString;
    }

    public String getFilter() {
        return mFilter;
    }

    public void setFilter(String filter) {
        this.mFilter = filter;
    }

    public static class OrderBy {
        public static final String ORDER_BY_FILENAME = "fileName";
        public static final String ORDER_BY_CREATE_TIME = "creationTime";
        // if order by reverse, need to add '-' modifier.
        public static final String ORDER_BY_FILENAME_REVERSE = "-fileName";
        public static final String ORDER_BY_CREATE_TIME_REVERSE = "-creationTime";
        public static final String ORDER_BY_SIZE = "size";
        public static final String ORDER_BY_SIZE_REVERSE = "-size";
        public static final String ORDER_BY_LAST_MODIFIER_REVERSE = "-lastModified";
    }

    public static class Filter {
        public static final String FILTER_ALL_FILE = "allFiles";
        public static final String FILTER_ALL_SHARED = "allShared";
        public static final String FILTER_REVOKED = "revoked";
    }

}
