package com.skydrm.rmc.datalayer.repo.myvault;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.myvault.MyVaultFileBean;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatListener;
import com.skydrm.rmc.datalayer.repo.NxlRepo;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.dbbridge.DBProvider;
import com.skydrm.rmc.dbbridge.IDBMyVaultItem;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.LogSystem;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.filemark.FavoriteMarkImpl;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.service.protect.IProtectService;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.StringUtils;
import com.skydrm.sdk.IRecipients;
import com.skydrm.sdk.exception.NotGrantedShareRights;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.policy.AdhocPolicy;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.myVault.MyVaultFileListResult;
import com.skydrm.sdk.rms.rest.myVault.MyVaultListFileRequestParas;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileParams;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileResult;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.types.favorite.AllRepoFavFileRequestParas;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;
import com.skydrm.sdk.utils.ParseJsonUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyVaultRepo extends NxlRepo implements IDataService, IProtectService, Parcelable {
    public MyVaultRepo() {
    }

    protected MyVaultRepo(Parcel in) {
    }

    public static final Creator<MyVaultRepo> CREATOR = new Creator<MyVaultRepo>() {
        @Override
        public MyVaultRepo createFromParcel(Parcel in) {
            return new MyVaultRepo(in);
        }

        @Override
        public MyVaultRepo[] newArray(int size) {
            return new MyVaultRepo[size];
        }
    };

    @Override
    public void updateResetAllOperationStatus() {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateMyVaultItemResetOperationStatus();
    }

    @Override
    public List<INxlFile> list(int type) {
        if (type == NxlFileType.SHARED_BY_ME.getValue()) {
            return listActiveShared();
        } else if (type == NxlFileType.PROTECTED.getValue()) {
            return listProtected();
        } else if (type == NxlFileType.REVOKED.getValue()) {
            return listRevoked();
        } else if (type == NxlFileType.DELETED.getValue()) {
            return listDeleted();
        } else if (type == NxlFileType.FAVORITE.getValue()) {
            return listFavorite();
        } else if (type == NxlFileType.OFFLINE.getValue()) {
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
        List<MyVaultFileListResult.ResultsBean.DetailBean.FilesBean> remotes = syncInternal();
        List<IDBMyVaultItem> locals = SkyDRMApp.getInstance().getDBProvider().queryAllMyVaultItem();
        filterOutModifiedItems(remotes, locals);

        syncFav();

        return list(type);
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
        // sync favorite.
        try {
            syncFavInternal();
        } catch (SessionInvalidException
                | InvalidRMClientException
                | RmsRestAPIException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearCache() {
        //FileUtils.deleteRecursively(getMyVaultMountPoint());
        //0 mean all files.
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
        //return FileUtils.getSize(getMyVaultMountPoint());
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

    public boolean protectFile(String plainPath,
                               Rights rights,
                               Obligations obligations,
                               Expiry expiry, @Nullable INxFile fileItem) throws IllegalArgumentException,
            IllegalStateException, InvalidRMClientException, FileNotFoundException, RmsRestAPIException,
            SessionInvalidException, NotNxlFileException, TokenAccessDenyException {
        if (plainPath == null || plainPath.isEmpty()) {
            throw new IllegalArgumentException("Argument plain path must not be null.");
        }
        if (rights == null) {
            throw new IllegalArgumentException("Argument rights must not be null.");
        }
        if (obligations == null) {
            throw new IllegalArgumentException("Argument obligations must not be null.");
        }
        if (expiry == null) {
            throw new IllegalArgumentException("Argument obligations must not be null.");
        }
        File f = new File(plainPath);
        if (!f.exists() || f.isDirectory()) {
            throw new IllegalStateException("Illegal file state retrieved.");
        }
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        String ownerID = session
                .getRmUser()
                .getMembershipId();

        String nxlPath = FileOperation.protectFile(ownerID, f, rights, obligations, expiry);
        if (nxlPath == null || nxlPath.isEmpty()) {
            throw new IllegalStateException("Try encrypt file failed.Invalid file performed.");
        }

        // prepare param
        String srcPathId = "Local";
        String srcPathDisplay = "Local";
        String srcRepoId = "Local";
        String srcRepoName = "Local";
        String srcRepoType = "Local";

        if (fileItem != null) {
            BoundService service = fileItem.getService();
            srcPathId = fileItem.getDisplayPath();
            srcPathDisplay = fileItem.getDisplayPath();
            srcRepoId = service.rmsRepoId;
            srcRepoName = service.rmsNickName;
            srcRepoType = service.alias;
            // param rectify
            if (srcRepoId == null || srcRepoId.isEmpty()) {
                srcRepoId = "";
            }
            if (srcRepoName == null || srcRepoName.isEmpty()) {
                srcRepoName = srcRepoType;
            }
        }

        MyVaultUploadFileParams params = new MyVaultUploadFileParams.Builder()
                .setNxlFile(new File(nxlPath))
                .setSrcPathId(srcPathId)
                .setSrcPathDisplay(srcPathDisplay)
                .setSrcRepoId(srcRepoId)
                .setSrcRepoName(srcRepoName)
                .setSrcRepoType(srcRepoType)
                .build();

        MyVaultUploadFileResult result = session.getRmsRestAPI()
                .getMyVaultService(session.getRmUser())
                .uploadFileToMyVault(params, new ProgressRequestListener() {
                    @Override
                    public void onRequestProgress(long bytesWritten, long contentLength, boolean done) throws IOException {

                    }
                });
        if (result != null) {
            // send log
            LogSystem.sendProtectLog(new File(nxlPath));
            // list file.
            sync(0);

            FileUtils.deleteFile(nxlPath);
            return true;
        } else {
            FileUtils.deleteFile(nxlPath);
        }

        return false;
    }

    public boolean shareLocalFile(String plainPath, String filePathId, String filePath,
                                  Rights rights, Obligations obligations, @Nullable Expiry expiry,
                                  final List<String> emailList, String comments, boolean bAsAttachment)
            throws FileNotFoundException, RmsRestAPIException {
        if (plainPath == null || plainPath.isEmpty()) {
            throw new IllegalArgumentException("The plain file path is nullable.");
        }
        File f = new File(plainPath);
        if (!f.exists() || !f.isFile()) {
            throw new IllegalStateException("The file is in wrong state,not exists or just a dir.");
        }
        if (rights == null || obligations == null) {
            throw new IllegalArgumentException("The rights or obligations applied to this file is nullable.");
        }
        if (f.length() == 0) {
            throw new IllegalStateException("Invalid file performed.");
        }
        if (emailList == null || emailList.isEmpty()) {
            throw new IllegalStateException("The email list requires at least one recipient.");
        }

        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        // 1. prepare issuer.
        String membershipId = null;
        try {
            membershipId = session.getRmUser().getMembershipId();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        if (membershipId == null || membershipId.isEmpty()) {
            throw new IllegalStateException("The issuer state is wrong maybe should try re-login.");
        }

        // 2. prepare ad-hoc policy
        AdhocPolicy adhocPolicy = new AdhocPolicy(membershipId, rights, obligations, expiry);
        String duid = session.getRmsClient().shareLocalPlainFileToMyVault(f.getPath(), bAsAttachment, adhocPolicy,
                filePathId, filePath,
                new IRecipients() {
                    @Override
                    public Iterator<String> iterator() {
                        return emailList.iterator();
                    }
                }, comments);

        if (duid == null || duid.isEmpty()) {
            return false;
        }

        try {
            sync(0);
        } catch (SessionInvalidException
                | InvalidRMClientException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean shareLocalFile(String nxlPath, boolean bAsAttachment,
                                  final List<String> emails, String comments)
            throws NotGrantedShareRights, RmsRestAPIException, NotNxlFileException, TokenAccessDenyException {
        if (nxlPath == null || nxlPath.isEmpty()) {
            return false;
        }

        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        String duid = session.getRmsClient().shareLocalNxlFileToMyVault(nxlPath, bAsAttachment, new IRecipients() {
            @Override
            public Iterator<String> iterator() {
                return emails.iterator();
            }
        }, comments);

        if (duid == null || duid.isEmpty()) {
            return false;
        }

        try {
            sync(0);
        } catch (SessionInvalidException
                | InvalidRMClientException e) {
            e.printStackTrace();
        }

        return true;
    }

    private File getMyVaultMountPoint() {
        return RenderHelper.getMyVaultMountPoint();
    }

    private List<INxlFile> syncFav()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        //post
        FavoriteMarkImpl.getInstance().syncMarkedFileToRms();
        //get and notify
        syncFavInternal();
        return listFavorite();
    }

    private List<INxlFile> listActiveShared() {
        return adapt2NxlItem(SkyDRMApp.getInstance().getDBProvider().queryMyVaultActiveSharedItem());
    }

    private List<INxlFile> listProtected() {
        return adapt2NxlItem(SkyDRMApp.getInstance().getDBProvider().queryMyVaultProtectedItem());
    }

    private List<INxlFile> listRevoked() {
        return adapt2NxlItem(SkyDRMApp.getInstance().getDBProvider().queryMyVaultRevokedItem());
    }

    private List<INxlFile> listDeleted() {
        return adapt2NxlItem(SkyDRMApp.getInstance().getDBProvider().queryMyVaultDeletedItem());
    }

    private List<INxlFile> listFavorite() {
        return adapt2NxlItem(SkyDRMApp.getInstance().getDBProvider().queryMyVaultFavoriteItem());
    }

    private List<INxlFile> listOffline() {
        return adapt2NxlItem(SkyDRMApp.getInstance().getDBProvider().queryMyVaultOfflineItem());
    }

    private void syncFavInternal() throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        SkyDRMApp app = SkyDRMApp.getInstance();
        SkyDRMApp.Session2 session = app.getSession();
        List<ParseJsonUtils.AllRepoFavoListBean> allRepoFavFileList = session
                .getRmsRestAPI()
                .getFavoriteService(session.getRmUser())
                .getFavoriteFileListInAllRepos(new AllRepoFavFileRequestParas());

        List<INxlFile> local = listFavorite();
        filterOutFavModifiedItems(allRepoFavFileList, local);

        app.onUpdateFileMark(allRepoFavFileList);
    }

    private List<INxlFile> listInternal() {
        return adapt2NxlItem(SkyDRMApp.getInstance().getDBProvider().queryAllMyVaultItem());
    }

    private List<INxlFile> adapt2NxlItem(List<IDBMyVaultItem> lfs) {
        List<INxlFile> ret = new ArrayList<>();
        if (lfs == null || lfs.size() == 0) {
            return ret;
        }
        for (IDBMyVaultItem i : lfs) {
            ret.add(MyVaultFile.newByDBItem(i));
        }
        return ret;
    }

    private List<MyVaultFileListResult.ResultsBean.DetailBean.FilesBean> syncInternal() throws RmsRestAPIException,
            SessionInvalidException,
            InvalidRMClientException {
        RemoteFileConfig config = new RemoteFileConfig.Build()
                .setFilterType(MyVaultListFileRequestParas.FilterType.FILTER_TYPE_ALL_FILES)
                .build();
        MyVaultListFileRequestParas params = new MyVaultListFileRequestParas(config.orderBy,
                config.filterType, config.searchText);

        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        MyVaultFileListResult result = session.getRmsRestAPI()
                .getMyVaultService(session.getRmUser())
                .listMyVaultFile(params);
        //filterOutDuplicateItems(files);
        return result.getResults().getDetail().getFiles();
    }

    private void filterOutDuplicateItems(List<MyVaultFileListResult.ResultsBean.DetailBean.FilesBean> files) {
        if (files == null || files.size() == 0) {
            return;
        }
        List<Integer> toBeRemoved = new ArrayList<>();
        List<String> target = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MyVaultFileListResult.ResultsBean.DetailBean.FilesBean f = files.get(i);
            if (!target.contains(f.getDuid())) {
                target.add(f.getDuid());
            } else {
                toBeRemoved.add(i);
            }
        }
        if (toBeRemoved.size() == 0) {
            return;
        }
        for (int i = 0; i < toBeRemoved.size(); i++) {
            int index = toBeRemoved.get(i);
            files.remove(index);
        }
    }

    private boolean filterOutFavModifiedItems(List<ParseJsonUtils.AllRepoFavoListBean> remote, List<INxlFile> local) {
        if (remote == null || remote.size() == 0) {
            if (local == null || local.size() == 0) {
                return false;
            }
            return batchRemoveMarker(local);
        }

        List<ParseJsonUtils.AllRepoFavoListBean> rVaultFavList = new ArrayList<>();
        for (ParseJsonUtils.AllRepoFavoListBean f : remote) {
            if (f.isFromMyVault()) {
                rVaultFavList.add(f);
            }
        }
        if (rVaultFavList.size() == 0) {
            return batchRemoveMarker(local);
        }

        Map<String, Boolean> remove = new HashMap<>();
        for (INxlFile l : local) {
            if (l == null) {
                continue;
            }
            if (contains(remote, l)) {
                continue;
            }
            remove.put(l.getPathId(), false);
        }
        boolean removeAffect = SkyDRMApp.getInstance().getDBProvider().batchUpdateMyVaultFileItem(remove);

        Map<String, Boolean> updates = new HashMap<>();
        for (ParseJsonUtils.AllRepoFavoListBean r : rVaultFavList) {
            if (r == null) {
                continue;
            }
            if (contains(local, r)) {
                continue;
            }
            updates.put(r.getPathId(), r.isFavorited());
        }
        boolean updateAffect = SkyDRMApp.getInstance().getDBProvider().batchUpdateMyVaultFileItem(updates);
        return removeAffect || updateAffect;
    }

    private boolean contains(List<ParseJsonUtils.AllRepoFavoListBean> remote, INxlFile l) {
        if (remote == null || remote.size() == 0) {
            return false;
        }
        if (l == null) {
            return false;
        }
        for (ParseJsonUtils.AllRepoFavoListBean r : remote) {
            if (equals(r, l)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(List<INxlFile> local, ParseJsonUtils.AllRepoFavoListBean r) {
        if (local == null || local.size() == 0) {
            return false;
        }
        if (r == null) {
            return false;
        }
        for (INxlFile l : local) {
            if (equals(r, l)) {
                return true;
            }
        }
        return false;
    }

    private boolean equals(ParseJsonUtils.AllRepoFavoListBean r, INxlFile l) {
        if (r == null) {
            return false;
        }
        if (l == null) {
            return false;
        }
        if (!r.getPathId().equals(l.getPathId())) {
            return false;
        }
        return r.isFavorited() == ((NxlDoc) l).isFavorite();
    }

    private boolean batchRemoveMarker(List<INxlFile> local) {
        if (local == null || local.size() == 0) {
            return false;
        }
        Map<String, Boolean> removed = new HashMap<>();
        for (INxlFile f : local) {
            removed.put(f.getPathId(), false);
        }
        return SkyDRMApp.getInstance().getDBProvider().batchUpdateMyVaultFileItem(removed);
    }

    private boolean filterOutModifiedItems(List<MyVaultFileListResult.ResultsBean.DetailBean.FilesBean> remote,
                                           List<IDBMyVaultItem> local) {
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

        List<String> upserts = new ArrayList<>();
        for (MyVaultFileListResult.ResultsBean.DetailBean.FilesBean i : remote) {
            if (i == null) {
                continue;
            }
            if (contains(local, i)) {
                continue;
            }
            upserts.add(i.getDuid());
            dbProvider.upsertMyVaultFileItem(MyVaultFileBean.getInsertItem(i));
        }

        List<Integer> _ids = new ArrayList<>();
        for (IDBMyVaultItem i : local) {
            if (i == null) {
                continue;
            }
            if (contains(remote, i)) {
                continue;
            }
            if (upserts.contains(i.getDuid())) {
                continue;
            }
            _ids.add(i.getMyVaultFileTBPK());
        }

        boolean deleted = dbProvider.batchDeleteMyVaultFileItem(_ids);
        return upserts.size() > 0 || deleted;
    }

    private boolean batchInsert(List<MyVaultFileListResult.ResultsBean.DetailBean.FilesBean> remote,
                                DBProvider dbProvider) {
        if (remote == null || remote.size() == 0) {
            return false;
        }
        List<MyVaultFileBean> inserts = new ArrayList<>();
        for (MyVaultFileListResult.ResultsBean.DetailBean.FilesBean i : remote) {
            if (i == null) {
                continue;
            }
            inserts.add(MyVaultFileBean.getInsertItem(i));
        }
        if (inserts.size() == 0) {
            return false;
        }

        try {
            return dbProvider.batchInsertMyVaultFileItem(inserts);
        } catch (Exception e) {
            e.printStackTrace();
            int upsertAffected = 0;
            for (MyVaultFileBean i : inserts) {
                dbProvider.upsertMyVaultFileItem(i);
                upsertAffected++;
            }
            return upsertAffected > 0;
        }
    }

    private boolean batchDelete(List<IDBMyVaultItem> local, DBProvider dbProvider) {
        List<Integer> _ids = new ArrayList<>();
        for (IDBMyVaultItem i : local) {
            _ids.add(i.getMyVaultFileTBPK());
        }
        return dbProvider.batchDeleteMyVaultFileItem(_ids);
    }

    private boolean contains(List<IDBMyVaultItem> lfs,
                             MyVaultFileListResult.ResultsBean.DetailBean.FilesBean r) {
        if (lfs == null || lfs.size() == 0) {
            return false;
        }
        if (r == null) {
            return false;
        }
        for (IDBMyVaultItem l : lfs) {
            if (equals(r, l)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(List<MyVaultFileListResult.ResultsBean.DetailBean.FilesBean> rfs,
                             IDBMyVaultItem l) {
        if (rfs == null || rfs.size() == 0) {
            return false;
        }
        if (l == null) {
            return false;
        }
        for (MyVaultFileListResult.ResultsBean.DetailBean.FilesBean r : rfs) {
            if (equals(r, l)) {
                return true;
            }
        }
        return false;
    }

    private boolean equals(MyVaultFileListResult.ResultsBean.DetailBean.FilesBean r,
                           IDBMyVaultItem l) {
        if (r == null || l == null) {
            return false;
        }
//        if (!r.getDuid().equals(l.getDuid())) {
//            return false;
//        }
        return r.getSize() == l.getSize() &&
                r.getSharedOn() == l.getSharedOn() &&
                r.isRevoked() == l.isRevoked() &&
                r.isDeleted() == l.isDeleted() &&
                r.isShared() == l.isShared() &&
//                r.isFavorited() == l.isFavorite() &&
                r.getName().equalsIgnoreCase(l.getName()) &&
                StringUtils.list2Str(r.getSharedWith()).equals(StringUtils.list2Str(l.getSharedWith())) &&
                StringUtils.list2Str(r.getRights()).equals(StringUtils.list2Str(l.getRights()));
    }

    @Override
    public String getServiceName(@NonNull Context ctx) {
        return ctx.getString(R.string.my_vault);
    }

    @Override
    public String getClassificationRaw() {
        return null;
    }

    @Override
    public User.IExpiry getIExpiry() {
        return null;
    }

    @Override
    public String getWatermark() {
        return null;
    }

    @Override
    public void protect(String normalPath,
                        Rights rights, Obligations obligations, Expiry expiry,
                        String parentPathId,
                        IProtectCallback callback) {

    }

    @Override
    public void protect(String normalPath,
                        Map<String, Set<String>> tags,
                        String parentPathId,
                        IProtectCallback callback) {

    }

    @Override
    public boolean upload(File nxlFile, String parentPathId)
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    static class RemoteFileConfig {
        private String filterType;
        private String orderBy;
        private String searchText;

        private RemoteFileConfig(RemoteFileConfig.Build build) {
            this.filterType = build.filterType;
            this.orderBy = build.orderBy;
            this.searchText = build.searchText;
        }

        static class Build {
            String filterType;
            String orderBy = MyVaultListFileRequestParas.OrderBy.ORDER_BY_CREATE_TIME;
            String searchText;

            RemoteFileConfig.Build setFilterType(String filterType) {
                this.filterType = filterType;
                return this;
            }

            public RemoteFileConfig.Build setOrderBy(String orderBy) {
                this.orderBy = orderBy;
                return this;
            }

            public RemoteFileConfig.Build setSearchText(String searchText) {
                this.searchText = searchText;
                return this;
            }

            public RemoteFileConfig build() {
                return new RemoteFileConfig(this);
            }
        }
    }
}
