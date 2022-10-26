package com.skydrm.rmc.ui.base;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.common.DeleteNxlFileTask;
import com.skydrm.rmc.ui.common.GetNxlDataTask;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.utils.sort.SortContext;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

public abstract class NxlBaseFilePresenter implements IFileContact.IPresenter {
    private static List<NxlFileItem> mTmpItems = new ArrayList<>();
    protected IDataService[] mService;
    protected IFileContact.IView mView;

    protected String mRoot = "/";
    private List<INxlFile> mCurrentLayer = new ArrayList<>();
    protected SortType mSortType = SortType.TIME_DESCEND;

    private boolean initialize;
    private boolean showEmpty;
    private boolean refresh;

    private GetNxlDataCallback mGetNxlDataCallback;
    private DeleteCallback mDeleteCallback;

    protected NxlBaseFilePresenter(IFileContact.IView view, IDataService... service) {
        this.mService = service;
        this.mView = view;
    }

    public static List<NxlFileItem> getSearchItem() {
        return mTmpItems;
    }

    public void setRoot(String root) {
        this.mRoot = root;
    }

    public void setSortType(SortType type) {
        this.mSortType = type;
    }

    @Override
    public void initialize(int type) {
        initialize = true;
        if (mView != null) {
            mView.initialize(true);
        }
        getData(mRoot, type, SkyDRMApp.getInstance().isNetworkAvailable());
    }

    @Override
    public void sort(SortType sortType) {
        this.mSortType = sortType;
        update(mCurrentLayer, true);
    }

    @Override
    public void list(int type, String pathId) {
        getData(pathId, type, false);
    }

    @Override
    public void refresh(int type, String pathId) {
        refresh = true;
        getData(pathId, type, true);
    }

    @Override
    public void delete(INxlFile f, int pos) {
        if (mDeleteCallback == null) {
            mDeleteCallback = new DeleteCallback();
        }
        mDeleteCallback.setPosition(pos);
        mDeleteCallback.setFile(f);
        DeleteNxlFileTask task = new DeleteNxlFileTask(f, mDeleteCallback);
        task.run();
    }

    @Override
    public void onReleaseResource() {
        if (mView != null) {
            mView = null;
        }
        if (mGetNxlDataCallback != null) {
            mGetNxlDataCallback = null;
        }
        if (mDeleteCallback != null) {
            mDeleteCallback = null;
        }
    }

    private void getData(boolean sync, int type) {
        getData("/", type, sync);
    }

    private void getData(String pathId, int type, boolean sync) {
        getData(pathId, type, sync, false);
    }

    private void getData(String pathId, int type, boolean sync, boolean recursively) {
        if (mGetNxlDataCallback == null) {
            mGetNxlDataCallback = new GetNxlDataCallback();
        }
        GetNxlDataTask task = new GetNxlDataTask(mService, pathId, sync, type, mGetNxlDataCallback);
        task.setRecursively(recursively);
        task.run();
    }

    private void update(List<INxlFile> current, boolean sort) {
        mTmpItems.clear();
        if (!sort) {
            mCurrentLayer.clear();
        }
        if (current == null || current.size() == 0) {
            if (mView != null) {
                mView.setEmptyView(true);
            }
            showEmpty = true;
            return;
        }
        if (!sort) {
            mCurrentLayer.addAll(current);
        }
        if (showEmpty) {
            if (mView != null) {
                mView.setEmptyView(false);
            }
            showEmpty = false;
        }
        List<NxlFileItem> data = SortContext.sortNxlItem(current, mSortType);
        mTmpItems.addAll(data);
        if (mView != null) {
            mView.update(data);
        }
    }

    private class GetNxlDataCallback implements GetNxlDataTask.ITaskCallback<GetNxlDataTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(GetNxlDataTask.Result results) {
            if (initialize) {
                if (mView != null) {
                    mView.initialize(false);
                }
                initialize = false;
            }
            if (refresh) {
                if (mView != null) {
                    mView.setLoadingIndicator(false);
                }
                refresh = false;
            }
            update(results.data, false);
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (initialize) {
                if (mView != null) {
                    mView.initialize(false);
                }
                initialize = false;
            }
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

    private class DeleteCallback implements DeleteNxlFileTask.ITaskCallback<DeleteNxlFileTask.Result, Exception> {
        private INxlFile mFile;
        private int mPos = -1;

        public void setPosition(int mPos) {
            this.mPos = mPos;
        }

        public void setFile(INxlFile f) {
            this.mFile = f;
        }

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(DeleteNxlFileTask.Result results) {
            if (mView != null) {
                mView.notifyItemDelete(mPos);
            }
            if (mFile != null) {
                mCurrentLayer.remove(mFile);
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mView != null) {
                mView.showErrorView(e);
            }
        }

    }

}
