package com.skydrm.rmc.ui.service.protect.file;

import android.content.Context;
import android.os.Parcel;

import com.skydrm.rmc.reposystem.exception.FileDownloadException;

import java.io.File;

public class LocalProtectFile extends ProtectBaseFile {
    private File mFile;

    public LocalProtectFile(File f) {
        this.mFile = f;
    }

    private LocalProtectFile(Parcel in) {
        mFile = (File) in.readSerializable();
    }

    @Override
    public String getName() {
        if (mFile == null) {
            return "";
        }
        return mFile.getName();
    }

    @Override
    public void tryGetFile(Context ctx, ICallBack callBack) {
        if (mFile == null) {
            if (callBack != null) {
                callBack.onDownloadFailed(new FileDownloadException("Target file is null."));
            }
            return;
        }
        String path = mFile.getPath();
        if (path.isEmpty()) {
            if (callBack != null) {
                callBack.onDownloadFailed(new FileDownloadException("Unknown error."));
            }
        } else {
            File f = new File(path);
            if (!f.exists()) {
                if (callBack != null) {
                    callBack.onDownloadFailed(new FileDownloadException("Target file doesn't exists."));
                }
                return;
            }
            if (f.isDirectory()) {
                if (callBack != null) {
                    callBack.onDownloadFailed(new FileDownloadException("Target file is a dir."));
                }
                return;
            }
            if (callBack != null) {
                callBack.onDownloadFinished(path);
            }
        }
    }

    @Override
    public void release() {

    }

    public static final Creator<LocalProtectFile> CREATOR = new Creator<LocalProtectFile>() {
        @Override
        public LocalProtectFile createFromParcel(Parcel in) {
            return new LocalProtectFile(in);
        }

        @Override
        public LocalProtectFile[] newArray(int size) {
            return new LocalProtectFile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mFile);
    }
}
