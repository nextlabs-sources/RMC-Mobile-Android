package com.skydrm.rmc.ui.service.protect.file;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.Nullable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.INxFile;

import java.io.File;
import java.io.Serializable;

public class MySpaceProtectFile extends ProtectBaseFile {
    private INxFile mFile;
    private DownloadCallback mDownloadCallback;

    public MySpaceProtectFile(INxFile f) {
        this.mFile = f;
    }

    private MySpaceProtectFile(Parcel in) {
        mFile = (INxFile) in.readSerializable();
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public void tryGetFile(Context ctx, ICallBack callBack) {
        if (mDownloadCallback == null) {
            mDownloadCallback = new DownloadCallback(callBack);
        }

        try {
            if (callBack != null) {
                callBack.onPreDownload();
            }
            File file = SkyDRMApp.getInstance().getRepoSystem().getFile(mFile, mDownloadCallback);
            if (file != null) {
                if (callBack != null) {
                    callBack.onDownloadFinished(file.getPath());
                }
            }
        } catch (Exception e) {
            if (callBack != null) {
                callBack.onDownloadFailed(new FileDownloadException(e.getMessage()));
            }
        }
    }

    @Override
    public void release() {

    }

    public static final Creator<MySpaceProtectFile> CREATOR = new Creator<MySpaceProtectFile>() {
        @Override
        public MySpaceProtectFile createFromParcel(Parcel in) {
            return new MySpaceProtectFile(in);
        }

        @Override
        public MySpaceProtectFile[] newArray(int size) {
            return new MySpaceProtectFile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable((Serializable) mFile);
    }

    static class DownloadCallback implements IRemoteRepo.IDownLoadCallback {
        private ICallBack mCallback;

        DownloadCallback(ICallBack callBack) {
            this.mCallback = callBack;
        }

        @Override
        public void cancelHandler(ICancelable handler) {

        }

        @Override
        public void onFinishedDownload(boolean taskStatus,
                                       String localPath,
                                       @Nullable FileDownloadException e) {
            if (mCallback != null) {
                mCallback.onDownloadFinished(localPath);
            }
        }

        @Override
        public void progressing(long newValue) {
            if (mCallback != null) {
                mCallback.onDownloadProgress(newValue);
            }
        }
    }

}
