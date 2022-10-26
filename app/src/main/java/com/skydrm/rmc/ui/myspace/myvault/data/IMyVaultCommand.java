package com.skydrm.rmc.ui.myspace.myvault.data;

import android.content.Context;

import com.skydrm.rmc.ui.myspace.myvault.ICommand;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileParams;

/**
 * Created by hhu on 4/28/2018.
 */

public interface IMyVaultCommand extends ICommand {
    void uploadFileToMyVault(Context context,
                             MyVaultUploadFileParams params,
                             boolean bDisplayUi,
                             ICommandExecuteCallback<Result.UploadResult, Error> callBack);
}
