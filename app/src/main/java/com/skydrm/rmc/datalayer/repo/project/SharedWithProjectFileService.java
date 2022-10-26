package com.skydrm.rmc.datalayer.repo.project;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.project.SharedWithProjectFileBean;
import com.skydrm.rmc.datalayer.repo.base.FileServiceBase;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.dbbridge.IDBSharedWithProjectItem;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.utils.commonUtils.StringUtils;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.sharedwithspace.ListFileParams;
import com.skydrm.sdk.rms.rest.sharedwithspace.ListFileResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SharedWithProjectFileService extends FileServiceBase<INxlFile, IDBSharedWithProjectItem> implements Parcelable {
    private int projectId = -1;
    private int _project_id = -1;

    SharedWithProjectFileService(int projectId, int _project_id) {
        this.projectId = projectId;
        this._project_id = _project_id;
    }

    private SharedWithProjectFileService(Parcel in) {
        projectId = in.readInt();
        _project_id = in.readInt();
    }

    public static final Creator<SharedWithProjectFileService> CREATOR = new Creator<SharedWithProjectFileService>() {
        @Override
        public SharedWithProjectFileService createFromParcel(Parcel in) {
            return new SharedWithProjectFileService(in);
        }

        @Override
        public SharedWithProjectFileService[] newArray(int size) {
            return new SharedWithProjectFileService[size];
        }
    };

    void onHeartBeat() {
        try {
            List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes = syncInternal();
            List<IDBSharedWithProjectItem> locals = listInternal("/");
            filterOutModifiedItems(remotes, locals);
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        } catch (RmsRestAPIException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<INxlFile> syncCurrentPath(String pathId)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes = syncInternal();
        List<IDBSharedWithProjectItem> locals = listInternal("/");
        if (filterOutModifiedItems(remotes, locals)) {
            return listCurrentPath("/");
        }
        return adapt2NxlItem(locals);
    }

    @Override
    protected List<INxlFile> syncTree(String pathId)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return syncCurrentPath(pathId);
    }

    List<INxlFile> listOfflineFile() {
        List<IDBSharedWithProjectItem> items = listInternal("/");
        if (items == null || items.size() == 0) {
            return null;
        }
        List<IDBSharedWithProjectItem> offline = new ArrayList<>();
        for (IDBSharedWithProjectItem i : items) {
            if (i.isOffline()) {
                offline.add(i);
            }
        }
        return adapt2NxlItem(offline);
    }

    @Override
    protected List<INxlFile> listTree(String pathId) {
        return listCurrentPath(pathId);
    }

    @Override
    protected List<IDBSharedWithProjectItem> listInternal(String pathId) {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .queryAllSharedWithProjectFileItem(_project_id);
    }

    @Override
    protected INxlFile newByDBItem(IDBSharedWithProjectItem item) {
        return new SharedWithProjectFile(item);
    }

    @Override
    protected INxlFile newByDBItem(IDBSharedWithProjectItem item,
                                   List<INxlFile> children) {
        return null;
    }

    @Override
    public boolean deleteFile(String pathId, boolean recursively) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean createFolder(String parentPathId, String name, boolean autoRename)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean uploadFile(String pathId, File nxlFile)
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException {
        throw new UnsupportedOperationException();
    }

    private List<ListFileResult.ResultsBean.DetailBean.FilesBean> syncInternal()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        ListFileResult result = session.getRmsRestAPI()
                .getSharedWithSpaceService(session.getRmUser())
                .listFile(new ListFileParams(1, String.valueOf(projectId)));
        if (result == null) {
            return null;
        }
        ListFileResult.ResultsBean results = result.getResults();
        if (results == null) {
            return null;
        }
        ListFileResult.ResultsBean.DetailBean detail = results.getDetail();
        if (detail == null) {
            return null;
        }
        return detail.getFiles();
    }

    private boolean filterOutModifiedItems(List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes,
                                           List<IDBSharedWithProjectItem> locals) {
        if (remotes == null || remotes.isEmpty()) {
            if (locals == null || locals.isEmpty()) {
                return false;
            }
            return batchDelete(_project_id);
        }
        if (locals == null || locals.isEmpty()) {
            return batchInsert(remotes);
        }

        List<String> upsertAffected = new ArrayList<>();
        for (ListFileResult.ResultsBean.DetailBean.FilesBean r : remotes) {
            if (r == null) {
                continue;
            }
            if (contains(locals, r)) {
                continue;
            }
            if (upsertItem(r)) {
                upsertAffected.add(r.getDuid());
            }
        }

        List<Integer> _ids = new ArrayList<>();
        for (IDBSharedWithProjectItem l : locals) {
            if (l == null) {
                continue;
            }
            if (contains(remotes, l)) {
                continue;
            }
            if (upsertAffected.contains(l.getDuid())) {
                continue;
            }
            _ids.add(l.getSharedWithProjectFileTBPK());
        }
        boolean delete = batchDelete(_ids);

        return upsertAffected.size() > 0 || delete;
    }

    private boolean batchDelete(int _project_id) {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .deleteSharedWithProjectFileItem(_project_id);
    }

    private boolean batchDelete(List<Integer> _ids) {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .batchDeleteSharedWithProjectFileItem(_ids);
    }

    private boolean batchInsert(List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes) {
        if (remotes == null || remotes.isEmpty()) {
            return false;
        }
        List<SharedWithProjectFileBean> inserts = new ArrayList<>();
        for (ListFileResult.ResultsBean.DetailBean.FilesBean r : remotes) {
            if (r == null) {
                continue;
            }
            inserts.add(SharedWithProjectFileBean.getInsertItem(r));
        }
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .batchInsertSharedWithProjectFileItem(_project_id, inserts);
    }

    private boolean upsertItem(ListFileResult.ResultsBean.DetailBean.FilesBean r) {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .upsertSharedWithProjectFileItem(_project_id, r.getDuid(), r.getName(), r.getSize(),
                        r.getFileType(), r.getSharedDate(), r.getSharedBy(),
                        r.getTransactionId(), r.getTransactionCode(), r.getSharedLink(),
                        StringUtils.list2Str(r.getRights()), r.isIsOwner(),
                        r.getProtectionType(), r.getSharedByProject());
    }

    private boolean contains(List<IDBSharedWithProjectItem> locals,
                             ListFileResult.ResultsBean.DetailBean.FilesBean r) {
        if (locals == null || locals.isEmpty()) {
            return false;
        }
        if (r == null) {
            return false;
        }
        for (IDBSharedWithProjectItem l : locals) {
            if (equals(l, r)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(List<ListFileResult.ResultsBean.DetailBean.FilesBean> remotes,
                             IDBSharedWithProjectItem l) {
        if (remotes == null || remotes.isEmpty()) {
            return false;
        }
        if (l == null) {
            return false;
        }
        for (ListFileResult.ResultsBean.DetailBean.FilesBean r : remotes) {
            if (equals(l, r)) {
                return true;
            }
        }
        return false;
    }

    private boolean equals(IDBSharedWithProjectItem l,
                           ListFileResult.ResultsBean.DetailBean.FilesBean r) {

        if (l == null && r == null) {
            return true;
        }
        if (l == null || r == null) {
            return false;
        }
        if (!l.getDuid().equals(r.getDuid())) {
            return false;
        }
        return TextUtils.equals(l.getName(), r.getName()) &&
                l.getSize() == r.getSize() &&
                l.getSharedDate() == r.getSharedDate() &&
                TextUtils.equals(l.getSharedBy(), r.getSharedBy()) &&
                TextUtils.equals(l.getTransactionId(), r.getTransactionId()) &&
                TextUtils.equals(l.getTransactionCode(), r.getTransactionCode()) &&
                rightsEquals(l.getRights(), r.getRights()) &&
                TextUtils.equals(l.getSharedBySpace(), r.getSharedByProject()) &&
                l.isOwner() == r.isIsOwner();
    }

    private boolean rightsEquals(List<String> remote, List<String> local) {
        if (remote == null && local == null) {
            return true;
        }
        if (remote == null || local == null) {
            return false;
        }
        return remote.equals(local);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(projectId);
        dest.writeInt(_project_id);
    }
}
