package com.skydrm.rmc.datalayer.repo.library;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlFolder;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.utils.sort.IBaseSortable;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.io.IOException;
import java.util.List;

public class LibraryNode extends NxlFolder implements IBaseSortable {

    LibraryNode(String name, String pathId, String pathDisplay,
                long lastModified, long creationTime,
                List<INxlFile> children) {
        super(name, pathId, pathDisplay,
                lastModified, creationTime,
                children);
    }

    @Override
    public void download(int type, DownloadListener listener)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException, IOException {

    }

    @Override
    public void delete()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {

    }

    @Override
    public String getSortableName() {
        return mName;
    }

    @Override
    public long getSortableSize() {
        return 0;
    }

    @Override
    public long getSortableTime() {
        return mLastModifiedTime;
    }
}
