package com.skydrm.rmc.ui.fragment;

import android.text.TextUtils;
import android.util.Log;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.IWorkingFolderObserver;
import com.skydrm.rmc.reposystem.RunningMode;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.sort.SortContext;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

public class AllPresenter implements IAllContact.IPresenter, IWorkingFolderObserver {
    private IAllContact.IView mView;

    private boolean initialize;
    private boolean showNoRepoView;
    private boolean showEmptyView;
    private boolean showLoadingView;
    private boolean refresh;

    private SortType mSortType = SortType.NAME_ASCEND;
    private List<INxFile> mCurrentLists = new ArrayList<>();
    private static List<NXFileItem> mTmpItems = new ArrayList<>();
    private ListFileCallback mListFileCallback;

    private String mCurrentPathId = "/";
    private String mRefreshPathId = "/";

    private INxFile mCurrentFolder;

    public AllPresenter(IAllContact.IView view) {
        this.mView = view;
        SkyDRMApp.getInstance()
                .getRepoSystem()
                .attachWorkingFolderObserver(this);
    }

    public static List<NXFileItem> getSearchItems() {
        return mTmpItems;
    }

    @Override
    public void initialize(int type) {
        SkyDRMApp.getInstance()
                .changeRunningMode(RunningMode.Normal);
        checkAndFireInitialTask("/");
    }

    @Override
    public void sort(SortType sortType) {
        this.mSortType = sortType;
        if (isRepoConnected()) {
            update(mCurrentLists, true);
        } else {
            if (showNoRepoView) {
                return;
            }
            if (mView != null) {
                mView.showNoRepoView(true);
                showNoRepoView = true;
            }
        }
    }

    @Override
    public void list(int type, String pathId) {
        SkyDRMApp.getInstance()
                .changeRunningMode(RunningMode.Normal);

        if (isRepoConnected()) {
            if (mCurrentFolder != null) {
                Log.d("SyncDrive", "Enter target folder.");
                enterFolder(mCurrentFolder);
            } else {
                Log.d("SyncDrive", "start list");
                checkAndFireInitialTask(pathId);
            }
        } else {
            if (showNoRepoView) {
                return;
            }
            if (mView != null) {
                mView.showNoRepoView(true);
                showNoRepoView = true;
            }
        }

    }

    @Override
    public void refreshRepo() {
        SkyDRMApp.getInstance()
                .changeRunningMode(RunningMode.Normal);

        mCurrentFolder = null;
        mRefreshPathId = "";
        updateCategoryBarStatus(false, "");
        checkAndFireInitialTask("/");
        Log.d("SyncDrive", "Refresh repo");
    }

    @Override
    public void refresh(int type, String pathId) {
        refresh = true;
        Log.d("SyncDrive", "start refresh" + refresh);
        syncFolder();
    }

    @Override
    public void delete(INxFile f, int pos) {

    }

    @Override
    public void onReleaseResource() {
        if (mView != null) {
            mView = null;
        }
        if (mListFileCallback != null) {
            mListFileCallback = null;
        }
    }

    private void checkAndFireInitialTask(String pathId) {
        if (isRepoConnected()) {
            // start background task to retrieve all connected repo lists.
            listCurrent(pathId);
        } else {
            if (initialize) {
                if (mView != null) {
                    mView.initialize(false);
                }
                initialize = false;
            }
            if (showEmptyView) {
                if (mView != null) {
                    mView.setEmptyView(false);
                }
                showEmptyView = false;
            }
            if (mView != null) {
                Log.d("SyncDrive", "Show no repo view.");
                mView.showNoRepoView(true);
                showNoRepoView = true;
            }
        }
    }

    private boolean isRepoConnected() {
        return SkyDRMApp.getInstance().getRepoSystem().getSizeOfLivingRepo() != 0;
    }

    private void listCurrent(String pathId) {
        //list root.
        if (pathId.equals("/")) {
            listRoot();
        } else {
            // list current.
            listCurrent();
        }
    }

    private void listRoot() {
        Log.d("SyncDrive", "listRoot");
        if (mListFileCallback == null) {
            mListFileCallback = new ListFileCallback();
        }
        try {
            List<INxFile> roots = SkyDRMApp.getInstance()
                    .getRepoSystem()
                    .listRoot(mListFileCallback);
            if (roots == null || roots.isEmpty()) {
                if (initialize) {
                    if (mView != null) {
                        mView.initialize(true);
                    }
                }
                if (showNoRepoView) {
                    if (mView != null) {
                        mView.showNoRepoView(false);
                        showNoRepoView = false;
                    }
                }
                if (mView != null) {
                    showEmptyView = true;
                    mView.setEmptyView(true);
                }
            } else {
                update(roots, false);
            }
        } catch (Exception e) {
            if (initialize) {
                if (mView != null) {
                    mView.initialize(false);
                }
                initialize = false;
            }
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    private void listCurrent() {
        try {
            Log.d("SyncDrive", "listCurrent");
            List<INxFile> children = SkyDRMApp.getInstance()
                    .getRepoSystem()
                    .listFolder();

            update(children, false);
        } catch (Exception e) {
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    private NXFolder getFolder(String pathId) {
        NXFolder ret = null;
        if (pathId == null || pathId.isEmpty()) {
            return ret;
        }
        if (mCurrentLists.isEmpty()) {
            return ret;
        }
        for (INxFile f : mCurrentLists) {
            if (f.getLocalPath().equals(pathId)) {
                ret = (NXFolder) f;
                break;
            }
        }
        return ret;
    }

    @Override
    public boolean needInterceptBackPress() {
        return !mCurrentPathId.equals("/");
    }

    @Override
    public void enterFolder(INxFile f) {
        if (f == null) {
            return;
        }
        if (!f.isFolder()) {
            return;
        }
        mCurrentFolder = f;
        mCurrentPathId = f.getLocalPath();
        mRefreshPathId = f.getDisplayPath();
        Log.d("SyncDrive", "enterFolder with mRefreshPathId" + mRefreshPathId);
        if (mListFileCallback == null) {
            mListFileCallback = new ListFileCallback();
        }

        try {
            List<INxFile> files = SkyDRMApp.getInstance()
                    .getRepoSystem()
                    .enterFolder(f, mListFileCallback);
            if (files == null || files.isEmpty()) {
                if (mView != null) {
                    mView.setEmptyView(true);
                    showEmptyView = true;
                }
            } else {
                update(files, false);
            }
            updateCategoryBarStatus(!mRefreshPathId.equals("/"), mRefreshPathId);
        } catch (Exception e) {
            if (showNoRepoView) {
                if (mView != null) {
                    mView.showNoRepoView(false);
                }
                showNoRepoView = false;
            }
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    @Override
    public void back() {
        INxFile parent = SkyDRMApp.getInstance().getRepoSystem().backToParent();
        if (parent == null) {
            updateCategoryBarStatus(false, "");
            listRoot();
            return;
        }
        mCurrentFolder = parent;
        mCurrentPathId = parent.getLocalPath();
        mRefreshPathId = parent.getDisplayPath();

        updateCategoryBarStatus(!mCurrentPathId.equals("/"), parent.getDisplayPath());
        update(parent.getChildren(), false);
    }

    private void update(List<INxFile> files, boolean sortOnly) {
        mTmpItems.clear();
        if (!sortOnly) {
            mCurrentLists.clear();
        }
        if (files == null || files.isEmpty()) {
            Log.d("SyncDrive", "empty result " + refresh);
            if (showEmptyView) {
                return;
            }
            if (mView != null) {
                mView.setEmptyView(true);
                showEmptyView = true;
            }
            return;
        }
        Log.d("SyncDrive", "update: " + files.size() + "," + refresh);
        if (initialize) {
            if (mView != null) {
                mView.initialize(false);
            }
            initialize = false;
        }
        if (showEmptyView) {
            if (mView != null) {
                mView.setEmptyView(false);
            }
            showEmptyView = false;
        }
        if (showNoRepoView) {
            if (mView != null) {
                mView.showNoRepoView(false);
            }
            showNoRepoView = false;
        }
        if (showLoadingView) {
            if (mView != null) {
                mView.setLoadingIndicator(false);
            }
            showLoadingView = false;
        }
        if (refresh) {
            if (mView != null) {
                mView.setLoadingIndicator(false);
            }
            refresh = false;
        }
        if (!sortOnly) {
            mCurrentLists.addAll(files);
        }
        List<NXFileItem> sortedItems = SortContext.sortRepoFile(files, mSortType);
        mTmpItems.addAll(sortedItems);

        if (mView != null) {
            mView.update(sortedItems);
        }
    }

    private void updateCategoryBarStatus(boolean active, String pathId) {
        if (mView != null) {
            mView.updateCategoryBarStatus(active, pathId);
        }
    }

    private void syncFolder() {
        if (mListFileCallback == null) {
            mListFileCallback = new ListFileCallback();
        }
        try {
            SkyDRMApp.getInstance()
                    .getRepoSystem()
                    .syncWorkingFolder(mListFileCallback);
        } catch (Exception e) {
            if (refresh) {
                if (mView != null) {
                    mView.setLoadingIndicator(false);
                }
                refresh = false;
            }
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    @Override
    public void onChildrenChanged(INxFile workingFolder) {
        Log.d("SyncDrive", "onChildrenChanged");
        if (!SkyDRMApp.getInstance().getRepoSystem().isInSyntheticRoot()) {
            CommonUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listCurrent();
                }
            });
        }
    }

    class ListFileCallback implements IRemoteRepo.IListFilesCallback {

        @Override
        public void onFinishedList(boolean taskStatus, INxFile f, String errorMsg) {
            // means task invoke success.
            if (taskStatus) {
                if (f == null) {
                    if (mView != null) {
                        mView.showErrorView(new Exception(errorMsg));
                    }
                    return;
                }
                if (showNoRepoView) {
                    Log.d("SyncDrive", "Already show no repo view.");
                    return;
                }
                if (checkRefreshFolder(f.getDisplayPath())) {
                    return;
                }
                List<INxFile> mergedFileLists = getMergedFileLists(f);
                if (mergedFileLists.equals(mCurrentLists)) {
                    if (refresh) {
                        if (mView != null) {
                            mView.setLoadingIndicator(false);
                        }
                        refresh = false;
                    }
                    Log.d("SyncDrive", "No new items found.");
                    return;
                }
                update(mergedFileLists, false);
            } else {
                if (initialize) {
                    if (mView != null) {
                        mView.initialize(false);
                    }
                    initialize = false;
                }
                if (showNoRepoView) {
                    if (mView != null) {
                        mView.showNoRepoView(false);
                    }
                    showNoRepoView = false;
                }
                if (showLoadingView) {
                    if (mView != null) {
                        mView.setLoadingIndicator(false);
                    }
                    showLoadingView = false;
                }
                if (refresh) {
                    if (mView != null) {
                        mView.setLoadingIndicator(false);
                    }
                    refresh = false;
                }
                // handle error case.
                if (mView != null) {
                    mView.showErrorView(new Exception(TextUtils.isEmpty(errorMsg) ?
                            "Unknown error" : errorMsg));
                }
            }
        }

        private boolean checkRefreshFolder(String currentFolderPathId) {
            Log.d("SyncDrive", "[The current folder pathId is: " + currentFolderPathId + ",and the refresh pathId is: " + mRefreshPathId + "]");
            if (currentFolderPathId == null || currentFolderPathId.isEmpty()) {
                return false;
            }
            //if had left refresh folder the return data would be useless
            //fix bug 46404 on the status that mRefreshPath mismatch with whichBackgroundPathReturned ,
            //in this case repo system keep the file lists mismatch with the ui display either.
            //just enter folder according the enteredFolder recorded already.
            if (refresh && !currentFolderPathId.equals(mRefreshPathId)) {
                try {
                    if (mCurrentFolder == null) {
                        return false;
                    }
                    SkyDRMApp.getInstance()
                            .getRepoSystem()
                            .enterFolder(mCurrentFolder, this);

                    listCurrent();
                } catch (Exception e) {
                    if (mView != null) {
                        mView.showErrorView(e);
                    }
                }
                return true;
            }
            return false;
        }
    }

    private List<INxFile> getMergedFileLists(INxFile root) {
        List<INxFile> locals = new ArrayList<>(mCurrentLists);
        if (root == null) {
            return locals;
        }
        List<INxFile> remotes = root.getChildren();
        if (remotes == null || remotes.size() == 0) {
            locals.clear();
            return locals;
        }

        if (locals.size() == remotes.size()
                && locals.containsAll(remotes)) {
            return locals;
        }

        List<INxFile> toBeRemoved = new ArrayList<>();
        for (INxFile f : locals) {
            if (!remotes.contains(f)) {
                toBeRemoved.add(f);
            }
        }
        locals.removeAll(toBeRemoved);

        List<INxFile> toBeInserted = new ArrayList<>();
        for (INxFile f : remotes) {
            if (!locals.contains(f)) {
                toBeInserted.add(f);
            }
        }
        locals.addAll(toBeInserted);
        return locals;
    }

}
