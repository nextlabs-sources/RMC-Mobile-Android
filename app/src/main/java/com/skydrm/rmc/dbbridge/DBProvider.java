package com.skydrm.rmc.dbbridge;

import android.content.Context;

import com.skydrm.rmc.database.DaoMaster;
import com.skydrm.rmc.database.DaoSession;
import com.skydrm.rmc.database.table.log.ActivityLogBean;
import com.skydrm.rmc.database.table.myvault.MyVaultFileBean;
import com.skydrm.rmc.database.table.project.ProjectBean;
import com.skydrm.rmc.database.table.project.ProjectFileBean;
import com.skydrm.rmc.database.table.project.ProjectFileDao;
import com.skydrm.rmc.database.table.project.ProjectFileExBean;
import com.skydrm.rmc.database.table.project.ProjectFileExDao;
import com.skydrm.rmc.database.table.project.ProjectMemberBean;
import com.skydrm.rmc.database.table.project.SharedWithProjectFileBean;
import com.skydrm.rmc.database.table.project.SharedWithProjectFileDao;
import com.skydrm.rmc.database.table.server.Server;
import com.skydrm.rmc.database.table.sharedwithme.SharedWithMeFileBean;
import com.skydrm.rmc.database.table.workspace.WorkSpaceFileBean;
import com.skydrm.rmc.dbbridge.myvault.DBMyVaultItem;
import com.skydrm.rmc.dbbridge.project.DBProjectFileItem;
import com.skydrm.rmc.dbbridge.project.DBProjectItem;
import com.skydrm.rmc.dbbridge.project.DBSharedWithProjectFileItem;
import com.skydrm.rmc.dbbridge.server.ServerImpl;
import com.skydrm.rmc.dbbridge.sharedwithme.DBSharedWitheMeItem;
import com.skydrm.rmc.dbbridge.user.UserImpl;
import com.skydrm.rmc.dbbridge.workspace.DBWorkSpaceFileItem;
import com.skydrm.rmc.ui.service.offline.db.OfflineLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DBProvider {
    private DaoSession mDaoSession;
    private final Object mServerTBLock = new Object();
    private final Object mUserTBLock = new Object();
    private final Object mMyVaultTBLock = new Object();
    private final Object mSharedWithMeLock = new Object();
    private final Object mProjectLock = new Object();
    private final ReentrantReadWriteLock mProjectFileLock = new ReentrantReadWriteLock();
    private final Object mProjectFileExLock = new Object();
    //private final Object mProjectFileLock = new Object();
    private final Object mProjectMemberLock = new Object();
    private final Object mActivityLogLock = new Object();
    private final ReentrantReadWriteLock mWorkSpaceFileLock = new ReentrantReadWriteLock();
    private final Object mSharedWithProjectFileLock = new Object();

    private int _server_id = -1;
    private int _user_id = -1;

    public DBProvider(Context c) {
        DaoMaster master = new DaoMaster(c);
        mDaoSession = master.newSession();
    }

    public void initDB() {
        mDaoSession.initDatabase();

        try {
            _server_id = queryServerTbPK();
            _user_id = queryUserTbPK();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUserLogout() {
        synchronized (mServerTBLock) {
            mDaoSession.getServerDao().clearLoginStatus(_server_id);
            _server_id = -1;
        }
        synchronized (mUserTBLock) {
            mDaoSession.getUserDao().clearLoginStatus(_user_id);
            _user_id = -1;
        }
    }

    //region Server table.
    public void upsertServerItem(String routerUrl, String rmsUrl,
                                 String tenantId, boolean isOnPremise) {
        synchronized (mServerTBLock) {
            _server_id = mDaoSession.getServerDao().queryPrimaryKey(routerUrl, tenantId);
            if (_server_id == -1) {
                _server_id = mDaoSession.getServerDao().insert(routerUrl, rmsUrl, tenantId, isOnPremise);
            } else {
                mDaoSession.getServerDao().update(_server_id, routerUrl, rmsUrl, tenantId, isOnPremise);
            }
        }
    }

    private int queryServerTbPK() {
        synchronized (mServerTBLock) {
            return mDaoSession.getServerDao().queryPrimaryKey();
        }
    }

    public int queryServerTbPK(String routerUrl, String tenantId) {
        synchronized (mServerTBLock) {
            return mDaoSession.getServerDao().queryPrimaryKey(routerUrl, tenantId);
        }
    }

    public IServer queryServerItem() {
        if (_server_id == -1) {
            throw new IllegalStateException("Empty user login status found.");
        }
        synchronized (mServerTBLock) {
            return new ServerImpl(mDaoSession.getServerDao().queryServerItem(_server_id));
        }
    }

    public List<IServer> queryServerTb() {
        synchronized (mServerTBLock) {
            List<IServer> rt = new ArrayList<>();
            for (Server s : mDaoSession.getServerDao().queryAll()) {
                rt.add(new ServerImpl(s));
            }
            return rt;
        }
    }
    //endregion

    //region User table.
    public void upsertUserItem(String name, String email,
                               int userId, int idpType,
                               long ttl, String ticket,
                               String tenantId, String tokenGroupName, String defaultTenant, String defaultTenantUrl,
                               String preferencesRawJson, String userRawJson) {
        synchronized (mUserTBLock) {
            _user_id = mDaoSession.getUserDao().queryPrimaryKey(_server_id, email);
            if (_user_id == -1) {
                _user_id = mDaoSession.getUserDao().insert(_server_id,
                        name, email,
                        userId, idpType,
                        ttl, ticket,
                        tenantId, tokenGroupName,
                        defaultTenant, defaultTenantUrl,
                        preferencesRawJson, userRawJson);
            } else {
                String older = queryUserPreferenceRawJson(_user_id);
                mDaoSession.getUserDao().update(_user_id, _server_id,
                        name, email,
                        userId, idpType,
                        ttl, ticket,
                        tenantId, tokenGroupName,
                        defaultTenant, defaultTenantUrl,
                        diffPreferenceRawJson(preferencesRawJson, older), userRawJson);
            }
        }
    }

    private String diffPreferenceRawJson(String newer, String older) {
        //If nothing in newer then keep the older one.
        if (newer == null || newer.isEmpty()) {
            if (older == null || older.isEmpty()) {
                return "{}";
            }
            return older;
        }
        // None of preferences putted before. Keep the new one as the first.
        if (older == null || older.isEmpty()) {
            return newer;
        }
        String rawRet = "{}";
        try {
            JSONObject newObj = new JSONObject(newer);
            JSONObject oldObj = new JSONObject(older);
            JSONObject preferencesObj = newObj.optJSONObject("preferences");
            if (preferencesObj != null) {
                long sync_with_rms_millis = preferencesObj.optLong("sync_with_rms_millis");
                oldObj.put("sync_with_rms_millis", sync_with_rms_millis);
            }
            rawRet = oldObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rawRet;
    }

    public void updateUserItemTenantAdmin(boolean isTenantAdmin) {
        if (_user_id == -1) {
            return;
        }
        synchronized (mUserTBLock) {
            mDaoSession.getUserDao().updateTenantAdmin(_user_id, isTenantAdmin);
        }
    }

    public void updateUserItemProjectAdmin(boolean isProjectAdmin) {
        if (_user_id == -1) {
            return;
        }
        synchronized (mUserTBLock) {
            mDaoSession.getUserDao().updateProjectAdmin(_user_id, isProjectAdmin);
        }
    }

    public void updateUserItemTenantAndProjectAdmin(boolean isTenantAdmin, boolean isProjectAdmin) {
        if (_user_id == -1) {
            return;
        }
        synchronized (mUserTBLock) {
            mDaoSession.getUserDao().updateTenantAndProjectAdmin(_user_id, isTenantAdmin, isProjectAdmin);
        }
    }

    private int queryUserTbPK() {
        synchronized (mUserTBLock) {
            return mDaoSession.getUserDao().queryPrimaryKey();
        }
    }

    private String queryUserPreferenceRawJson(int _id) {
        if (_id == -1) {
            return "";
        }
        synchronized (mUserTBLock) {
            return mDaoSession.getUserDao().queryPreferenceRawJson(_id);
        }
    }

    public int queryUserTbPK(String email) {
        synchronized (mUserTBLock) {
            return mDaoSession.getUserDao().queryPrimaryKey(_server_id, email);
        }
    }

    public IUser queryUserItem() {
        if (_user_id == -1) {
            throw new IllegalStateException("Empty user login status found.");
        }
        synchronized (mUserTBLock) {
            return new UserImpl(mDaoSession.getUserDao().query(_user_id));
        }
    }
    //endregion

    //region MyVaultFile table.
    public boolean batchInsertMyVaultFileItem(List<MyVaultFileBean> inserts) {
        if (_user_id == -1) {
            return false;
        }
        if (inserts == null || inserts.size() == 0) {
            return false;
        }
        synchronized (mMyVaultTBLock) {
            return mDaoSession.getMyVaultFileDao().batchInsert(_user_id, inserts);
        }
    }

    public boolean batchUpdateMyVaultFileItem(Map<String, Boolean> updates) {
        if (_user_id == -1) {
            return false;
        }
        if (updates == null || updates.size() == 0) {
            return false;
        }
        synchronized (mMyVaultTBLock) {
            return mDaoSession.getMyVaultFileDao().batchUpdate(_user_id, updates);
        }
    }

    public void upsertMyVaultFileItem(String pathId, String pathDisplay, String repoId,
                                      long sharedOn, String sharedWith, String rights, String name,
                                      String fileType, String duid,
                                      boolean isRevoked, boolean isDeleted, boolean isShared,
                                      long size, String metadata,
                                      boolean isFavorite) {
        //If _user_id is -1, then reject all services.
        if (_user_id == -1) {
            return;
        }
        synchronized (mMyVaultTBLock) {
            int _id = mDaoSession.getMyVaultFileDao().queryPrimaryKey(_user_id, duid);
            if (_id == -1) {
                mDaoSession.getMyVaultFileDao().insert(_user_id, pathId, pathDisplay, repoId,
                        sharedOn, sharedWith, rights, name, fileType, duid, isRevoked, isDeleted, isShared,
                        size, metadata, isFavorite);
            } else {
                mDaoSession.getMyVaultFileDao().update(_id, pathId, pathDisplay, repoId,
                        sharedOn, sharedWith, rights, name, fileType, duid, isRevoked, isDeleted, isShared,
                        size, metadata, isFavorite);
            }
        }
    }

    public void upsertMyVaultFileItem(MyVaultFileBean i) {
        //If _user_id is -1, then reject all services.
        if (_user_id == -1) {
            return;
        }
        if (i == null) {
            return;
        }
        synchronized (mMyVaultTBLock) {
            int _id = mDaoSession.getMyVaultFileDao().queryPrimaryKey(_user_id, i.duid);
            if (_id == -1) {
                mDaoSession.getMyVaultFileDao().insert(_user_id, i.pathId, i.pathDisplay, i.repoId,
                        i.sharedOn, i.sharedWith, i.rights, i.name,
                        i.fileType, i.duid, i.isRevoked, i.isDeleted, i.isShared,
                        i.size, i.metadata.toRawJson(), i.isFavorite);
            } else {
                mDaoSession.getMyVaultFileDao().update(_id, i.pathId, i.pathDisplay, i.repoId,
                        i.sharedOn, i.sharedWith, i.rights, i.name,
                        i.fileType, i.duid, i.isRevoked, i.isDeleted, i.isShared,
                        i.size, i.metadata.toRawJson(), i.isFavorite);
            }
        }
    }

    public List<IDBMyVaultItem> queryAllMyVaultItem() {
        List<IDBMyVaultItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mMyVaultTBLock) {
            List<MyVaultFileBean> myVaultFileBeans = mDaoSession.getMyVaultFileDao().queryAll(_user_id);
            if (myVaultFileBeans == null || myVaultFileBeans.size() == 0) {
                return ret;
            }
            for (MyVaultFileBean mvf : myVaultFileBeans) {
                ret.add(new DBMyVaultItem(mvf));
            }
            return ret;
        }
    }

    public List<IDBMyVaultItem> queryMyVaultActiveSharedItem() {
        List<IDBMyVaultItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mMyVaultTBLock) {
            List<MyVaultFileBean> myVaultFileBeans = mDaoSession.getMyVaultFileDao().queryActiveShared(_user_id);
            if (myVaultFileBeans == null || myVaultFileBeans.size() == 0) {
                return ret;
            }
            for (MyVaultFileBean mvf : myVaultFileBeans) {
                ret.add(new DBMyVaultItem(mvf));
            }
            return ret;
        }
    }

    public List<IDBMyVaultItem> queryMyVaultProtectedItem() {
        List<IDBMyVaultItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mMyVaultTBLock) {
            List<MyVaultFileBean> myVaultFileBeans = mDaoSession.getMyVaultFileDao().queryProtected(_user_id);
            if (myVaultFileBeans == null || myVaultFileBeans.size() == 0) {
                return ret;
            }
            for (MyVaultFileBean mvf : myVaultFileBeans) {
                ret.add(new DBMyVaultItem(mvf));
            }
            return ret;
        }
    }

    public List<IDBMyVaultItem> queryMyVaultRevokedItem() {
        List<IDBMyVaultItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mMyVaultTBLock) {
            List<MyVaultFileBean> myVaultFileBeans = mDaoSession.getMyVaultFileDao().queryRevoked(_user_id);
            if (myVaultFileBeans == null || myVaultFileBeans.size() == 0) {
                return ret;
            }
            for (MyVaultFileBean mvf : myVaultFileBeans) {
                ret.add(new DBMyVaultItem(mvf));
            }
            return ret;
        }
    }

    public List<IDBMyVaultItem> queryMyVaultDeletedItem() {
        List<IDBMyVaultItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mMyVaultTBLock) {
            List<MyVaultFileBean> myVaultFileBeans = mDaoSession.getMyVaultFileDao().queryDeleted(_user_id);
            if (myVaultFileBeans == null || myVaultFileBeans.size() == 0) {
                return ret;
            }
            for (MyVaultFileBean mvf : myVaultFileBeans) {
                ret.add(new DBMyVaultItem(mvf));
            }
            return ret;
        }
    }

    public List<IDBMyVaultItem> queryMyVaultFavoriteItem() {
        List<IDBMyVaultItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mMyVaultTBLock) {
            List<MyVaultFileBean> myVaultFileBeans = mDaoSession.getMyVaultFileDao().queryFavorite(_user_id);
            if (myVaultFileBeans == null || myVaultFileBeans.size() == 0) {
                return ret;
            }
            for (MyVaultFileBean mvf : myVaultFileBeans) {
                ret.add(new DBMyVaultItem(mvf));
            }
            return ret;
        }
    }

    public List<IDBMyVaultItem> queryMyVaultOfflineItem() {
        List<IDBMyVaultItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mMyVaultTBLock) {
            List<MyVaultFileBean> data = mDaoSession.getMyVaultFileDao()
                    .queryOffline(_user_id);
            if (data == null || data.size() == 0) {
                return ret;
            }
            for (MyVaultFileBean f : data) {
                ret.add(new DBMyVaultItem(f));
            }
            return ret;
        }
    }

    public void updateMyVaultItemLocalPath(int _id, String localPath) {
        if (_id == -1) {
            return;
        }
        synchronized (mMyVaultTBLock) {
            mDaoSession.getMyVaultFileDao().updateLocalPath(_id, localPath);
        }
    }

    public void updateMyVaultItemSharedWith(int _id, String sharedWith) {
        if (_id == -1) {
            return;
        }
        synchronized (mMyVaultTBLock) {
            mDaoSession.getMyVaultFileDao().updateSharedWith(_id, sharedWith);
        }
    }

    public void updateMyVaultItemRevoked(int _id, boolean revoked) {
        if (_id == -1) {
            return;
        }
        synchronized (mMyVaultTBLock) {
            mDaoSession.getMyVaultFileDao().updateRevoked(_id, revoked);
        }
    }

    public void updateMyVaultItemDeleted(int _id, boolean deleted) {
        if (_id == -1) {
            return;
        }
        synchronized (mMyVaultTBLock) {
            mDaoSession.getMyVaultFileDao().updateDeleted(_id, deleted);
        }
    }

    public void updateMyVaultItemOperationStatus(int _id, int status) {
        if (_id == -1) {
            return;
        }
        synchronized (mMyVaultTBLock) {
            mDaoSession.getMyVaultFileDao().updateOperationStatus(_id, status);
        }
    }

    public void updateMyVaultItemFavorite(int _id, boolean favorite) {
        if (_id == -1) {
            return;
        }
        synchronized (mMyVaultTBLock) {
            mDaoSession.getMyVaultFileDao().updateFavorite(_id, favorite);
        }
    }

    public void updateMyVaultItemOffline(int _id, boolean offline) {
        if (_id == -1) {
            return;
        }
        synchronized (mMyVaultTBLock) {
            mDaoSession.getMyVaultFileDao().updateOffline(_id, offline);
        }
    }

    public boolean deleteMyVaultFileItem(int _id) {
        if (_id == -1) {
            return false;
        }
        synchronized (mMyVaultTBLock) {
            mDaoSession.getMyVaultFileDao().deleteOne(_id);
            return true;
        }
    }

    public boolean batchDeleteMyVaultFileItem(List<Integer> _ids) {
        if (_ids == null || _ids.size() == 0) {
            return false;
        }
        synchronized (mMyVaultTBLock) {
            return mDaoSession.getMyVaultFileDao().batchDelete(_ids);
        }
    }

    public void updateMyVaultItemResetOperationStatus() {
        if (_user_id == -1) {
            return;
        }
        synchronized (mMyVaultTBLock) {
            mDaoSession.getMyVaultFileDao()
                    .updateResetAllOperationStatus(_user_id);
        }
    }
    //endregion

    //region SharedWithMeFile table.
    public boolean batchInsertSharedWitheMeFileItem(List<SharedWithMeFileBean> inserts) {
        if (_user_id == -1) {
            return false;
        }
        if (inserts == null || inserts.size() == 0) {
            return false;
        }
        synchronized (mSharedWithMeLock) {
            return mDaoSession.getSharedWithMeFileDao().batchInsert(_user_id, inserts);
        }
    }

    public void upsertSharedWithMeItem(String duid, String name, long size,
                                       String fileType, long sharedDate, String sharedBy,
                                       String transactionId, String transactionCode, String sharedLink,
                                       String rights, String comment, boolean isOwner,
                                       int protectionType) {
        //If _user_id is -1, then reject all services.
        if (_user_id == -1) {
            return;
        }
        synchronized (mSharedWithMeLock) {
            int _id = mDaoSession.getSharedWithMeFileDao().queryPrimaryKey(_user_id, duid);
            if (_id == -1) {
                //Insert a new one.
                mDaoSession.getSharedWithMeFileDao().insert(_user_id, duid, name, size,
                        fileType, sharedDate, sharedBy, transactionId, transactionCode, sharedLink,
                        rights, comment, isOwner, protectionType);
            } else {
                //Update existing one.
                mDaoSession.getSharedWithMeFileDao().update(_id, duid, name, size,
                        fileType, sharedDate, sharedBy, transactionId, transactionCode, sharedLink,
                        rights, comment, isOwner, protectionType);
            }
        }
    }

    public List<IDBSharedWithMeItem> queryAllSharedWithMeItem() {
        List<IDBSharedWithMeItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mSharedWithMeLock) {
            List<SharedWithMeFileBean> beans = mDaoSession.getSharedWithMeFileDao()
                    .queryAll(_user_id);
            for (SharedWithMeFileBean f : beans) {
                ret.add(new DBSharedWitheMeItem(f));
            }
            return ret;
        }
    }

    public List<IDBSharedWithMeItem> queryOfflineSharedWithMeItem() {
        List<IDBSharedWithMeItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mSharedWithMeLock) {
            List<SharedWithMeFileBean> beans = mDaoSession.getSharedWithMeFileDao()
                    .queryOffline(_user_id);
            for (SharedWithMeFileBean f : beans) {
                ret.add(new DBSharedWitheMeItem(f));
            }
            return ret;
        }
    }

    public void updateSharedWitheMeItemLocalPath(int _id, String localPath) {
        if (_id == -1) {
            return;
        }
        synchronized (mSharedWithMeLock) {
            mDaoSession.getSharedWithMeFileDao()
                    .updateLocalPath(_id, localPath);
        }
    }

    public void updateSharedWitheMeItemOfflineStatus(int _id, boolean offline) {
        if (_id == -1) {
            return;
        }
        synchronized (mSharedWithMeLock) {
            mDaoSession.getSharedWithMeFileDao()
                    .updateOfflineStatus(_id, offline);
        }
    }

    public void updateSharedWitheMeItemOperationStatus(int _id, int status) {
        if (_id == -1) {
            return;
        }
        synchronized (mSharedWithMeLock) {
            mDaoSession.getSharedWithMeFileDao()
                    .updateOperationStatus(_id, status);
        }
    }

    public void updateSharedWitheMeItemRightsAndObligations(int _id, int rights, String obligations) {
        if (_id == -1) {
            return;
        }
        synchronized (mSharedWithMeLock) {
            mDaoSession.getSharedWithMeFileDao()
                    .updateRightsAndObligation(_id, rights, obligations);
        }
    }

    public int querySharedWithMeItemOfflineRights(int _id) {
        if (_id == -1) {
            return -1;
        }
        synchronized (mSharedWithMeLock) {
            return mDaoSession.getSharedWithMeFileDao()
                    .queryOfflineRights(_id);
        }
    }

    public String querySharedWithMeItemOfflineObligations(int _id) {
        if (_id == -1) {
            return "";
        }
        synchronized (mSharedWithMeLock) {
            return mDaoSession.getSharedWithMeFileDao()
                    .queryOfflineObligations(_id);
        }
    }

    public void deleteSharedWithMeFileItem(int _id) {
        if (_id == -1) {
            return;
        }
        synchronized (mSharedWithMeLock) {
            mDaoSession.getSharedWithMeFileDao()
                    .deleteOne(_id);
        }
    }

    public boolean batchDeleteSharedWithMeFileItem(List<Integer> _ids) {
        if (_ids == null || _ids.size() == 0) {
            return false;
        }
        synchronized (mSharedWithMeLock) {
            return mDaoSession.getSharedWithMeFileDao()
                    .batchDelete(_ids);
        }
    }

    public void updateSharedWithMeFileItemResetOperationStatus() {
        //If _user_id is -1, then reject all services.
        if (_user_id == -1) {
            return;
        }
        synchronized (mSharedWithMeLock) {
            mDaoSession.getSharedWithMeFileDao()
                    .updateResetAllOperationStatus(_user_id);
        }
    }
    //endregion

    //region Project&ProjectMember table.
    public int upsertProjectItem(int id, String parentTenantId, String parentTenantName, String tokenGroupName,
                                 String name, String description, String displayName, long creationTime,
                                 long configurationModified, int totalMembers, int totalFiles,
                                 boolean isOwnedByMe, String ownerRawJson, String accountType,
                                 long trialEndTime, String expiry, String watermark) {
        //If _user_id is -1 then deny all services.
        int _id = -1;
        if (_user_id == -1) {
            return _id;
        }
        synchronized (mProjectLock) {
            _id = mDaoSession.getProjectDao().queryPrimaryKey(_user_id, id);
            if (_id == -1) {
                _id = mDaoSession.getProjectDao().insert(_user_id, id, parentTenantId, parentTenantName,
                        tokenGroupName, name, description, displayName, creationTime,
                        configurationModified, totalMembers, totalFiles,
                        isOwnedByMe, ownerRawJson, accountType,
                        trialEndTime, expiry, watermark);
            } else {
                mDaoSession.getProjectDao().update(_id, id, parentTenantId, parentTenantName,
                        tokenGroupName, name, description, displayName, creationTime,
                        configurationModified, totalMembers, totalFiles,
                        isOwnedByMe, ownerRawJson, accountType,
                        trialEndTime, expiry, watermark);
            }
            return _id;
        }
    }

    public boolean updateProjectItem(int _id, int id, String name, String description, String displayName,
                                     long creationTime, int totalMembers, int totalFiles, boolean isOwnedByMe,
                                     String accountType, long trialEndTime) {
        if (_user_id == -1) {
            return false;
        }
        if (_id == -1) {
            return false;
        }
        synchronized (mProjectLock) {
            mDaoSession.getProjectDao().update(_id, id, name, description, displayName,
                    creationTime, totalMembers, totalFiles, isOwnedByMe, accountType, trialEndTime);
            return true;
        }
    }

    public boolean updateProjectItemClassification(int _id, String classificationRaw) {
        if (_user_id == -1) {
            return false;
        }
        if (_id == -1) {
            return false;
        }
        synchronized (mProjectLock) {
            mDaoSession.getProjectDao()
                    .update(_id, classificationRaw);
            return true;
        }
    }

    public void updateProjectItemAccessCount(int _id) {
        if (_user_id == -1) {
            return;
        }
        if (_id == -1) {
            return;
        }
        synchronized (mProjectLock) {
            mDaoSession.getProjectDao()
                    .updateUserAccessCount(_id);
        }
    }

    public void updateProjectItemLastRefreshMillis(int _id) {
        if (_user_id == -1) {
            return;
        }
        if (_id == -1) {
            return;
        }
        synchronized (mProjectLock) {
            mDaoSession.getProjectDao()
                    .updateLastRefreshTime(_id);
        }
    }

    public void updateProjectItemLastAccessTime(int _id, long lastAccessTime) {
        if (_user_id == -1) {
            return;
        }
        if (_id == -1) {
            return;
        }
        synchronized (mProjectLock) {
            mDaoSession.getProjectDao()
                    .updateLastAccessTime(_id, lastAccessTime);
        }
    }

    public void updateProjectItemTrialEndTime(int _id, long trialEndTime) {
        if (_user_id == -1) {
            return;
        }
        if (_id == -1) {
            return;
        }
        synchronized (mProjectLock) {
            mDaoSession.getProjectDao()
                    .update(_id, trialEndTime);
        }
    }

    public void updateProjectItemQuotaAndUsage(int _id, long usage, long quota) {
        if (_user_id == -1) {
            return;
        }
        if (_id == -1) {
            return;
        }
        synchronized (mProjectLock) {
            mDaoSession.getProjectDao()
                    .update(_id, usage, quota);
        }
    }

    public void updateProjectItemTotalFiles(int _id, int total) {
        if (_user_id == -1) {
            return;
        }
        if (_id == -1) {
            return;
        }
        synchronized (mProjectLock) {
            mDaoSession.getProjectDao()
                    .updateTotalFiles(_id, total);
        }
    }

    public void upsertProjectMemberItem(int _project_id, int userId, String displayName,
                                        String email, long creationTime) {
        if (_project_id == -1) {
            return;
        }
        synchronized (mProjectMemberLock) {
            mDaoSession.getProjectDao()
                    .upsertMember(_project_id, userId, displayName, email, creationTime);
        }
    }

    public boolean batchInsertProjectMemberItem(int _project_id, List<ProjectMemberBean> inserts) {
        if (_project_id == -1) {
            return false;
        }
        if (inserts == null || inserts.size() == 0) {
            return false;
        }
        synchronized (mProjectMemberLock) {
            return mDaoSession.getProjectDao()
                    .batchInsertMember(_project_id, inserts);
        }
    }

    public List<IDBProjectItem> queryAllProjectItem() {
        List<IDBProjectItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mProjectLock) {
            List<ProjectBean> projectBeans = mDaoSession.getProjectDao()
                    .queryAll(_user_id);
            if (projectBeans == null || projectBeans.size() == 0) {
                return ret;
            }
            for (ProjectBean i : projectBeans) {
                ret.add(new DBProjectItem(i));
            }
            return ret;
        }
    }

    public List<IDBProjectItem> queryRecentProjectItem() {
        List<IDBProjectItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        synchronized (mProjectLock) {
            List<ProjectBean> projectBeans = mDaoSession.getProjectDao()
                    .queryRecent(_user_id);
            if (projectBeans == null || projectBeans.size() == 0) {
                return ret;
            }
            for (ProjectBean i : projectBeans) {
                ret.add(new DBProjectItem(i));
            }
            return ret;
        }
    }

    public IDBProjectItem queryProjectItem(int _id) {
        if (_user_id == -1) {
            return null;
        }
        if (_id == -1) {
            return null;
        }
        synchronized (mProjectLock) {
            return new DBProjectItem(mDaoSession.getProjectDao().queryOne(_id));
        }
    }

    public IDBProjectItem queryProjectItemByProjectID(int id) {
        if (_user_id == -1) {
            return null;
        }
        if (id == -1) {
            return null;
        }
        synchronized (mProjectLock) {
            return new DBProjectItem(mDaoSession.getProjectDao().queryOne(_user_id, id));
        }
    }

    public long queryProjectItemUsage(int _id) {
        if (_user_id == -1) {
            return -1;
        }
        if (_id == -1) {
            return -1;
        }
        synchronized (mProjectLock) {
            return mDaoSession.getProjectDao()
                    .queryUsage(_id);
        }
    }

    public long queryProjectItemQuota(int _id) {
        if (_user_id == -1) {
            return -1;
        }
        if (_id == -1) {
            return -1;
        }
        synchronized (mProjectLock) {
            return mDaoSession.getProjectDao()
                    .queryQuota(_id);
        }
    }

    public long queryProjectItemLastRefreshMillis(int _id) {
        if (_user_id == -1) {
            return -1;
        }
        if (_id == -1) {
            return -1;
        }
        synchronized (mProjectLock) {
            return mDaoSession.getProjectDao()
                    .queryLastRefreshMillis(_id);
        }
    }

    public void deleteProjectItem(int _project_id) {
        if (_project_id == -1) {
            return;
        }
        synchronized (mProjectLock) {
            mDaoSession.getProjectDao()
                    .deleteOne(_project_id);
        }
    }

    public List<IDBProjectItem.IMember> queryAllProjectMember(int _project_id) {
        if (_project_id == -1) {
            return null;
        }
        List<IDBProjectItem.IMember> ret = new ArrayList<>();
        synchronized (mProjectMemberLock) {
            List<ProjectMemberBean> results = mDaoSession.getProjectDao()
                    .queryAllMember(_project_id);
            if (results == null || results.size() == 0) {
                return ret;
            }
            for (ProjectMemberBean i : results) {
                ret.add(new DBProjectItem.Member(i));
            }
            return ret;
        }
    }

    public void deleteProjectMemberItem(int _project_member_id) {
        if (_project_member_id == -1) {
            return;
        }
        synchronized (mProjectMemberLock) {
            mDaoSession.getProjectDao().deleteMember(_project_member_id);
        }
    }

    public boolean batchDeleteProjectMemberItem(List<Integer> _ids) {
        synchronized (mProjectMemberLock) {
            return mDaoSession.getProjectDao().batchDeleteMember(_ids);
        }
    }
    //endregion

    //region ProjectFile table.
    public void upsertProjectFileItem(int _project_id, String id, String duid, String pathDisplay,
                                      String pathId, String name, String fileType,
                                      long lastModified, long creationTime, long size,
                                      boolean isFolder, String ownerRawJson,
                                      String lastModifiedUserRawJson) {
        //If _project_id is -1 then deny all services.
        if (_project_id == -1) {
            return;
        }
        ProjectFileDao fileDao = mDaoSession.getProjectFileDao();
        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();
            int _id = fileDao.queryPrimaryKey(_project_id, id);
            if (_id == -1) {
                //Insert new item.
                if (isFolder) {
                    fileDao.insert(_project_id, id, "", pathDisplay, pathId,
                            name, "", lastModified, creationTime, size, true,
                            ownerRawJson, lastModifiedUserRawJson);
                } else {
                    fileDao.insert(_project_id, id, duid, pathDisplay, pathId,
                            name, fileType, lastModified, creationTime, size, false,
                            ownerRawJson, lastModifiedUserRawJson);
                }
            } else {
                //Update existing item.
                if (isFolder) {
                    fileDao.update(_id, id, "", pathDisplay, pathId,
                            name, "", lastModified, creationTime, size, true,
                            ownerRawJson, lastModifiedUserRawJson);
                } else {
                    fileDao.update(_id, id, duid, pathDisplay, pathId,
                            name, fileType, lastModified, creationTime, size, false,
                            ownerRawJson, lastModifiedUserRawJson);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void upsertProjectFileExItem(int _project_id, String id, boolean isShared, boolean isRevoked,
                                        String shareWithProjectRawJson, String shareWithPersonRawJson) {
        ProjectFileExDao dao = mDaoSession.getProjectFileExDao();
        synchronized (mProjectFileExLock) {
            int _id = dao.queryPrimaryKey(_project_id, id);
            if (_id == -1) {
                dao.insert(_project_id, id, isShared, isRevoked,
                        shareWithProjectRawJson, shareWithPersonRawJson);
            } else {
                dao.update(_id, isShared, isRevoked,
                        shareWithProjectRawJson, shareWithPersonRawJson);
            }
        }
    }

    public boolean batchInsertProjectFileItem(int _project_id, List<ProjectFileBean> inserts) {
        if (_project_id == -1) {
            return false;
        }
        if (inserts == null || inserts.size() == 0) {
            return false;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();

            ProjectFileDao fileDao = mDaoSession.getProjectFileDao();
            return fileDao.batchInsert(_project_id, inserts);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean batchInsertProjectFileExItem(int _project_id, List<ProjectFileExBean> inserts) {
        if (inserts == null || inserts.size() == 0) {
            return false;
        }

        synchronized (mProjectFileExLock) {
            return mDaoSession.getProjectFileExDao()
                    .batchInsert(_project_id, inserts);
        }
    }

    public void batchUpdateProjectFileItem(List<ProjectFileBean> updates) {
        if (updates == null || updates.size() == 0) {
            return;
        }

        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getProjectFileDao().batchUpdate(updates);
        } finally {
            writeLock.unlock();
        }
    }

    public List<IDBProjectFileItem> queryAllProjectFileItem(int _project_id) {
        List<IDBProjectFileItem> ret = new ArrayList<>();
        if (_project_id == -1) {
            return ret;
        }
        List<ProjectFileExBean> beans;

        ReentrantReadWriteLock.ReadLock readLock = mProjectFileLock.readLock();
        try {
            readLock.lock();
            beans = mDaoSession.getProjectFileExDao().queryAll(_project_id);
        } finally {
            readLock.unlock();
        }

        if (beans == null || beans.size() == 0) {
            return ret;
        }
        for (ProjectFileExBean i : beans) {
            ret.add(new DBProjectFileItem(i));
        }
        return ret;
    }

    public List<IDBProjectFileItem> queryProjectRecentFileItem(int _project_id) {
        List<IDBProjectFileItem> ret = new ArrayList<>();
        if (_project_id == -1) {
            return ret;
        }
        ReentrantReadWriteLock.ReadLock readLock = mProjectFileLock.readLock();

        List<ProjectFileExBean> beans;
        try {
            readLock.lock();

            beans = mDaoSession.getProjectFileExDao().queryRecent(_project_id, 6);
        } finally {
            readLock.unlock();
        }
        if (beans == null || beans.size() == 0) {
            return ret;
        }
        for (ProjectFileExBean i : beans) {
            ret.add(new DBProjectFileItem(i));
        }
        return ret;
    }

    public List<IDBProjectFileItem> queryProjectAllSharedFileItem(int _project_id) {
        List<IDBProjectFileItem> ret = new ArrayList<>();
        if (_project_id == -1) {
            return ret;
        }
        ReentrantReadWriteLock.ReadLock readLock = mProjectFileLock.readLock();

        List<ProjectFileExBean> beans;
        try {
            readLock.lock();

            beans = mDaoSession.getProjectFileExDao()
                    .queryAllShared(_project_id);
        } finally {
            readLock.unlock();
        }
        if (beans == null || beans.size() == 0) {
            return ret;
        }
        for (ProjectFileExBean i : beans) {
            ret.add(new DBProjectFileItem(i));
        }
        return ret;
    }

    public void updateProjectFileItemShareWithProject(int _project_file_id,
                                                      String shareWithProjectRawJson) {
        synchronized (mProjectFileExLock) {
            mDaoSession.getProjectFileExDao()
                    .updateShareWithProject(_project_file_id, shareWithProjectRawJson);
        }
    }

    public void updateProjectFileItemShareStatus(int _project_file_id, boolean isShared) {
        synchronized (mProjectFileExLock) {
            mDaoSession.getProjectFileExDao()
                    .updateShareStatus(_project_file_id, isShared);
        }
    }

    public void updateProjectFileItemRevokeStatus(int _project_file_id, boolean isRevoked) {
        synchronized (mProjectFileExLock) {
            mDaoSession.getProjectFileExDao()
                    .updateRevokeStatus(_project_file_id, isRevoked);
        }
    }

    public int queryProjectFileItemOfflineRights(int _id) {
        if (_id == -1) {
            return -1;
        }
        ReentrantReadWriteLock.ReadLock readLock = mProjectFileLock.readLock();
        try {
            readLock.lock();

            return mDaoSession.getProjectFileDao().queryRights(_id);
        } finally {
            readLock.unlock();
        }
    }

    public String queryProjectFileItemOfflineObligations(int _id) {
        if (_id == -1) {
            return "";
        }
        ReentrantReadWriteLock.ReadLock readLock = mProjectFileLock.readLock();
        try {
            readLock.lock();

            return mDaoSession.getProjectFileDao().queryObligation(_id);
        } finally {
            readLock.unlock();
        }
    }

    public void deleteProjectFileItem(int _id) {
        if (_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getProjectFileDao().deleteOne(_id);
        } finally {
            writeLock.unlock();
        }
    }

    public void deleteProjectFileItem(int _project_id, String pathId) {
        if (_project_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getProjectFileDao().deleteOne(_project_id, pathId);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean batchDeleteProjectFileItem(List<Integer> _ids) {
        if (_ids == null || _ids.size() == 0) {
            return false;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();

            return mDaoSession.getProjectFileDao().batchDelete(_ids);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateProjectFileItemLocalPath(int _id, String localPath) {
        if (_id == -1) {
            return;
        }

        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getProjectFileDao().updateLocalPath(_id, localPath);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateProjectFileItemOperationStatus(int _id, int status) {
        if (_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getProjectFileDao().updateOperationStatus(_id, status);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateProjectFileItemRightsAndObligations(int _id, int rights, String obligationRaw) {
        if (_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getProjectFileDao().update(_id, rights, obligationRaw);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateProjectFileItemOfflineMarker(int _id, boolean offline) {
        if (_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getProjectFileDao().updateOffline(_id, offline);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateProjectFileItemLastModifiedTimeValue(int _id, long lastModifiedTime) {
        if (_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mProjectFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getProjectFileDao().updateLastModifiedTime(_id, lastModifiedTime);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateProjectFileItemResetLocalPath(int _project_id) {
        if (_user_id == -1) {
            return;
        }
        if (_project_id == -1) {
            return;
        }
        synchronized (mProjectFileLock) {
            mDaoSession.getProjectFileDao().updateResetAllLocalPath(_project_id);
        }
    }

    public void updateProjectFileItemResetOperationStatus(int _project_id) {
        if (_user_id == -1) {
            return;
        }
        if (_project_id == -1) {
            return;
        }
        synchronized (mProjectFileLock) {
            mDaoSession.getProjectFileDao().updateResetAllOperationStatus(_project_id);
        }
    }
    //endregion

    //region SharedWithProjectFile table.
    public boolean upsertSharedWithProjectFileItem(int _project_id, String duid, String name, long size,
                                                   String fileType, long sharedDate, String sharedBy,
                                                   String transactionId, String transactionCode,
                                                   String sharedLink, String rights, boolean isOwner,
                                                   int protectionType, String sharedBySpace) {
        if (_project_id == -1) {
            return false;
        }
        SharedWithProjectFileDao dao = mDaoSession.getSharedWithProjectFileDao();
        synchronized (mSharedWithProjectFileLock) {
            int _id = dao.queryPrimaryKey(_project_id, duid);
            if (_id == -1) {
                dao.insert(_project_id, duid, name, size,
                        fileType, sharedDate, sharedBy,
                        transactionId, transactionCode,
                        sharedLink, rights, isOwner,
                        protectionType, sharedBySpace);
            } else {
                dao.update(_id, duid, name, size,
                        fileType, sharedDate, sharedBy,
                        transactionId, transactionCode,
                        sharedLink, rights, isOwner,
                        protectionType, sharedBySpace);
            }
        }
        return true;
    }

    public void updateSharedWithProjectFileItemLocalPath(int _id, String path) {
        if (_id == -1) {
            return;
        }
        synchronized (mSharedWithProjectFileLock) {
            mDaoSession.getSharedWithProjectFileDao()
                    .update(_id, path);
        }
    }

    public void updateSharedWithProjectFileItemOperationStatus(int _id, int status) {
        if (_id == -1) {
            return;
        }
        synchronized (mSharedWithProjectFileLock) {
            mDaoSession.getSharedWithProjectFileDao()
                    .update(_id, status);
        }
    }

    public void updateSharedWithProjectFileItemOfflineStatus(int _id, boolean offline) {
        if (_id == -1) {
            return;
        }
        synchronized (mSharedWithProjectFileLock) {
            mDaoSession.getSharedWithProjectFileDao()
                    .update(_id, offline);
        }
    }

    public void updateSharedWithProjectFileItemRightsAndObligations(int _id,
                                                                    int rights,
                                                                    String obligationsRaw) {
        if (_id == -1) {
            return;
        }
        synchronized (mSharedWithProjectFileLock) {
            mDaoSession.getSharedWithProjectFileDao()
                    .update(_id, rights, obligationsRaw);
        }
    }

    public boolean batchInsertSharedWithProjectFileItem(int _project_id, List<SharedWithProjectFileBean> inserts) {
        if (_project_id == -1) {
            return false;
        }
        if (inserts == null || inserts.isEmpty()) {
            return false;
        }
        synchronized (mSharedWithProjectFileLock) {
            return mDaoSession.getSharedWithProjectFileDao()
                    .batchInsert(_project_id, inserts);
        }
    }


    public int querySharedWithProjectFileItemOfflineRights(int _id) {
        if (_id == -1) {
            return -1;
        }
        synchronized (mSharedWithProjectFileLock) {
            return mDaoSession.getSharedWithProjectFileDao()
                    .queryRights(_id);
        }
    }

    public String querySharedWithProjectFileItemOfflineObligations(int _id) {
        if (_id == -1) {
            return "";
        }
        synchronized (mSharedWithProjectFileLock) {
            return mDaoSession.getSharedWithProjectFileDao()
                    .queryObligations(_id);
        }
    }

    public List<IDBSharedWithProjectItem> queryAllSharedWithProjectFileItem(int _project_id) {
        List<IDBSharedWithProjectItem> ret = new ArrayList<>();

        List<SharedWithProjectFileBean> beans;
        synchronized (mSharedWithProjectFileLock) {
            beans = mDaoSession.getSharedWithProjectFileDao()
                    .queryAll(_project_id);
        }
        if (beans == null || beans.isEmpty()) {
            return ret;
        }
        for (SharedWithProjectFileBean i : beans) {
            ret.add(new DBSharedWithProjectFileItem(i));
        }
        return ret;
    }

    public void deleteOneSharedWithProjectFileItem(int _id) {
        synchronized (mSharedWithProjectFileLock) {
            mDaoSession.getSharedWithProjectFileDao()
                    .delete(_id);
        }
    }

    public boolean deleteSharedWithProjectFileItem(int _project_id) {
        synchronized (mSharedWithProjectFileLock) {
            return mDaoSession.getSharedWithProjectFileDao()
                    .deleteAll(_project_id);
        }
    }

    public boolean batchDeleteSharedWithProjectFileItem(List<Integer> _ids) {
        if (_ids == null || _ids.isEmpty()) {
            return false;
        }
        synchronized (mSharedWithProjectFileLock) {
            return mDaoSession.getSharedWithProjectFileDao()
                    .batchDelete(_ids);
        }
    }

    //endregion

    //region WorkSpaceFile table.
    public boolean batchInsertWorkSpaceFileItem(List<WorkSpaceFileBean> inserts) {
        if (_user_id == -1) {
            return false;
        }
        if (inserts == null || inserts.size() == 0) {
            return false;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            return mDaoSession.getWorkSpaceFileDao().batchInsert(_user_id, inserts);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean batchUpdateWorkSpaceFileItem(List<WorkSpaceFileBean> updates) {
        if (_user_id == -1) {
            return false;
        }
        if (updates == null || updates.size() == 0) {
            return false;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            return mDaoSession.getWorkSpaceFileDao().batchUpdate(updates);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean batchDeleteWorkSpaceFileItem(List<Integer> deletes) {
        if (_user_id == -1) {
            return false;
        }
        if (deletes == null || deletes.size() == 0) {
            return false;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            return mDaoSession.getWorkSpaceFileDao().batchDelete(deletes);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean upsertWorkSpaceFileItem(String id, String duid, String pathDisplay,
                                           String pathId, String name, String fileType,
                                           long lastModified, long creationTime, long size,
                                           boolean isFolder, String uploaderRawJson,
                                           String lastModifiedUserRawJson) {
        if (_user_id == -1) {
            return false;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            int _id = mDaoSession.getWorkSpaceFileDao().queryPrimaryKey(_user_id, id);
            if (_id == -1) {
                // insert new item
                if (isFolder) {
                    return mDaoSession.getWorkSpaceFileDao().insert(_user_id, id, "",
                            pathDisplay, pathId, name, "",
                            lastModified, creationTime, size,
                            true, uploaderRawJson, lastModifiedUserRawJson);
                } else {
                    return mDaoSession.getWorkSpaceFileDao().insert(_user_id, id, duid,
                            pathDisplay, pathId, name, fileType,
                            lastModified, creationTime, size,
                            false, uploaderRawJson,
                            lastModifiedUserRawJson);
                }
            } else {
                // update existing item.
                if (isFolder) {
                    return mDaoSession.getWorkSpaceFileDao().update(_id, id, "",
                            pathDisplay, pathId, name, "",
                            lastModified, creationTime, size,
                            true, uploaderRawJson, lastModifiedUserRawJson);
                } else {
                    return mDaoSession.getWorkSpaceFileDao().update(_id, id, duid,
                            pathDisplay, pathId, name, fileType,
                            lastModified, creationTime, size,
                            false, uploaderRawJson, lastModifiedUserRawJson);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public List<IDBWorkSpaceFileItem> queryAllWorkSpaceFileItem() {
        List<IDBWorkSpaceFileItem> ret = new ArrayList<>();
        if (_user_id == -1) {
            return ret;
        }
        ReentrantReadWriteLock.ReadLock readLock = mWorkSpaceFileLock.readLock();
        try {
            readLock.lock();

            List<WorkSpaceFileBean> beans = mDaoSession.getWorkSpaceFileDao().queryAll(_user_id);
            for (WorkSpaceFileBean f : beans) {
                ret.add(new DBWorkSpaceFileItem(f));
            }
            return ret;
        } finally {
            readLock.unlock();
        }
    }

    public void updateWorkSpaceItemResetOperationStatus() {
        if (_user_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getWorkSpaceFileDao()
                    .updateResetAllOperationStatus(_user_id);
        } finally {
            writeLock.unlock();
        }
    }

    public void deleteWorkSpaceFileItem(int _id) {
        if (_user_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getWorkSpaceFileDao().delete(_id);
        } finally {
            writeLock.unlock();
        }
    }

    public void deleteWorkSpaceFileItem(String pathId) {
        if (_user_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getWorkSpaceFileDao().delete(_user_id, pathId);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateWorkSpaceFileItemLocalPath(int _id, String localPath) {
        if (_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getWorkSpaceFileDao().update(_id, localPath);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateWorkSpaceFileItemRightsAndObligations(int _id, int rights, String obligations) {
        if (_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getWorkSpaceFileDao().update(_id, rights, obligations);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateWorkSpaceFileItemOfflineMarker(int _id, boolean offline) {
        if (_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getWorkSpaceFileDao().update(_id, offline);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateWorkSpaceFileItemOperationStatus(int _id, int status) {
        if (_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getWorkSpaceFileDao().update(_id, status);
        } finally {
            writeLock.unlock();
        }
    }

    public int queryWorkSpaceFileItemOfflineRights(int _id) {
        if (_id == -1) {
            return -1;
        }
        ReentrantReadWriteLock.ReadLock readLock = mWorkSpaceFileLock.readLock();
        try {
            readLock.lock();

            return mDaoSession.getWorkSpaceFileDao()
                    .queryOfflineRights(_id);
        } finally {
            readLock.unlock();
        }
    }

    public String queryWorkSpaceFileItemOfflineObligations(int _id) {
        if (_id == -1) {
            return "";
        }
        ReentrantReadWriteLock.ReadLock readLock = mWorkSpaceFileLock.readLock();
        try {
            readLock.lock();

            return mDaoSession.getWorkSpaceFileDao().queryOfflineObligations(_id);
        } finally {
            readLock.unlock();
        }
    }

    public void updateWorkSpaceFileItemLastModifiedTimeValue(int _id, long lastModifiedTime) {
        if (_id == -1) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = mWorkSpaceFileLock.writeLock();
        try {
            writeLock.lock();

            mDaoSession.getWorkSpaceFileDao().updateLastModifiedTime(_id, lastModifiedTime);
        } finally {
            writeLock.unlock();
        }
    }
    //endregion

    //region ActivityLog table.
    public void insertActivityLogItem(String duid, int operationId,
                                      int deviceType, String fileName, String filePath,
                                      int accessResult, long accessTime, String activityData) {
        if (_user_id == -1) {
            return;
        }
        synchronized (mActivityLogLock) {
            mDaoSession.getActivityLogDao().insert(_user_id, duid, operationId,
                    deviceType, fileName, filePath,
                    accessResult, accessTime, activityData);
        }
    }

    public List<OfflineLog> queryAllActivityLogItem() {
        if (_user_id == -1) {
            return null;
        }
        synchronized (mActivityLogLock) {
            List<OfflineLog> ret = new ArrayList<>();
            List<ActivityLogBean> activityLogBeans = mDaoSession.getActivityLogDao().queryAll(_user_id);
            if (activityLogBeans == null || activityLogBeans.size() == 0) {
                return ret;
            }
            for (ActivityLogBean i : activityLogBeans) {
                ret.add(OfflineLog.newByDBItem(i));
            }
            return ret;
        }
    }

    public void deleteAllActivityLogItem() {
        if (_user_id == -1) {
            return;
        }
        synchronized (mActivityLogLock) {
            mDaoSession.getActivityLogDao().deleteAll(_user_id);
        }
    }
    //endregion
}
