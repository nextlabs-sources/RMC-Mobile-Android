package com.skydrm.rmc.engine.eventBusMsg;

import android.support.annotation.Nullable;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;
import com.skydrm.sdk.rms.types.RemoteViewResult2;

/**
 * Created by aning on 5/25/2017.
 */

public class MyVaultFileShareEvent {
    private INxlFile iMyVaultFileEntry;
    private MyVaultMetaDataResult myVaultMetaData;
    private FileFrom fileFrom;
    private CmdOperate cmdOperate;
    private boolean bNxlFile;
    // simple remote view result data
    private RemoteViewResult2.ResultsBean mRemoteViewResultBean;

    public MyVaultFileShareEvent(INxlFile iMyVaultFileEntry,
                                 @Nullable MyVaultMetaDataResult myVaultMetaData,
                                 FileFrom fileFrom,
                                 CmdOperate cmdOperate,
                                 boolean bNxlFile,
                                 @Nullable RemoteViewResult2.ResultsBean remoteViewResultBean) {
        this.iMyVaultFileEntry = iMyVaultFileEntry;
        this.myVaultMetaData = myVaultMetaData;
        this.fileFrom = fileFrom;
        this.cmdOperate = cmdOperate;
        this.bNxlFile = bNxlFile;
        this.mRemoteViewResultBean = remoteViewResultBean;
    }

    public RemoteViewResult2.ResultsBean getRemoteViewResultBean() {
        return mRemoteViewResultBean;
    }

    public INxlFile getMyVaultFileEntry() {
        return iMyVaultFileEntry;
    }

    public MyVaultMetaDataResult getMyVaultMetaData() {
        return myVaultMetaData;
    }

    public FileFrom getFileFrom() {
        return fileFrom;
    }

    public CmdOperate getCmdOperate() {
        return cmdOperate;
    }

    public boolean isbNxlFile() {
        return bNxlFile;
    }
}
