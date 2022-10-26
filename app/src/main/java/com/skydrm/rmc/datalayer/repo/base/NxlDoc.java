package com.skydrm.rmc.datalayer.repo.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.downloader.DownloadManager;
import com.skydrm.rmc.ui.project.feature.centralpolicy.PolicyEvaluation;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class NxlDoc extends NxlFileBase implements IFileInfo, IOffline, Parcelable {
    protected String mDuid;
    protected long mFileSize;
    protected String mFileType;
    protected String mLocalPath;
    protected boolean isFavorite;
    protected boolean isOffline;
    protected int mModifyRightsStatus;
    protected int mEditStatus;
    protected int mOperationStatus;

    public NxlDoc(Parcel in) {
        super(in);
        this.mDuid = in.readString();
        this.mFileSize = in.readLong();
        this.mFileType = in.readString();
        this.mLocalPath = in.readString();
        this.isFavorite = in.readByte() != 0;
        this.isOffline = in.readByte() != 0;
        this.mModifyRightsStatus = in.readInt();
        this.mEditStatus = in.readInt();
        this.mOperationStatus = in.readInt();
    }

    public NxlDoc(String fileName, String duid,
                  long fileSize, String fileType,
                  String filePathId, String filePathDisplay,
                  String localPath,
                  boolean isFavorite, boolean isOffline,
                  int modifyRightsStatus, int editStatus, int operationStatus,
                  long lastModifiedTime, long creationTime) {
        super(fileName, filePathId, filePathDisplay, lastModifiedTime, creationTime);
        this.mDuid = duid;
        this.mFileSize = fileSize;
        this.mFileType = fileType;
        this.mLocalPath = localPath;
        this.isFavorite = isFavorite;
        this.isOffline = isOffline;

        this.mModifyRightsStatus = modifyRightsStatus;
        this.mEditStatus = editStatus;
        this.mOperationStatus = operationStatus;
    }

    public abstract String getOfflineObligations();

    protected abstract int getOfflineRights();

    protected abstract String getPartialPath();

    protected abstract String createNewPartialPath();

    protected abstract void setLocalPath(String path);

    protected abstract void partialDownload(File doc, final DownloadListener listener)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException;

    protected abstract Pair<Integer, String> getOfflineRightsAndObligations();

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public List<INxlFile> getChildren() {
        return null;
    }

    @Override
    public void markAsFavorite() {

    }

    @Override
    public void unMarkAsFavorite() {

    }

    @Override
    public INxlFileFingerPrint getFingerPrint() throws RmsRestAPIException,
            IOException, TokenAccessDenyException, InvalidRMClientException, SessionInvalidException {
        String localPath = getLocalPath();
        File f = new File(localPath);
        if (f.exists() && f.isFile() && f.length() > 0x4000) {
            //Hit a point may need to clear partial path.
            resetPartialPath();
        } else {
            String partialPath = getPartialPath();
            File pf = new File(partialPath);
            if (pf.exists() && pf.isFile() && pf.length() != 0) {
                localPath = partialPath;
            } else {
                String newPartialPath = createNewPartialPath();
                File doc = new File(newPartialPath);
                doc.createNewFile();
                partialDownload(doc, null);
                localPath = newPartialPath;
            }
        }
        return SkyDRMApp.getInstance()
                .getSession()
                .getRmsClient()
                .extractFingerPrint(localPath);
    }

    @Override
    public void clearCache() {
        //Just filter out files already offline.
        if (isOffline) {
            return;
        }
        deleteLocalFile();
        deleteLocalPartialFile();
    }

    @Override
    public long getCacheSize() {
        if (isOffline) {
            return 0;
        }
        return getLocalFileSize() + getLocalPartialFileSize();
    }

    @Override
    public void cancel() {
        DownloadManager.getInstance().cancel(mPathId);
        updateOfflineStatus(false);
    }

    @Override
    public void doPolicyEvaluation(final INxlFileFingerPrint fp, final IPolicyCallback callback) {
        final int rights = INxlRights.VIEW + INxlRights.EDIT + INxlRights.PRINT
                + INxlRights.SHARE + INxlRights.DOWNLOAD
                + INxlRights.WATERMARK + INxlRights.DECRYPT;

        if (isOffline & !SkyDRMApp.getInstance().isNetworkAvailable()) {
            Pair<Integer, String> result = getOfflineRightsAndObligations();
            if (result == null) {
                if (callback != null) {
                    callback.onFailed(new Exception("Operation failed."));
                }
            } else {
                if (callback != null) {
                    int first = result.first == null ? -1 : result.first;
                    callback.onSuccess(integer2Rights(first), result.second);
                }
            }
            return;
        }

        PolicyEvaluation.evaluate(PolicyEvaluation.buildEvalBean(fp, mName, rights, 0),
                new PolicyEvaluation.IEvaluationCallback() {
                    @Override
                    public void onEvaluated(String result) {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject responseObj = new JSONObject(result);
                                if (responseObj.has("results")) {
                                    JSONObject resultsObj = responseObj.optJSONObject("results");
                                    if (resultsObj != null) {
                                        int rights = resultsObj.optInt("rights");
                                        JSONArray obligations = resultsObj.optJSONArray("obligations");
                                        List<String> rightsArray = integer2Rights(rights);
                                        if (obligations != null && obligations.length() != 0) {
                                            if (rightsArray != null) {
                                                rightsArray.add("WATERMARK");
                                            }
                                        }
                                        if (callback != null) {
                                            callback.onSuccess(rightsArray, obligations == null ?
                                                    "" : obligations.toString());
                                        }
                                    } else {
                                        if (callback != null) {
                                            callback.onFailed(new Exception("Unknown result."));
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                if (callback != null) {
                                    callback.onFailed(e);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        if (callback != null) {
                            callback.onFailed(e);
                        }
                    }
                });
    }

    public boolean hasReShareRights(INxlFileFingerPrint fp)
            throws InvalidRMClientException, RmsRestAPIException, SessionInvalidException {
        if (fp == null) {
            return false;
        }
        if (fp.hasRights()) {
            return fp.hasShare();
        } else {
            return evaluateReShareRights(fp);
        }
    }

    public long getFileSize() {
        return mFileSize;
    }

    public String getDuid() {
        return mDuid;
    }

    public String getLocalPath() {
        if (mLocalPath == null || mLocalPath.isEmpty()) {
            return "";
        }
        File doc = new File(mLocalPath);
        if (doc.exists() && doc.isFile() && doc.length() != 0) {
            return mLocalPath;
        }
        return "";
    }

    protected String getFileType() {
        return mFileType;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public void setOperationStatus(int status) {
        this.mOperationStatus = status;
    }

    public int getModifyRightsStatus() {
        return mModifyRightsStatus;
    }

    public int getEditStatus() {
        return mEditStatus;
    }

    public int getOperationStatus() {
        return mOperationStatus;
    }

    public boolean hasOfflineViewRights() {
        int rights = getOfflineRights();
        if (rights < 0) {
            return false;
        }
        return (rights & INxlRights.VIEW) == INxlRights.VIEW;
    }

    private boolean evaluateReShareRights(INxlFileFingerPrint fp)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        final int evalRights = INxlRights.VIEW + INxlRights.EDIT + INxlRights.PRINT
                + INxlRights.SHARE + INxlRights.DOWNLOAD
                + INxlRights.WATERMARK + INxlRights.DECRYPT;
        int evalType = 0;//defined by rms api.
        String response = PolicyEvaluation.evaluate(fp, mName, evalRights, evalType);
        if (response == null || response.isEmpty()) {
            return false;
        }
        try {
            JSONObject responseObj = new JSONObject(response);
            JSONObject resultsObj = responseObj.optJSONObject("results");
            if (resultsObj == null) {
                return false;
            }
            int rights = resultsObj.optInt("rights");
            return (rights & INxlRights.SHARE) == INxlRights.SHARE;
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }
    }

    private boolean evaluateDecryptRights(INxlFileFingerPrint fp)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        final int evalRights = INxlRights.VIEW + INxlRights.EDIT + INxlRights.PRINT
                + INxlRights.SHARE + INxlRights.DOWNLOAD
                + INxlRights.WATERMARK + INxlRights.DECRYPT;
        int evalType = 0;//defined by rms api.
        String response = PolicyEvaluation.evaluate(fp, mName, evalRights, evalType);
        if (response == null || response.isEmpty()) {
            return false;
        }
        try {
            JSONObject responseObj = new JSONObject(response);
            JSONObject resultsObj = responseObj.optJSONObject("results");
            if (resultsObj == null) {
                return false;
            }
            int rights = resultsObj.optInt("rights");
            return (rights & INxlRights.DECRYPT) == INxlRights.DECRYPT;
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }
    }

    private boolean evaluateDownloadRights(INxlFileFingerPrint fp)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        final int evalRights = INxlRights.VIEW + INxlRights.EDIT + INxlRights.PRINT
                + INxlRights.SHARE + INxlRights.DOWNLOAD
                + INxlRights.WATERMARK + INxlRights.DECRYPT;
        int evalType = 0;//defined by rms api.
        String response = PolicyEvaluation.evaluate(fp, mName, evalRights, evalType);
        if (response == null || response.isEmpty()) {
            return false;
        }
        try {
            JSONObject responseObj = new JSONObject(response);
            JSONObject resultsObj = responseObj.optJSONObject("results");
            if (resultsObj == null) {
                return false;
            }
            int rights = resultsObj.optInt("rights");
            return (rights & INxlRights.DOWNLOAD) == INxlRights.DOWNLOAD;
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }
    }

    protected void resetPartialPath() {
        String partialPath = getPartialPath();
        //Partial file does not exists.
        if (partialPath == null || partialPath.isEmpty()) {
            return;
        }
        File pf = new File(partialPath);
        pf.delete();
    }

    protected void deleteLocalFile() {
        String localPath = getLocalPath();
        //Means file is not downloaded.
        if (localPath == null || localPath.isEmpty()) {
            return;
        }
        File f = new File(localPath);
        if (f.isFile() && f.delete()) {
            setLocalPath("");
        }
    }

    protected void deleteLocalPartialFile() {
        String partialPath = getPartialPath();
        if (partialPath == null || partialPath.isEmpty()) {
            return;
        }
        File pf = new File(partialPath);
        pf.delete();
    }

    private long getLocalFileSize() {
        long ret = 0;
        String localPath = getLocalPath();
        if (localPath == null || localPath.isEmpty()) {
            return ret;
        }
        File f = new File(localPath);
        ret = f.length();
        return ret;
    }

    private long getLocalPartialFileSize() {
        long ret = 0;
        String partialPath = getPartialPath();
        if (partialPath == null || partialPath.isEmpty()) {
            return ret;
        }
        File pf = new File(partialPath);
        ret = pf.length();
        return ret;
    }

    public static List<String> integer2Rights(int permissions) {
        List<String> ret = new ArrayList<>();
        // Rights to view.
        if ((permissions & INxlRights.VIEW) == INxlRights.VIEW) {
            ret.add(Constant.RIGHTS_VIEW);
        }
        // Rights to print.
        if ((permissions & INxlRights.PRINT) == INxlRights.PRINT) {
            ret.add(Constant.RIGHTS_PRINT);
        }
        // Rights to edit.
        if ((permissions & INxlRights.EDIT) == INxlRights.EDIT) {
            ret.add(Constant.RIGHTS_EDIT);
        }
        // Rights to share.
        if ((permissions & INxlRights.SHARE) == INxlRights.SHARE) {
            ret.add(Constant.RIGHTS_SHARE);
        }
        // Rights to download.
        if ((permissions & INxlRights.DOWNLOAD) == INxlRights.DOWNLOAD) {
            ret.add(Constant.RIGHTS_DOWNLOAD);
        }
        // Rights to waterMark.
        if ((permissions & INxlRights.WATERMARK) == INxlRights.WATERMARK) {
            ret.add(Constant.RIGHTS_WATERMARK);
        }
        // Rights to decrypt
        if ((permissions & INxlRights.DECRYPT) == INxlRights.DECRYPT) {
            ret.add(Constant.RIGHTS_DECRYPT);
        }
        return ret;
    }

    public static int rights2Integer(List<String> rights) {
        int rt = 0;
        for (String right : rights) {
            if (right.equals(Constant.RIGHTS_VIEW)) {
                rt += INxlRights.VIEW;
            }
            if (right.equals(Constant.RIGHTS_DOWNLOAD)) {
                rt += INxlRights.DOWNLOAD;
            }
            if (right.equals(Constant.RIGHTS_SHARE)) {
                rt += INxlRights.SHARE;
            }
            if (right.equals(Constant.RIGHTS_PRINT)) {
                rt += INxlRights.PRINT;
            }
            if (right.equals(Constant.RIGHTS_EDIT)) {
                rt += INxlRights.EDIT;
            }
            if (right.equals(Constant.RIGHTS_WATERMARK)) {
                rt += INxlRights.WATERMARK;
            }
            if (right.equals(Constant.RIGHTS_DECRYPT)) {
                rt += INxlRights.DECRYPT;
            }
        }
        return rt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mPathId);
        dest.writeString(mPathDisplay);
        dest.writeLong(mLastModifiedTime);
        dest.writeLong(mCreationTime);

        dest.writeString(mDuid);
        dest.writeLong(mFileSize);
        dest.writeString(mFileType);
        dest.writeString(mLocalPath);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeByte((byte) (isOffline ? 1 : 0));
        dest.writeInt(mModifyRightsStatus);
        dest.writeInt(mEditStatus);
        dest.writeInt(mOperationStatus);
    }
}
