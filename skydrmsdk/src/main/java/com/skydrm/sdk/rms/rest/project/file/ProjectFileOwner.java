package com.skydrm.sdk.rms.rest.project.file;

import android.os.Parcel;
import android.os.Parcelable;

public class ProjectFileOwner implements Parcelable{
        /**
         * userId : 1
         * displayName : rupali.choudhury@nextlabs.com
         * email : rupali.choudhury@nextlabs.com
         */

        private int userId;
        private String displayName;
        private String email;



    public ProjectFileOwner() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectFileOwner that = (ProjectFileOwner) o;

        if (userId != that.userId) return false;
        if (!displayName.equals(that.displayName)) return false;
        return email.equals(that.email);

    }

    @Override
    public String toString() {
        return "ProjectFileOwner{" +
                "userId=" + userId +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        int result = userId;
        result = 31 * result + displayName.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }

    public ProjectFileOwner(int userId, String displayName, String email) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
    }

    public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public ProjectFileOwner copy(){
               return new ProjectFileOwner(
                        this.userId,
                        this.displayName,
                        this.email
                );
        }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeString(this.displayName);
        dest.writeString(this.email);
    }

    protected ProjectFileOwner(Parcel in) {
        this.userId = in.readInt();
        this.displayName = in.readString();
        this.email = in.readString();
    }

    public static final Creator<ProjectFileOwner> CREATOR = new Creator<ProjectFileOwner>() {
        @Override
        public ProjectFileOwner createFromParcel(Parcel source) {
            return new ProjectFileOwner(source);
        }

        @Override
        public ProjectFileOwner[] newArray(int size) {
            return new ProjectFileOwner[size];
        }
    };
}
