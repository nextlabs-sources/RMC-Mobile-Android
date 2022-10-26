package com.skydrm.rmc.ui.myspace.myvault.view.interactor;

import android.content.Context;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.myspace.myvault.view.task.GetMyVaultMetadataTask;
import com.skydrm.rmc.ui.myspace.myvault.view.task.RevokeTask;

public class VaultFileInfoPresenter implements IVaultFileInfoContact.IPresenter {
    private INxlFile mFile;
    private IVaultFileInfoContact.IView mView;
    private RevokeCallback mRevokeCallback;

    public VaultFileInfoPresenter(INxlFile f, IVaultFileInfoContact.IView v) {
        this.mFile = f;
        this.mView = v;
    }

    @Override
    public void getMetadata() {
        new GetMyVaultMetadataTask((MyVaultFile) mFile, new LoadTask.ITaskCallback<GetMyVaultMetadataTask.Result, Exception>() {
            @Override
            public void onTaskPreExecute() {
                if (mView != null) {
                    mView.setLoadingIndicator(true);
                }
            }

            @Override
            public void onTaskExecuteSuccess(GetMyVaultMetadataTask.Result results) {
                if (mView != null) {
                    mView.setLoadingIndicator(false);
                }
                if (mView != null) {
                    mView.updateMetadata(results.result);
                }
            }

            @Override
            public void onTaskExecuteFailed(Exception e) {
                if (mView != null) {
                    mView.setLoadingIndicator(false);
                }
                if (mView != null) {
                    mView.showErrorView(e);
                }
            }
        }).run();
    }

    @Override
    public void copyLink(Context ctx, String text) {
        try {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ctx
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ctx
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData
                        .newPlainText(ctx.getResources().getString(R.string.common_message), text);
                clipboard.setPrimaryClip(clip);
            }
        } catch (Exception e) {
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    @Override
    public void revokeRights() {
        if (mFile == null) {
            return;
        }
        if (mFile instanceof MyVaultFile) {
            if (mRevokeCallback == null) {
                mRevokeCallback = new RevokeCallback();
            }
            RevokeTask task = new RevokeTask((MyVaultFile) mFile, mRevokeCallback);
            task.run();
        }
    }

    @Override
    public void onDestroy() {
        if (mView != null) {
            mView = null;
        }
        if (mRevokeCallback != null) {
            mRevokeCallback = null;
        }
    }

    class RevokeCallback implements RevokeTask.ITaskCallback<RevokeTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            if (mView != null) {
                mView.setLoadingIndicator(true);
            }
        }

        @Override
        public void onTaskExecuteSuccess(RevokeTask.Result results) {
            if (mView != null) {
                mView.setLoadingIndicator(false);
            }
            if (mView != null) {
                mView.onRevokeRights();
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mView != null) {
                mView.setLoadingIndicator(false);
                mView.showErrorView(e);
            }
        }
    }
}
