package com.skydrm.rmc.datalayer.repo.sharedwithme;

import android.support.annotation.NonNull;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.sharedwithme.SharedWithMeFileBean;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatListener;
import com.skydrm.rmc.datalayer.repo.NxlRepo;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.dbbridge.DBProvider;
import com.skydrm.rmc.dbbridge.IDBSharedWithMeItem;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.utils.commonUtils.StringUtils;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeListFileRequestParams;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeListFileResult;

import java.util.ArrayList;
import java.util.List;

public class SharedWithMeRepo extends NxlRepo implements IDataService {

    @Override
    public void updateResetAllOperationStatus() {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateSharedWithMeFileItemResetOperationStatus();
    }

    @Override
    public List<INxlFile> list(int type) {
        if (type == NxlFileType.OFFLINE.getValue()) {
            return listOffline();
        }
        return listInternal();
    }

    @Override
    public List<INxlFile> list(int type, String pathId, boolean recursively) {
        return list(type);
    }

    @Override
    public List<INxlFile> sync(int type)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException {
        List<SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean> remotes = syncInternal();
        List<IDBSharedWithMeItem> locals = SkyDRMApp.getInstance().getDBProvider().queryAllSharedWithMeItem();
        boolean altered = filterOutModifiedItems(remotes, locals);
        return altered ? list(type) : adapt2NxlItem(locals);
    }

    @Override
    public List<INxlFile> sync(int type, String pathId, boolean recursively)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException {
        if (type == NxlFileType.OFFLINE.getValue()) {
            return listOffline();
        }
        return sync(type);
    }

    @Override
    public void onHeatBeat(IHeartBeatListener l) {
        try {
            sync(-1);
        } catch (SessionInvalidException
                | RmsRestAPIException
                | InvalidRMClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearCache() {
        List<INxlFile> list = list(0);
        if (list == null || list.size() == 0) {
            return;
        }
        for (INxlFile f : list) {
            if (f == null) {
                continue;
            }
            f.clearCache();
        }
    }

    @Override
    public long getCacheSize() {
        long ret = 0;
        List<INxlFile> list = list(0);
        if (list == null || list.size() == 0) {
            return ret;
        }
        for (INxlFile f : list) {
            if (f == null) {
                continue;
            }
            ret += f.getCacheSize();
        }
        return ret;
    }

    private List<INxlFile> listInternal() {
        return adapt2NxlItem(SkyDRMApp.getInstance().getDBProvider().queryAllSharedWithMeItem());
    }

    private List<INxlFile> listOffline() {
        return adapt2NxlItem(SkyDRMApp.getInstance().getDBProvider().queryOfflineSharedWithMeItem());
    }

    private List<INxlFile> adapt2NxlItem(List<IDBSharedWithMeItem> lfs) {
        List<INxlFile> ret = new ArrayList<>();
        if (lfs == null || lfs.size() == 0) {
            return ret;
        }
        for (IDBSharedWithMeItem i : lfs) {
            ret.add(SharedWithMeFile.newByDBItem(i));
        }
        return ret;
    }

    private boolean filterOutModifiedItems(List<SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean> remote,
                                           List<IDBSharedWithMeItem> local) {
        DBProvider dbProvider = SkyDRMApp.getInstance().getDBProvider();
        if (remote == null || remote.size() == 0) {
            if (local == null || local.size() == 0) {
                return false;
            }
            return batchDelete(local, dbProvider);
        }
        if (local == null || local.size() == 0) {
            return batchInsert(remote, dbProvider);
        }

        List<String> upsertAffected = new ArrayList<>();
        for (SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean r : remote) {
            if (r == null) {
                continue;
            }
            if (contains(local, r)) {
                continue;
            }
            dbProvider.upsertSharedWithMeItem(r.getDuid(), r.getName(), r.getSize(),
                    r.getFileType(), r.getSharedDate(), r.getSharedBy(), r.getTransactionId(),
                    r.getTransactionCode(), r.getSharedLink(), StringUtils.list2Str(r.getRights()),
                    r.getComment(), r.isIsOwner(), r.getProtectionType());
            upsertAffected.add(r.getDuid());
        }

        List<Integer> _ids = new ArrayList<>();
        for (IDBSharedWithMeItem l : local) {
            if (l == null) {
                continue;
            }
            if (contains(remote, l)) {
                continue;
            }
            if (upsertAffected.contains(l.getDuid())) {
                continue;
            }
            _ids.add(l.getSharedWithMeFileTBPK());
        }
        boolean deleted = dbProvider.batchDeleteSharedWithMeFileItem(_ids);

        return upsertAffected.size() > 0 || deleted;
    }

    private boolean batchInsert(List<SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean> remote,
                                DBProvider dbProvider) {
        return dbProvider.batchInsertSharedWitheMeFileItem(getInsertsItem(remote));
    }

    private boolean batchDelete(List<IDBSharedWithMeItem> local, DBProvider dbProvider) {
        List<Integer> _ids = new ArrayList<>();
        for (IDBSharedWithMeItem i : local) {
            _ids.add(i.getSharedWithMeFileTBPK());
        }
        return dbProvider.batchDeleteSharedWithMeFileItem(_ids);
    }

    @NonNull
    private List<SharedWithMeFileBean> getInsertsItem(List<SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean> remote) {
        List<SharedWithMeFileBean> inserts = new ArrayList<>();
        for (SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean r : remote) {
            if (r == null) {
                continue;
            }
            inserts.add(SharedWithMeFileBean.getInsertItem(r));
        }
        return inserts;
    }

    private boolean contains(List<IDBSharedWithMeItem> lfs,
                             SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean r) {
        if (lfs == null || lfs.size() == 0) {
            return false;
        }
        if (r == null) {
            return false;
        }
        for (IDBSharedWithMeItem l : lfs) {
            if (equals(r, l)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(List<SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean> rfs,
                             IDBSharedWithMeItem l) {
        if (rfs == null || rfs.size() == 0) {
            return false;
        }
        if (l == null) {
            return false;
        }
        for (SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean r : rfs) {
            if (equals(r, l)) {
                return true;
            }
        }
        return false;
    }

    private boolean equals(SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean r,
                           IDBSharedWithMeItem l) {
        if (r == null || l == null) {
            return false;
        }
        return l.getName().equals(r.getName()) &&
                l.getSize() == r.getSize() &&
                l.getSharedDate() == r.getSharedDate() &&
                l.getSharedBy().equals(r.getSharedBy()) &&
                l.getTransactionId().equals(r.getTransactionId()) &&
                l.getTransactionCode().equals(r.getTransactionCode()) &&
                //l.getRights() == r.getRights() &&
                l.isOwner() == r.isIsOwner() &&
                l.getProtectionType() == r.getProtectionType();
    }

    private List<SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean> syncInternal()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SharedWithMeListFileResult result = SkyDRMApp.getInstance().getSession()
                .getRmsRestAPI()
                .getSharedWithMeService(SkyDRMApp.getInstance().getSession().getRmUser())
                .listFile(new SharedWithMeListFileRequestParams());
        SharedWithMeListFileResult.ResultsBean.DetailBean detail = result.getResults().getDetail();

        if (detail == null) {
            return null;
        }

        return detail.getFiles();
    }
}
