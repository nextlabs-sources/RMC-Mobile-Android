package com.skydrm.rmc.ui.myspace.myvault.data;

import android.content.Context;

import com.skydrm.rmc.ui.myspace.myvault.data.task.MyVaultFileUploadTask;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileParams;

/**
 * Created by hhu on 4/28/2018.
 */

public class MyVaultCommandExecutor implements IMyVaultCommand {

    @Override
    public void uploadFileToMyVault(Context context,
                                    MyVaultUploadFileParams params,
                                    boolean bDisplayUi,
                                    ICommandExecuteCallback<Result.UploadResult, Error> callBack) {
        MyVaultFileUploadTask uploadTask = new MyVaultFileUploadTask(context, params, bDisplayUi, callBack);
        uploadTask.run();
    }
}
