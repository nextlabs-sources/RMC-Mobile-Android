package com.skydrm.rmc.datalayer.repo.base;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.dbbridge.base.Owner;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.sdk.rms.rest.workspace.ListFileResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    static boolean isDirectChild(String parent, String child) {
        if (parent == null || parent.isEmpty()) {
            return false;
        }
        if (child == null || child.isEmpty()) {
            return false;
        }
        if (!child.startsWith(parent)) {
            return false;
        }
        if (parent.length() == child.length()) {
            return false;
        }
        // /  /1.txt /test/  /test/a/ /test/a/b/
        int idx = child.indexOf("/", parent.length());
        if (idx == -1) {
            //Direct file
            return true;
        }
        //Direct folder.
        return idx == child.length() - 1;
    }

    public static String generateUploaderRawJson(ListFileResult.ResultsBean.DetailBean.FilesBean.UploaderBean uploader) {
        String ret = "{}";
        if (uploader == null) {
            return ret;
        }
        return Owner.generateRawJson(
                uploader.getUserId(),
                uploader.getDisplayName(),
                uploader.getEmail());
    }

    public static String generateLastModifiedUserRawJson(ListFileResult.ResultsBean.DetailBean.FilesBean.LastModifiedUserBean modifier) {
        String ret = "{}";
        if (modifier == null) {
            return ret;
        }
        return Owner.generateRawJson(
                modifier.getUserId(),
                modifier.getDisplayName(),
                modifier.getEmail());
    }

    public static File getProjectsMountPoint(String projectName) {
        try {
            boolean dirMakeSucceed = true;
            File userRootFile = SkyDRMApp.getInstance().getCommonDirs().userRootFile();
            File projectsMountPoint = new File(userRootFile, "projects");
            if (!projectsMountPoint.exists()) {
                dirMakeSucceed = projectsMountPoint.mkdirs();
            }

            if (dirMakeSucceed) {
                projectsMountPoint = new File(projectsMountPoint, projectName);
                if (!projectsMountPoint.exists()) {
                    dirMakeSucceed = projectsMountPoint.mkdirs();
                }

                if (dirMakeSucceed) {
                    projectsMountPoint = new File(projectsMountPoint, "ROOT");
                    if (!Helper.makeSureDirExist(projectsMountPoint)) {
                        throw new RuntimeException("make projects point failed" + projectsMountPoint.getPath());
                    }
                }
            }

            return projectsMountPoint;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getWorkSpaceMountPoint() throws SessionInvalidException {
        File userRootDir = SkyDRMApp.getInstance().getCommonDirs().userRootFile();
        File workSpaceRootDir = new File(userRootDir, "workspace");
        boolean dirMakeSucceed = true;
        if (!workSpaceRootDir.exists()) {
            dirMakeSucceed = workSpaceRootDir.mkdirs();
        }
        if (dirMakeSucceed) {
            workSpaceRootDir = new File(workSpaceRootDir, "ROOT");
            if (!makeSureDirExist(workSpaceRootDir)) {
                throw new RuntimeException(String.format("Create workspace mount point %s failed.",
                        workSpaceRootDir.getPath()));
            }
        }
        return workSpaceRootDir;
    }

    public static File getTmpMountPoint() throws SessionInvalidException {
        File userRootDir = SkyDRMApp.getInstance().getCommonDirs().userRootFile();
        File tmpRootDir = new File(userRootDir, "tmp");
        boolean dirMakeSucceed = true;
        if (!tmpRootDir.exists()) {
            dirMakeSucceed = tmpRootDir.mkdirs();
        }
        if (dirMakeSucceed) {
            return tmpRootDir;
        }
        throw new RuntimeException(String.format("Create tmp mount point %s failed.",
                tmpRootDir.getPath()));
    }

    private static boolean makeSureDirExist(File file) {
        if (file.isDirectory()) {
            return true;
        }
        if (!file.exists()) {
            file.mkdirs();
        } else if (!file.isDirectory() && file.canWrite()) {
            file.delete();
            file.mkdirs();
        } else {
            return false;
        }
        return true;
    }

    public static List<String> translateToStringList(List<Integer> inputs) {
        List<String> ret = new ArrayList<>();
        if (inputs == null || inputs.isEmpty()) {
            return ret;
        }
        for (Integer id : inputs) {
            ret.add(String.valueOf(id));
        }
        return ret;
    }

    public static List<Integer> translateToIntegerList(List<String> inputs) {
        List<Integer> ret = new ArrayList<>();
        if (inputs == null || inputs.isEmpty()) {
            return ret;
        }
        for (String s : inputs) {
            ret.add(Integer.valueOf(s));
        }
        return ret;
    }
}
