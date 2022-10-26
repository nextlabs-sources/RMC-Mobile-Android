package com.skydrm.rmc.engine.eventBusMsg;

/**
 * Created by hhu on 6/5/2017.
 */

/**
 * this msg is EventBus msg used for notify data list in MySpace files change.
 */
public class FolderCreateEvent {
    private boolean isSyntheticRoot;
    private String folderName;

    public FolderCreateEvent(boolean syntheticRoot, String folderName) {
        this.isSyntheticRoot = syntheticRoot;
        this.folderName = folderName;
    }

    public boolean isSyntheticRoot() {
        return isSyntheticRoot;
    }

    public String getFolderName() {
        return folderName;
    }
}
