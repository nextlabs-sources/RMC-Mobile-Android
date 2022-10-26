package com.skydrm.sdk.rms.types.favorite;

/**
 * Created by aning on 8/15/2017.
 * -- the request parameters that getting all repos favorite files in a list fashion.
 */

public class AllRepoFavFileRequestParas {
    // note: We can use page and size fields to support pagination on client.
    // page num
    private int page;
    // Maximum number of data to return
    private int size;
    // support order by fileName, lastModifiedTime, size
    private String orderBy;
    // Search by filename. such as q.fileName=document
    private String searchText;

    /**
     * Default constructor, directly get favorite files, means don't support pagination, sorting and search.
     */
    public AllRepoFavFileRequestParas() {
        page = -1;
        size = -1;
        orderBy = null;
        searchText = null;
    }

    /**
     * @param orderBy    order type {@link OrderBy}
     * @param searchText search by file name.
     */
    public AllRepoFavFileRequestParas(String orderBy, String searchText) {
        page = -1;
        size = -1;
        this.orderBy = orderBy;
        this.searchText = searchText;
    }

    /**
     * @param page       page number
     * @param size       Maximum number of data to return. You can use page and size fields to support pagination on client.
     * @param orderBy    order type {@link OrderBy}
     * @param searchText search by file name.
     */
    public AllRepoFavFileRequestParas(int page, int size, String orderBy, String searchText) {
        this.page = page;
        this.size = size;
        this.orderBy = orderBy;
        this.searchText = searchText;
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

    public String getSearchText() {
        return searchText;
    }

    public static class OrderBy {
        public static final String ORDER_BY_FILENAME = "name";
        public static final String ORDER_BY_LAST_MODIFIED_TIME = "lastModifiedTime";
        public static final String ORDER_BY_SIZE = "fileSize";
        // if order by reverse, need to add '-' modifier.
        public static final String ORDER_BY_FILENAME_REVERSE = "-name";
        public static final String ORDER_BY_LAST_MODIFIED_TIME_REVERSE = "-lastModifiedTime";
        public static final String ORDER_BY_SIZE_REVERSE = "-fileSize";
    }
}
