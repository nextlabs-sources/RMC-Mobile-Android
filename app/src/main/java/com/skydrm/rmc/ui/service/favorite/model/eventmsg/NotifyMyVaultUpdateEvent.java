package com.skydrm.rmc.ui.service.favorite.model.eventmsg;

import com.skydrm.rmc.ui.myspace.myvault.model.domain.VaultType;

/**
 * Created by hhu on 8/30/2017.
 */

public class NotifyMyVaultUpdateEvent {
    private VaultType mFileType;

    public NotifyMyVaultUpdateEvent(VaultType vaultType) {
        this.mFileType = vaultType;
    }

    public VaultType getFileType() {
        return mFileType;
    }
}
