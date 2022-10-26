package com.skydrm.rmc.ui.service.share;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.service.share.view.ProjectListFragment;
import com.skydrm.rmc.ui.widget.LoadingDialog2;

import java.io.Serializable;
import java.util.List;

public class ShareActivity extends BaseActivity {
    private ShareCallback mCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        checkThenLoadShareFragmentAsRoot();
    }

    private void checkThenLoadShareFragmentAsRoot() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        ISharingService service = intent.getParcelableExtra(Constant.SHARING_SERVICE);
        ISharingFile file = intent.getParcelableExtra(Constant.SHARING_ENTRY);
        if (service == null || file == null) {
            finish();
            return;
        }
        mCallback = new ShareCallback(this, service, file);
        file.share(mCallback);
    }

    public void replaceLoadSharedWithFragAsRoot() {
        SharedWithFragment frag = SharedWithFragment.newInstance();
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void replaceLoadProjectListFragAsRoot(int id, List<String> recipients) {
        ProjectListFragment frag = ProjectListFragment.newInstance();
        Bundle args = new Bundle();
        args.putInt(Constant.PROJECT_ID, id);
        args.putSerializable(Constant.RECIPIENTS, (Serializable) recipients);
        frag.setArguments(args);
        frag.setUserVisibleHint(true);
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCallback != null) {
            mCallback = null;
        }
    }

    static class ShareCallback implements IMarkCallback {
        private Context mCtx;
        private ISharingService mService;
        private ISharingFile mFile;
        private LoadingDialog2 mLoadingDialog;

        ShareCallback(Context ctx, ISharingService service, ISharingFile file) {
            this.mService = service;
            this.mFile = file;
            this.mCtx = ctx;
        }

        @Override
        public void onMarkStart() {
            showLoadingDialog();
        }

        @Override
        public void onMarkAllow() {
            dismissLoadingDialog();

            ShareFragment frag = ShareFragment.newInstance();
            frag.setUserVisibleHint(true);
            Bundle args = new Bundle();
            args.putParcelable(Constant.SHARING_SERVICE, (Parcelable) mService);
            args.putParcelable(Constant.SHARING_ENTRY, (Parcelable) mFile);
            frag.setArguments(args);
            ((BaseActivity) mCtx).loadRootFragment(R.id.fl_container, frag);
        }

        @Override
        public void onMarkFailed(MarkException e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(mCtx, e);
        }

        private void showLoadingDialog() {
            mLoadingDialog = LoadingDialog2.newInstance();
            mLoadingDialog.showModalDialog(mCtx);
        }

        private void dismissLoadingDialog() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismissDialog();
                mLoadingDialog = null;
            }
        }
    }

}
