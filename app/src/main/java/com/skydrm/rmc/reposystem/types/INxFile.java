package com.skydrm.rmc.reposystem.types;

import java.util.List;

public interface INxFile {


    boolean isFolder();

    boolean isSite();   // Special for SharePoint/SharePointOnline

    boolean isMarkedAsFavorite();

    boolean isMarkedAsOffline();

    @Deprecated
    boolean isCached(); // Local Disk has this file, not stable

    boolean isNewCreated();

    String getName();

    // special designed for UI usage
    String getDisplayPath();

    String getLocalPath();

    String getCloudPath();

    String getCloudFileID();

    long getSize();

    long getLastModifiedTimeLong();

    String getParent();

    boolean hasChildren();

    List<INxFile> getChildren();

    @Deprecated
    INxFile findNode(String path);

    BoundService getService();

    /**
     *  in order to compitable with Google drive's file type :   google-doc, google-slide, google-sheet, google-draw
     *
     */
    String getUserDefinedStr();

}
