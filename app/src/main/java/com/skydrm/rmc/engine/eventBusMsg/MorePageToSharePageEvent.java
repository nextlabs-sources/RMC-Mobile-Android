package com.skydrm.rmc.engine.eventBusMsg;

import android.support.annotation.Nullable;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.types.RemoteViewResult2;

import java.io.File;

/**
 * Created by aning on 5/10/2017.
 */

public class MorePageToSharePageEvent {
    private File workingFile;
    private INxFile clickFileItem;
    private INxlFileFingerPrint fileFingerPrint;
    private CmdOperate cmdOperate;
    private boolean bNxlFile;
    // simple remote view result data
    private RemoteViewResult2.ResultsBean mRemoteViewResultBean;
    // for myVault view file info
    private INxlFile mIMyVaultFileEntry;

    public MorePageToSharePageEvent(INxlFile mIMyVaultFileEntry,
                                    File workingFile,
                                    INxFile clickFileItem,
                                    INxlFileFingerPrint fileFingerPrint,
                                    CmdOperate cmdOperate,
                                    boolean bNxlFile,
                                    @Nullable RemoteViewResult2.ResultsBean remoteViewResultBean) {
        this.mIMyVaultFileEntry = mIMyVaultFileEntry;
        this.workingFile = workingFile;
        this.clickFileItem = clickFileItem;
        this.fileFingerPrint = fileFingerPrint;
        this.cmdOperate = cmdOperate;
        this.bNxlFile = bNxlFile;
        this.mRemoteViewResultBean = remoteViewResultBean;
    }

    public INxlFile getIMyVaultFileEntry() {
        return mIMyVaultFileEntry;
    }

    public RemoteViewResult2.ResultsBean getRemoteViewResultBean() {
        return mRemoteViewResultBean;
    }

    public File getWorkingFile() {
        return workingFile;
    }

    public INxFile getClickFileItem() {
        return clickFileItem;
    }

    public INxlFileFingerPrint getFileFingerPrint() {
        return fileFingerPrint;
    }

    public CmdOperate getCmdOperate() {
        return cmdOperate;
    }

    public boolean isbNxlFile() {
        return bNxlFile;
    }
}
