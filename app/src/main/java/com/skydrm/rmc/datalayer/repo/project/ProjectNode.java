package com.skydrm.rmc.datalayer.repo.project;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlFolder;
import com.skydrm.rmc.dbbridge.IDBProjectFileItem;
import com.skydrm.rmc.dbbridge.IDBProjectItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.utils.sort.IBaseSortable;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.io.IOException;
import java.util.List;

public class ProjectNode extends NxlFolder implements IBaseSortable {
    private int mDownloadFinished;
    private int mTotal;
    private IOwner mOwner;
    private IOwner mLastModifiedUser;

    private IDBProjectFileItem mDBItem;

    private ProjectNode(String name, String pathId, String pathDisplay,
                        IOwner owner, IOwner lastModifiedUser,
                        long lastModifiedTime, long creationTime,
                        List<INxlFile> children) {
        super(name, pathId, pathDisplay, lastModifiedTime, creationTime, children);

        this.mOwner = owner;
        this.mLastModifiedUser = lastModifiedUser;
    }

    private ProjectNode(IDBProjectFileItem item,
                        List<INxlFile> children) {
        super(item.getName(), item.getPathId(), item.getPathDisplay(),
                item.getLastModified(), item.getCreationTime(), children);
        this.mDBItem = item;

        this.mOwner = item.getOwner();
        this.mLastModifiedUser = item.getLastModifiedUser();
    }

    @Override
    public void download(int type, final DownloadListener listener) throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException, IOException {
        final List<INxlFile> children = getChildren();
        if (children != null && children.size() != 0) {
            mTotal = children.size();
            for (INxlFile c : children) {
                c.download(type, new DownloadListener() {
                    @Override
                    public void onProgress(int i) {

                    }

                    @Override
                    public void onComplete() {
                        mDownloadFinished++;
                        listener.onProgress((int) ((mDownloadFinished * 1.0
                                / mTotal) * 100));
                    }

                    @Override
                    public void cancel() {
                        mTotal--;
                    }
                });
            }
        }
    }

    @Override
    public void delete() throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException {
        if (mDBItem == null) {
            return;
        }
        IDBProjectItem idbProjectItem = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());

        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        boolean delete = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .deleteFile(idbProjectItem.getId(), mPathId);

        if (delete) {
            FileService fs = new FileService(idbProjectItem.getId(),
                    idbProjectItem.getProjectTBPK());
            fs.deleteFile(mPathId, true);

            mDBItem = null;
        }

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

    public IOwner getOwner() {
        return mOwner;
    }

    public IOwner getLastModifiedUser() {
        return mLastModifiedUser;
    }

    static ProjectNode newByDBItem(IDBProjectFileItem item,
                                   List<INxlFile> children) {
        return new ProjectNode(item, children);
    }
}
