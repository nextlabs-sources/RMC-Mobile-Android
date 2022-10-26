package com.skydrm.rmc.datalayer.repo.workspace;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.workspace.WorkSpaceFileBean;
import com.skydrm.rmc.datalayer.repo.base.FileServiceBase;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.Utils;
import com.skydrm.rmc.dbbridge.DBProvider;
import com.skydrm.rmc.dbbridge.IDBWorkSpaceFileItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.workspace.CreateFolderResult;
import com.skydrm.sdk.rms.rest.workspace.ListFileParam;
import com.skydrm.sdk.rms.rest.workspace.ListFileResult;
import com.skydrm.sdk.rms.rest.workspace.UploadFileResult;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkSpaceFileService extends FileServiceBase<INxlFile, IDBWorkSpaceFileItem> implements Runnable, Parcelable {
    private static final DevLog mLog = new DevLog(WorkSpaceFileService.class.getSimpleName());

    private long mUsage;
    private long mQuota;
    private int mTotalFiles;

    WorkSpaceFileService() {

    }

    private WorkSpaceFileService(Parcel in) {
    }

    public WorkSpaceInfo getWorkSpaceInfo()
            throws InvalidRMClientException, RmsRestAPIException, SessionInvalidException {
        syncByLayer("");
        return new WorkSpaceInfo(mUsage, mQuota, mTotalFiles);
    }

    @Override
    protected List<IDBWorkSpaceFileItem> listInternal(String pathId) {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .queryAllWorkSpaceFileItem();
    }

    @Override
    protected INxlFile newByDBItem(IDBWorkSpaceFileItem item) {
        return WorkSpaceFile.newByDBItem(item);
    }

    @Override
    protected INxlFile newByDBItem(IDBWorkSpaceFileItem item, List<INxlFile> children) {
        return WorkSpaceNode.newByDBItem(item, children);
    }

    @Override
    protected List<INxlFile> syncCurrentPath(String pathId)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes = syncByLayer(pathId);
        List<IDBWorkSpaceFileItem> locals = listByLayer(pathId);
        if (filterModifiedItems(remotes, locals, false)) {
            return listCurrentPath(pathId);
        }
        return adapt2NxlItem(locals);
    }

    @Override
    protected List<INxlFile> syncTree(String pathId)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes = buildRemoteTreeLists(pathId);
        List<IDBWorkSpaceFileItem> locals = buildLocalTreeList(pathId);
        if (filterModifiedItems(remotes, locals, true)) {
            return listTree(pathId);
        }
        return adaptList2Tree(locals, pathId);
    }

    @Override
    public boolean deleteFile(String pathId, boolean recursively) {
        if (recursively) {
            //delete children.
            List<Integer> deletedItem = findDeletedItem(listInternal("/"), pathId);
            if (deletedItem != null && deletedItem.size() != 0) {
                SkyDRMApp.getInstance()
                        .getDBProvider()
                        .batchDeleteWorkSpaceFileItem(deletedItem);
            }
        }

        SkyDRMApp.getInstance()
                .getDBProvider()
                .deleteWorkSpaceFileItem(pathId);

        return true;
    }

    @Override
    public boolean createFolder(String parentPathId, String name, boolean autoRename)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        IRmUser rmUser = session.getRmUser();

        CreateFolderResult result = session.getRmsRestAPI()
                .getWorkSpaceService(rmUser)
                .createFolder(parentPathId, name, autoRename);

        if (result != null) {
            List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes = syncByLayer(parentPathId);
            List<IDBWorkSpaceFileItem> locals = listByLayer(parentPathId);
            return filterModifiedItems(remotes, locals, false);
        }

        return false;
    }

    @Override
    public boolean uploadFile(String pathId, File nxlFile)
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        IRmUser user = session.getRmUser();

        UploadFileResult result = session.getRmsRestAPI()
                .getWorkSpaceService(user)
                .uploadFile(nxlFile.getPath(), pathId, 0, new ProgressRequestListener() {
                    @Override
                    public void onRequestProgress(long bytesWritten, long contentLength, boolean done)
                            throws IOException {
                        mLog.d("uploadWorkSpaceFile" + bytesWritten);
                    }
                });

        if (result != null) {
            List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes = syncByLayer(pathId);
            List<IDBWorkSpaceFileItem> locals = listByLayer(pathId);
            return filterModifiedItems(remotes, locals, false);
        }

        return false;
    }

    private List<Integer> findDeletedItem(List<IDBWorkSpaceFileItem> treeList, String parent) {
        List<Integer> ret = new ArrayList<>();
        if (treeList == null || treeList.size() == 0) {
            return ret;
        }
        List<IDBWorkSpaceFileItem> items = listByLayer(treeList, parent);
        if (items == null || items.size() == 0) {
            return ret;
        }
        for (IDBWorkSpaceFileItem child : items) {
            ret.add(child.getWorkSpaceFileTBPK());
            if (child.isFolder()) {
                ret.addAll(findDeletedItem(treeList, child.getPathId()));
            }
        }
        return ret;
    }

    List<INxlFile> listOffline() {
        List<INxlFile> ret = new ArrayList<>();
        List<IDBWorkSpaceFileItem> treeList = listInternal("/");
        if (treeList == null || treeList.size() == 0) {
            return ret;
        }
        List<IDBWorkSpaceFileItem> items = new ArrayList<>();
        for (IDBWorkSpaceFileItem item : treeList) {
            if (item == null) {
                continue;
            }
            if (item.isOffline()) {
                items.add(item);
            }
        }
        ret.addAll(adapt2NxlItem(items));
        return ret;
    }

    void onHeartBeat() {
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.REGULAR_BACK_GROUND).execute(this);
    }

    private boolean filterModifiedItems(List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes,
                                        List<IDBWorkSpaceFileItem> locals,
                                        boolean recursively) {
        DBProvider dbProvider = SkyDRMApp.getInstance().getDBProvider();
        if (remotes == null || remotes.isEmpty()) {
            if (locals == null || locals.isEmpty()) {
                return false;
            }
            List<Integer> _ids = new ArrayList<>();
            for (IDBWorkSpaceFileItem l : locals) {
                if (recursively && l.isFolder()) {
                    _ids.addAll(findDeleteItems(listInternal("/"), l.getPathId()));
                }
                _ids.add(l.getWorkSpaceFileTBPK());
            }
            return dbProvider.batchDeleteWorkSpaceFileItem(_ids);
        }
        if (locals == null || locals.isEmpty()) {
            return batchInsert(dbProvider, remotes);
        }

        List<String> upsertAffected = new ArrayList<>();
        for (ListFileResult.ResultsBean.DetailBean.FilesBean r : remotes) {
            if (r == null) {
                continue;
            }
            if (contains(locals, r)) {
                continue;
            }
            boolean inserted = dbProvider.upsertWorkSpaceFileItem(r.getId(), r.getDuid(), r.getPathDisplay(),
                    r.getPathId(), r.getName(), r.getFileType(),
                    r.getLastModified(), r.getCreationTime(), r.getSize(),
                    r.isFolder(),
                    Utils.generateUploaderRawJson(r.getUploader()),
                    Utils.generateLastModifiedUserRawJson(r.getLastModifiedUser()));
            if (inserted) {
                upsertAffected.add(r.getId());
            }
        }

        List<Integer> _ids = new ArrayList<>();
        for (IDBWorkSpaceFileItem l : locals) {
            if (l == null) {
                continue;
            }
            if (contains(remotes, l)) {
                continue;
            }
            if (upsertAffected.contains(l.getId())) {
                continue;
            }
            if (recursively && l.isFolder()) {
                _ids.addAll(findDeleteItems(listInternal("/"), l.getPathId()));
            }
            _ids.add(l.getWorkSpaceFileTBPK());
        }
        boolean delete = dbProvider.batchDeleteWorkSpaceFileItem(_ids);

        return delete || upsertAffected.size() > 0;
    }

    private boolean contains(List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes,
                             IDBWorkSpaceFileItem l) {
        if (remotes == null || remotes.size() == 0) {
            return false;
        }
        if (l == null) {
            return false;
        }
        for (ListFileResult.ResultsBean.DetailBean.FilesBean r : remotes) {
            if (equal(l, r)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(List<IDBWorkSpaceFileItem> locals,
                             ListFileResult.ResultsBean.DetailBean.FilesBean r) {
        if (locals == null || locals.size() == 0) {
            return false;
        }
        if (r == null) {
            return false;
        }
        for (IDBWorkSpaceFileItem l : locals) {
            if (equal(l, r)) {
                return true;
            }
        }
        return false;
    }

    private boolean equal(IDBWorkSpaceFileItem l,
                          ListFileResult.ResultsBean.DetailBean.FilesBean r) {
        if (l == null || r == null) {
            return false;
        }
        if (l.getId().equals(r.getId())) {
            return l.getLastModified() == r.getLastModified() &&
                    l.getSize() == r.getSize() &&
                    l.getCreationTime() == r.getCreationTime() &&
                    uploaderEquals(l.getUploader(), r.getUploader()) &&
                    lastModifiedUserEquals(l.getLastModifiedUser(), r.getLastModifiedUser());
        }
        return false;
    }

    private boolean uploaderEquals(IOwner l,
                                   ListFileResult.ResultsBean.DetailBean.FilesBean.UploaderBean r) {
        if (l == null && r == null) {
            return true;
        }
        if (l == null || r == null) {
            return false;
        }
        return l.getUserId() == r.getUserId() &&
                TextUtils.equals(l.getEmail(), r.getEmail()) &&
                TextUtils.equals(l.getName(), r.getDisplayName());
    }

    private boolean lastModifiedUserEquals(IOwner l,
                                           ListFileResult.ResultsBean.DetailBean.FilesBean.LastModifiedUserBean r) {
        if (l == null && r == null) {
            return true;
        }
        if (l == null || r == null) {
            return false;
        }
        return l.getUserId() == r.getUserId() &&
                TextUtils.equals(l.getEmail(), r.getEmail()) &&
                TextUtils.equals(l.getName(), r.getDisplayName());
    }

    private boolean batchInsert(DBProvider dbProvider,
                                List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes) {
        if (dbProvider == null) {
            return false;
        }
        if (remotes == null || remotes.isEmpty()) {
            return false;
        }
        List<WorkSpaceFileBean> inserts = new ArrayList<>();
        for (ListFileResult.ResultsBean.DetailBean.FilesBean f : remotes) {
            inserts.add(WorkSpaceFileBean.getInsertItem(f));
        }
        return dbProvider.batchInsertWorkSpaceFileItem(inserts);
    }

    private List<Integer> findDeleteItems(List<IDBWorkSpaceFileItem> tree, String parent) {
        List<Integer> ret = new ArrayList<>();
        if (tree == null || tree.isEmpty()) {
            return ret;
        }
        List<IDBWorkSpaceFileItem> items = listByLayer(tree, parent);
        if (items == null || items.isEmpty()) {
            return ret;
        }
        for (IDBWorkSpaceFileItem i : items) {
            ret.add(i.getWorkSpaceFileTBPK());
            if (i.isFolder()) {
                ret.addAll(findDeleteItems(tree, i.getPathId()));
            }
        }
        return ret;
    }

    private List<ListFileResult.ResultsBean.DetailBean.FilesBean> buildRemoteTreeLists(String pathId)
            throws InvalidRMClientException, RmsRestAPIException, SessionInvalidException {
        List<ListFileResult.ResultsBean.DetailBean.FilesBean> ret = new ArrayList<>();

        List<ListFileResult.ResultsBean.DetailBean.FilesBean> filesBeans = syncByLayer(pathId);
        if (filesBeans == null || filesBeans.size() == 0) {
            return ret;
        }
        for (ListFileResult.ResultsBean.DetailBean.FilesBean f : filesBeans) {
            ret.add(f);
            if (f.isFolder()) {
                ret.addAll(buildRemoteTreeLists(f.getPathId()));
            }
        }
        return ret;
    }

    private List<ListFileResult.ResultsBean.DetailBean.FilesBean> syncByLayer(String pathId)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        ListFileResult listFileResult = syncInternal(pathId);
        if (listFileResult == null) {
            return null;
        }
        ListFileResult.ResultsBean results = listFileResult.getResults();
        if (results == null) {
            return null;
        }
        mQuota = results.getQuota();
        mUsage = results.getUsage();
        ListFileResult.ResultsBean.DetailBean detail = results.getDetail();
        if (detail == null) {
            return null;
        }
        mTotalFiles = detail.getTotalFiles();
        return detail.getFiles();
    }

    private ListFileResult syncInternal(String pathId)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        try {
            return session.getRmsRestAPI()
                    .getWorkSpaceService(session.getRmUser())
                    .listFile(ListFileParam.newOne(pathId));
        } catch (RmsRestAPIException e) {
            removeLocalIfNecessary(e, pathId);
            throw e;
        }
    }

    @Override
    public void run() {
        try {
            mLog.d("onHeartBeat----------Begin---------------------");
            List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes = buildRemoteTreeLists("/");
            mLog.d("onHeartBeat----------remotes---------------------" + remotes.size());
            List<IDBWorkSpaceFileItem> locals = buildLocalTreeList("/");
            mLog.d("onHeartBeat----------locals---------------------" + locals.size());
            filterModifiedItems(remotes, locals, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final Creator<WorkSpaceFileService> CREATOR = new Creator<WorkSpaceFileService>() {
        @Override
        public WorkSpaceFileService createFromParcel(Parcel in) {
            return new WorkSpaceFileService(in);
        }

        @Override
        public WorkSpaceFileService[] newArray(int size) {
            return new WorkSpaceFileService[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    private void removeLocalIfNecessary(RmsRestAPIException e, String pathId) {
        if (e == null || pathId == null || pathId.isEmpty()) {
            return;
        }
        if (e.getDomain() == RmsRestAPIException.ExceptionDomain.FileNotFound) {
            deleteFile(pathId, pathId.endsWith("/"));
        }
    }

}
