package com.skydrm.rmc.domain;

/**
 * Created by hhu on 6/9/2017.
 */

public class LocalFileItem {
    private ILocalFile localFile;
    private String title;

    public LocalFileItem(ILocalFile localFile, String title) {
        this.localFile = localFile;
        this.title = title;
    }

    public ILocalFile getLocalFile() {
        return localFile;
    }

    public void setLocalFile(ILocalFile localFile) {
        this.localFile = localFile;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
