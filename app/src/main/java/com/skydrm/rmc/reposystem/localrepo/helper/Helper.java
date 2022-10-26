package com.skydrm.rmc.reposystem.localrepo.helper;

import android.util.Log;

import com.skydrm.rmc.reposystem.types.INxFile;

import java.io.File;


public class Helper {
    static public final String TAG = "NX_Helper";

    static public String getParent(INxFile file) {
        if (file == null)
            throw new NullPointerException("file is null");

        if (file.getLocalPath().equals("/")) {
            return "/";
        } else {
            int index = file.getLocalPath().lastIndexOf('/');
            if (index == -1)
                throw new RuntimeException("file not a standard path");
            return file.getLocalPath().substring(0, index + 1);
        }
    }

    static public String nxPath2AbsPath(File root, String origPath) {
        File f = new File(root, origPath);
        return f.getAbsolutePath();
    }

    static public String absPath2NxPath(File root, String absPath) {
        return absPath.substring(root.getAbsolutePath().length());
    }


    static public void makeSureDocExist(File file) {
        try {
            assert !file.isDirectory();
            if (!file.exists()) {
                //Bug: make sure the parent folder exist
                makeSureDirExist(file.getParentFile());

                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static public boolean makeSureDirExist(File file) {
        try {
            if (file.isDirectory()) {
                return true;
            }
            if (!file.exists()) {
                file.mkdirs();
            } else if (!file.isDirectory() && file.canWrite()) {
                file.delete();
                file.mkdirs();
            } else {
                Log.e(TAG, "error");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    static public void RecursionDeleteFile(File file) {

        // recursive outlet
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    static public void deleteFile(File file) {
        try {
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    /**
     * get myVault file from local
     */
    public static File getLocalMyVaultFile(String localPath) {
        File myVaultFile = new File(localPath);
        if (myVaultFile.exists()) {
            return myVaultFile;
        } else {
            return null;
        }
    }

    /**
     * get sharedWithMe file from local
     */
    public static File getLocalSharedWithMeFile(String localPath) {
        File sharedWithMe = new File(localPath);
        if (sharedWithMe.exists()) {
            return sharedWithMe;
        } else {
            return null;
        }
    }

    /**
     * get project file from local
     */
    public static File getLocalProjectFile(String localPath) {
        File projectFile = new File(localPath);
        if (projectFile.exists()) {
            return projectFile;
        } else {
            return null;
        }
    }

    /**
     * Judge the file if is Google file: google-doc, google-sheet, google-slide and google-draw.
     * @return
     */
    public static boolean isGoogleFile(INxFile document) {
        return document.getUserDefinedStr().equals("application/vnd.google-apps.document")
                || document.getUserDefinedStr().equals("application/vnd.google-apps.spreadsheet")
                || document.getUserDefinedStr().equals("application/vnd.google-apps.presentation")
                || document.getUserDefinedStr().equals("application/vnd.google-apps.drawing");
    }
}
