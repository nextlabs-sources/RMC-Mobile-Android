package com.skydrm.rmc.ui.repository.contact;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.repository.IFileFilter;
import com.skydrm.rmc.ui.repository.IRepoFileContact;
import com.skydrm.rmc.utils.sort.SortContext;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

public class RepoFilePresenter implements IRepoFileContact.IRepoFilePresenter {
    private IRepoFileContact.IRepoFileView mView;
    private BoundService mRepoService;
    private INxFile mRoot;
    private INxFile mDestFolder;
    private IFileFilter mFilter;
    private SortType mSortType = SortType.NAME_ASCEND;
    private List<INxFile> mTmpFiles = new ArrayList<>();

    private boolean empty;
    private String mCurrentPathId = "/";

    public RepoFilePresenter(IRepoFileContact.IRepoFileView view,
                             BoundService service,
                             IFileFilter filter) {
        this.mView = view;
        this.mRepoService = service;
        this.mFilter = filter;
    }

    @Override
    public void initialize(int type) {
        if (mRepoService == null) {
            empty = true;
            if (mView != null) {
                mView.setEmptyView(true);
            }
            return;
        }

        mRoot = SkyDRMApp.getInstance()
                .getRepoSystem()
                .folderTreeClone(mRepoService);
        mDestFolder = mRoot;

        if (mRoot == null) {
            empty = true;
            if (mView != null) {
                mView.setEmptyView(true);
            }
            return;
        }
        mCurrentPathId = mRoot.getLocalPath();
        update(mRoot.getChildren(), false);
    }

    @Override
    public void sort(SortType sortType) {
        mSortType = sortType;
        update(mTmpFiles, true);
    }

    @Override
    public void list(int type, String pathId) {
        update(getChildren(pathId), false);
    }

    @Override
    public void refresh(int type, String pathId) {
        //ignore.
    }

    @Override
    public void delete(INxFile f, int pos) {
        //ignore.
    }

    @Override
    public void onReleaseResource() {
        if (mView != null) {
            mView = null;
        }
    }

    private List<INxFile> getChildren(String pathId) {
        List<INxFile> ret = new ArrayList<>();
        if (mRoot == null) {
            mCurrentPathId = "/";
            return ret;
        }

        // Regard null or empty pathId as the fake root "/"
        if (pathId == null || pathId.isEmpty()) {
            mCurrentPathId = "/";
            return mRoot.getChildren();
        }

        mCurrentPathId = pathId;
        mDestFolder = findChildNode(pathId, mRoot);
        if (mDestFolder == null) {
            return ret;
        }
        return mDestFolder.getChildren();
    }

    private INxFile findChildNode(String pathId, INxFile root) {
        if (root == null) {
            return null;
        }
        String tmp = "";
        if (pathId == null || pathId.isEmpty()) {
            tmp = "/";
        } else {
            tmp = pathId;
        }
        if (tmp.equals("/")) {
            return root;
        }
        for (INxFile f : root.getChildren()) {
            if (f == null) {
                continue;
            }
            if (f.isFolder()) {
                if (f.getLocalPath().equals(tmp)) {
                    return f;
                } else {
                    INxFile node = findChildNode(tmp, f);
                    if (node != null) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    private List<INxFile> findChildren(String pathId, INxFile root) {
        List<INxFile> ret = new ArrayList<>();
        if (root == null) {
            return ret;
        }
        String tmp = "";
        if (pathId == null || pathId.isEmpty()) {
            tmp = "/";
        } else {
            tmp = pathId;
        }
        if (tmp.equals("/")) {
            ret.addAll(root.getChildren());
            return ret;
        }
        for (INxFile f : root.getChildren()) {
            if (f == null) {
                continue;
            }
            if (!f.isFolder()) {
                continue;
            }
            if (f.getLocalPath().equals(tmp)) {
                ret.addAll(f.getChildren());
            } else {
                ret.addAll(findChildren(pathId, f));
            }
        }
        return ret;
    }

    private void update(List<INxFile> files, boolean isSort) {
        if (files == null || files.size() == 0) {
            empty = true;
            if (mView != null) {
                mView.setEmptyView(true);
            }
            return;
        }

        if (!isSort) {
            mTmpFiles.clear();
            mTmpFiles.addAll(files);
        }

        if (mView != null) {
            if (empty) {
                mView.setEmptyView(false);
            }
            mView.update(sortWithFilter(files));
        }
    }

    private List<NXFileItem> sortWithFilter(List<INxFile> files) {
        if (files == null || files.size() == 0) {
            return null;
        }
        if (mFilter == null) {
            return sort(files);
        }
        List<INxFile> filtered = new ArrayList<>();
        for (INxFile f : files) {
            if (f == null) {
                continue;
            }
            if (mFilter.accept(f.getName())) {
                filtered.add(f);
            }
        }
        return sort(filtered);
    }

    private List<NXFileItem> sort(List<INxFile> files) {
        return SortContext.sortRepoFile(files, mSortType);
    }

    @Override
    public boolean isRoot() {
        return mCurrentPathId.equals("/");
    }

    @Override
    public INxFile getDestFolder() {
        return mDestFolder;
    }

    @Override
    public void navigateBack(String parentPathId) {
        list(-1, parentPathId);
    }
}
