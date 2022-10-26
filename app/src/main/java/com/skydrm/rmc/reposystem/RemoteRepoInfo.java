package com.skydrm.rmc.reposystem;

/**
 * As required, remote repo should provide some information about repo's user and repos itself.
 */
public class RemoteRepoInfo {
    public String displayName = "unknown";
    public String email = "unknown";
    public long remoteTotalSpace = 0; // in bytes
    public long remoteUsedSpace = 0; // in bytes

    @Override
    public String toString() {
        return "RemoteRepoInfo{" +
                "displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", remoteTotalSpace=" + remoteTotalSpace +
                ", remoteUsedSpace=" + remoteUsedSpace +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteRepoInfo that = (RemoteRepoInfo) o;

        if (remoteTotalSpace != that.remoteTotalSpace) return false;
        if (remoteUsedSpace != that.remoteUsedSpace) return false;
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null)
            return false;
        return email != null ? email.equals(that.email) : that.email == null;

    }

    @Override
    public int hashCode() {
        int result = displayName != null ? displayName.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (int) (remoteTotalSpace ^ (remoteTotalSpace >>> 32));
        result = 31 * result + (int) (remoteUsedSpace ^ (remoteUsedSpace >>> 32));
        return result;
    }
}
