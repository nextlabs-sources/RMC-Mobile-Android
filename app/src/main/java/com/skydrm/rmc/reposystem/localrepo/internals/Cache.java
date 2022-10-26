package com.skydrm.rmc.reposystem.localrepo.internals;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.io.Files;
import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.Utils;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.reposystem.types.NxFileBase;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Cache {
    static private final String CACHE = "directory.CACHE";
    static private final String ROOT = "ROOT";
    static private DevLog log = new DevLog(Cache.class.getSimpleName());
    public Lock readLock;
    public Lock writeLock;
    private volatile NXFolder cachedTree = null; // used by multi-threads
    private NXFolder workingFolder; // a subTree from mCacheTree
    private File cachedSerializedFile;    // mMountBase+CACHE
    private File localFilesRoot;     // mMountBase+ROOT
    private BoundService service; // bound service about LocalRepos' remote info
    private ReentrantReadWriteLock reentrantReadWriteLock;
//    private long serializingPoint = System.currentTimeMillis();

    static private String util_pathJoint(String p1, String p2) {
        String ret = p1;
        if (ret.endsWith("/")) {
            ret += p2;
        } else {
            ret += "/" + p2;
        }
        return ret;
    }

    /**
     * - create a file :  directory.CACHE
     * - create a folder: ROOT
     * - recover the cacheTree
     */
    public void init(File newMountBase, BoundService service) throws Exception {
        this.service = service;
        //  CacheFile
        cachedSerializedFile = new File(newMountBase, CACHE);
        if (!cachedSerializedFile.exists() && !cachedSerializedFile.createNewFile()) {
            log.e("error: can not create cache file");
            throw new RuntimeException(SkyDRMApp.getInstance().getString(R.string.err_excpt_cache_file_create));
        }
        //  LocalFilesRoot
        localFilesRoot = new File(newMountBase, ROOT);
        if (!Helper.makeSureDirExist(localFilesRoot)) {
            log.e("error: can not crate local file root dir");
            throw new RuntimeException(SkyDRMApp.getInstance().getString(R.string.err_excpt_cache_dir_create));
        }

        reentrantReadWriteLock = new ReentrantReadWriteLock();
        readLock = reentrantReadWriteLock.readLock();
        writeLock = reentrantReadWriteLock.writeLock();

        cachedTree = deserializeCacheTree();

        /*
            for first installed , mCacheTree is null ,so create a new one,
            sometimes failed by calling the method deserializeCacheTree
        */
        if (cachedTree == null) {
            cachedTree = new NXFolder("/", "/", "root", 0);
            // bug- if new created ,set refreshed is 0 ,to force refresh
            cachedTree.setLastRefreshed(0);
            cachedTree.setBoundService(service);
        }
        // by default , working folder is the root
        workingFolder = cachedTree;
    }

    public
    @NonNull
    NXFolder getCacheTree() {
        return getCacheTree(false);
    }

    public File getRootDir() {
        return localFilesRoot;
    }

    public NXFolder getWorkingFolder() {
        // sanity check
        if (cachedTree == null) {
            log.e("mCacheTree is null ,it's a bug");
            return null;
        }

        if (workingFolder == null) {
            workingFolder = cachedTree;
            return workingFolder;
        }
        return (NXFolder) findNodeInTree(workingFolder.getLocalPath());

    }

    public
    @Nullable
    File getDocument(NXDocument doc) {
        INxFile f = tryToGetFromCache(doc);
        if (f != null) {
            String absPath = Helper.nxPath2AbsPath(getRootDir(), f.getLocalPath());
            File rt = new File(absPath);

            if (rt.exists()) {
                if (!Helper.isGoogleFile(f) && rt.length() == f.getSize()) {
                    doc.setCached(true);
                    return rt;
                } else if (Helper.isGoogleFile(f) && rt.length() > 0) {
                    doc.setCached(true);
                    return rt;
                }
            } else {
                Helper.deleteFile(rt);
                doc.setCached(false);
                return null;
            }

        }
        return null;
    }

    public
    @Nullable
    INxFile tryToGetFromCache(INxFile base) {
        readLock.lock();
        INxFile f;
        try {
            f = cachedTree.findNode(base.getLocalPath());
        } finally {
            readLock.unlock();
        }
        if (f == null) {
            return null;
        }
        ((NxFileBase) f).setBoundService(service);
        if (f instanceof NXDocument) {

            File fileOnSdCard = new File(Helper.nxPath2AbsPath(getRootDir(), f.getLocalPath()));

            if (fileOnSdCard.exists()) {
                if (!Helper.isGoogleFile(f) && fileOnSdCard.length() == f.getSize()) {
                    ((NxFileBase) f).setCached(true);
                    return f;
                } else if(Helper.isGoogleFile(f) && fileOnSdCard.length() > 0) {
                    ((NxFileBase) f).setCached(true);
                    return f;
                }
            } else {
                ((NxFileBase) f).setCached(false);
                Helper.deleteFile(fileOnSdCard);
                return null;
            }


        }
        if (f instanceof NXFolder) {
            Utils.attachService(f, service, false);
            return f;
        }
        return null;
    }

    public void onChangeWorkingFolder(INxFile folder) {
        // sanity check
        if (folder == null || !folder.isFolder()) {
            return;
        }
        //make sure the {@param folder} is one member of the Root
        INxFile subNode = findNodeInTree(folder.getLocalPath());
        if (subNode == null) {
            log.e("change current working folder failed , folder dose not exist in Root ");
            return;
        }
        workingFolder = (NXFolder) subNode;
        Utils.attachService(workingFolder, service, false);
    }


    public void addDocument(@NonNull NXFolder parentFolder, @NonNull NXDocument newDoc, @NonNull File file){
        NXFolder targetFolder;
        if (workingFolder.isSameLocalPath(parentFolder)) {
            targetFolder = workingFolder;
        } else {
            targetFolder = (NXFolder) findNodeInTree(parentFolder);
            if (targetFolder == null || !targetFolder.isFolder()) {
                return;
            }
        }
        // add the node of newdoc into targetFolder
        log.v("addDocument:" + newDoc);
        writeLock.lock();
        try {
            targetFolder.addChild(newDoc);
        }finally {
            writeLock.unlock();
        }
        // copy file into the path specified by newdoc
        String absolutePath = Helper.nxPath2AbsPath(localFilesRoot,newDoc.getLocalPath());
        if(!TextUtils.equals(file.getAbsolutePath(),absolutePath)){
            // make sure file exist
            File dest = new File(absolutePath);
            dest.getParentFile().mkdirs();
            try {
                Files.copy(file, dest);
                newDoc.setCached(true);
            } catch (IOException e) {
                e.printStackTrace();
                newDoc.setCached(false);
            }
        }
    }

    /**
     * Be called when @{folder} from cloud been updated
     * - update folder self
     * - update folder's children
     * - serilalize into disk
     * bug-found:
     * - must update folder's lastRefresh
     */
    public void onSyncFolder(final @NonNull NXFolder folder) {
        class UpdatePolicy {
            INxFile target;

            public UpdatePolicy(NXFolder targetFolder) {
                this.target = targetFolder;
            }

            public void proceed(NXFolder toBeUpdate) {
                List<INxFile> lOld = new LinkedList<>(target.getChildren());
                List<INxFile> lNew = new LinkedList<>(toBeUpdate.getChildren());

                if (lOld.isEmpty() && lNew.isEmpty()) {
                    // Both  empty                          -- nothing to do
                    return;
                }
                if (lOld.isEmpty() && !lNew.isEmpty()) {
                    // lOld is empty while tobeU is not      -- shallowCopy lNew
                    ((NxFileBase) target).addChild(lNew);
                    Utils.attachService(target, service, false);
                    return;
                }
                if (!lOld.isEmpty() && lNew.isEmpty()) {
                    // lOld is not empty while lNew is      -- remove cur
                    for (INxFile i : lOld) {
                        removeNode(i);
                    }
                    ((NxFileBase) target).setChildren(lNew);
                    return;
                }
                // Both non-empty
                // -- significant algo begins:
                Iterator<INxFile> curIt = lOld.iterator();
                while (curIt.hasNext()) {
                    NxFileBase node = (NxFileBase) curIt.next();
                    // check if current updating node still exist at the newNode,
                    NxFileBase newNode = findThenDel(lNew, node);
                    if (newNode != null) {
                        updateNode(node, newNode);
                    } else {
                        removeNode(node);
                        curIt.remove();
                    }
                }
                // for rest files, those are additions , add them into current folder
                if (!lNew.isEmpty()) {
                    for (INxFile i : lNew) {
                        lOld.add(i);
                    }
                }

                ((NxFileBase) target).setChildren(lOld);
                Utils.attachService(target, service, false); // correct each item's bound-service
            }

            // if match , del the match node from list
            private NxFileBase findThenDel(List<INxFile> list, INxFile file) {
                Iterator<INxFile> it = list.iterator();
                while (it.hasNext()) {
                    INxFile i = it.next();
                    if (i.getLocalPath().equals(file.getLocalPath())) {
                        it.remove();
                        return (NxFileBase) i;
                    }
                }
                return null;
            }

            // old node need to update, modify some fileds
            private void updateNode(@NonNull NxFileBase aOld, @NonNull NxFileBase aNew) {
                // update
                aOld.setCloudPath(aNew.getCloudPath());
                aOld.setmCloudPathID(aNew.getCloudFileID());
                aOld.setNewCreated(false);
                // todo: osm  if for doc' content has modified by cloud what should local do!!!
                aOld.setSize(aNew.getSize());
                aOld.setLastModifiedTimeLong(aNew.getLastModifiedTimeLong());
            }

            private void removeNode(INxFile aOld) {
                // todo:for next release
            }
        }


        NXFolder targetRoot = (NXFolder) cachedTree.findNode(folder.getLocalPath());
        if (targetRoot == null) {
            log.e("in Folder Merger:\n" +
                    " folder to be updated does not exist in mCacheTree, bug!!!!" + folder.getDisplayPath());
            return;
        } else {
            log.i("merge folder" + folder.getDisplayPath());
        }
        // update targetRoot self  according to {@param folder}
        {
            targetRoot.setLastModifiedTimeLong(folder.getLastModifiedTimeLong());
            targetRoot.setmCloudPathID(folder.getCloudFileID());
            targetRoot.setCloudPath(folder.getCloudPath());
            targetRoot.setSize(folder.getSize());
            targetRoot.setNewCreated(false);
            targetRoot.updateRefreshTimeWisely();
        }


        writeLock.lock();
        try {
            // update targetRoot's children
            new UpdatePolicy(targetRoot).proceed(folder);
        } catch (Exception ignored) {
            log.e(ignored);
        } finally {
            writeLock.unlock();
        }
    }

    public NXFolder findParentOfWorkingFolder() {
        return (NXFolder) findNodeInTree(Helper.getParent(workingFolder));
    }

    public INxFile findNodeInTree(final INxFile file) {
        // sanity check
        if (file == null) return null;
        if (file.getLocalPath() == null) return null;

        INxFile tree = getCacheTree(true);
        readLock.lock();
        try {
            INxFile rt = tree.findNode(file.getLocalPath());
            if (rt == null) {
                return null;
            }
            Utils.attachService(rt, service, false);
            return rt;
        } finally {
            readLock.unlock();
        }
    }

    public INxFile findNodeInTree(final String path) {
        if (path == null) return null;
        readLock.lock();
        try {
            INxFile tree = getCacheTree(true);
            INxFile rt = tree.findNode(path);
            if (rt == null) {
                return null;
            }
            Utils.attachService(rt, service, false);
            return rt;
        } finally {
            readLock.unlock();
        }
    }


    public void enumerateCacheTreeSafe(Utils.OnEnumerate onEnumerate) {
        readLock.lock();
        try {
            Utils.EnumerateAllFiles(cachedTree, onEnumerate);
        } finally {
            readLock.unlock();
        }
    }

    public NXFolder treeClone() {
        // - algorithm: shallowCopy and clone the whole tree files
        readLock.lock();
        try {
            // root's self info shallowCopy first
            NXFolder tree = new NXFolder(cachedTree);
            // algo begin:
            Stack<NXFolder> workingStack = new Stack<>();
            Stack<NXFolder> tobeCopyStack = new Stack<>();
            workingStack.push(cachedTree);
            tobeCopyStack.push(tree);
            while (!workingStack.isEmpty()) {
                NXFolder cur = tobeCopyStack.pop();
                //handle children of in cacheTree
                for (INxFile child : workingStack.pop().getChildren()) {
                    NxFileBase node;
                    if (child.isFolder()) {
                        workingStack.push((NXFolder) child);
                        node = new NXFolder(child);
                        tobeCopyStack.push((NXFolder) node);
                    } else {
                        node = new NXDocument(child);
                    }
                    cur.addChild(node);
                }
            }
            return tree;
        } finally {
            readLock.unlock();
        }
    }

    public void enumerateImmediateChildrenInCacheTreeBy(NXFolder folder, Utils.OnEnumerate onEnumerate) {
        INxFile target = findNodeInTree(folder);
        if (target == null) {
            return;
        }
        readLock.lock();
        try {
            for (INxFile f : target.getChildren()) {
                if (f != null) {
                    onEnumerate.onFileFound(f);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    // restore CacheTree from Disk
    // all returned INxFile must be attached Service
    public NXFolder deserializeCacheTree() {
        NXFolder rtValue;
        log.i("deserializeCacheTree:" + service);
        ObjectInputStream objectInputStream = null;
        writeLock.lock();
        try {
            if (!cachedSerializedFile.exists() || cachedSerializedFile.length() < 10) {
                return null;
            }
            objectInputStream = new ObjectInputStream(new FileInputStream(cachedSerializedFile));
            rtValue = (NXFolder) objectInputStream.readObject();
            Utils.attachService(rtValue, service, true);
        } catch (Exception e) {
            log.e("Error: Cache::deserializeCacheTree" + e.toString(), e);
            rtValue = null;
        } finally {
            writeLock.unlock();
            closeSilent(objectInputStream);
        }
        return rtValue;
    }

    // write CacheTree into Disk
    public void serializeCacheTree() {
        ObjectOutputStream objectOutputStream = null;
        writeLock.lock();
        try {
            log.i("serializeCacheTree:" + service);
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(cachedSerializedFile));
            objectOutputStream.writeObject(cachedTree);
            objectOutputStream.close();
        } catch (Exception e) {
            log.e("Error: Cache::serializeCacheTree" + e.toString(), e);
        } finally {
            writeLock.unlock();
            closeSilent(objectOutputStream);
        }
    }


    private void closeSilent(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception ignore) {
            log.e(ignore);
        }
    }

    private NXFolder getCacheTree(boolean bFast) {
        readLock.lock();
        try {
            if (cachedSerializedFile == null) {
                cachedTree = new NXFolder("/", "/", "root", 0);
                workingFolder = cachedTree;    // by default , working folder is the root
            }
            if (!bFast) {
                Utils.attachService(cachedTree, service, true);
            }
            return cachedTree;
        } finally {
            readLock.unlock();
        }
    }
}
