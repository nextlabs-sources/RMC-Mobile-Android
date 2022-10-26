package com.skydrm.rmc.datalayer.repo.library;

import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.datalayer.repo.base.FileServiceBase;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class LibraryFileService extends FileServiceBase<INxlFile, LibraryFileType> implements Parcelable {

    LibraryFileService() {

    }

    private LibraryFileService(Parcel in) {
    }

    public static final Creator<LibraryFileService> CREATOR = new Creator<LibraryFileService>() {
        @Override
        public LibraryFileService createFromParcel(Parcel in) {
            return new LibraryFileService(in);
        }

        @Override
        public LibraryFileService[] newArray(int size) {
            return new LibraryFileService[size];
        }
    };

    @Override
    protected List<INxlFile> syncCurrentPath(String pathId)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return listCurrentPath(pathId);
    }

    @Override
    protected List<INxlFile> syncTree(String pathId)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return listTree(pathId);
    }

    @Override
    protected List<INxlFile> listTree(String pathId) {
        List<INxlFile> tree = new ArrayList<>();
        List<INxlFile> current = listCurrentPath(pathId);
        if (current == null || current.isEmpty()) {
            return tree;
        }
        for (INxlFile f : current) {
            tree.add(f);
            if (f.isFolder()) {
                tree.addAll(listTree(f.getPathId()));
            }
        }
        return current;
    }

    @Override
    protected List<INxlFile> listCurrentPath(String pathId) {
        List<INxlFile> ret = new ArrayList<>();
        List<LibraryFileType> current = listInternal(pathId);
        if (current == null || current.isEmpty()) {
            return ret;
        }
        for (LibraryFileType type : current) {
            if (type == null) {
                continue;
            }
            if (type.isFolder()) {
                ret.add(newByDBItem(type, null));
            } else {
                ret.add(newByDBItem(type));
            }
        }
        return ret;
    }

    @Override
    protected List<LibraryFileType> listInternal(String pathId) {
        List<LibraryFileType> ret = new ArrayList<>();
        File root = new File(pathId);
        if (!root.exists() || !root.isDirectory()) {
            return ret;
        }
        File[] tree = root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isHidden();
            }
        });
        if (tree == null || tree.length == 0) {
            return ret;
        }
        for (File f : tree) {
            ret.add(new LibraryFileType(f));
        }
        return ret;
    }

    @Override
    protected INxlFile newByDBItem(LibraryFileType f) {
        return new LibraryFile(f.getName(), "", f.getFileSize(),
                "", f.getPathId(), f.getPathId(),
                f.getPathId(), false, false,
                -1, -1, -1,
                f.getLastModified(), f.getCreationTime());
    }

    @Override
    protected INxlFile newByDBItem(LibraryFileType f, List<INxlFile> children) {
        return new LibraryNode(f.getName(), f.getPathId(), f.getPathId(),
                f.getLastModified(), f.getCreationTime(), children);
    }

    @Override
    public boolean deleteFile(String pathId, boolean recursively) {
        File f = new File(pathId);
        if (!f.exists()) {
            return false;
        }
        if (f.isDirectory()) {
            for (File sub : f.listFiles()) {
                if (!sub.exists()) {
                    continue;
                }
                if (sub.isDirectory()) {
                    deleteFile(sub.getPath(), true);
                } else {
                    sub.delete();
                }
            }
            return true;
        } else {
            return f.delete();
        }
    }

    @Override
    public boolean createFolder(String parentPathId, String name, boolean autoRename)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        File f = new File(parentPathId, name);
        if (f.exists() && f.isDirectory()) {
            return true;
        }
        return f.mkdirs();
    }

    @Override
    public boolean uploadFile(String pathId, File nxlFile)
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
