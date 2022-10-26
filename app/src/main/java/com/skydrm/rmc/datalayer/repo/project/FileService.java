package com.skydrm.rmc.datalayer.repo.project;

import android.database.sqlite.SQLiteConstraintException;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.project.ProjectFileBean;
import com.skydrm.rmc.database.table.project.ProjectFileExBean;
import com.skydrm.rmc.datalayer.repo.base.FileServiceBase;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.dbbridge.DBProvider;
import com.skydrm.rmc.dbbridge.IDBProjectFileItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.dbbridge.base.Owner;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.project.file.ListFileParam;
import com.skydrm.sdk.rms.rest.project.file.ListFileResult;
import com.skydrm.sdk.rms.rest.project.file.UploadFileResult;
import com.skydrm.sdk.rms.rest.project.file.UploadForNXLFileParam;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FileService extends FileServiceBase<INxlFile, IDBProjectFileItem> implements Parcelable {
    private int mId;
    private int _project_id;

    FileService(int id, int _id) {
        this.mId = id;
        this._project_id = _id;
    }

    private FileService(Parcel in) {
        mId = in.readInt();
        _project_id = in.readInt();
    }

    @Override
    protected List<IDBProjectFileItem> listInternal(String pathId) {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .queryAllProjectFileItem(_project_id);
    }

    @Override
    protected INxlFile newByDBItem(IDBProjectFileItem item) {
        return ProjectFile.newByDBItem(item);
    }

    @Override
    protected INxlFile newByDBItem(IDBProjectFileItem item, List<INxlFile> children) {
        return ProjectNode.newByDBItem(item, children);
    }

    List<INxlFile> syncRecentFile()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        ListFileResult listFileResult = session.getRmsRestAPI().getProjectService(session.getRmUser()).listFile(mId,
                ListFileParam.newProjectRecentFilesRequestParas());
        if (listFileResult == null) {
            return null;
        }
        ListFileResult.ResultsBean results = listFileResult.getResults();
        if (results == null) {
            return null;
        }
        long quota = results.getQuota();
        long usage = results.getUsage();

        DBProvider dbProvider = SkyDRMApp.getInstance()
                .getDBProvider();
        dbProvider
                .updateProjectItemQuotaAndUsage(_project_id, usage, quota);

        ListFileResult.ResultsBean.DetailBean detail = results.getDetail();
        if (detail == null) {
            return null;
        }
        List<ListFileResult.ResultsBean.DetailBean.FilesBean> files = detail.getFiles();
        if (files != null && files.size() != 0) {
            for (ListFileResult.ResultsBean.DetailBean.FilesBean f : files) {
                if (f == null) {
                    continue;
                }
                dbProvider.upsertProjectFileItem(_project_id, f.getId(), f.getDuid(), f.getPathDisplay(),
                        f.getPathId(), f.getName(), f.getFileType(), f.getLastModified(),
                        f.getCreationTime(), f.getSize(), f.isFolder(),
                        getOwnerRawJson(f.getOwner()),
                        getModifiedUserRawJson(f.getLastModifiedUser()));

                dbProvider.upsertProjectFileExItem(_project_id, f.getId(), f.isIsShared(), f.isRevoked(),
                        ProjectFileExBean.generateShareWithProjectRawJson(f.getShareWithProject()),
                        "{}");

            }
            return listRecentFile();
        }
        return null;
    }

    List<INxlFile> listRecentFile() {
        List<IDBProjectFileItem> recent = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectRecentFileItem(_project_id);
        return adapt2NxlItem(recent);
    }

    List<INxlFile> listAllSharedFile() {
        List<IDBProjectFileItem> allShared = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectAllSharedFileItem(_project_id);
        return adapt2NxlItem(allShared);
    }

    List<INxlFile> listOfflineFile() {
        List<IDBProjectFileItem> items = listInternal("/");
        if (items == null || items.size() == 0) {
            return null;
        }
        List<IDBProjectFileItem> offline = new ArrayList<>();
        for (IDBProjectFileItem i : items) {
            if (i.isOffline()) {
                offline.add(i);
            }
        }
        return adapt2NxlItem(offline);
    }

    void onHearBeat() {
        try {
            List<ListFileResult.ResultsBean.DetailBean.FilesBean> rTreeList = buildRemoteTreeList("/");
            List<IDBProjectFileItem> lTreeList = buildLocalTreeList("/");
            filterOutModifiedItems(rTreeList, lTreeList, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteFile(String pathId, boolean recursively) {
        if (recursively) {
            //delete children.
            List<Integer> deletedItem = findDeletedItem(listInternal("/"), pathId);
            if (deletedItem != null && deletedItem.size() != 0) {
                SkyDRMApp.getInstance()
                        .getDBProvider()
                        .batchDeleteProjectFileItem(deletedItem);
            }
        }

        //delete file.
        SkyDRMApp.getInstance()
                .getDBProvider()
                .deleteProjectFileItem(_project_id, pathId);

        return true;
    }

    @Override
    public boolean createFolder(String parentPathId, String name, boolean autoRename)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        String folderName = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .createFolder(mId, parentPathId, name, autoRename);

        if (folderName != null && !folderName.isEmpty()) {
            List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes = syncByLayer(parentPathId);
            List<IDBProjectFileItem> locals = listByLayer(parentPathId);
            return filterOutModifiedItems(remotes, locals, false);
        }
        return false;
    }

    @Override
    public boolean uploadFile(String pathId, File file)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        UploadForNXLFileParam param = new UploadForNXLFileParam();
        UploadForNXLFileParam.ParametersBean parametersBean = new UploadForNXLFileParam.ParametersBean();
        parametersBean.setName(file.getName());
        parametersBean.setParentPathId(pathId);
        parametersBean.setType(0);
        param.setParameters(parametersBean);

        UploadFileResult result = session.getRmsRestAPI().getProjectService(session.getRmUser())
                .uploadNXLFile(mId, param, file, new ProgressRequestListener() {
                    @Override
                    public void onRequestProgress(long bytesWritten, long contentLength, boolean done) throws IOException {

                    }
                });

        if (result != null) {
            List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes = syncByLayer(pathId);
            List<IDBProjectFileItem> locals = listByLayer(pathId);
            return filterOutModifiedItems(remotes, locals, false);
        }

        return false;
    }

    @Override
    public List<INxlFile> syncCurrentPath(String pathId) throws InvalidRMClientException,
            RmsRestAPIException, SessionInvalidException {
        List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes = syncByLayer(pathId);
        List<IDBProjectFileItem> locals = listByLayer(pathId);
        if (filterOutModifiedItems(remotes, locals, false)) {
            return listCurrentPath(pathId);
        }
        return adapt2NxlItem(locals);
    }

    @Override
    public List<INxlFile> syncTree(String pathId) throws InvalidRMClientException,
            RmsRestAPIException, SessionInvalidException {
        List<ListFileResult.ResultsBean.DetailBean.FilesBean> rTreeList = buildRemoteTreeList(pathId);
        List<IDBProjectFileItem> lTreeList = buildLocalTreeList(pathId);
        if (filterOutModifiedItems(rTreeList, lTreeList, true)) {
            return listFile(pathId, true);
        }
        return adaptList2Tree(lTreeList, pathId);
    }

    private List<ListFileResult.ResultsBean.DetailBean.FilesBean> buildRemoteTreeList(String pathId)
            throws InvalidRMClientException, RmsRestAPIException, SessionInvalidException {
        List<ListFileResult.ResultsBean.DetailBean.FilesBean> ret = new ArrayList<>();

        List<ListFileResult.ResultsBean.DetailBean.FilesBean> results = syncByLayer(pathId);
        if (results == null || results.size() == 0) {
            return ret;
        }
        for (ListFileResult.ResultsBean.DetailBean.FilesBean r : results) {
            ret.add(r);
            if (r.isFolder()) {
                ret.addAll(buildRemoteTreeList(r.getPathId()));
            }
        }
        return ret;
    }

    private boolean filterOutModifiedItems(List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes,
                                           List<IDBProjectFileItem> locals, boolean recursively) {
        long startTimeMillis = System.currentTimeMillis();
        try {
            DBProvider dbProvider = SkyDRMApp.getInstance().getDBProvider();
            if (remotes == null || remotes.size() == 0) {
                if (locals == null || locals.size() == 0) {
                    return false;
                }
                List<Integer> _ids = new ArrayList<>();
                for (IDBProjectFileItem i : locals) {
                    if (i == null) {
                        continue;
                    }
                    if (recursively) {
                        if (i.isFolder()) {
                            _ids.addAll(findDeletedItem(listInternal("/"), i.getPathId()));
                        }
                    }
                    _ids.add(i.getProjectFileTBPK());
                }
                return dbProvider.batchDeleteProjectFileItem(_ids);
            }
            if (locals == null || locals.size() == 0) {
                return batchInsert(remotes, dbProvider);
            }

            List<String> upsertAffected = new ArrayList<>();
            for (ListFileResult.ResultsBean.DetailBean.FilesBean r : remotes) {
                if (r == null) {
                    continue;
                }
                if (contains(locals, r)) {
                    continue;
                }
                dbProvider.upsertProjectFileItem(_project_id, r.getId(), r.getDuid(),
                        r.getPathDisplay(), r.getPathId(), r.getName(), r.getFileType(),
                        r.getLastModified(), r.getCreationTime(), r.getSize(), r.isFolder(),
                        getOwnerRawJson(r.getOwner()),
                        getModifiedUserRawJson(r.getLastModifiedUser()));

                dbProvider.upsertProjectFileExItem(_project_id, r.getId(), r.isIsShared(), r.isRevoked(),
                        ProjectFileExBean.generateShareWithProjectRawJson(r.getShareWithProject()),
                        "{}");

                upsertAffected.add(r.getId());
            }

            List<Integer> _ids = new ArrayList<>();
            for (IDBProjectFileItem l : locals) {
                if (l == null) {
                    continue;
                }
                if (contains(remotes, l)) {
                    continue;
                }
                // If the file is merged,then next round check just skip that.
                if (upsertAffected.contains(l.getId())) {
                    continue;
                }
                if (recursively) {
                    if (l.isFolder()) {
                        _ids.addAll(findDeletedItem(listInternal("/"), l.getPathId()));
                    }
                }
                _ids.add(l.getProjectFileTBPK());
            }
            boolean delete = dbProvider.batchDeleteProjectFileItem(_ids);

            return delete || upsertAffected.size() > 0;
        } finally {
            long endTimeMillis = System.currentTimeMillis();
            Log.d("SyncProjectFile", "filterOutModifiedItems time consumes[Millis]." + (endTimeMillis - startTimeMillis));
        }
    }

    private String getModifiedUserRawJson(ListFileResult.ResultsBean.DetailBean.FilesBean.LastModifiedUserBean r) {
        String ret = "{}";
        if (r == null) {
            return ret;
        }
        return Owner.generateRawJson(r.getUserId(),
                r.getDisplayName(),
                r.getEmail());
    }

    private String getOwnerRawJson(ListFileResult.ResultsBean.DetailBean.FilesBean.OwnerBean r) {
        String ret = "{}";
        if (r == null) {
            return ret;
        }
        return Owner.generateRawJson(r.getUserId(),
                r.getDisplayName(),
                r.getEmail());
    }

    private boolean contains(List<IDBProjectFileItem> locals,
                             ListFileResult.ResultsBean.DetailBean.FilesBean r) {
        if (locals == null || locals.size() == 0) {
            return false;
        }
        if (r == null) {
            return false;
        }
        for (IDBProjectFileItem l : locals) {
            if (equals(r, l)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes,
                             IDBProjectFileItem l) {
        if (remotes == null || remotes.size() == 0) {
            return false;
        }
        if (l == null) {
            return false;
        }
        for (ListFileResult.ResultsBean.DetailBean.FilesBean r : remotes) {
            if (equals(r, l)) {
                return true;
            }
        }
        return false;
    }

    private boolean equals(ListFileResult.ResultsBean.DetailBean.FilesBean r,
                           IDBProjectFileItem l) {
        if (r == null || l == null) {
            return false;
        }
        //If is the same file,then check every changeable fields.
        if (r.getId().equals(l.getId())) {
            return r.getLastModified() == l.getLastModified() &&
                    r.getSize() == l.getSize() &&
                    r.isIsShared() == l.isShared() &&
                    r.isRevoked() == l.isRevoked() &&
                    r.getCreationTime() == l.getCreationTime() &&
                    shareWithProjectListsEquals(r.getShareWithProject(), l.getShareWithProject()) &&
                    ownerEquals(r.getOwner(), l.getOwner()) &&
                    modifiedUserEquals(r.getLastModifiedUser(), l.getLastModifiedUser());
        }
        //If is not the same file.
        //return false directly.[quick check.]
        return false;
    }

    private boolean ownerEquals(ListFileResult.ResultsBean.DetailBean.FilesBean.OwnerBean ro,
                                IOwner lo) {
        if (ro == null && lo == null) {
            return true;
        }
        if (ro == null || lo == null) {
            return false;
        }
        return ro.getUserId() == lo.getUserId() &&
                TextUtils.equals(ro.getDisplayName(), lo.getName()) &&
                TextUtils.equals(ro.getEmail(), lo.getEmail());
    }

    private boolean modifiedUserEquals(ListFileResult.ResultsBean.
                                               DetailBean.FilesBean.LastModifiedUserBean ru,
                                       IOwner lu) {
        if (ru == null && lu == null) {
            return true;
        }
        if (ru == null || lu == null) {
            return false;
        }
        return ru.getUserId() == lu.getUserId() &&
                TextUtils.equals(ru.getDisplayName(), lu.getName()) &&
                TextUtils.equals(ru.getEmail(), lu.getEmail());
    }

    private boolean shareWithProjectListsEquals(List<Integer> remote, List<Integer> local) {
        if (remote == null && local == null) {
            return true;
        }
        if (remote == null || local == null) {
            return false;
        }
        return remote.equals(local);
    }


    private boolean batchInsert(List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes,
                                DBProvider dbProvider) {
        try {
            if (remotes == null || remotes.size() == 0) {
                return false;
            }
            List<ProjectFileBean> inserts = new ArrayList<>();
            List<ProjectFileExBean> insertsEx = new ArrayList<>();
            for (ListFileResult.ResultsBean.DetailBean.FilesBean r : remotes) {
                inserts.add(ProjectFileBean.getInsertBean(r));
                insertsEx.add(ProjectFileExBean.getInsertBean(r));
            }
            if (inserts.size() == 0) {
                return false;
            }
            return dbProvider.batchInsertProjectFileItem(_project_id, inserts)
                    & dbProvider.batchInsertProjectFileExItem(_project_id, insertsEx);
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SQLiteConstraintException) {
                int upsertAffected = 0;
                for (ListFileResult.ResultsBean.DetailBean.FilesBean r : remotes) {
                    if (r == null) {
                        continue;
                    }
                    dbProvider.upsertProjectFileItem(_project_id, r.getId(), r.getDuid(),
                            r.getPathDisplay(), r.getPathId(), r.getName(), r.getFileType(),
                            r.getLastModified(), r.getCreationTime(), r.getSize(), r.isFolder(),
                            getOwnerRawJson(r.getOwner()),
                            getModifiedUserRawJson(r.getLastModifiedUser()));

                    dbProvider.upsertProjectFileExItem(_project_id, r.getId(), r.isIsShared(), r.isRevoked(),
                            ProjectFileExBean.generateShareWithProjectRawJson(r.getShareWithProject()),
                            "{}");
                    upsertAffected++;
                }
                return upsertAffected > 0;
            }
        }
        return false;
    }

    private List<Integer> findDeletedItem(List<IDBProjectFileItem> treeList, String parent) {
        List<Integer> ret = new ArrayList<>();
        if (treeList == null || treeList.size() == 0) {
            return ret;
        }
        List<IDBProjectFileItem> items = listByLayer(treeList, parent);
        if (items == null || items.size() == 0) {
            return ret;
        }
        for (IDBProjectFileItem child : items) {
            ret.add(child.getProjectFileTBPK());
            if (child.isFolder()) {
                ret.addAll(findDeletedItem(treeList, child.getPathId()));
            }
        }
        return ret;
    }

    private List<ListFileResult.ResultsBean.DetailBean.FilesBean> syncByLayer(String pathId)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        ListFileResult rootResult = syncInternal(pathId);
        if (rootResult == null) {
            return null;
        }
        ListFileResult.ResultsBean results = rootResult.getResults();
        if (results == null) {
            return null;
        }

        ListFileResult.ResultsBean.DetailBean detail = results.getDetail();
        if (detail == null) {
            return null;
        }
        return detail.getFiles();
    }

    private ListFileResult syncInternal(String pathId) throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();

        try {
            return session.getRmsRestAPI()
                    .getProjectService(session.getRmUser())
                    .listFile(mId, new ListFileParam(pathId));
        } catch (RmsRestAPIException e) {
            removeLocalIfNecessary(e, pathId);
            throw e;
        }
    }

    @Override
    public List<INxlFile> listCurrentPath(String pathId) {
        return adapt2NxlItem(listByLayer(pathId));
    }

    @Override
    public List<INxlFile> listTree(String pathId) {
        return adaptList2Tree(buildLocalTreeList(pathId), pathId);
    }

    public static final Creator<FileService> CREATOR = new Creator<FileService>() {
        @Override
        public FileService createFromParcel(Parcel in) {
            return new FileService(in);
        }

        @Override
        public FileService[] newArray(int size) {
            return new FileService[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeInt(_project_id);
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
