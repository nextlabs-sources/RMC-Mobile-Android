package com.skydrm.rmc.reposystem;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.errorHandler.ErrorCode;
import com.skydrm.rmc.filemark.FavoriteMarkImpl;
import com.skydrm.rmc.filemark.IMarkItem;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.localrepo.LocalRepoFactory;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.sort.Sorts;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.utils.sort.SortType;
import com.skydrm.sdk.utils.ParseJsonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;
import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;
import static com.skydrm.rmc.ExecutorPools.Select_Type.REGULAR_BACK_GROUND;


/**
 * Designed to maintain repositories
 * - cloud disk can be accessed from DateBase
 * - config whole system's mount point, i.e. where to store it
 * Knowledge:
 * -
 */
public class RepoSystem implements IRepoSystem {
    static private final DevLog log = new DevLog(RepoSystem.class.getSimpleName());
    private File mountPoint;
    private List<ILocalRepo> stockRepos;        // All repos system supported
    private List<ILocalRepo> livingRepos;       // All activated local repos;
    private ILocalRepo focusedRepo;
    private NXFolder shadowRoot;
    private RunningMode repoMode; // by default
    private FavoriteState favoriteState;
    private OfflineState offlineState;
    private WrapperWorkingFolderObserver observer;
    private SortType sortType;

    public RepoSystem() {
        init();
    }


    private void init() {
        mountPoint = null;
        stockRepos = new ArrayList<>();
        livingRepos = new ArrayList<>();
        focusedRepo = null;
        shadowRoot = new NXFolder("/", "/", "root", 0);
        repoMode = RunningMode.SYNTHETIC;
        favoriteState = new FavoriteState();
        offlineState = new OfflineState();
        observer = new WrapperWorkingFolderObserver();
        sortType = SortType.NAME_ASCEND;
    }

    private void deinit() {
        try {
            // clear stockRepos
            if (stockRepos == null) {
                return;
            }
            for (ILocalRepo i : stockRepos) {
                try {
                    i.deactivate();
                } catch (Exception e) {
                    log.e(e);
                }
            }
            stockRepos.clear();
            stockRepos = null;
            // clear livingRepos
            if (livingRepos != null) {
                livingRepos.clear();
                livingRepos = null;
            }
            // clear focusedRepo
            focusedRepo = null;
            // clear shadowRoot
            shadowRoot = null;
            // clear fav&off state
            favoriteState = null;
            offlineState = null;
            // clear observer
            observer = null;
        } catch (Exception e) {
            //igonre
        }
    }


    @Override
    public void create(File mountPoint, String userID) {
        /**
         * According to requirement, each user has its own stockRepos, so distinguish each other by sid
         */
        // sanity check
        if (this.mountPoint != null) {
            // former system has been used, so close it first, then create the new instance
            close(); // will call deinit() internally
            init();
        }
        // create new session
        this.mountPoint = new File(mountPoint, userID);        // amend mount point
        if (!Helper.makeSureDirExist(this.mountPoint)) {
            throw new RuntimeException(ErrorCode.E_FS_MOUNTPOINT_INVALID);
        }
    }

    @Override
    public void close() {
        // close repo system , save and release all resource
        try {
            saveToLocal();
        } catch (Exception e) {
            log.e(e);
        }
        deinit();
    }

    private void updateRunningMode() {
        if (livingRepos == null) {
            return;
        }
        if (livingRepos.size() == 1) {
            changeState(RunningMode.FOCUSED);
        } else {
            changeState(RunningMode.SYNTHETIC);
        }
    }

    @Override
    public void changeState(RunningMode newRunningMode) {
        repoMode = newRunningMode;
        // check status
        if (RunningMode.FOCUSED == repoMode) {
            if (focusedRepo == null) {
                if (livingRepos != null && !livingRepos.isEmpty()) {
                    focusedRepo = livingRepos.get(0);
                }
            }
        } else if (RunningMode.SYNTHETIC == repoMode) {
            if (livingRepos == null || livingRepos.isEmpty()) {
                focusedRepo = null;
                log.e("logic error, change mode into the synthetic, but list of living repo null or empty");
            } else if (livingRepos.size() == 1) {
                log.e("logic error, change mode into the synthetic, but list of living repo is not great than 2");
                focusedRepo = livingRepos.get(0);
            } else {
                focusedRepo = null;
            }
        }
    }

    @Override
    public RunningMode getState() {
        return repoMode;
    }

    @Override
    public void setSortType(SortType type) {
        sortType = type;
    }

    @Override
    public void attachWorkingFolderObserver(IWorkingFolderObserver observer) {
        if (this.observer != null) {
            this.observer.setObj(observer);
        }
    }


    @Override
    public void attach(@Nullable List<BoundService> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (BoundService s : list) {
            attach(s);
        }

    }

    private void attach(BoundService service) {
        /**
         * new service attaches into stockRepos
         * <p/>
         * if the service has exist in stockRepos , return directly
         */
        if (service == null) {
            log.v("in attach, service is null");
            return;
        }
        if (!service.isValidRepo()) {
            log.v("in attach, service is not a valid repo");
            return;
        }
        if (findInStockRepo(service) != null) { // has exist
            log.v("in attach, service has existed");
            return;
        }
        try {
            stockRepos.add(LocalRepoFactory.create(service, mountPoint, observer));
        } catch (Exception e) {
            log.e(e);
        }

    }

    @Override
    public void detach(@Nullable BoundService service) {
        try {
            // sanity check
            if (service == null) {
                log.v("in detach -- service is null");
                return;
            }
            // - service may still working, so try to deactivate it first
            deactivateRepo(service);

            // - remove from stockRepos
            ILocalRepo repo = findInStockRepo(service);
            if (repo == null) {
                log.v("service can not found in stock repo,detach");
                return;
            } else {
                repo.deactivate();
                repo.uninstall();
            }
            deleteInStockRepo(repo);
        } catch (Exception e) {
            log.e(e.getMessage(), e);
        }
    }

    @Override
    public void activate() {
        // add all repo in stocks into livingRepo only for which is not invalid
        if (livingRepos.isEmpty()) {
            for (ILocalRepo i : stockRepos) {
                //by osmond:  for spoiled repo, can not be activated
                BoundService s = i.getLinkedService();
                if (s == null) {
                    continue;
                }
                if (s.selected == 1) {
                    if (s.isValidRepo()) {   // cares about health repo only
                        livingRepos.add(i);
                    } else {
                        s.selected = 0;
                    }
                }
            }
        }
        NXFolder aRoot = new NXFolder("/", "/", "root", 0);
        for (ILocalRepo i : livingRepos) {
            try {
                i.activate();
                aRoot.addChild(i.getRoot(null).getChildren());
            } catch (Exception e) {
                log.e(e);
            }
        }
        // update ShadowRoot
        shadowRoot = aRoot;
    }

    @Override
    public void activateRepo(BoundService service) throws Exception {
        try {
            // sanity check
            if (service == null) {
                return;
            }
            // avoid reactivating the same one
            if (findInLivingRepo(service) != null) {
                return;
            }
            // avoid activating spoiled repo
            if (!service.isValidRepo()) {
                return;
            }

            // for this service not in stocks, add it in
            ILocalRepo repo = findInStockRepo(service);
            if (repo == null) {
                attach(service);
                // find again
                repo = findInStockRepo(service);
                if (repo == null) {
                    throw new RuntimeException("this repo can not be used");
                }
            }
            repo.activate();
            addInLivingRepo(repo);
            // change status of running mode
            updateRunningMode();
        } catch (RuntimeException e) {
            log.e(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deactivateRepo(BoundService service) throws Exception {
        // sanity check
        if (service == null) {
            log.v("service is null in deactivateRepo");
            return;
        }
        ILocalRepo repo = findInLivingRepo(service);
        if (repo == null) {
            log.v("can not find repo by service in deactivateRepo");
            return;
        }
        repo.deactivate();
        removeFromLivingRepo(repo);
        // change status of running mode
        updateRunningMode();
    }

    @Override
    public void selectOnlyMyDrive() throws Exception {
        List<BoundService> ss = SkyDRMApp.getInstance().getUserLinkedRepos();
        for (BoundService s : ss) {
            if (s.type == BoundService.ServiceType.MYDRIVE) {
                selectOnlyOneRepo(s);
                return;
            }
        }
        // for can not find myDrive, this is bug,use select all instead
        selectAllRepo();
    }

    @Override
    public void selectOnlyOneRepo(BoundService service) throws Exception {
        // sanity check
        if (service == null) {
            throw new RuntimeException(ErrorCode.E_RT_PARAM_SERVICE_INVALID);
        }
        // clear living repos
        for (ILocalRepo r : livingRepos) {
            r.deactivate();
        }
        livingRepos.clear();

        // for this service not in repos , add it
        ILocalRepo tobeSelected = findInStockRepo(service);
        if (tobeSelected == null) {
            attach(service);
            // find again
            tobeSelected = findInStockRepo(service);
            if (tobeSelected == null) {
                throw new RuntimeException("This repo can not be used");
            }
        }
        tobeSelected.activate();
        addInLivingRepo(tobeSelected);
        updateRunningMode();
        focusedRepo = tobeSelected;
    }

    @Override
    public void selectAllRepo() throws Exception {
        // un-select all
        livingRepos.clear();
        for (ILocalRepo r : stockRepos) {
            // make sure if r.service is valid
            if (!r.getLinkedService().isValidRepo()) {
                continue;
            }
            r.activate();
            livingRepos.add(r);
        }
        updateRunningMode();
    }

    @Override
    public void updateRepo(BoundService service) {
        // sanity check
        if (service == null) {
            return;
        }
        ILocalRepo repo = findInStockRepo(service);
        if (repo == null) {
            return;
        }
        BoundService repoService = repo.getLinkedService();
        if (repoService == null) {
            return;
        }
        // update
        {
            repoService.rmsNickName = service.rmsNickName;
            repoService.rmsRepoId = service.rmsRepoId;
            repoService.accountToken = service.accountToken;

        }

        IRemoteRepo remoteRepo = repo.getRemoteRepo();
        if (remoteRepo != null) {
            remoteRepo.updateToken(service.accountToken);
        }


    }

    @Override
    public boolean isInSyntheticRoot() {
        return repoMode == RunningMode.SYNTHETIC && livingRepos.size() > 1;
    }

    @Override
    public List<INxFile> listRoot(final IRemoteRepo.IListFilesCallback callback) throws Exception {
        // sanity check
        if (callback == null) {
            throw new RuntimeException(ErrorCode.E_RT_PARAM_CALLBACK_INVALID);
        }
        if (livingRepos.isEmpty()) {
            throw new RuntimeException(ErrorCode.E_RT_PARAM_INVALID + "no any livingRepo refreshRoot");
        }
        // get from local first
        NXFolder root = new NXFolder("/", "/", "root", 0);
        List<ILocalRepo> currentRepos = new ArrayList<>(livingRepos);
        for (ILocalRepo i : currentRepos) {
            try {
                INxFile repoRoot = i.getRoot(null);
                root.addChild(sort(repoRoot.getChildren()));
            } catch (Exception e) {
                log.e(e);
            }
        }
        updateRunningMode();
        // prepare shadow root
        shadowRoot = root;

        // refresh from network
        new SyncRootAsyncTask(currentRepos, new SyncRootAsyncTask.SyncCallback() {
            @Override
            public void onFinished(Map<ILocalRepo, NXFolder> result) {
                NXFolder resultRoot = new NXFolder("/", "/", "root", 0);
                int count = 0;
                for (Map.Entry<ILocalRepo, NXFolder> e : result.entrySet()) {
                    if (findInLivingRepo(e.getKey().getLinkedService()) != null) {
                        count++;
                        resultRoot.addChild(e.getValue().getChildren());
                    }
                }
                if (count >= 2) {
                    changeState(RunningMode.SYNTHETIC);
                } else {
                    changeState(RunningMode.FOCUSED);
                }
                RepoSystem.this.shadowRoot = resultRoot;
                callback.onFinishedList(true, RepoSystem.this.shadowRoot, "");

            }
        }).executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI));
        return shadowRoot.getChildren();
    }

    @Override
    public void syncWorkingFolder(@Nonnull final IRemoteRepo.IListFilesCallback callback) throws Exception {
        switch (repoMode) {
            case SYNTHETIC:
                if (livingRepos.isEmpty()) {
                    callback.onFinishedList(false, null, "Please choose one repository at least.");
                    break;
                }
                // fix-bug:avoid trigger ConcurrentModificationException
                // the following SyncRootAsyncTask is a time-heavily task, during recursion , livingRepos may be changed ,so shallowCopy it
                new SyncRootAsyncTask(new ArrayList<>(livingRepos), new SyncRootAsyncTask.SyncCallback() {
                    @Override
                    public void onFinished(Map<ILocalRepo, NXFolder> result) {
                        NXFolder resultRoot = new NXFolder("/", "/", "root", 0);
                        int count = 0;
                        for (Map.Entry<ILocalRepo, NXFolder> e : result.entrySet()) {
                            if (findInLivingRepo(e.getKey().getLinkedService()) != null) {
                                count++;
                                resultRoot.addChild(e.getValue().getChildren());
                            }
                        }
                        if (count >= 2) {
                            changeState(RunningMode.SYNTHETIC);
                        } else {
                            changeState(RunningMode.FOCUSED);
                        }
                        RepoSystem.this.shadowRoot = resultRoot;
                        callback.onFinishedList(true, RepoSystem.this.shadowRoot, "");
                    }
                }).executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI));
                break;
            case FOCUSED:
                ILocalRepo repo = focusedRepo;
                if (repo == null) {
                    callback.onFinishedList(false, null, "No focused repository");
                    break;
                }
                repo.syncWorkingFolder(new IRemoteRepo.IListFilesCallback() {
                    @Override
                    public void onFinishedList(boolean taskStatus, INxFile file, String errorMsg) {
                        callback.onFinishedList(taskStatus, taskStatus ? sort(file) : file, errorMsg);
                    }
                });
                break;
        }
    }

    @Override
    public List<INxFile> listFolder() throws Exception {
        NXFolder root = new NXFolder("/", "/", "root", 0);
        // for synthetic mode , always get each repo's root
        if (isInSyntheticRoot()) {
            for (ILocalRepo i : livingRepos) {
                try {
                    root.addChild(sort(i.getWorkingFolder().getChildren()));
                } catch (Exception e) {
                    log.e(e);
                }
            }

            shadowRoot = root;
            return shadowRoot.getChildren();
        } else {
            // on focused mode
            ILocalRepo target = null;
            if (focusedRepo != null) {
                target = focusedRepo;
            } else {
                // assert living repo's size ==1
                if (livingRepos != null && livingRepos.size() == 1) {
                    target = livingRepos.get(0);
                }
            }
            if (target == null) {
                return root.getChildren();
            }
            return target.getWorkingFolder().getChildren();
        }

    }

    @Nullable
    @Override
    public File getFile(INxFile document, IRemoteRepo.IDownLoadCallback callback) throws Exception {
        try {
            BoundService service = document.getService();
            if (service == null) {
                throw new RuntimeException("Null service in" + document.getLocalPath());
            }
            ILocalRepo repo = findInStockRepo(service);
            if (repo == null) {
                throw new RuntimeException("can not find attached repo by" + service);
            }
            return repo.getDocument(document, callback);
        } catch (Exception e) {
            log.e(e.getMessage(), e);
            throw e;
        }
    }

    @Nullable
    @Override
    public File getFilePartialContent(INxFile document, int start, int length, IRemoteRepo.IDownLoadCallback callback) throws Exception {
        try {
            BoundService service = document.getService();
            if (service == null) {
                throw new RuntimeException("Null service in" + document.getLocalPath());
            }
            ILocalRepo repo = findInStockRepo(service);
            if (repo == null) {
                throw new RuntimeException("can not find attached repo by" + service);
            }
            return repo.getDocumentPartialContent(document, start, length, callback);
        } catch (Exception e) {
            log.e(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void uploadFile(BoundService selService,
                           INxFile parentFolder,
                           String fileName,
                           File localFile,
                           IRemoteRepo.IUploadFileCallback callback) throws FileUploadException {
        ILocalRepo repo;
        repo = findInStockRepo(selService);
        if (repo == null) {
            throw new FileUploadException("can not find proper cloud service", FileUploadException.ExceptionCode.ParamInvalid);
        }
        repo.uploadFile(parentFolder, fileName, localFile, callback);
    }

    @Override
    public void deleteFile(INxFile file) {
        try {
            BoundService service = file.getService();
            ILocalRepo repo = findInLivingRepo(service);
            repo.delete(file);
        } catch (Exception e) {
            log.e(e.getMessage(), e);
        }
    }


    @Override
    public void createFolder(String folderName) throws FolderCreateException {
        createFolder(focusedRepo.getLinkedService(), folderName);
    }

    //
    @Override
    public void createFolder(BoundService service, String folderName) throws FolderCreateException {

        if (service == null) {
            throw new FolderCreateException("service is null", FolderCreateException.ExceptionCode.ParamInvalid);
        }
        if (repoMode == RunningMode.FAVORITE || repoMode == RunningMode.OFFLINE) {
            throw new FolderCreateException("can not working on fav/off mode", FolderCreateException.ExceptionCode.IllegalOperation);
        }
        ILocalRepo localRepo = findInStockRepo(service);
        if (localRepo == null) {
            throw new FolderCreateException("can not find local repository", FolderCreateException.ExceptionCode.IllegalOperation);
        }
        INxFile workingFolder = null;
        try {
            workingFolder = localRepo.getWorkingFolder();
        } catch (Exception e) {
            throw new FolderCreateException("can not find working folder in local repository", FolderCreateException.ExceptionCode.IllegalOperation);
        }
        localRepo.createFolder(workingFolder, folderName);

    }

    @Override
    public void createFolder(BoundService service, INxFile parent, String folderName) throws FolderCreateException {
        if (service == null) {
            throw new FolderCreateException("service is null", FolderCreateException.ExceptionCode.ParamInvalid);
        }
        if (parent == null) {
            throw new FolderCreateException("parent is null", FolderCreateException.ExceptionCode.ParamInvalid);
        }
        if (folderName == null || folderName.isEmpty()) {
            throw new FolderCreateException("folder name is null or empty", FolderCreateException.ExceptionCode.ParamInvalid);
        }
        ILocalRepo localRepo = findInStockRepo(service);
        if (localRepo == null) {
            throw new FolderCreateException("can not find local repository", FolderCreateException.ExceptionCode.IllegalOperation);
        }
        localRepo.createFolder(parent, folderName);
    }


    @Override
    public INxFile findWorkingFolder() throws Exception {
        switch (repoMode) {
            case SYNTHETIC:
                return sort(shadowRoot);
            case FOCUSED:
                if (focusedRepo == null) {
                    throw new RuntimeException("Error: invalid param, focused repos is null");
                }
                return sort(focusedRepo.getWorkingFolder());
        }
        throw new Exception(ErrorCode.E_RT_SHOULD_NEVER_REACH_HERE);
    }

    /**
     * must judge @{folder} is in which living repo , change focusedRepo as it, and then to do the remains
     * <p/>
     * - add support for FAVORITE&OFFLINE mode
     */
    @Override
    public List<INxFile> enterFolder(INxFile folder, IRemoteRepo.IListFilesCallback callback) throws Exception {
        // sanity check
        // find the folder is belong to which living repo
        BoundService service = folder.getService();
        if (service == null) {
            throw new RuntimeException(ErrorCode.E_RT_PARAM_INVALID + "can not get service of the folder");
        }
        ILocalRepo repo = findInLivingRepo(folder.getService());
        if (repo == null) {
            throw new RuntimeException(ErrorCode.E_RT_PARAM_INVALID + "can not get host repo of the folder");
        }

        focusedRepo = repo;
        changeState(RunningMode.FOCUSED); // change focused repo
        return sort(repo.listFolder(folder, callback, true));
    }

    @Override
    public void updateFilesMark3(List<ParseJsonUtils.AllRepoFavoListBean> list) {
        // sanity check
        if (list == null) {
            return;
        }
        // find mydrive repo
        ILocalRepo mydrive = null;
        for (ILocalRepo r : new ArrayList<>(stockRepos)) {
            if (r.getLinkedService().type == BoundService.ServiceType.MYDRIVE) {
                mydrive = r;
                break;
            }
        }
        if (mydrive == null) {
            return;
        }

        // convert data type
        Map<String, Boolean> batched = new HashMap<>();
        for (ParseJsonUtils.AllRepoFavoListBean bean : list) {
            if (bean == null) {
                continue;
            }
            if (bean.isFromMyVault()) {
                continue;
            }
            //All favorite files are only from myVault and myDrive
            //If we filter myVault the rest are myDrive.So we can ignore the judge condition.
            //another reason is that the repo type can be Both "S3"[centos store] and "ONEDRIVEFROBUSINESS"[autorms store]
//            if (!TextUtils.equals(bean.getRepoType(), "S3")) {
//                continue;
//            }
            batched.put(bean.getPathId(), bean.isFavorited());
        }

        // update each each item in my drive repo
        mydrive.updateBatchedFavoriteMarks(batched);
    }

    @Override
    public void markAsFavorite(INxFile file) {
        try {
            BoundService service = file.getService();
            if (service == null) {
                return;
            }
            ILocalRepo repo = findInStockRepo(service);
            if (repo == null) {
                return;
            }
            repo.markAsFavorite(file);
            FavoriteMarkImpl.getInstance().addMarkFileCacheSet(service.rmsRepoId, adapter(file, repo.getParent(file).getCloudFileID(), true));
            log.i("markAsFavorite: " + file.getDisplayPath());
        } catch (Exception e) {
            log.e(e.getMessage(), e);
            throw e;
        }
    }

    private IMarkItem adapter(INxFile f, String parentFileId, boolean isMark) {
        class MarkItem implements IMarkItem {
            INxFile f;
            String parentFileId;
            boolean isMark;

            public MarkItem(INxFile f, String parentFileId, boolean isMark) {
                this.f = f;
                this.parentFileId = parentFileId;
                this.isMark = isMark;
            }

            @Override
            public boolean isUnMark() {
                return !isMark;
            }

            @Override
            public String getPathId() {
                return f.getCloudPath();
            }

            @Override
            public String getDisplayPath() {
                return f.getDisplayPath();
            }

            @Override
            public String getParentFileId() {
                return parentFileId;
            }

            @Override
            public long getFileSize() {
                return f.getSize();
            }

            @Override
            public long getLastModifiedTime() {
                return f.getLastModifiedTimeLong();
            }
        }

        return new MarkItem(f, parentFileId, isMark);
    }

    @Override
    public void unmarkAsFavorite(INxFile file) {
        try {
            BoundService service = file.getService();
            if (service == null) {
                return;
            }
            ILocalRepo repo = findInLivingRepo(service);
            if (repo == null) {
                return;
            }
            repo.unmarkAsFavorite(file);
            FavoriteMarkImpl.getInstance().addMarkFileCacheSet(service.rmsRepoId, adapter(file, repo.getParent(file).getCloudFileID(), false));
            log.i("unmarkAsFavorite: " + file.getDisplayPath());
        } catch (Exception e) {
            log.e(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void markAsOffline(INxFile file) {
        try {
            BoundService service = file.getService();
            if (service == null) {
                return;
            }
            ILocalRepo repo = findInLivingRepo(service);
            if (repo == null) {
                return;
            }
            repo.markAsOffline(file);
            log.i("markAsOffline: " + file.getLocalPath());
        } catch (Exception e) {
            log.e(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void unmarkAsOffline(INxFile file) {
        try {
            BoundService service = file.getService();
            if (service == null) {
                return;
            }
            ILocalRepo repo = findInLivingRepo(service);
            if (repo == null) {
                return;
            }
            repo.unmarkAsOffline(file);
            log.i("unmarkAsOffline: " + file.getLocalPath());
        } catch (Exception e) {
            log.e(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<INxFile> getFavoriteFiles() {
        try {
            return sort(favoriteState.getFavoriteFiles());
        } catch (Exception e) {
            log.e(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<INxFile> getOfflineFiles() {
        try {
            return sort(offlineState.getOfflineFiles());
        } catch (Exception e) {
            log.e(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public INxFile backToParent() {
        INxFile rt = null;
        switch (repoMode) {
            case SYNTHETIC:
                rt = shadowRoot;
                break;
            /*
                FOCUSED MODE may change to SYNTHETIC
             */
            case FOCUSED:
                ILocalRepo repo = focusedRepo;
                if (repo != null) {
                    rt = repo.backToParent();
                    // check if need to change to SYNTHETIC mode
                    if (rt.getDisplayPath().equalsIgnoreCase("/") && livingRepos.size() > 1) {
                        changeState(RunningMode.SYNTHETIC);
                        try {
                            return sort(listWorkingFolder());
                        } catch (Exception ignored) {
                        }
                    }
                }
                break;
        }
        return sort(rt);
    }

    @Override
    public INxFile getParent(INxFile child, boolean byService) {
        if (byService) {
            ILocalRepo repo = findInStockRepo(child.getService());
            if (repo != null) {
                return repo.getParent(child);
            }
            throw new RuntimeException("should never reach here");
        }

        INxFile rt = null;
        switch (repoMode) {
            case SYNTHETIC:
                rt = shadowRoot;
                break;
            case FOCUSED:
                ILocalRepo repo = focusedRepo;
                if (repo != null) {
                    rt = repo.getParent(child);
                }
                break;
        }
        return rt;
    }

    @Override
    public void clearCache(final SkyDRMApp.ClearCacheListener listener) {
        class ClearTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                //long start = System.currentTimeMillis();
                try {
                    for (ILocalRepo i : stockRepos) {
                        i.clearCache();
                    }
                    RenderHelper.clearSharedWithMeLocalCache();
                    RenderHelper.clearMyVaultLocalCache();
                } catch (Exception ignored) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                listener.finished();
            }
        }
        new ClearTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }

    @Override
    public void clearCache() {
        for (ILocalRepo i : stockRepos) {
            i.clearCache();
        }
    }

    @Override
    public void clearRepoCache(final BoundService boundService, final SkyDRMApp.ClearCacheListener listener) {
        if (boundService == null) {
            throw new RuntimeException("Error, invalid param boundService");
        }
        if (listener == null) {
            throw new RuntimeException("Error, invalid param listener");
        }

        class ClearTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                //long start = System.currentTimeMillis();
                try {
                    ILocalRepo i = findInStockRepo(boundService);
                    if (i == null) {
                        return null;
                    }
                    i.clearCache();
                } catch (Exception ignored) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                listener.finished();
            }
        }
        new ClearTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }

    @Override
    public long calReposCacheSize() {
        long size = 0;
        for (ILocalRepo i : stockRepos) {
            try {
                size += i.calCacheSize();
            } catch (Exception ignored) {

            }
        }
        return size;
    }

    @Override
    public void getRepoInformation(BoundService boundService, ILocalRepo.IRepoInfoCallback callback) {
        ILocalRepo repo = findInStockRepo((boundService));
        if (repo == null) {
            attach(boundService);
            //find again
            repo = findInStockRepo(boundService);
            if (repo == null) {
                throw new RuntimeException(ErrorCode.E_REPO_CANNOT_FIND_LOCAL_REPO + "assigned by boundService");
            }

        }

        // check network status
        if (!SkyDRMApp.getInstance().isNetworkAvailable()) {
            callback.result(false, null, ErrorCode.E_IO_NO_NETWORK);
            return;
        }
        repo.getRepoInfo(callback);
    }

    @Override
    public int getSizeOfLivingRepo() {
        return livingRepos.size();
    }

    private INxFile listWorkingFolder() throws Exception {
        NXFolder root = new NXFolder("/", "/", "root", 0);
        switch (repoMode) {
            case SYNTHETIC:     // for synthetic mode , always get each repo's root
                for (ILocalRepo i : livingRepos) {
                    try {
                        root.addChild(i.getRoot(null).getChildren());
                    } catch (Exception e) {
                        log.e(e);
                    }
                }
                break;
            case FOCUSED:
                if (focusedRepo == null) {
                    throw new RuntimeException(ErrorCode.E_RT_PARAM_INVALID + "focused repos is null");
                }
                try {
                    root.addChild(focusedRepo.getWorkingFolder().getChildren());
                } catch (Exception e) {
                    log.e(e);
                }
                break;

        }
        shadowRoot = root;
        return shadowRoot;

    }

    @Override
    public List<BoundService> getStockedNotSpoiledServiceInRepoSystem() {
        List<BoundService> rt = new ArrayList<>();
        for (ILocalRepo i : stockRepos) {
            BoundService s = i.getLinkedService();
            if (s != null && s.isValidRepo()) {
                rt.add(s);
            }
        }
        return rt;
    }

    @Override
    public INxFile folderTreeClone(BoundService service) {
        ILocalRepo repo = findInStockRepo(service);
        if (repo == null) {
            return null;
        }
        return repo.folderTreeClone();
    }

    @Override
    public void saveToLocal() {
        if (stockRepos == null || stockRepos.size() == 0) {
            return;
        }
        for (final ILocalRepo r : stockRepos) {
            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        r.notifyToSaveItself();
                    } catch (Exception e) {
                        log.e(e);
                    }
                }
            });
        }
    }

    /**
     * use BoundService.id to find ILocalRepo
     */
    @Override
    public ILocalRepo findInStockRepo(BoundService service) {
        if (service == null) {
            return null;
        }
        for (ILocalRepo i : stockRepos) {
            if (service.isSameOneInLocal(i.getLinkedService())) {
                return i;
            }
        }
        return null;
    }

    private void deleteInStockRepo(ILocalRepo repo) {
        if (repo == null) {
            return;
        }
        stockRepos.remove(repo);
    }

    private INxFile sort(INxFile parent) {
        return Sorts.sort(parent, sortType);
    }

    private List<INxFile> sort(List<INxFile> files) {
        return Sorts.sort(files, sortType);
    }

    @Override
    public
    @Nullable
    ILocalRepo findInLivingRepo(BoundService service) {
        if (service == null) {
            return null;
        }
        //sanity check.
        if (livingRepos == null || livingRepos.size() == 0) {
            return null;
        }
        for (ILocalRepo i : livingRepos) {
            if (service.isSameOneInLocal(i.getLinkedService())) {
                return i;
            }
        }
        return null;
    }

    private void addInLivingRepo(ILocalRepo repo) {
        // sanity check
        if (livingRepos == null) {
            livingRepos = new LinkedList<>();
            livingRepos.add(repo);
            return;
        }
        // check if had exist, avoid adding same one
        BoundService repoService = repo.getLinkedService();
        for (ILocalRepo i : livingRepos) {
            BoundService s = i.getLinkedService();
            if (TextUtils.equals(repoService.account, s.account) && repoService.type == s.type) {
                return;
            }
        }
        // not exist ,add in
        livingRepos.add(repo);
    }

    private void removeFromLivingRepo(ILocalRepo repo) {
        livingRepos.remove(repo);
    }

    static private class SyncRootAsyncTask extends AsyncTask<Void, Void, Map<ILocalRepo, NXFolder>> {
        List<ILocalRepo> repos; // may trigger ConcurrentModificationException
        SyncCallback callback;

        private SyncRootAsyncTask(List<ILocalRepo> repos, SyncCallback callback) {
            this.repos = repos;
            this.callback = callback;
        }

        @Override
        protected Map<ILocalRepo, NXFolder> doInBackground(Void... params) {
            Map<ILocalRepo, NXFolder> result = new HashMap<>(repos.size());
            for (ILocalRepo i : repos) {
                try {
                    NXFolder tree = (NXFolder) i.syncRoot(); // get this repo 's root
                    result.put(i, tree);
                } catch (Exception e) {
                    log.e("SyncRootAsyncTask" + e.toString(), e);
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Map<ILocalRepo, NXFolder> result) {
            callback.onFinished(result);
        }

        public interface SyncCallback {
            void onFinished(Map<ILocalRepo, NXFolder> result);
        }
    }

    private class FavoriteState {
        public List<INxFile> getFavoriteFiles() {
            NXFolder root = new NXFolder("/", "/", "root", 0);
            // normal way
//            for (ILocalRepo i : livingRepos) {
//                try {
//                    root.addChild(i.getFavoriteDocuments());
//                } catch (Exception e) {
//                    log.e(e);
//                }
//            }
            // new requirement, only find from MyDrive
            for (ILocalRepo r : stockRepos) {
                try {
                    if (r.getLinkedService().type == BoundService.ServiceType.MYDRIVE) {
                        root.addChild(r.getFavoriteDocuments());
                        break;
                    }
                } catch (Exception e) {
                    log.e(e);
                }
            }
            return root.getChildren();
        }
    }

    private class OfflineState {

        public List<INxFile> getOfflineFiles() {
            NXFolder root = new NXFolder("/", "/", "root", 0);
            for (ILocalRepo i : livingRepos) {
                try {
                    root.addChild(i.getOfflineDocuments());
                } catch (Exception e) {
                    log.e(e);
                }
            }
            return root.getChildren();
        }
    }

    private class WrapperWorkingFolderObserver implements IWorkingFolderObserver {

        IWorkingFolderObserver observer;

        public void setObj(IWorkingFolderObserver obj) {
            observer = obj;
        }

        @Override
        public void onChildrenChanged(INxFile workingFolder) {
            if (!sanityCheck(workingFolder)) {
                return;
            }
            observer.onChildrenChanged(workingFolder);
        }

        private boolean sanityCheck(INxFile workingFolder) {
            try {
                if (observer == null) {
                    return false;
                }
                if (repoMode != RunningMode.FOCUSED) {
                    return false;
                }
                if (workingFolder == null) {
                    return false;
                }
                BoundService service = workingFolder.getService();
                if (service == null) {
                    return false;
                }
                if (focusedRepo.getLinkedService().type != service.type) {
                    return false;
                }
                if (!TextUtils.equals(focusedRepo.getLinkedService().account, service.account)) {
                    return false;
                }
                return true;
            } catch (Exception e) {
                log.e(e);
            }
            return false;

        }
    }


}


