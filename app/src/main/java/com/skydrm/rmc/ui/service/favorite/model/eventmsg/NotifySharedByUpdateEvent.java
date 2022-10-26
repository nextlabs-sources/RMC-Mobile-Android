package com.skydrm.rmc.ui.service.favorite.model.eventmsg;

import com.skydrm.rmc.ui.myspace.myvault.model.domain.VaultType;

/**
 * Created by hhu on 8/30/2017.
 */

public class NotifySharedByUpdateEvent {
    private VaultType mFileType = VaultType.TYPE_ALL_FILES;

    public NotifySharedByUpdateEvent(VaultType vaultType) {
        this.mFileType = vaultType;
    }

    public VaultType getFileType() {
        return mFileType;
    }
}
