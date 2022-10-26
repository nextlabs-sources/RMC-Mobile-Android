package com.skydrm.rmc.ui.service.favorite.view;

import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.engine.eventBusMsg.NxFileDeleteEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NxFileBase;
import com.skydrm.rmc.ui.DividerItemDecoration;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.service.favorite.iteractor.FavoritePresenter;
import com.skydrm.rmc.ui.service.favorite.iteractor.IFavoriteContact;
import com.skydrm.rmc.ui.service.favorite.model.FavoriteAdapter;
import com.skydrm.rmc.ui.service.favorite.model.IFavoriteFile;
import com.skydrm.rmc.ui.service.favorite.model.bean.FavoriteItem;
import com.skydrm.rmc.ui.service.favorite.model.eventmsg.FavoriteFileUpdateFromMyDriveEvent;
import com.skydrm.rmc.ui.service.favorite.model.eventmsg.FavoriteSortEvent;
import com.skydrm.rmc.ui.myspace.MySpaceFileCtxMenu;
import com.skydrm.rmc.ui.myspace.myvault.view.widget.MyVaultFileMenu;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.myspace.sharewithme.OnItemClickListener;
import com.skydrm.rmc.ui.widget.NxlSwipeRefreshLayout;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuRecyclerView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

public class FavoriteFragment extends BaseFragment implements IFavoriteContact.IView {
    @BindView(R.id.swipeToLoadLayout)
    NxlSwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerView)
    SwipeMenuRecyclerView mSwipeMenuRecyclerView;

    private static final int TYPE = 5;
    private IFavoriteContact.IPresenter mPresenter;
    private FavoriteAdapter mAdapter;
    private MyVaultFileMenu mVaultFileMenu;
    private MySpaceFileCtxMenu mSpaceCtxMenu;

    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }

    @Override
    protected void onUserFirstVisible() {
        mH.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.initialize(TYPE);
            }
        }, 300);
    }

    @Override
    protected void onUserVisible() {
        mH.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.list(TYPE, "/");
            }
        }, 300);
    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.main_green_light),
                getResources().getColor(R.color.color_blue));
        mSwipeMenuRecyclerView.addItemDecoration(new DividerItemDecoration(_activity, null,
                false, true));
        mSwipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(_activity));
        mSwipeMenuRecyclerView.configAnimator();

        mAdapter = new FavoriteAdapter();
        mSwipeMenuRecyclerView.setAdapter(mAdapter);
        mPresenter = new FavoritePresenter(this);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.refresh(TYPE, "/");
            }
        });
        mAdapter.setOnItemClickListener(new OnItemClickListener<IFavoriteFile>() {
            @Override
            public void onItemClick(IFavoriteFile entry, int position) {
                NxlItemHelper.viewFavoriteFile(_activity, entry);
            }

            @Override
            public void onToggleItemMenu(IFavoriteFile entry, int position) {
                showFileCtxMenu(entry, position);
            }

            @Override
            public void onSwipeButton_01Click(IFavoriteFile entry, int position, View view) {
                NxlItemHelper.manageOrShareFavoriteFile(_activity, entry, view);
            }

            @Override
            public void onSwipeButton_02Click(IFavoriteFile entry, int position, View view) {
                NxlItemHelper.protectOrViewLogFavoriteFile(_activity, entry, view);
            }
        });
        initCtxMenu();
    }

    @Override
    protected View getLoadingTargetView() {
        return mSwipeRefreshLayout;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_vault_base;
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
    public void update(List<FavoriteItem> fls) {
        mAdapter.setData(fls);
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
        mAdapter.removeItem(pos);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(_activity, e);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sort(FavoriteSortEvent event) {
        mPresenter.sort(event.getSortType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMyDriveFavStatus(FavoriteFileUpdateFromMyDriveEvent event) {
        if (mPresenter != null) {
            mPresenter.list(TYPE, "/");
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
            mPresenter.list(TYPE, "/");
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

    private void showFileCtxMenu(IFavoriteFile f, int pos) {
        if (f instanceof MyVaultFile) {
            showMyVaultCtxMenu(f, pos);
        } else {
            showMyDriveCtxMenu(f, pos);
        }
    }

    private void initCtxMenu() {
        if (mVaultFileMenu == null) {
            mVaultFileMenu = MyVaultFileMenu.newInstance();
        }
        if (mSpaceCtxMenu == null) {
            mSpaceCtxMenu = MySpaceFileCtxMenu.newInstance();
        }
    }

    private void showMyVaultCtxMenu(IFavoriteFile f, int pos) {
        FragmentManager fm = getFragmentManager();
        if (fm == null) {
            return;
        }
        mVaultFileMenu.setFile((MyVaultFile) f);
        mVaultFileMenu.setPosition(pos);
        mVaultFileMenu.show(fm, MyVaultFileMenu.class.getSimpleName());
        mVaultFileMenu.setOnItemClickListener(new MyVaultFileMenu.OnItemClickListener() {
            @Override
            public void onSetFavorite(INxlFile f, int pos, boolean favorite) {
                unMarkAsFavorite(f, pos);
            }

            @Override
            public void onSetOffline(INxlFile f, int pos, boolean offline) {
                if (offline) {
                    unMarkAsOffline(f, pos);
                } else {
                    markAsOffline(f, pos);
                }
            }

            @Override
            public void onDeleteFile(INxlFile f, int pos) {
                deleteFile(f, pos);
            }
        });
    }

    private void showMyDriveCtxMenu(IFavoriteFile f, final int pos) {
        if (f instanceof NxFileBase) {
            final INxFile file = (NxFileBase) f;
            mSpaceCtxMenu.setNxFile(file);
            mSpaceCtxMenu.show(_activity.getSupportFragmentManager(),
                    MySpaceFileCtxMenu.class.getSimpleName());
        }
    }

    private void unMarkAsFavorite(INxlFile f, int pos) {
        f.unMarkAsFavorite();
        mAdapter.removeItem(pos);
    }

    private void markAsOffline(INxlFile f, final int pos) {
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
                mAdapter.setOperationStatus(pos, INxlFile.MARK_ERROR);
            }
        });
    }

    private void unMarkAsOffline(INxlFile f, int pos) {
        f.unMarkAsOffline();
        mAdapter.setOfflineStatus(pos, false);
    }

    private void deleteFile(INxlFile f, final int pos) {
        NxlItemHelper.showDeleteDialog(_activity, f,
                new NxlItemHelper.OnDeleteButtonClickListener() {
                    @Override
                    public void onClick(INxlFile f) {
                        mPresenter.delete(f, pos);
                    }
                });
    }
}
