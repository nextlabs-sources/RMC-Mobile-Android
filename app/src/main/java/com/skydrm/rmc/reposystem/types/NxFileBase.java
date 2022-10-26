package com.skydrm.rmc.reposystem.types;


import android.text.TextUtils;

import com.skydrm.rmc.ui.service.favorite.model.IFavoriteFile;
import com.skydrm.rmc.utils.sort.IRepoFileSortable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.lang.System.currentTimeMillis;

/**
 * - support serializable
 */
public abstract class NxFileBase implements Serializable, Cloneable, INxFile, IRepoFileSortable, IFavoriteFile {
    static private final long REFRESH_INTERNAL = 2 * 1000;   // 60S

    private String mName;
    private String mDisplayPath;
    private String mLocalPath;
    private String mCloudPath; // may be cloudpath is not begin with '/'
    private String mCloudPathID;
    private long mSize;
    private long mLastModifiedTimeLong; //the number of milliseconds since January 1, 1970, 00:00:00 GMT
    private boolean mIsFolder;
    private boolean mIsSite = false;
    private List<INxFile> mChildren = new ArrayList<>();
    private BoundService mService;
    private boolean mIsFavorite;
    private boolean mIsOffline;
    private boolean mIsCached;
    private boolean mIsNewCreated;
    private long lastRefreshed; // used by Refresh only
    private String mUserDefinedStr;

    public NxFileBase() {
        Date date = new Date();
        mLastModifiedTimeLong = date.getTime();
        this.mService = null;
        this.mIsFavorite = false;
        this.mIsOffline = false;
        this.mIsCached = false;
        this.mIsNewCreated = false;
        this.lastRefreshed = 0;
        this.mUserDefinedStr = "";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public long getLastRefreshed() {
        return lastRefreshed;
    }

    public void setLastRefreshed(long lastRefreshed) {
        this.lastRefreshed = lastRefreshed;
//        if (SkyDRMApp.DEBUG) {
//            Log.d(TAG, SimpleDateFormat.getDateTimeInstance().format(new Date(lastRefreshed))
//                    + " Path:"
//                    + mLocalPath);
//        }
    }

    public void updateRefreshTimeWisely() {
        long cur = currentTimeMillis();
        if (lastRefreshed < cur) {
            setLastRefreshed(currentTimeMillis());
        }

    }

    public boolean wantingRefresh() {
        return currentTimeMillis() - lastRefreshed > REFRESH_INTERNAL;
    }

    public boolean wantingRefresh(int seconds) {
        return currentTimeMillis() - lastRefreshed > seconds * 1000;
    }

    @Override
    public boolean isFolder() {
        return mIsFolder;
    }

    protected void setIsFolder(boolean mIsFolder) {
        this.mIsFolder = mIsFolder;
    }

    @Override
    public boolean isSite() {
        return mIsSite;
    }

    protected void setSite(boolean mSite) {
        this.mIsSite = mSite;
    }

    @Override
    public String getName() {
        return mName;
    }


    public void setName(String mName) {
        this.mName = mName;
    }

    @Override
    public String getDisplayPath() {
        return mDisplayPath;
    }

    public void setDisplayPath(String path) {
        this.mDisplayPath = path;
    }


    @Override
    public String getLocalPath() {
        return mLocalPath;
    }

    public void setLocalPath(String mFullpath) {
        this.mLocalPath = mFullpath;
    }

    @Override
    public String getCloudPath() {
        return mCloudPath;
    }

    public void setCloudPath(String FullServicePath) {
        this.mCloudPath = FullServicePath;
    }

    @Override
    public String getCloudFileID() {
        return this.mCloudPathID;
    }

    public void setmCloudPathID(String pathID) {
        this.mCloudPathID = pathID;
    }

    @Override
    public long getSize() {
        return mSize;
    }


    public void setSize(long mSize) {
        this.mSize = mSize;
    }

    @Override
    public long getLastModifiedTimeLong() {
        return mLastModifiedTimeLong;
    }


    public void setLastModifiedTimeLong(long time) {
        mLastModifiedTimeLong = time;
    }

    @Override
    public boolean hasChildren() {
        return mChildren != null && !mChildren.isEmpty();
    }

    public void addChild(NxFileBase child) {
        this.mChildren.add(child);
    }

    public void addChild(List<INxFile> children) {
        if (children == null) {
            return;
        }
        for (INxFile i : children) {
            this.mChildren.add(i);
        }
    }

    @Override
    public String getParent() {
        if (this.mLocalPath.equals("/")) {
            return "";
        } else {
            int ind = this.mLocalPath.lastIndexOf(47);
            return this.mLocalPath.substring(0, ind + 1);
        }
    }

    @Override
    public List<INxFile> getChildren() {
        return mChildren;
    }

    public void setChildren(List<INxFile> children) {
        mChildren = children;
    }

    @Override
    public BoundService getService() {
        return mService;
    }

    public void setBoundService(BoundService service) {
        mService = service;
    }

    @Override
    public INxFile findNode(String path) {
        return null;
    }

    @Override
    public boolean isMarkedAsFavorite() {
        return mIsFavorite;
    }

    public void setMarkedAsFavorite(boolean isFavorite) {
        mIsFavorite = isFavorite;
    }

    @Override
    public boolean isMarkedAsOffline() {
        return mIsOffline;
    }

    public void setMarkedAsOffline(boolean isOffline) {
        mIsOffline = isOffline;
    }

    @Override
    public boolean isCached() {
        return mIsCached;
    }

    public void setCached(boolean isCached) {
        mIsCached = isCached;
    }

    @Override
    public boolean isNewCreated() {
        return mIsNewCreated;
    }

    public void setNewCreated(boolean mIsNewCreated) {
        this.mIsNewCreated = mIsNewCreated;
    }

    public boolean isSameLocalPath(INxFile file) {
        return TextUtils.equals(this.mLocalPath, file.getLocalPath());
    }

    @Override
    public String getUserDefinedStr() {
        return mUserDefinedStr;
    }

    public void setUserDefinedStr(String data) {
        if (data == null || data.isEmpty()) {
            mUserDefinedStr = "";
        } else {
            mUserDefinedStr = data;
        }
    }


    @Override
    public String toString() {
        String refreshStr = "[LastRefreshed: " + SimpleDateFormat.getDateTimeInstance().format(new Date(lastRefreshed)) + "] ";
        String modifiedStr = "[LastModified: " + SimpleDateFormat.getDateTimeInstance().format(new Date(mLastModifiedTimeLong)) + "] ";

        StringBuilder sb = new StringBuilder()
                .append("{")
                .append("[Name: " + mName + "] ")
                .append("[LocalPath: " + mLocalPath + "] ")
                .append("[CloudPath: " + mCloudPath + "] ")
                .append("[CloudPathID: " + mCloudPathID + "] ")
                .append(refreshStr)
                .append(modifiedStr)
                .append("[Size: " + mSize + "] ")
                .append("[Folder: " + mIsFolder + "] ")
                .append("[Site: " + mIsSite + "] ")
                .append("[Favorite: " + mIsFavorite + "] ")
                .append("[Offline: " + mIsOffline + "] ")
                .append("[Cached: " + mIsCached + "] ")
                .append("[NewCreated: " + mIsNewCreated + "] ")
                .append("}");
        return sb.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NxFileBase that = (NxFileBase) o;
        return mSize == that.mSize &&
                mLastModifiedTimeLong == that.mLastModifiedTimeLong &&
                mIsFolder == that.mIsFolder &&
                mIsSite == that.mIsSite &&
                mIsFavorite == that.mIsFavorite &&
                mIsOffline == that.mIsOffline &&
                mIsCached == that.mIsCached &&
                lastRefreshed == that.lastRefreshed &&
                Objects.equals(mName, that.mName) &&
                Objects.equals(mDisplayPath, that.mDisplayPath) &&
                Objects.equals(mLocalPath, that.mLocalPath) &&
                Objects.equals(mCloudPath, that.mCloudPath) &&
                Objects.equals(mCloudPathID, that.mCloudPathID) &&
                Objects.equals(mChildren, that.mChildren) &&
                Objects.equals(mUserDefinedStr, that.mUserDefinedStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mName, mDisplayPath, mLocalPath, mCloudPath, mCloudPathID, mSize,
                mLastModifiedTimeLong, mIsFolder, mIsSite,
                mChildren, mIsFavorite, mIsOffline, mIsCached, lastRefreshed, mUserDefinedStr);
    }

    @Override
    public BoundService getBoundService() {
        return mService;
    }

    @Override
    public String getSortableName() {
        return mName;
    }

    @Override
    public long getSortableSize() {
        return mSize;
    }

    @Override
    public long getSortableTime() {
        return mLastModifiedTimeLong;
    }

    @Override
    public long getLastModifiedTime() {
        return mLastModifiedTimeLong;
    }

    @Override
    public boolean isFavorite() {
        return mIsFavorite;
    }

    @Override
    public boolean isOffline() {
        return mIsOffline;
    }

    @Override
    public int getOperationStatus() {
        return -1;
    }


}
