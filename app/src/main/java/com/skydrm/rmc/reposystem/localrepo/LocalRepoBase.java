package com.skydrm.rmc.reposystem.localrepo;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.errorHandler.ErrorCode;
import com.skydrm.rmc.reposystem.ILocalRepo;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.IWorkingFolderObserver;
import com.skydrm.rmc.reposystem.RepoInfo;
import com.skydrm.rmc.reposystem.Utils;
import com.skydrm.rmc.reposystem.exception.FileListException;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.localrepo.internals.Cache;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.remoterepo.RemoteRepoFactory;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.reposystem.types.NxFileBase;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;
import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;
import static com.skydrm.rmc.ExecutorPools.Select_Type.REGULAR_BACK_GROUND;


public class LocalRepoBase implements ILocalRepo {
    //Verify the folder name of regular expressions
    private static final String FOLDER_NAME_PATTERN = "^[\\u00C0-\\u1FFF\\u2C00-\\uD7FF\\w \\x22\\x23\\x27\\x2C\\x2D]+$";
    //private static final String DOCUMENT_NAME_PATTERN = "^[\\u00C0-\\u1FFF\\u2C00-\\uD7FF\\w \\x22\\x23\\x27\\x2C\\x2D]+$";
    protected File mountPoint;   //   /CallerDefined/cache/[sid]/[service]/
    protected BoundService service;   // each repo must be bound with a service to hold basic information of RemoteRepo
    protected IRemoteRepo remoteRepo; // local repo must link with a remote repo, 1 v.s. 1
    protected Cache cache = new Cache();      // the actual docs will be got from cache object
    private DevLog log = new DevLog(LocalRepoBase.class.getSimpleName());
    private boolean isActive = false;
    // current refresh,
    volatile private NXFolder refreshingFolder = null;
    private IWorkingFolderObserver observer;


    public LocalRepoBase(BoundService service, File mountPoint,
                         IWorkingFolderObserver observer) throws Exception {
        install(mountPoint, service);
        setObserver(observer);
    }

    /**
     * @param mountPoint the base is used by this file system as a beginning point that every items installed
     *                   the caller can change this mount point, for example: use SD-card or others
     */
    @Override
    public void install(File mountPoint, BoundService service) throws Exception {
        // sanity check
        if (mountPoint == null || !mountPoint.exists()) {
            log.e("mount point is null");
            throw new RuntimeException(SkyDRMApp.getInstance().getString(R.string.err_excpt_local_repo_invalid_mount_point));
        }
        if (service == null) {
            log.e("service is null");
            throw new RuntimeException(SkyDRMApp.getInstance().getString(R.string.err_excpt_local_repo_invalid_service_param));
        }
        this.service = service;
        remoteRepo = RemoteRepoFactory.create(this.service);
        this.mountPoint = amendMountPoint(service, mountPoint);
        cache.init(this.mountPoint, service);
        if (service.isSelected()) {
            ExecutorPools.SelectSmartly(NETWORK_TASK).execute(new PrefetchTask(cache.getCacheTree()));
        }
    }

    private File amendMountPoint(BoundService service, File mountPoint) throws Exception {
        // amend mount base
        switch (service.type) {
            case SHAREPOINT_ONLINE:
            case SHAREPOINT:
                mountPoint = new File(mountPoint,
                        service.alias + "_" + service.alias + "_" + URLEncoder.encode(service.accountID + "\n" + service.account, "UTF-8"));
                break;
            case MYDRIVE:
                mountPoint = new File(mountPoint, service.alias);
                break;
            case DROPBOX:
            case ONEDRIVE:
            case GOOGLEDRIVE:
            case BOX:
                mountPoint = new File(mountPoint, service.alias + "_" + service.accountID);
                break;
            default:
                log.e("this service type dost not supported");
                throw new RuntimeException(SkyDRMApp.getInstance().getString(R.string.err_excpt_local_repo_service_supported));
        }
        if (!Helper.makeSureDirExist(mountPoint)) {
            log.e("mount point path is not exist," + mountPoint.getAbsolutePath());
            throw new RuntimeException(SkyDRMApp.getInstance().getString(R.string.err_excpt_local_repo_install) + mountPoint.getPath());
        }
        return mountPoint;
    }

    @Override
    public void uninstall() {
        deactivate();
        Helper.RecursionDeleteFile(mountPoint);
    }

    @Override
    public void activate() {
        if (!isActive) {
            service.selected = 1;
            isActive = true;
        }
    }

    @Override
    public void deactivate() {
        if (isActive) {
            service.selected = 0;
            //fix-bug: when doing the deactivate, user root as the current working folder
            cache.onChangeWorkingFolder(cache.getCacheTree());
            isActive = false;
            refreshingFolder = null;
        }
    }

    @Override
    public void setObserver(IWorkingFolderObserver observer) {
        this.observer = observer;
    }

    public boolean isObserverResultMatchWorkingFolder(@Nullable INxFile result) {
        try {
            if (!isActive) {
                return false;
            }
            if (refreshingFolder == null) {
                return false;
            }
            if (result == null) {
                return false;
            }
            return TextUtils.equals(refreshingFolder.getLocalPath(), result.getLocalPath());
        } catch (Exception e) {
            log.e(e);
            return false;
        }
    }

    @Override
    public BoundService getLinkedService() {
        return service;
    }

    @Override
    public IRemoteRepo getRemoteRepo() {
        return remoteRepo;
    }

    @Override
    public void updateBatchedFavoriteMarks(final Map<String, Boolean> marks) {
        log.v("updateBatchedFavoriteMarks");
        // update each matched files
        cache.enumerateCacheTreeSafe(new Utils.OnEnumerate() {
            @Override
            public void onFileFound(INxFile file) {
                Boolean bFav = marks.get(file.getCloudFileID());
                if (bFav != null) {
                    log.v("set Fav mark:\n path:" + file.getDisplayPath() + "\t " + bFav);
                    ((NxFileBase) file).setMarkedAsFavorite(bFav);
                } else {
                    ((NxFileBase) file).setMarkedAsFavorite(false);
                }
            }
        });
    }

    @Override
    public void markAsFavorite(INxFile file) {
        changeItemFeature(file, ItemFeatureOperate.SET_FAVORITE);
    }

    @Override
    public void unmarkAsFavorite(INxFile file) {
        changeItemFeature(file, ItemFeatureOperate.UNSET_FAVORITE);
    }

    @Override
    public void markAsOffline(INxFile file) {
        changeItemFeature(file, ItemFeatureOperate.SET_OFFLINE);
    }

    @Override
    public void unmarkAsOffline(INxFile file) {
        changeItemFeature(file, ItemFeatureOperate.UNSET_OFFLINE);
    }

    private void changeItemFeature(INxFile file, ItemFeatureOperate feature) {
        if (file == null) {
            return;
        }
        INxFile child = cache.getCacheTree().findNode(file.getLocalPath());
        if (child == null) {
            return;
        }
        switch (feature) {
            case SET_FAVORITE:
                ((NxFileBase) file).setMarkedAsFavorite(true);
                ((NxFileBase) child).setMarkedAsFavorite(true);
                break;
            case UNSET_FAVORITE:
                ((NxFileBase) file).setMarkedAsFavorite(false);
                ((NxFileBase) child).setMarkedAsFavorite(false);
                break;
            case SET_OFFLINE:
                ((NxFileBase) file).setMarkedAsOffline(true);
                ((NxFileBase) child).setMarkedAsOffline(true);
                break;
            case UNSET_OFFLINE:
                ((NxFileBase) file).setMarkedAsOffline(false);
                ((NxFileBase) child).setMarkedAsOffline(false);
                break;
        }
    }

    @Override
    public List<INxFile> getFavoriteDocuments() {
        final List<INxFile> newFavorites = new ArrayList<>();
        cache.enumerateCacheTreeSafe(new Utils.OnEnumerate() {
            @Override
            public void onFileFound(INxFile file) {
                if (file.isMarkedAsFavorite()) {
                    newFavorites.add(file);
                }

            }
        });
        return newFavorites;
    }

    @Override
    public List<INxFile> getOfflineDocuments() {
//        return new ArrayList<>(offlines);
        final List<INxFile> newOfflines = new ArrayList<>();
        cache.enumerateCacheTreeSafe(new Utils.OnEnumerate() {
            @Override
            public void onFileFound(INxFile file) {
                if (file.isMarkedAsOffline()) {
                    newOfflines.add(file);
                }
            }
        });
        // replace
        return newOfflines;

    }

    @Override
    public INxFile getWorkingFolder() throws Exception {

        NXFolder workingFolder = cache.getWorkingFolder();
        // update
        refreshingFolder = workingFolder;
        ExecutorPools.SelectSmartly(NETWORK_TASK).execute(new WorkingFolderRefreshTask(workingFolder));
        return workingFolder;
    }

    @Override
    public void syncWorkingFolder(IRemoteRepo.IListFilesCallback callback) throws Exception {
        // do not change the current working folder
        fireAsyncTaskSyncFolder(cache.getWorkingFolder(), false, callback);
    }

    @Override
    public INxFile getRoot(IRemoteRepo.IListFilesCallback callback) throws Exception {
        // try get from cache frist
        NXFolder root = cache.getCacheTree();
        // root has children or callback is null
        if (!root.getChildren().isEmpty() || callback == null) {
            changeWorkingFolder(root);
            return root;
        }
        // cache-matching failed , call network to get latest one
        fireAsyncTaskSyncFolder(root, true, callback);
        return null;
    }

    @Override
    public INxFile syncRoot() {
        try {
            NXFolder root = (NXFolder) remoteRepo.listFiles(new NXFolder(cache.getCacheTree()));
            if (root != null) {
                cache.onSyncFolder(root);
            }
            return cache.getCacheTree();
        } catch (FileListException e) {
            log.e(e);
        }
        return cache.getCacheTree(); // return default
    }

    @Override
    public File getDocument(INxFile document, IRemoteRepo.IDownLoadCallback callback) throws Exception {
        // sanity check
        if (document.isFolder())
            throw new RuntimeException(ErrorCode.E_NXLF_PARAM_FOLDER_REQUIRED);
        if (!(document instanceof NXDocument)) {
            throw new RuntimeException(ErrorCode.E_NXLF_PARAM_FOLDER_REQUIRED);
        }
        // try to get from cache firstF
        File rt = cache.getDocument((NXDocument) document);
        if (rt != null) {
            return rt;
        }
        // download from cloud
        syncFile((NXDocument) document, callback);
        return null;

    }

    @Override
    public File getDocumentPartialContent(INxFile document, int start, int length, IRemoteRepo.IDownLoadCallback callback) throws Exception {
        // sanity check
        if (document.isFolder())
            throw new RuntimeException(ErrorCode.E_NXLF_PARAM_FOLDER_REQUIRED);
        if (!(document instanceof NXDocument)) {
            throw new RuntimeException(ErrorCode.E_NXLF_PARAM_FOLDER_REQUIRED);
        }

        if (start < 0 || length < 0) {
            throw new IllegalArgumentException(ErrorCode.E_FS_PARTIAL_DOWNLOAD_INVALID_PARAS);
        }

        // try to get from cache first --- Note: here we will get the full size file(partial file content not cache.)
        File rt = cache.getDocument((NXDocument) document);
        if (rt != null) {
            return rt;
        }

        // download partial content.
        remoteRepo.downloadFilePartial(document,
                Helper.nxPath2AbsPath(cache.getRootDir(), document.getLocalPath()),
                start,
                length,
                callback);

        return null;
    }

    @Override
    public void uploadFile(final INxFile parentFolder,
                           final String fileName,
                           final File localFile,
                           final IRemoteRepo.IUploadFileCallback callback) throws FileUploadException {
        // Sanity check
        // - parent
        if (parentFolder == null || !parentFolder.isFolder()) {
            throw new FileUploadException("illegal parentFolder", FileUploadException.ExceptionCode.ParamInvalid);
        }
        INxFile parent = cache.findNodeInTree(parentFolder);
        if (parent == null) {
            throw new FileUploadException("illegal parentFolder, not exist in cache", FileUploadException.ExceptionCode.ParamInvalid);
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new FileUploadException("illegal file name", FileUploadException.ExceptionCode.ParamInvalid);
        }
        if (fileName.length() > 128) {
            throw new FileUploadException("file name too long, must be less than 128", FileUploadException.ExceptionCode.NameTooLong);
        }
        // - fileName
        // -- collision of identical name with existed one
        try {
            cache.readLock.lock();
            for (INxFile f : parent.getChildren()) {
                if (f != null && !f.isFolder() && TextUtils.equals(f.getName(), fileName)) {
                    throw new FileUploadException("file name collided with an existed one",
                            FileUploadException.ExceptionCode.NamingCollided);
                }
            }
        } finally {
            cache.readLock.unlock();
        }
        // -- violation of naming convention
//        Pattern pattern = Pattern.compile(DOCUMENT_NAME_PATTERN);
//        if (!pattern.matcher(fileName).matches()) {
//            throw new FileUploadException("name violation", FileUploadException.ExceptionCode.NamingViolation);
//        }
        // - the local file
        if (localFile == null || !localFile.exists()) {
            throw new FileUploadException("local file does not exist", FileUploadException.ExceptionCode.ParamInvalid);
        }
        if (localFile.length() == 0) {
            throw new FileUploadException("local file does not have any content", FileUploadException.ExceptionCode.ParamInvalid);
        }
        // - callback
        if (callback == null) {
            throw new FileUploadException("callback is null", FileUploadException.ExceptionCode.ParamInvalid);
        }

        remoteRepo.uploadFile(parent, fileName, localFile, new IRemoteRepo.IUploadFileCallback() {
            @Override
            public void cancelHandler(ICancelable handler) {
                callback.cancelHandler(handler);
            }

            @Override
            public void onFinishedUpload(boolean taskStatus, NXDocument uploadedDoc, FileUploadException e) {
                if (taskStatus) {
                    // update successfully
                    uploadedDoc.setBoundService(service);
                    // tell cache to receive this file
                    cache.addDocument((NXFolder) parentFolder, uploadedDoc, localFile);
                    // make sure folder focus not changed
                    if (isObserverResultMatchWorkingFolder(parentFolder)) {
                        // notify observers
                        observer.onChildrenChanged(cache.findNodeInTree(parentFolder));
                    }
                }
                callback.onFinishedUpload(taskStatus, uploadedDoc, e);
            }

            @Override
            public void progressing(long newValue) {
                callback.progressing(newValue);
            }
        });

    }

    @Override
    public void updateFile(INxFile parentFolder,
                           INxFile updateFile,
                           File localFile,
                           IRemoteRepo.IUploadFileCallback callback) throws Exception {
        // Sanity check
        if (callback == null)
            throw new NullPointerException(ErrorCode.E_RT_PARAM_CALLBACK_INVALID);

        remoteRepo.updateFile(parentFolder, updateFile, localFile, callback);

    }

    @Override
    public List<INxFile> listFolder(INxFile folder, IRemoteRepo.IListFilesCallback callback,
                                    boolean isChangeWorkingFolder) throws Exception {
        // sanity check
        if (!folder.isFolder())
            throw new RuntimeException(ErrorCode.E_NXLF_PARAM_FOLDER_REQUIRED + "folder");
        // try get from cache first
        NXFolder f = (NXFolder) cache.tryToGetFromCache(folder);

        if (f != null) {
            if (f.hasChildren() ||
                    // has refreshed in 1 minute
                    !f.wantingRefresh()) {
                if (isChangeWorkingFolder) {
                    changeWorkingFolder(f);
                }
                return f.getChildren();
            }
        }

        // for failed of cache matching ,
        if (callback == null)
            return null;

        fireAsyncTaskSyncFolder((NXFolder) folder, isChangeWorkingFolder, callback);

        return null;
    }

    @Override
    public void createFolder(INxFile parentFolder, final String subFolderName) throws FolderCreateException {
        // sanity check
        // - param invalid
        if (parentFolder == null || !parentFolder.isFolder()) {
            throw new FolderCreateException("Error occurred while creating folder,invalid param of parent folder", FolderCreateException.ExceptionCode.ParamInvalid);
        }
        if (subFolderName == null || subFolderName.isEmpty()) {
            throw new FolderCreateException("Error occurred while creating folder,invalid param of subfolder", FolderCreateException.ExceptionCode.ParamInvalid);
        }
        // select the node in cache;
        parentFolder = cache.findNodeInTree(parentFolder);
        if (parentFolder == null) {
            throw new FolderCreateException("Error occurred while creating folder,node of parent folder can not find in cache tree", FolderCreateException.ExceptionCode.ParamInvalid);
        }
        // - collision of identical name with existed one
        try {
            cache.readLock.lock();
            for (INxFile f : parentFolder.getChildren()) {
                if (f != null && f.isFolder() && TextUtils.equals(f.getName(), subFolderName)) {
                    throw new FolderCreateException("Error occurred while creating folder,subfolder name collided with an existed one", FolderCreateException.ExceptionCode.NamingCollided);
                }
            }
        } finally {
            cache.readLock.unlock();
        }
        // - violation of naming convention
        Pattern pattern = Pattern.compile(FOLDER_NAME_PATTERN);
        if (!pattern.matcher(subFolderName).matches()) {
            throw new FolderCreateException("Error occurred while creating folder,naming violation", FolderCreateException.ExceptionCode.NamingViolation);
        }
        // myspace
        remoteRepo.createFolder(parentFolder, subFolderName);
        // force current tree's node to refresh
        try {
            cache.onSyncFolder((NXFolder) remoteRepo.listFiles(new NXFolder(parentFolder)));
            // tell observer this event
            observer.onChildrenChanged(cache.findNodeInTree(parentFolder));
        } catch (FileListException e) {
            log.e(e);
        }
    }

    @Override
    public void delete(final INxFile file) throws Exception {
        // del remote
        ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new Runnable() {
            @Override
            public void run() {
                remoteRepo.deleteFile(file);
            }
        });
        // delete in local cache , file must be in working folder
        NXFolder workingFolder = cache.getWorkingFolder();
        INxFile tobeDel = null;
        for (INxFile child : workingFolder.getChildren()) {
            if (TextUtils.equals(child.getLocalPath(), file.getLocalPath())) {
                tobeDel = child;
                break;
            }
        }
        if (tobeDel != null) {
            log.i("del file: " + file.toString());
            workingFolder.getChildren().remove(tobeDel);
        }
        // notify
        observer.onChildrenChanged(workingFolder);
    }

    @Override
    public INxFile backToParent() {
        NXFolder parent = cache.findParentOfWorkingFolder();
        if (parent == null) {
            return null;
        }
        cache.onChangeWorkingFolder(parent);

        //update refresing
        if (parent.wantingRefresh()) {
            refreshingFolder = parent;
            ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new WorkingFolderRefreshTask(parent));
        }
        return parent;
    }

    @Override
    public INxFile getParent(INxFile child) {
        if (child == null) {
            return null;
        }
        return cache.findNodeInTree(Helper.getParent(child));
    }

    @Override
    public void clearCache() {
        // find all docs exist at disk while not marked as offline&favorite
        final ArrayList<File> files = new ArrayList<>();

        cache.enumerateCacheTreeSafe(new Utils.OnEnumerate() {
            @Override
            public void onFileFound(INxFile file) {
                if (!file.isFolder() && !file.isMarkedAsOffline()) {
                    File absPath = new File(cache.getRootDir(), file.getLocalPath());
                    if (absPath.exists()) {
                        files.add(absPath);
                        ((NxFileBase) file).setCached(false);
                    }

                }
            }
        });
        // job
        if (files.isEmpty()) {
            return;
        }
        // task
        new DeleteFileTask(cache.getRootDir(), files).run();
    }

    @Override
    public long calCacheSize() {
        return calSize(false);
    }

    @Override
    public void getRepoInfo(final IRepoInfoCallback callback) {
        class AT extends AsyncTask<Void, Void, Boolean> {
            LocalRepoBase base;
            RepoInfo info = new RepoInfo();
            String errorMsg = "unknown";

            public AT(LocalRepoBase base) {
                this.base = base;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                // calc local
                File root = base.cache.getRootDir();
                //  total local size
                info.localTotalSize = root.getUsableSpace();
                //  cached size
                info.localCachedSize = base.calSize(false);
                //  offline size
                info.localOfflineSize = base.calSize(true);
                // calc remote
                return base.remoteRepo.getInfo(info);
            }

            @Override
            protected void onPostExecute(Boolean status) {
                if (status) {
                    callback.result(true, info, "OK");
                } else {
                    callback.result(false, info, errorMsg);

                }
            }
        }
        new AT(this).executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }


    @Override
    public INxFile folderTreeClone() {
        return cache.treeClone();
    }


    @Override
    public void notifyToSaveItself() {
        cache.serializeCacheTree();
    }

    /**
     * call this method meanings user is now working at this folder,
     * background refresh task should take more attention on this folder
     *
     * @param folder
     */
    private void changeWorkingFolder(NXFolder folder) {
        cache.onChangeWorkingFolder(folder);
        //update
        refreshingFolder = folder;
        ExecutorPools.SelectSmartly(NETWORK_TASK).execute(new WorkingFolderRefreshTask(folder));
    }

    private void changeWorkingFolderWithoutRefersh(NXFolder folder) {
        cache.onChangeWorkingFolder(folder);
        //update
        refreshingFolder = folder;
    }

    private long calSize(final boolean bCountOffline) {
        long size = 0;
        // find all docs exist at disk while not marked as offline&favorite
        final ArrayList<File> files = new ArrayList<>();
        cache.enumerateCacheTreeSafe(new Utils.OnEnumerate() {
            @Override
            public void onFileFound(INxFile file) {
                if (file.isFolder()) {
                    return;
                }
                if (bCountOffline) {
                    if (file.isMarkedAsOffline()) {
                        File absPath = new File(cache.getRootDir(), file.getLocalPath());
                        if (absPath.exists()) {
                            files.add(absPath);
                        }
                    }
                } else {
                    if (!file.isMarkedAsOffline()) {
                        File absPath = new File(cache.getRootDir(), file.getLocalPath());
                        if (absPath.exists()) {
                            files.add(absPath);
                        }
                    }
                }

            }
        });
        // job
        for (File f : files) {
            size += f.length();
        }
        return size;
    }

    private void syncFile(NXDocument document, IRemoteRepo.IDownLoadCallback callback) throws Exception {
        // sanity check
        if (document == null) {
            throw new NullPointerException(ErrorCode.E_RT_PARAM_DOC_INVALID);
        }
        if (callback == null) {
            throw new NullPointerException(ErrorCode.E_RT_PARAM_CALLBACK_INVALID);
        }
        remoteRepo.downloadFile(document,
                Helper.nxPath2AbsPath(cache.getRootDir(), document.getLocalPath()),
                callback); // must set a legal path
    }

    /**
     * - markSign
     * - notify cache
     */
    private void fireAsyncTaskSyncFolder(@NonNull NXFolder folder, boolean isChangeWorkingFolder, @NonNull IRemoteRepo.IListFilesCallback callback) {
        class AT extends AsyncTask<Void, Void, NXFolder> {
            NXFolder folder;
            LocalRepoBase base;
            IRemoteRepo.IListFilesCallback callback;
            boolean isChangeWorkingFolder;

            public AT(NXFolder folder, LocalRepoBase base, IRemoteRepo.IListFilesCallback callback, boolean isChangeWorkingFolder) {
                this.folder = folder;
                this.base = base;
                this.callback = callback;
                this.isChangeWorkingFolder = isChangeWorkingFolder;
            }

            @Override
            protected NXFolder doInBackground(Void... params) {
                try {
                    if (!folder.wantingRefresh()) {
                        log.i("in internals return directly" + folder.getDisplayPath());
                        return folder;
                    }
                    return (NXFolder) base.remoteRepo.listFiles(new NXFolder(folder));
                } catch (FileListException e) {
                    log.e(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(NXFolder aFolder) {
                if (aFolder != null) {
                    if (isChangeWorkingFolder) {
                        base.changeWorkingFolderWithoutRefersh(aFolder);
                    }
                    // tell cache about the new updating folder
                    cache.onSyncFolder(aFolder);
                    /**
                     * bug-prone, return updated folder to UI , must make sure the node is a reference to CachedTree
                     */
                    INxFile rtNxFile = cache.findNodeInTree(aFolder); // since iNxFile has updated in cache, it must ok
                    if (rtNxFile == null) {
                        log.v("on fireAsyncTaskSyncFolder, can not find this file in cached-tree" + aFolder.getLocalPath());
                        callback.onFinishedList(false, null, SkyDRMApp.getInstance().getString(R.string.fs_hint_msg_failed_listing));
                    } else {
                        callback.onFinishedList(true, rtNxFile, "ok");
                    }
                } else {
                    callback.onFinishedList(false, null, SkyDRMApp.getInstance().getString(R.string.fs_hint_msg_failed_listing));

                }
            }
        }
        new AT(folder, this, callback, isChangeWorkingFolder).executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }

    public enum ItemFeatureOperate {
        SET_FAVORITE,
        SET_OFFLINE,
        UNSET_FAVORITE,
        UNSET_OFFLINE
    }

    class DeleteFileTask implements Runnable {
        File mountPoint;
        List<File> files;

        public DeleteFileTask(File mountPoint, List<File> files) {
            this.mountPoint = mountPoint;
            this.files = files;
        }

        @Override
        public void run() {
            try {
                for (File f : files) {
                    if (!f.delete()) {
                        log.e("failed to delete file:" + f.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                log.e(e);
            }
        }
    }

    class WorkingFolderRefreshTask implements Runnable {
        String targetPath;
        String refreshPath;
        private NXFolder currentTarget;   // currentTarget may be not the latest folder, it that ,ignore this task
        private int refershInternalSeconds = 30;

        public WorkingFolderRefreshTask(@NonNull NXFolder targetFolder) {
            this.currentTarget = targetFolder;
            targetPath = currentTarget.getLocalPath();
            refreshPath = refreshingFolder.getLocalPath();
        }

        private boolean filterOut() {
            // policy check
            if (!isActive) {
                log.e("inactive repo, ignore");
                return true;
            }
            if (!currentTarget.wantingRefresh(refershInternalSeconds)) {
                log.i("--less than " + refershInternalSeconds + "s, ignore: " + targetPath);
                return true;
            }
            // policy filter out first, make sure this task only care about refresingFolder
            if (!TextUtils.equals(targetPath, refreshPath)) {
                // target is not latest, ignore this task
                log.i("--not the latest path,ignored and return");
                return true;
            }
            return false;
        }

        @Override
        public void run() {
            log.i("WorkingFolderRefreshTask");
            log.i("--targetFolder: " + targetPath);
            log.i("--userWorkingFolder: " + refreshPath);

            try {
                if (filterOut()) {
                    return;
                }
                // Task
                log.i("--do refreshing: " + targetPath);
                NXFolder newFolder = (NXFolder) remoteRepo.listFiles(new NXFolder(currentTarget));

                if (newFolder == null) {
                    return;
                }
                // result
                handleResult(newFolder);

            } catch (Exception e) {
                log.e(e);
            }
        }

        private void handleResult(@NonNull NXFolder folder) {
            // update cache
            cache.onSyncFolder(folder);
            // since the remoteRepos's listFiles is a time consuming task,
            // the refreshing folder may be changed, check it here, if not changed
            // tell observes
            if (isObserverResultMatchWorkingFolder(folder)) {
                // notify
                log.i("--notify observer from workingFolder's refreshingTask");
                // bug-prone {@param folder} and it children may not attach a boundservice
                //  so, use the same node in cache instead
                observer.onChildrenChanged(cache.findNodeInTree(folder));
                //
                // policy:
                //      to refresh folder's subfolder
                //
                cache.enumerateImmediateChildrenInCacheTreeBy(folder, new Utils.OnEnumerate() {
                    @Override
                    public void onFileFound(INxFile file) {
                        if (file.isFolder()) {
                            log.i("WorkingFolderRefreshTask-->continue:" + file.getDisplayPath());
                            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(new PrefetchTask((NXFolder) file));
                        }
                    }
                });
            } else {
                log.i("--changed of refresh path after return of remote api ");
            }
        }
    }

    /**
     * task of retrieving all folder info under {@param currentTarget }
     */
    class PrefetchTask implements Runnable {
        NXFolder targetFolder;

        public PrefetchTask(NXFolder targetFolder) {
            this.targetFolder = targetFolder;
        }

        @Override
        public void run() {
            // sanity check
            if (!isActive) {
                log.e("inactive ,ignore");
                return;
            }
            if (targetFolder == null) {
                return;
            }
            if (!targetFolder.isFolder()) {
                return;
            }
            //
            // policy check
            //      for root , force refresh
            //      for sub,    check wantingRefresh
            //
            if (!targetFolder.getLocalPath().equals("/") &&
                    !targetFolder.wantingRefresh()) {
                log.i("ignore:" + targetFolder.toString());
                return;
            }
            try {
                NXFolder newFolder;
                try {
                    newFolder = (NXFolder) remoteRepo.listFiles(new NXFolder(targetFolder));
                } catch (Exception ignored) {
                    newFolder = null;
                }
                if (newFolder == null) {
                    return;
                }
                cache.onSyncFolder(newFolder); // notify local cache to update
                // prefetch subFolder
                cache.enumerateImmediateChildrenInCacheTreeBy(newFolder, new Utils.OnEnumerate() {
                    @Override
                    public void onFileFound(INxFile file) {
                        if (file.isFolder()) {
                            log.i("PrefetchTask-->continue:" + file.getDisplayPath());
                            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(new PrefetchTask((NXFolder) file));
                        }
                    }
                });
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }
}

