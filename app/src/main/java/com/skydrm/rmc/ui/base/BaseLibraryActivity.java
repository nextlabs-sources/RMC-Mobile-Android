package com.skydrm.rmc.ui.base;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.DividerItemDecoration;
import com.skydrm.rmc.ui.common.NxlAdapter;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.ui.widget.NxlSwipeRefreshLayout;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuRecyclerView;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseLibraryActivity extends BaseActivity implements IFileContact.IView {
    @BindView(R.id.project_files_toolbar3)
    Toolbar mToolbar;

    @BindView(R.id.swipeToLoadLayout)
    NxlSwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerView)
    SwipeMenuRecyclerView mSwipeMenuRecyclerView;

    @BindView(R.id.project_files_path_widget)
    RelativeLayout mRlPathSite;
    @BindView(R.id.project_files_folder_back)
    ImageButton mIbFolderBack;
    @BindView(R.id.project_files_folder_path)
    TextView mTvPathDisplay;

    @BindView(R.id.select_pathId)
    Button mBtSelect;

    private String mRootPathId = "/";

    private NxlAdapter mFileAdapter;
    protected IFileContact.IPresenter mPresenter;

    private ProgressDialog mProgressDialog;

    protected abstract IFileContact.IPresenter createPresenter();

    protected abstract boolean resolveIntent(Intent i);

    protected abstract int getFileType();

    protected abstract String getAction();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectionfolder3);
        ButterKnife.bind(this);

        if (resolveIntent(getIntent())) {
            setViewHelperController(mSwipeRefreshLayout);
            initViewAndEvents();
            mPresenter = createPresenter();
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.initialize(getFileType());
    }

    @Override
    protected View getLoadingTargetView() {
        return mSwipeRefreshLayout;
    }

    @Override
    protected void setViewHelperController(View target) {
        if (target == null) {
            return;
        }
        this.mViewHelperController = new ViewHelperController(target);
    }

    @Override
    public void initialize(boolean active) {
        if (active) {
            showLoadingDialog();
        } else {
            dismissLoadingDialog();
        }
    }

    @Override
    public void update(List<NxlFileItem> data) {
        mFileAdapter.setData(data);
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
        return true;
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(this, e);
    }

    @Override
    public void onBackPressed() {
        String currentPathId = mFileAdapter.getPathId();
        if (!TextUtils.equals(currentPathId, "/")) {
            handlerFolderBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.releaseResource(mPresenter);
        CommonUtils.releaseResource(mFileAdapter);
    }

    private void initViewAndEvents() {
        initRecyclerView();
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.refresh(getFileType(), mFileAdapter.getPathId());
            }
        });
        mRlPathSite.setVisibility(View.GONE);
        mIbFolderBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlerFolderBack();
            }
        });
        mBtSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPath();
            }
        });
    }

    private void initRecyclerView() {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.main_green_light),
                getResources().getColor(R.color.color_blue));
        mSwipeMenuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mSwipeMenuRecyclerView.setLayoutManager(layoutManager);
        // config animator
        mSwipeMenuRecyclerView.configAnimator();
        mSwipeMenuRecyclerView.addItemDecoration(new DividerItemDecoration(this, null,
                false, true));
        mFileAdapter = new NxlAdapter(this);
        mFileAdapter.setCreatedByMe(true);
        mFileAdapter.setSelectFolderMode(true);
        mFileAdapter.setOnItemClickListener(new NxlAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(INxlFile f, int pos) {
                if (f.isFolder()) {
                    mTvPathDisplay.setText(f.getPathDisplay());
                    displayPathSite(true);
                    listCurrent();
                }
            }
        });
        mSwipeMenuRecyclerView.setAdapter(mFileAdapter);
    }

    public void displayPathSite(boolean visible) {
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

    public void handlerFolderBack() {
        String parentPath = FileUtils.getParent(mFileAdapter.getPathId());
        mFileAdapter.setPathId(parentPath);
        mTvPathDisplay.setText(parentPath);
        displayPathSite(!parentPath.equals(mRootPathId));
        listCurrent();
    }

    private void listCurrent() {
        mPresenter.list(getFileType(), mFileAdapter.getPathId());
    }

    private void selectPath() {
        Intent i = new Intent();
        if (Constant.ACTION_PROJECT_ADD_FILE.equals(getAction())) {
            i.putExtra(Constant.PROJECT_PATH_ID, mFileAdapter.getPathId());
        } else if (Constant.ACTION_NEW_FOLDER_FROM_PROJECT.equals(getAction())) {
            i.putExtra(Constant.PROJECT_PARENT_PATH_ID, mFileAdapter.getPathId());
        } else if (Constant.ACTION_NEW_FOLDER_FROM_WORKSPACE.equals(getAction())) {
            i.putExtra(Constant.CREATE_FOLDER_PARENT_PATH_ID, mFileAdapter.getPathId());
        } else if (Constant.ACTION_PROJECT_ADD_FILE.equals(getAction())) {
            i.putExtra(Constant.PROJECT_PATH_ID, mFileAdapter.getPathId());
        }
        setResult(RESULT_OK, i);
        finish();
    }

    private void showLoadingDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage(getString(R.string.wait_load));
        }
        mProgressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
