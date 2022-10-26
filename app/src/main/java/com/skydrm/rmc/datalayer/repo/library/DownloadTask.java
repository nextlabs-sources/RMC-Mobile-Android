package com.skydrm.rmc.datalayer.repo.library;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.service.protect.IProtectFile;
import com.skydrm.rmc.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class DownloadTask extends LoadTask<Void, String> {
    private InputStream mSource;
    private File mDest;

    private IProtectFile.ICallBack mCallback;

    private Exception mExp;

    public DownloadTask(InputStream source, File dest, IProtectFile.ICallBack callBack) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.REGULAR_BACK_GROUND));
        this.mSource = source;
        this.mCallback = callBack;
        this.mDest = dest;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onPreDownload();
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (mSource != null) {
            try {
                FileUtils.copy(mSource, mDest);
            } catch (IOException e) {
                mExp = e;
                e.printStackTrace();
            }
        }
        return mDest.getPath();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mExp != null) {
            if (mCallback != null) {
                mCallback.onDownloadFailed(new FileDownloadException(mExp.getMessage(),
                        FileDownloadException.ExceptionCode.Common));
            }
        } else {
            if (mCallback != null) {
                mCallback.onDownloadFinished(mDest.getPath());
            }
        }
    }
}
