package com.skydrm.rmc.datalayer.repo.base;

import com.skydrm.rmc.ui.service.offline.IOfflineCallback;

import java.util.List;

public abstract class NxlFolder extends NxlFileBase {
    protected String id;
    private List<INxlFile> mChildren;

    public NxlFolder(String name,
                     String pathId, String pathDisplay,
                     long lastModified, long creationTime,
                     List<INxlFile> children) {
        super(name, pathId, pathDisplay, lastModified, creationTime);
        this.mChildren = children;
    }

    @Override
    public List<INxlFile> getChildren() {
        return mChildren;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public void markAsOffline(IOfflineCallback callback) {

    }

    @Override
    public void markAsFavorite() {

    }

    @Override
    public void unMarkAsOffline() {

    }

    @Override
    public void unMarkAsFavorite() {

    }

    @Override
    public void clearCache() {
        List<INxlFile> children = getChildren();
        if (children == null || children.size() == 0) {
            return;
        }
        for (INxlFile f : children) {
            if (f == null) {
                continue;
            }
            f.clearCache();
        }
    }

    @Override
    public long getCacheSize() {
        long ret = 0;
        List<INxlFile> children = getChildren();
        if (children == null || children.size() == 0) {
            return ret;
        }
        for (INxlFile f : children) {
            if (f == null) {
                continue;
            }
            ret += f.getCacheSize();
        }
        return ret;
    }
}
