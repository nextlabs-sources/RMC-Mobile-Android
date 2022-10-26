package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;

import java.io.File;

/**
 * Created by aning on 5/11/2017.
 */

public class CommandOperateEvent {
    private INxFile clickFileItem;
    private CmdOperate cmdOperate;

    public CommandOperateEvent(INxFile clickFileItem, CmdOperate cmdOperate) {
        this.clickFileItem = clickFileItem;
        this.cmdOperate = cmdOperate;
    }

    public INxFile getClickFileItem() {
        return clickFileItem;
    }

    public CmdOperate getCmdOperate() {
        return cmdOperate;
    }

    // for MySpace command Add operate: Add-protect & Add-share
    public static class CommandAddMsg {
        private File workingFile;
        private INxFile parentFolder;
        private CmdOperate cmdOperate;
        private FileFrom fileFrom;
        private BoundService boundService;
        // project id
        public IProject project;
        // project current working folder
        private String currentPathId = null;

        // for event post from CmdOperateFileActivity to ProtectShareActivity
        public CommandAddMsg(File workingFile, INxFile parentFolder, BoundService boundService, CmdOperate cmdOperate) {
            this.workingFile = workingFile;
            this.parentFolder = parentFolder;
            this.boundService = boundService;
            this.cmdOperate = cmdOperate;
        }

        // for mySpace Add file
        public CommandAddMsg(File workingFile, FileFrom fileFrom, CmdOperate cmdOperate) {
            this.workingFile = workingFile;
            this.fileFrom = fileFrom;
            this.cmdOperate = cmdOperate;
        }

        // for project Add file
        public CommandAddMsg(File workingFile, IProject p, String currentPathId, FileFrom fileFrom, CmdOperate cmdOperate) {
            this.workingFile = workingFile;
            this.project = p;
            this.currentPathId = currentPathId;
            this.fileFrom = fileFrom;
            this.cmdOperate = cmdOperate;
        }

        public int getProjectId() {
            return project.getId();
        }

        public String getCurrentPathId() {
            return currentPathId;
        }

        public File getWorkingFile() {
            return this.workingFile;
        }

        public FileFrom getFileFrom() {
            return this.fileFrom;
        }

        public INxFile getParentFolder() {
            return this.parentFolder;
        }

        public BoundService getBoundService() {
            return this.boundService;
        }

        public CmdOperate getCmdOperate() {
            return this.cmdOperate;
        }

        public String getProjectExpiry() {
            return project.getExpiry();
        }

        public String getProjectWatermark() {
            return project.getWatermark();
        }
    }

    // for mySpace and project command Scan a document
    public static class CommandScanMsg {
        private File file;
        private CmdOperate cmdOperate;
        private FileFrom fileFrom;
        // project current working folder
        private String currentParentId = null;
        private IProject project;

        // for mySpace scan
        public CommandScanMsg(File file, CmdOperate cmdOperate, FileFrom fileFrom) {
            this.file = file;
            this.cmdOperate = cmdOperate;
            this.fileFrom = fileFrom;
        }

        // for project scan
        public CommandScanMsg(IProject project, File file, String currentParentId, CmdOperate cmdOperate, FileFrom fileFrom) {
            this.project = project;
            this.file = file;
            this.currentParentId = currentParentId;
            this.cmdOperate = cmdOperate;
            this.fileFrom = fileFrom;
        }

        public IProject getProject() {
            return project;
        }

        public File getFile() {
            return file;
        }

        public int getProjectId() {
            return project.getId();
        }

        public String getCurrentParentId() {
            return currentParentId;
        }

        public CmdOperate getCmdOperate() {
            return cmdOperate;
        }

        public FileFrom getFileFrom() {
            return fileFrom;
        }

        public String getProjectExpiry() {
            return project.getExpiry();
        }

        public String getWatermark() {
            return project.getWatermark();
        }
    }

    // for project add from three party
    public static class CommandProjectAddFrom3D {
        public INxFile nxFile;
        public IProject project;
        public String currentPathId;
        public FileFrom fileFrom;
        public CmdOperate cmdOperate;

        public CommandProjectAddFrom3D(INxFile nxFile, IProject p, String currentPathId, FileFrom fileFrom, CmdOperate cmdOperate) {
            this.nxFile = nxFile;
            this.project = p;
            this.currentPathId = currentPathId;
            this.fileFrom = fileFrom;
            this.cmdOperate = cmdOperate;
        }

        public INxFile getNxFile() {
            return nxFile;
        }

        public void setNxFile(INxFile nxFile) {
            this.nxFile = nxFile;
        }

        public int getProjectId() {
            return project.getId();
        }

        public String getCurrentPathId() {
            return currentPathId;
        }

        public FileFrom getFileFrom() {
            return fileFrom;
        }

        public CmdOperate getCmdOperate() {
            return cmdOperate;
        }

        public String getProjectExpiry() {
            return project.getExpiry();
        }

        public String getWatermark() {
            return project.getWatermark();
        }
    }
}
