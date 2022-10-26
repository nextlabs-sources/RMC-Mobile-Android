package com.skydrm.rmc.datalayer.repo.library;

import android.content.Context;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatListener;
import com.skydrm.rmc.datalayer.repo.NxlRepo;
import com.skydrm.rmc.datalayer.repo.base.IFileService;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.service.share.ISharingFile;
import com.skydrm.rmc.ui.service.share.ISharingService;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.types.SharingLocalFilePara;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LibraryRepo extends NxlRepo implements ISharingService, IDataService, Parcelable {
    private IFileService<INxlFile> mFS;

    public LibraryRepo() {
        mFS = new LibraryFileService();
    }

    private LibraryRepo(Parcel in) {
        mFS = in.readParcelable(LibraryFileService.class.getClassLoader());
    }

    public static final Creator<LibraryRepo> CREATOR = new Creator<LibraryRepo>() {
        @Override
        public LibraryRepo createFromParcel(Parcel in) {
            return new LibraryRepo(in);
        }

        @Override
        public LibraryRepo[] newArray(int size) {
            return new LibraryRepo[size];
        }
    };

    @Override
    public List<INxlFile> list(int type) {
        return list(type, Environment.getExternalStorageDirectory().getPath(), false);
    }

    @Override
    public List<INxlFile> list(int type, String pathId, boolean recursively) {
        return mFS.listFile(pathId, recursively);
    }

    @Override
    public List<INxlFile> sync(int type)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException {
        return sync(type, Environment.getExternalStorageDirectory().getPath(), false);
    }

    @Override
    public List<INxlFile> sync(int type, String pathId, boolean recursively)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException {
        return mFS.syncFile(pathId, recursively);
    }

    @Override
    public void clearCache() {

    }

    @Override
    public long getCacheSize() {
        return 0;
    }

    @Override
    public void updateResetAllOperationStatus() {

    }

    @Override
    public void onHeatBeat(IHeartBeatListener l) {

    }

    @Override
    public int getId() {
        return -1;
    }

    @Override
    public String getServiceName(@NonNull Context ctx) {
        return ctx.getString(R.string.Library);
    }

    @Override
    public boolean shareToProject(@NonNull ISharingFile file,
                                  @NonNull List<Integer> recipients,
                                  String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {

        return false;
    }

    @Override
    public boolean shareToPerson(@NonNull ISharingFile file,
                                 @NonNull List<String> recipients,
                                 String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        if (file instanceof LibraryFile) {
            LibraryFile lf = (LibraryFile) file;

            File f = new File(lf.getLocalPath());
            if (!f.exists() || !f.isFile()) {
                return false;
            }
            SharingLocalFilePara params;
            int permissions;
            Expiry expiry;
            String watermark;
            String tags = "";
            if (RenderHelper.isNxlFile(f.getPath())) {
                INxlFileFingerPrint fp;
                try {
                    fp = file.getFingerPrint();
                } catch (IOException | TokenAccessDenyException e) {
                    throw new RmsRestAPIException(e.getMessage(), e);
                }
                if (fp == null) {
                    return false;
                }
                permissions = fp.toInteger();
                expiry = fp.getExpiry();
                watermark = fp.getDisplayWatermark();
                tags = fp.toJsonFormat();
                params = new SharingLocalFilePara(f, permissions, expiry,
                        watermark, tags,
                        recipients, comments);
            } else {
                Rights rights = lf.getEncryptionRights();
                if (rights == null) {
                    throw new RmsRestAPIException("Rights need to be set correctly when try sharing local file.");
                }
                Obligations obligations = lf.getEncryptionObligations();
                permissions = rights.toInteger();
                expiry = lf.getEncryptionExpiry();
                watermark = obligations.getDisplayWatermark();

                params = new SharingLocalFilePara(f, permissions, expiry,
                        watermark, tags,
                        recipients, comments);
            }

            SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
            String duid = session.getRmsRestAPI()
                    .getSharingService(session.getRmUser())
                    .sharingLocalFile(params);

            return duid != null && !duid.isEmpty();
        }
        return false;
    }

    @Override
    public boolean updateRecipients(@NonNull ISharingFile file,
                                    List<String> newRecipients,
                                    List<String> removedRecipients,
                                    String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {

        return false;
    }

    @Override
    public boolean revokeAllRights(@NonNull ISharingFile file)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mFS, flags);
    }
}
