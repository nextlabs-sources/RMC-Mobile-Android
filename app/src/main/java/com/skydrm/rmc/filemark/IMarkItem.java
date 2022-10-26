package com.skydrm.rmc.filemark;

/**
 * Created by aning on 8/17/2017.
 */

public interface IMarkItem {

    /**
     * Get flag that is mark or unmark
     */
    boolean isUnMark();

    String getPathId();

    String getDisplayPath();

    String getParentFileId();

    long getFileSize();

    long getLastModifiedTime();
}
