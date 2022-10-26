package com.skydrm.rmc.ui.repository.feature;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.repository.IRepoFileContact;
import com.skydrm.rmc.ui.repository.contact.RepoFileFilter;
import com.skydrm.rmc.ui.repository.contact.RepoFilePresenter;
import com.skydrm.rmc.ui.widget.WrapperLinearLayoutManager;
import com.skydrm.rmc.utils.ViewUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;

public class RepoFileFragment extends BaseFragment implements IRepoFileContact.IRepoFileView {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.bt_select)
    Button mBtSelect;

    private static final int TYPE_REPO_FILE = 2;
    private RepoFileAdapter mRepoFileAdapter;

    private IRepoFileContact.IRepoFilePresenter mPresenter;
    private BoundService mBoundService;

    private OnRepoFileItemClickListener mOnRepoFileItemClickListener;
    private OnPopupFragmentListener mOnPopupFragmentListener;

    public static RepoFileFragment newInstance() {
        return new RepoFileFragment();
    }

    public void setOnRepoFileItemClickListener(OnRepoFileItemClickListener listener) {
        this.mOnRepoFileItemClickListener = listener;
    }

    public void setOnPopupFragmentListener(OnPopupFragmentListener listener) {
        this.mOnPopupFragmentListener = listener;
    }

    public boolean needIntercept() {
        if (mPresenter == null) {
            return false;
        }
        return !mPresenter.isRoot();
    }

    public void intercept() {
        String parentPathId = getParentPathId(mRepoFileAdapter.getParentPathId());
        mRepoFileAdapter.setParentPathId(parentPathId);
        if (parentPathId.isEmpty() || parentPathId.equals("/")) {
            mToolbar.setTitle(mBoundService.getDisplayName());
        } else {
            mToolbar.setTitle(parentPathId);
        }
        mPresenter.navigateBack(parentPathId);
    }

    @Override
    protected void onUserFirstVisible() {
        mPresenter.initialize(TYPE_REPO_FILE);
    }

    @Override
    protected void onUserVisible() {

    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        initData();
        if (mBoundService == null) {
            ToastUtil.showToast(_activity, "Bound service is required to perform the following action.");
            finishParent();
            return;
        }
        mPresenter = new RepoFilePresenter(this, mBoundService, new RepoFileFilter());
        mToolbar.setTitle(mBoundService.getDisplayName());

        mRecyclerView.setLayoutManager(new WrapperLinearLayoutManager(_activity));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRepoFileAdapter = new RepoFileAdapter(_activity);
        mRecyclerView.setAdapter(mRepoFileAdapter);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interceptOrPopup();
            }
        });
        mRepoFileAdapter.setOnItemClickListener(new RepoFileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NXFileItem entity, int position) {
                INxFile f = entity.getNXFile();
                if (f == null) {
                    return;
                }
                if (f.isFolder()) {
                    mToolbar.setTitle(f.getLocalPath());
                    mPresenter.list(TYPE_REPO_FILE, f.getLocalPath());
                } else {
                    //Select file.
                    goOperate(f);
                }
            }
        });
        if (ViewUtils.isVisible(mBtSelect)) {
            mBtSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectPath();
                }
            });
        }
    }

    private void interceptOrPopup() {
        if (needIntercept()) {
            intercept();
        } else {
            if (mOnPopupFragmentListener != null) {
                mOnPopupFragmentListener.onPopup();
            }
        }
    }

    @Override
    protected View getLoadingTargetView() {
        return mRecyclerView;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_repository_file;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
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
    public void setEmptyView(boolean active) {
        if (active) {
            showEmpty("");
        } else {
            hideEmpty();
        }
    }

    @Override
    public void update(List<NXFileItem> data) {
        mRepoFileAdapter.setData(data);
    }

    @Override
    public void setLoadingIndicator(boolean active) {

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
    public void onDestroy() {
        super.onDestroy();
        CommonUtils.releaseResource(mPresenter);
        if (mOnRepoFileItemClickListener != null) {
            mOnRepoFileItemClickListener = null;
        }
        if (mOnPopupFragmentListener != null) {
            mOnPopupFragmentListener = null;
        }
    }

    private void initData() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        mBoundService = (BoundService) args.getSerializable(Constant.BOUND_SERVICE);

        CmdOperate operateType = (CmdOperate) args.getSerializable(Constant.CMD_OPERATE_TYPE);
        if (operateType == CmdOperate.COMMAND_SELECT_PATH) {
            mBtSelect.setVisibility(View.VISIBLE);
        }

    }

    private String getParentPathId(String parentPath) {
        if (parentPath == null || parentPath.isEmpty()) {
            return "/";
        }
        if (parentPath.equals("/")) {
            return "/";
        }
        return parentPath.substring(0, parentPath.lastIndexOf('/'));
    }

    private void goOperate(INxFile f) {
        if (mOnRepoFileItemClickListener != null) {
            mOnRepoFileItemClickListener.onItemClick(f);
        }
    }

    private void selectPath() {
        Intent intent = _activity.getIntent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.DEST_FOLDER, (Serializable) mPresenter.getDestFolder());
        bundle.putSerializable(Constant.BOUND_SERVICE, mBoundService);
        intent.putExtras(bundle);
        _activity.setResult(RESULT_OK, intent);
        _activity.finish();
    }

    public interface OnRepoFileItemClickListener {
        void onItemClick(INxFile f);
    }

    public interface OnPopupFragmentListener {
        void onPopup();
    }
}
