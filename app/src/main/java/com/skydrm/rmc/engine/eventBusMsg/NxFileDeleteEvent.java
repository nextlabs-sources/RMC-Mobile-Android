package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.reposystem.types.INxFile;

/**
 * Created by hhu on 6/2/2017.
 */

/**
 * The event is used for EventBus post ui update msg,when delete file action occurred.
 */
public class NxFileDeleteEvent {
    private INxFile mDeletedItem;
    private boolean isSyntheticRoot;
    private int position;

    public NxFileDeleteEvent(boolean root, INxFile deletedItem, int notifyPosition) {
        this.isSyntheticRoot = root;
        this.mDeletedItem = deletedItem;
        this.position = notifyPosition;
    }

    public boolean isSyntheticRoot() {
        return isSyntheticRoot;
    }

    public INxFile getDeletedItem() {
        return mDeletedItem;
    }

    public int getPosition() {
        return position;
    }
}
