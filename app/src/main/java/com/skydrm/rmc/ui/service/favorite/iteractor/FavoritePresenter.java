package com.skydrm.rmc.ui.service.favorite.iteractor;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NxFileBase;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.common.DeleteNxlFileTask;
import com.skydrm.rmc.ui.common.GetNxlDataTask;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.service.favorite.model.IFavoriteFile;
import com.skydrm.rmc.ui.service.favorite.model.bean.FavoriteItem;
import com.skydrm.rmc.utils.sort.SortContext;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

public class FavoritePresenter implements IFavoriteContact.IPresenter {
    private IDataService[] mService;
    private IFavoriteContact.IView mView;
    private boolean initialize;
    private boolean refresh;
    private boolean empty;

    private static List<FavoriteItem> mSortedItems = new ArrayList<>();
    private List<IFavoriteFile> mTmpFiles = new ArrayList<>();
    private SortType mSortType = SortType.TIME_DESCEND;
    private GetFileCallback mGetFileCallback;
    private DeleteCallback mDeleteCallback;

    public FavoritePresenter(IFavoriteContact.IView v) {
        this.mView = v;
        this.mService = new IDataService[]{(IDataService) RepoFactory.getRepo(RepoType.TYPE_MYVAULT)};
        this.mGetFileCallback = new GetFileCallback();

    }

    public static List<FavoriteItem> getSearchItem() {
        return mSortedItems;
    }

    @Override
    public void initialize(int type) {
        initialize = true;
        getData(false);
    }

    @Override
    public void refresh(int type, String pathId) {
        refresh = true;
        getData(true);
    }

    @Override
    public void sort(SortType sortType) {
        mSortType = sortType;
        update(mTmpFiles, true);
    }

    @Override
    public void list(int type, String pathId) {
        getData(false);
    }

    @Override
    public void delete(INxlFile f, int pos) {
        if (mDeleteCallback == null) {
            mDeleteCallback = new DeleteCallback();
        }
        mDeleteCallback.setPosition(pos);
        DeleteNxlFileTask t = new DeleteNxlFileTask(f, mDeleteCallback);
        t.run();
    }

    @Override
    public void onReleaseResource() {
        if (mView != null) {
            mView = null;
        }
        if (mGetFileCallback != null) {
            mGetFileCallback = null;
        }
        if (mDeleteCallback != null) {
            mDeleteCallback = null;
        }
    }

    private void getData(boolean sync) {
        GetNxlDataTask t = new GetNxlDataTask(mService,
                "/", sync,
                NxlFileType.FAVORITE.getValue(),
                mGetFileCallback);
        t.run();
    }

    class GetFileCallback implements GetNxlDataTask.ITaskCallback<GetNxlDataTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            if (initialize) {
                if (mView != null) {
                    mView.initialize(true);
                }
            }
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
            update(getAll(results.data), false);
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

    private void update(List<IFavoriteFile> data, boolean sort) {
        mSortedItems.clear();
        if (!sort) {
            mTmpFiles.clear();
            if (data != null) {
                mTmpFiles.addAll(data);
            }
        }
        if (mTmpFiles == null || mTmpFiles.size() == 0) {
            if (mView != null) {
                mView.setEmptyView(true);
                empty = true;
                return;
            }
        }
        if (empty) {
            if (mView != null) {
                mView.setEmptyView(false);
            }
            empty = false;
        }
        List<FavoriteItem> favoriteItems = SortContext.sortFavoriteFile2(mTmpFiles, mSortType);
        mSortedItems.addAll(favoriteItems);
        if (mView != null) {
            mView.update(favoriteItems);
        }
    }

    private List<IFavoriteFile> getAll(List<INxlFile> fl) {
        List<IFavoriteFile> ret = new ArrayList<>();
        List<IFavoriteFile> vF = adapt2FavFile(fl);
        if (vF != null && vF.size() != 0) {
            ret.addAll(vF);
        }
        List<IFavoriteFile> dF = getMyDriveFavFile();
        if (dF != null && dF.size() != 0) {
            ret.addAll(dF);
        }
        return ret;
    }

    private List<IFavoriteFile> adapt2FavFile(List<INxlFile> fl) {
        List<IFavoriteFile> ret = new ArrayList<>();
        if (fl == null || fl.size() == 0) {
            return ret;
        }
        for (INxlFile f : fl) {
            ret.add((MyVaultFile) f);
        }
        return ret;
    }

    private List<IFavoriteFile> getMyDriveFavFile() {
        List<IFavoriteFile> ret = new ArrayList<>();
        List<INxFile> favoriteFiles = SkyDRMApp.getInstance().getRepoSystem().getFavoriteFiles();
        if (favoriteFiles == null || favoriteFiles.size() == 0) {
            return ret;
        }
        for (INxFile f : favoriteFiles) {
            ret.add((NxFileBase) f);
        }
        return ret;
    }

    class DeleteCallback implements DeleteNxlFileTask.ITaskCallback<DeleteNxlFileTask.Result, Exception> {
        private int mPos;

        public void setPosition(int pos) {
            this.mPos = pos;
        }

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(DeleteNxlFileTask.Result results) {
            if (mView != null) {
                mView.notifyItemDelete(mPos);
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
