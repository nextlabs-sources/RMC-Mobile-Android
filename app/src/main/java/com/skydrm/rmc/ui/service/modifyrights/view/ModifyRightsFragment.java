package com.skydrm.rmc.ui.service.modifyrights.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.service.fileinfo.FileInfoTask;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsFile;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsService;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;

import butterknife.BindView;

public class ModifyRightsFragment extends BaseFragment {
    @BindView(R.id.ll_root)
    LinearLayout mLlRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.rl_container)
    RelativeLayout mRlContainer;
    @BindView(R.id.bt_operate)
    Button mBtOperate;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;

    @BindView(R.id.fl_content)
    RelativeLayout mRlPreviewImageContainer;
    @BindView(R.id.tv_file_name)
    TextView mTvFileName;
    @BindView(R.id.tv_path)
    TextView mTvFilePath;
    @BindView(R.id.tv_change_save_location)
    TextView mTvChangeLocationSite;

    private IViewBuilder mViewBuilder;
    private IModifyRightsService mService;
    private IModifyRightsFile mFile;
    private GetFingerPrintCallback mGetFingerPrintCallback;

    public static ModifyRightsFragment newInstance() {
        return new ModifyRightsFragment();
    }

    public boolean needInterceptBackPress() {
        return mViewBuilder != null && mViewBuilder.needInterceptBackPress();
    }

    public void interceptBackPress() {
        if (mViewBuilder != null) {
            mViewBuilder.interceptBackPress();
        }
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
        if (!resolveBundle()) {
            finishParent();
        }
        initPreviewBar();
        mViewBuilder = new CentralFileViewBuilder(_activity, mLlRoot, mService, mFile);

        mViewBuilder.configureToolbar(mToolbar);
        mRlContainer.addView(mViewBuilder.buildRoot(_activity));
        mViewBuilder.configureOperateButton(mBtOperate);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
        tryGetFingerPrint();
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_modify_rights;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mViewBuilder != null) {
            CommonUtils.releaseResource(mViewBuilder);
            mViewBuilder = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mViewBuilder != null) {
            CommonUtils.releaseResource(mViewBuilder);
            mViewBuilder = null;
        }
        if (mGetFingerPrintCallback != null) {
            mGetFingerPrintCallback = null;
        }
    }

    private boolean resolveBundle() {
        Bundle args = getArguments();
        if (args == null) {
            return false;
        }
        mService = args.getParcelable(Constant.MODIFY_RIGHTS_SERVICE);
        mFile = args.getParcelable(Constant.MODIFY_RIGHTS_ENTRY);
        return true;
    }

    private void initPreviewBar() {
        mRlPreviewImageContainer.setVisibility(View.GONE);
        mTvFileName.setText(mFile.getName());
        mTvFilePath.setText(mService.getServiceName(_activity));
        mTvChangeLocationSite.setVisibility(View.GONE);
    }

    private void tryGetFingerPrint() {
        if (!(mFile instanceof NxlDoc)) {
            return;
        }
        mGetFingerPrintCallback = new GetFingerPrintCallback();
        FileInfoTask task = new FileInfoTask((NxlDoc) mFile, mGetFingerPrintCallback);
        task.run();
    }

    class GetFingerPrintCallback implements FileInfoTask.ITaskCallback<FileInfoTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            if (mViewBuilder != null) {
                mViewBuilder.showLoading();
            }
        }

        @Override
        public void onTaskExecuteSuccess(FileInfoTask.Result results) {
            if (mViewBuilder != null) {
                mViewBuilder.hideLoading();
                mViewBuilder.bindFingerPrint(results.fp);
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mViewBuilder != null) {
                mViewBuilder.hideLoading();
            }
            ExceptionHandler.handleException(_activity, e);
        }
    }
}
