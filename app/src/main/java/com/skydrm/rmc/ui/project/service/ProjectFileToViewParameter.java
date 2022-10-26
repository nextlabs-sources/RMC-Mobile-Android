package com.skydrm.rmc.ui.project.service;


import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.datalayer.repo.project.ProjectFile;
import com.skydrm.sdk.rms.rest.project.file.ProjectFileOwner;

public class ProjectFileToViewParameter implements Parcelable {

    public String id;
    public String duid;
    public String pathDisplay;
    public String pathId;
    public String name;
    public long lastModified;
    public long creationTime;
    public long size;
    public boolean folder;
    public ProjectFileOwner owner;

    public ProjectFileToViewParameter() {
    }


    public static ProjectFileToViewParameter classConvert(ProjectFile f) {
        ProjectFileToViewParameter parameter = new ProjectFileToViewParameter();
        parameter.setId(f.getId());
        parameter.setDuid(f.getDuid());
        parameter.setPathDisplay(f.getPathDisplay());
        parameter.setPathId(f.getPathId());
        parameter.setName(f.getName());
        parameter.setLastModified(f.getLastModifiedTime());
        parameter.setCreationTime(f.getCreationTime());
        parameter.setSize(f.getFileSize());
        parameter.setFolder(f.isFolder());
        ProjectFileOwner projectFileOwner = new ProjectFileOwner();
        projectFileOwner.setUserId(f.getOwner().getUserId());
        projectFileOwner.setDisplayName(f.getOwner().getName());
        projectFileOwner.setEmail(f.getOwner().getEmail());
        parameter.setOwner(projectFileOwner);
        return parameter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDuid() {
        return duid;
    }

    public void setDuid(String duid) {
        this.duid = duid;
    }

    public String getPathDisplay() {
        return pathDisplay;
    }

    public void setPathDisplay(String pathDisplay) {
        this.pathDisplay = pathDisplay;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public ProjectFileOwner getOwner() {
        return owner;
    }

    public void setOwner(ProjectFileOwner owner) {
        this.owner = owner;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.duid);
        dest.writeString(this.pathDisplay);
        dest.writeString(this.pathId);
        dest.writeString(this.name);
        dest.writeLong(this.lastModified);
        dest.writeLong(this.creationTime);
        dest.writeLong(this.size);
        dest.writeByte(this.folder ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.owner, flags);
    }

    protected ProjectFileToViewParameter(Parcel in) {
        this.id = in.readString();
        this.duid = in.readString();
        this.pathDisplay = in.readString();
        this.pathId = in.readString();
        this.name = in.readString();
        this.lastModified = in.readLong();
        this.creationTime = in.readLong();
        this.size = in.readLong();
        this.folder = in.readByte() != 0;
        this.owner = in.readParcelable(ProjectFileOwner.class.getClassLoader());
    }

    public static final Creator<ProjectFileToViewParameter> CREATOR = new Creator<ProjectFileToViewParameter>() {
        @Override
        public ProjectFileToViewParameter createFromParcel(Parcel source) {
            return new ProjectFileToViewParameter(source);
        }

        @Override
        public ProjectFileToViewParameter[] newArray(int size) {
            return new ProjectFileToViewParameter[size];
        }
    };
}


