package com.skydrm.rmc.ui.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.DividerItemDecoration;
import com.skydrm.rmc.ui.common.NxlAdapter;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.widget.NxlSwipeRefreshLayout;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuRecyclerView;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;

import java.util.List;

import butterknife.BindView;

public abstract class NxlBaseFragment extends BaseFragment implements IFileContact.IView {
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.bt_cancel)
    protected TextView mTvCancel;
    @BindView(R.id.rl_path_site)
    protected RelativeLayout mRlPathSite;
    @BindView(R.id.tv_back)
    protected TextView mTvBack;
    @BindView(R.id.tv_file_path)
    protected TextView mTvPathDisplay;
    @BindView(R.id.swipeToLoadLayout)
    protected NxlSwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.recyclerView)
    SwipeMenuRecyclerView mRecyclerView;

    protected LinearLayoutManager mLayoutManager;
    protected IFileContact.IPresenter mPresenter;
    protected NxlAdapter mAdapter;

    public abstract void showSortMenu();

    protected abstract boolean showToolbar();

    protected abstract void onToolbarNavigationClick();

    protected abstract boolean resolveBundle(Bundle arguments);

    protected abstract IFileContact.IPresenter createPresenter();

    protected abstract int getFileType();

    protected abstract String getRootPathId();

    public boolean needInterceptBackPress() {
        if (mAdapter == null) {
            return false;
        }
        return !mAdapter.getPathId().equals(getRootPathId());
    }

    public void interceptBackPress() {
        handleFolderBack();
    }

    public String getCurrentPathId() {
        if (mAdapter != null) {
            return mAdapter.getPathId();
        }
        return "/";
    }

    public void listCurrent() {
        if (mPresenter != null) {
            mPresenter.list(getFileType(), mAdapter == null ? "/" : mAdapter.getPathId());
        }
    }

    public void refreshCurrent() {
        if (mPresenter != null) {
            mPresenter.refresh(getFileType(), mAdapter == null ? "/" : mAdapter.getPathId());
        }
    }

    protected void onFileItemClick(INxlFile f, int pos) {
        NxlItemHelper.viewFile(_activity, f);
    }

    protected void onFolderClick(INxlFile f, int pos) {
        mTvPathDisplay.setText(f.getPathDisplay());
        displayPathSite(true);
        listCurrent();
    }

    protected void handleFolderBack() {
        String parentPathId = FileUtils.getParent(mAdapter.getPathId());
        String parentPathDisplay = FileUtils.getParent(mAdapter.getPathDisplay());

        mAdapter.setPathId(parentPathId);
        mAdapter.setPathDisplay(parentPathDisplay);

        mTvPathDisplay.setText(parentPathDisplay);

        displayPathSite(!parentPathId.equals(getRootPathId()));
        listCurrent();
    }

    protected void displayPathSite(boolean visible) {
        if (visible) {
            if (mRlPathSite.getVisibility() != View.VISIBLE) {
                mRlPathSite.setVisibility(View.VISIBLE);
            }
        } else {
            if (mRlPathSite.getVisibility() != View.GONE) {
                mRlPathSite.setVisibility(View.GONE);
            }
        }
    }

    protected void markAsOffline(INxlFile f, final int pos) {
        f.markAsOffline(new IOfflineCallback() {
            @Override
            public void onStarted() {
                mAdapter.setOperationStatus(pos, INxlFile.PROCESS);
            }

            @Override
            public void onProgress() {

            }

            @Override
            public void onMarkDone() {
                mAdapter.setOfflineStatus(pos, true);
            }

            @Override
            public void onMarkFailed(OfflineException e) {
                ExceptionHandler.handleException(_activity, e);
                mAdapter.setOperationStatus(pos, INxlFile.MARK_ERROR);
            }
        });
    }

    protected void unMarkAsOffline(INxlFile f, int pos) {
        f.unMarkAsOffline();
        mAdapter.setOfflineStatus(pos, false);
    }

    protected void markAsFavorite(INxlFile f, int pos) {
        f.markAsFavorite();
        mAdapter.setFavoriteStatus(pos, true);
    }

    protected void unMarkAsFavorite(INxlFile f, int pos) {
        f.unMarkAsFavorite();
        mAdapter.setFavoriteStatus(pos, false);
    }

    protected void deleteFile(INxlFile f, final int pos) {
        NxlItemHelper.showDeleteDialog(_activity, f,
                new NxlItemHelper.OnDeleteButtonClickListener() {
                    @Override
                    public void onClick(INxlFile f) {
                        mPresenter.delete(f, pos);
                    }
                });
    }

    @Override
    protected void onUserFirstVisible() {
        mPresenter.initialize(getFileType());
    }

    @Override
    protected void onUserVisible() {
        mH.postDelayed(new Runnable() {
            @Override
            public void run() {
                listCurrent();
            }
        }, 300);
    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        if (!resolveBundle(getArguments())) {
            finishParent();
        }
        mPresenter = createPresenter();
        if (mPresenter == null) {
            finishParent();
        }
        initToolbar();
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.main_green_light),
                getResources().getColor(R.color.color_blue));
        mRecyclerView.setLayoutManager(mLayoutManager = new LinearLayoutManager(_activity));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_activity, null,
                true, true));
        mRecyclerView.configAnimator();
        mRecyclerView.setAdapter(mAdapter = new NxlAdapter(_activity));
        mAdapter.setPathId(getRootPathId());
        displayPathSite(!TextUtils.equals(getCurrentPathId(), getRootPathId()));

        initEvents();
    }

    @Override
    protected View getLoadingTargetView() {
        return mRefreshLayout;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.layout_files_common;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseRsr();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRsr();
    }

    @Override
    public void initialize(boolean active) {
        if (active) {
            showLoading(getString(R.string.common_loading_message));
        } else {
            hideLoading();
        }
    }

    @Override
    public void update(List<NxlFileItem> data) {
        mAdapter.setData(data);
    }

    @Override
    public void setEmptyView(boolean active) {
        if (active) {
            showEmpty("");
        } else {
            hideEmpty();
        }
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        mRefreshLayout.setRefreshing(active);
    }

    @Override
    public void notifyItemDelete(int pos) {
        listCurrent();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(_activity, e);
    }

    private void initToolbar() {
        if (!showToolbar()) {
            return;
        }
        mToolbar.setVisibility(View.VISIBLE);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToolbarNavigationClick();
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
    }

    protected void initEvents() {
        mAdapter.setOnItemClickListener(new NxlAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(INxlFile f, int pos) {
                if (f.isFolder()) {
                    onFolderClick(f, pos);
                } else {
                    onFileItemClick(f, pos);
                }
            }
        });
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCurrent();
            }
        });
        if (mTvBack.getVisibility() == View.VISIBLE) {
            mTvBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleFolderBack();
                }
            });
        }
    }

    private void releaseRsr() {
        CommonUtils.releaseResource(mAdapter);
        CommonUtils.releaseResource(mPresenter);
    }

}
