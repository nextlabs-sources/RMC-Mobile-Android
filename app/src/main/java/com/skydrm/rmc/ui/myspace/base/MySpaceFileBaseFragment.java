package com.skydrm.rmc.ui.myspace.base;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.engine.eventBusMsg.NxFileDeleteEvent;
import com.skydrm.rmc.engine.eventBusMsg.favorites.FavoriteStatusChangeFromMorePageEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.ui.DividerItemDecoration;
import com.skydrm.rmc.ui.adapter.NXFileRecyclerViewAdapter;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.fragment.IAllContact;
import com.skydrm.rmc.ui.myspace.MySpaceFileCtxMenu;
import com.skydrm.rmc.ui.myspace.MySpaceFileItemHelper;
import com.skydrm.rmc.ui.service.favorite.model.eventmsg.FavoriteFileUpdateFromMyDriveEvent;
import com.skydrm.rmc.ui.widget.NxlSwipeRefreshLayout;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuRecyclerView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.sort.SortType;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

public abstract class MySpaceFileBaseFragment extends BaseFragment implements IAllContact.IView {
    @BindView(R.id.swipeToLoadLayout)
    NxlSwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerView)
    SwipeMenuRecyclerView mSwipeMenuRecyclerView;
    @BindView(R.id.home_files_category_layout2)
    RelativeLayout mRlCategoryBar;
    @BindView(R.id.tv_back)
    TextView mTvBack;
    @BindView(R.id.home_files_current_category2)
    TextView mTvDisplayPath;
    protected static int TYPE_ALL = 0x100;

    protected SortType mSortType = SortType.NAME_ASCEND;
    private NXFileRecyclerViewAdapter mAdapter;
    protected IAllContact.IPresenter mPresenter;

    private MySpaceFileCtxMenu mFileCtxMenu;

    public boolean needInterceptBackPress() {
        return mPresenter != null && mPresenter.needInterceptBackPress();
    }

    public void handleBackPress() {
        if (mPresenter != null) {
            mPresenter.back();
        }
    }

    @Override
    protected void onUserFirstVisible() {
        if (mPresenter != null) {
            mPresenter.initialize(TYPE_ALL);
        }
    }

    @Override
    protected void onUserVisible() {
        // When usr is visible to current page, there is no need to list current again.
        // This is still causing a listing file error. show no bound-service detected.
//        if (mPresenter != null) {
//            mPresenter.list(TYPE_ALL, "");
//        }
    }

    @Override
    protected void onUserInvisible() {

    }

    /**
     * Subscriber for bound service detached.
     *
     * @param boundService {@link BoundService}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDetachService(BoundService boundService) {
        if (mPresenter != null) {
            mPresenter.list(TYPE_ALL, "/");
        }
    }

    /**
     * Subscriber for select repo attached and add listener when sort action comes.
     *
     * @param sortType {@link SortType}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sort(SortType sortType) {
        if (mPresenter != null) {
            mPresenter.sort(sortType);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMyDriveFavStatus(FavoriteFileUpdateFromMyDriveEvent event) {
        if (mPresenter != null) {
            mPresenter.list(TYPE_ALL, "");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMyDriveFavStatus(FavoriteStatusChangeFromMorePageEvent event) {
        if (mPresenter != null) {
            mPresenter.list(TYPE_ALL, "");
        }
    }

    /**
     * Subscriber for delete file
     *
     * @param event {@link NxFileDeleteEvent}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteFile(NxFileDeleteEvent event) {
        if (mPresenter != null) {
            mPresenter.list(TYPE_ALL, event.isSyntheticRoot() ? "/" : "");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleSearchEnterFolder(NXFolder folder) {
        if (mPresenter != null) {
            mPresenter.enterFolder(folder);
        }
    }

    @Override
    protected void initViewAndEvents() {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.main_green_light),
                getResources().getColor(R.color.color_blue));
        mSwipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(_activity));
        mSwipeMenuRecyclerView.addItemDecoration(new DividerItemDecoration(_activity, null,
                false, true));
        mSwipeMenuRecyclerView.configAnimator();
        mAdapter = new NXFileRecyclerViewAdapter(_activity);
        mSwipeMenuRecyclerView.setAdapter(mAdapter);
        initEvents();
        initFileCtxMenu();
    }

    @Override
    protected View getLoadingTargetView() {
        return mSwipeRefreshLayout;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_myspace_content;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Override
    public void initialize(boolean active) {
        if (active) {
            showLoading("");
        } else {
            hideLoading();
        }
    }

    @Override
    public void update(List<NXFileItem> files) {
        mAdapter.setData(files);
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
        mSwipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void notifyItemDelete(int pos) {

    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(_activity, e);
    }

    @Override
    public void showNoRepoView(boolean active) {
        if (active) {
            showNoRepoView("");
        } else {
            hideNoRepoView();
        }
    }

    @Override
    public void updateCategoryBarStatus(boolean active, String pathId) {
        mRlCategoryBar.setVisibility(active ? View.VISIBLE : View.GONE);
        if (active) {
            mTvDisplayPath.setText(pathId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CommonUtils.releaseResource(mPresenter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonUtils.releaseResource(mPresenter);
    }

    private void initEvents() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.refresh(TYPE_ALL, "");
                    }
                }, 200);
            }
        });
        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter != null) {
                    mPresenter.back();
                }
            }
        });
        mAdapter.setOnContentClickListener(new NXFileRecyclerViewAdapter.OnContentClickListener() {
            @Override
            public void onItemClick(INxFile f, View view, int position) {
                if (f == null) {
                    return;
                }
                if (f.isFolder()) {
                    mPresenter.enterFolder(f);
                } else {
                    if (f instanceof NXDocument) {
                        MySpaceFileItemHelper.viewFile(_activity, (NXDocument) f);
                    }
                }
            }

            @Override
            public void onDetailClick(INxFile f, View view, int position) {
                showFileCtxMenu(f, position);
            }
        });
        mAdapter.setOnRightMenuClickListener(new NXFileRecyclerViewAdapter.OnRightMenuClickListener() {
            @Override
            public void onShare(INxFile f, int adapterPosition) {
                MySpaceFileItemHelper.share(_activity, f);
            }

            @Override
            public void onProtect(INxFile f, View view, int position) {
                MySpaceFileItemHelper.protect(_activity, f);
            }

            @Override
            public void onViewActivityLog(INxFile f, int position) {
                if (f instanceof NXDocument)
                    MySpaceFileItemHelper.viewActivityLog(_activity, (NXDocument) f);
            }
        });
    }

    private void initFileCtxMenu() {
        if (mFileCtxMenu == null) {
            mFileCtxMenu = MySpaceFileCtxMenu.newInstance();
        }
    }

    private void showFileCtxMenu(INxFile file, int pos) {
        mFileCtxMenu.setNxFile(file);
        mFileCtxMenu.show(_activity.getSupportFragmentManager(),
                MySpaceFileCtxMenu.class.getSimpleName());
    }
}
