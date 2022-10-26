package com.skydrm.rmc.reposystem;



public class RepoInfo extends RemoteRepoInfo {
    public long localOfflineSize = 0;
    public long localCachedSize = 0;
    public long localTotalSize = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RepoInfo repoInfo = (RepoInfo) o;

        if (localOfflineSize != repoInfo.localOfflineSize) return false;
        if (localCachedSize != repoInfo.localCachedSize) return false;
        return localTotalSize == repoInfo.localTotalSize;

    }

    @Override
    public int hashCode() {
        int result = (int) (localOfflineSize ^ (localOfflineSize >>> 32));
        result = 31 * result + (int) (localCachedSize ^ (localCachedSize >>> 32));
        result = 31 * result + (int) (localTotalSize ^ (localTotalSize >>> 32));
        return result;
    }
}
