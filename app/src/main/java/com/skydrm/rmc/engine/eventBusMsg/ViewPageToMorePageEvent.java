package com.skydrm.rmc.engine.eventBusMsg;

import android.support.annotation.Nullable;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.project.service.ProjectFileToViewParameter;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.engine.enumData.FileType;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.sdk.rms.types.RemoteViewResult2;

import java.io.File;

/**
 * Created by aning on 5/10/2017.
 */

// Event bus message that sending from viewActivity page to MoreActivity page
public class ViewPageToMorePageEvent {
    private File workingFile;
    private INxFile clickFileItem;
    private FileType fileType;
    private FileFrom fileFrom;
    private boolean bNxlFile;
    // project file list fileBean
    private ProjectFileToViewParameter mProjectFileToViewParameter;
    // project name
    private int projectId;
    private String projectName;
    // for myVault entry
    private INxlFile mFileBase;
    // remote view result data
    private RemoteViewResult2.ResultsBean mRemoteViewResultBean;
    private boolean offline;

    public ViewPageToMorePageEvent(File workingFile,
                                   INxFile clickFileItem,
                                   FileType fileType,
                                   FileFrom fileFrom,
                                   boolean bNxlFile,
                                   @Nullable RemoteViewResult2.ResultsBean mRemoteViewResultBean) {
        this.workingFile = workingFile;
        this.clickFileItem = clickFileItem;
        this.fileType = fileType;
        this.fileFrom = fileFrom;
        this.bNxlFile = bNxlFile;
        this.mRemoteViewResultBean = mRemoteViewResultBean;
    }

    // for project, should need the project owner id(userId)
    public ViewPageToMorePageEvent(File workingFile,
                                   INxFile clickFileItem,
                                   FileType fileType,
                                   FileFrom fileFrom,
                                   boolean bNxlFile,
                                   ProjectFileToViewParameter projectFileToViewParameter,
                                   String projectName,
                                   @Nullable RemoteViewResult2.ResultsBean mRemoteViewResultBean) {
        this.workingFile = workingFile;
        this.clickFileItem = clickFileItem;
        this.fileType = fileType;
        this.fileFrom = fileFrom;
        this.bNxlFile = bNxlFile;
        this.mProjectFileToViewParameter = projectFileToViewParameter;
        this.projectName = projectName;
        this.mRemoteViewResultBean = mRemoteViewResultBean;
    }

    // for myVault, should need myVault entry
    public ViewPageToMorePageEvent(File workingFile,
                                   INxFile clickFileItem,
                                   FileType fileType,
                                   FileFrom fileFrom,
                                   boolean bNxlFile,
                                   INxlFile f,
                                   @Nullable RemoteViewResult2.ResultsBean mRemoteViewResultBean) {
        this.workingFile = workingFile;
        this.clickFileItem = clickFileItem;
        this.fileType = fileType;
        this.fileFrom = fileFrom;
        this.bNxlFile = bNxlFile;
        this.mFileBase = f;
        this.mRemoteViewResultBean = mRemoteViewResultBean;
    }

    public RemoteViewResult2.ResultsBean getmRemoteViewResultBean() {
        return mRemoteViewResultBean;
    }

    public INxlFile getMyVaultFileEntry() {
        return mFileBase;
    }

    public ProjectFileToViewParameter getProjectFileToViewParameter() {
        return mProjectFileToViewParameter;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public File getWorkingFile() {
        return workingFile;
    }

    public INxFile getClickFileItem() {
        return clickFileItem;
    }

    public FileType getFileType() {
        return fileType;
    }

    public FileFrom getFileFrom() {
        return fileFrom;
    }

    public boolean isbNxlFile() {
        return bNxlFile;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}
