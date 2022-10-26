package com.skydrm.rmc.ui.repository.feature;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.skydrm.rmc.R;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.DividerItemDecoration;
import com.skydrm.rmc.ui.adapter.RepositorySelectAdapter;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class RepoSelectFragment extends BaseFragment {
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private RepositorySelectAdapter mAdapter;

    private OnRepoItemClickListener mOnRepoItemClickListener;

    public static RepoSelectFragment newInstance() {
        return new RepoSelectFragment();
    }

    public void setOnRepoItemClickListener(OnRepoItemClickListener listener) {
        this.mOnRepoItemClickListener = listener;
    }

    @Override
    protected void onUserFirstVisible() {

    }

    @Override
    protected void onUserVisible() {

    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
        mToolbar.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
        mAdapter = new RepositorySelectAdapter(_activity);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(_activity));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_activity, null,
                false, true));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setData(CommonUtils.getFilteredBoundServiceByAccountType());
        mAdapter.setOnItemClickListener(new RepositorySelectAdapter.OnItemClickListener() {
            @Override
            public void onLibraryItemClick() {
                checkPermissionThenLunch();
            }

            @Override
            public void onNormalItemClick(BoundService s, int pos) {
                lunch2RepoFileFragment(s);
            }
        });
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_repository_select;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonUtils.releaseResource(mAdapter);
        if (mOnRepoItemClickListener != null) {
            mOnRepoItemClickListener = null;
        }
    }

    private void checkPermissionThenLunch() {
        if (!(_activity instanceof BaseActivity)) {
            return;
        }
        final BaseActivity baseActivity = (BaseActivity) _activity;
        baseActivity.checkPermission(new BaseActivity.CheckPermissionListener() {
                                         @Override
                                         public void superPermission() {
                                             lunch2LibraryFileFragment();
                                         }

                                         @Override
                                         public void onPermissionDenied() {
                                             List<String> permission = new ArrayList<>();
                                             permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                                             permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                             baseActivity.checkPermissionNeverAskAgain(null, permission);
                                         }
                                     }, R.string.permission_storage_rationale,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void lunch2LibraryFileFragment() {
        Bundle args = getArguments();
        if (args == null) {
            finishParent();
            return;
        }

        if (mOnRepoItemClickListener != null) {
            mOnRepoItemClickListener.onLibraryItemClick();
        }
    }

    private void lunch2RepoFileFragment(BoundService s) {
        Bundle args = getArguments();
        if (args == null) {
            finishParent();
            return;
        }
        if (mOnRepoItemClickListener != null) {
            mOnRepoItemClickListener.onRepoItemClick(s);
        }
    }

    public interface OnRepoItemClickListener {
        void onLibraryItemClick();

        void onRepoItemClick(BoundService s);
    }

}
