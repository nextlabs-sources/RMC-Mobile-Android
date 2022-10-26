package com.skydrm.rmc.datalayer.repo.workspace;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlFolder;
import com.skydrm.rmc.dbbridge.IDBWorkSpaceFileItem;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.utils.sort.IBaseSortable;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.workspace.DeleteItemResult;

import java.io.IOException;
import java.util.List;

public class WorkSpaceNode extends NxlFolder implements IBaseSortable {
    private IDBWorkSpaceFileItem mDBItem;

    private WorkSpaceNode(IDBWorkSpaceFileItem item,
                          List<INxlFile> children) {
        super(item.getName(), item.getPathId(), item.getPathDisplay(),
                item.getLastModified(), item.getCreationTime(), children);

        this.mDBItem = item;
    }

    @Override
    public void download(int type, DownloadListener listener)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException, IOException {

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

    @Override
    public void delete()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();

        try {
            DeleteItemResult result = session.getRmsRestAPI()
                    .getWorkSpaceService(session.getRmUser())
                    .deleteItem(mPathId);

            if (result != null) {
                deleteLocal();
            }

        } catch (RmsRestAPIException e) {
            // Means file maybe deleted remote.
            // In this case we should delete local cache.
            if (e.getDomain() ==
                    RmsRestAPIException.ExceptionDomain.FileNotFound) {
                deleteLocal();
            }
            throw e;
        }

    }

    private void deleteLocal() {
        WorkSpaceFileService fs = new WorkSpaceFileService();
        fs.deleteFile(mPathId, true);

        mDBItem = null;
    }

    static WorkSpaceNode newByDBItem(IDBWorkSpaceFileItem item,
                                     List<INxlFile> children) {
        return new WorkSpaceNode(item, children);
    }

}
