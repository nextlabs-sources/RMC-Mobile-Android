package com.skydrm.rmc.datalayer.repo.base;

import android.os.Parcel;
import android.text.TextUtils;

public abstract class NxlFileBase implements INxlFile {
    protected String mName;
    protected String mPathId;
    protected String mPathDisplay;
    protected long mLastModifiedTime;
    protected long mCreationTime;

    NxlFileBase(Parcel in) {
        this.mName = in.readString();
        this.mPathId = in.readString();
        this.mPathDisplay = in.readString();
        this.mLastModifiedTime = in.readLong();
        this.mCreationTime = in.readLong();
    }

    NxlFileBase(String name, String pathId, String pathDisplay,
                long lastModifiedTime, long creationTime) {
        this.mName = name;
        this.mPathId = pathId;
        this.mPathDisplay = pathDisplay;
        this.mLastModifiedTime = lastModifiedTime;
        this.mCreationTime = creationTime;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getPathId() {
        return mPathId;
    }

    @Override
    public String getPathDisplay() {
        return mPathDisplay;
    }

    @Override
    public long getLastModifiedTime() {
        return mLastModifiedTime;
    }

    @Override
    public long getCreationTime() {
        return mCreationTime;
    }

    @Override
    public String getParent() {
        if (TextUtils.isEmpty(mPathId)) {
            return "/";
        }
        if (mPathId.equals("/")) {
            return "/";
        }
        if (mPathId.endsWith("/")) {//folder /a/
            String one = mPathId.substring(0, mPathId.lastIndexOf("/"));
            return one.substring(0, one.lastIndexOf("/") + 1);
        } else { //file /a/b.txt
            return mPathId.substring(0, mPathId.lastIndexOf("/") + 1);
        }
    }
}
