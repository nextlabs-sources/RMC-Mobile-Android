package com.skydrm.rmc.engine.intereface;

import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileResult;

/**
 * Created by aning on 5/14/2017.
 */

public interface IProtectComplete {
    void onProtectComplete(MyVaultUploadFileResult result);
}
