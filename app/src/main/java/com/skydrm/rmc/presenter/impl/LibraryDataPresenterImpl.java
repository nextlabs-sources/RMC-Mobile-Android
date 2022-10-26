package com.skydrm.rmc.presenter.impl;

import android.text.TextUtils;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.presenter.ILibraryDataPresenter;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.utils.sort.SortContext;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 5/11/2017.
 */
@Deprecated
public class LibraryDataPresenterImpl implements ILibraryDataPresenter {
    private static DevLog log = new DevLog(LibraryDataPresenterImpl.class.getSimpleName());
    private String pathIndex = "/";
    private String displayPath = "/";
    private INxFile iNxFile;
    private INxFile destFolder;

    @Override
    public List<NXFileItem> loadRoots(BoundService service) {
        iNxFile = SkyDRMApp.getInstance().getRepoSystem().folderTreeClone(service);
        if (iNxFile != null) {
            destFolder = iNxFile;
            List<INxFile> children = iNxFile.getChildren();
            return sort(children);
        }
        return null;
    }

    @Override
    public List<NXFileItem> loadFilterRoots(BoundService service, String fileter) {
        iNxFile = SkyDRMApp.getInstance().getRepoSystem().folderTreeClone(service);
        if (iNxFile != null) {
            destFolder = iNxFile;
            return sort(filter(iNxFile.getChildren(), fileter));
        }
        return null;
    }

    @Override
    public List<NXFileItem> getChildren(INxFile subSite) {
        destFolder = subSite;
        pathIndex = subSite.getLocalPath();
        displayPath = subSite.getDisplayPath();
        return sort(subSite.getChildren());
    }

    @Override
    public List<NXFileItem> getFilterChildren(INxFile subSite, String filter) {
        destFolder = subSite;
        pathIndex = subSite.getLocalPath();
        displayPath = subSite.getDisplayPath();
        return sort(filter(subSite.getChildren(), filter));
    }

    @Override
    public List<NXFileItem> getParentFile() {
        //  find parent by pathIndex, and list parent's children
        if (0 != pathIndex.lastIndexOf('/')) {
            pathIndex = pathIndex.substring(0, pathIndex.lastIndexOf('/'));
        } else {
            pathIndex = "/";
        }
        INxFile node = iNxFile.findNode(pathIndex);
        destFolder = node;
        displayPath = node.getDisplayPath();
        return sort(node.getChildren());
    }

    @Override
    public List<NXFileItem> getFilterParentFile(String filter) {
        //  find parent by pathIndex, and list parent's children
        if (0 != pathIndex.lastIndexOf('/')) {
            pathIndex = pathIndex.substring(0, pathIndex.lastIndexOf('/'));
        } else {
            pathIndex = "/";
        }
        INxFile node = iNxFile.findNode(pathIndex);
        destFolder = node;
        displayPath = node.getDisplayPath();
        return sort(filter(node.getChildren(), filter));
    }

    @Override
    public String getDisplayPath() {
        return displayPath;
    }

    @Override
    public INxFile getDestFolder() {
        return destFolder;
    }

    @Override
    public boolean isRoot() {
        return pathIndex.equals("/");
    }

    public List<NXFileItem> sort(List<INxFile> items) {
        return SortContext.sortRepoFile(items, SortType.NAME_ASCEND);
    }

    public List<INxFile> filter(List<INxFile> srcFiles, String filter) {
        List<INxFile> destFiles = new ArrayList<>();
        if (srcFiles != null && !srcFiles.isEmpty()) {
            for (INxFile nxFile : srcFiles) {
                if (!TextUtils.isEmpty(nxFile.getName()) && !nxFile.getName().endsWith(filter)) {
                    destFiles.add(nxFile);
                }
            }
        }
        return destFiles;
    }
}
