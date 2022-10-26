package com.skydrm.rmc.datalayer;

import com.skydrm.rmc.datalayer.repo.base.IBaseRepo;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.library.LibraryRepo;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultRepo;
import com.skydrm.rmc.datalayer.repo.project.ProjectRepo;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeRepo;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceRepo;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepoFactory {
    private static final Map<RepoType, IBaseRepo> repoStore = new ConcurrentHashMap<>();

    public static IBaseRepo getRepo(RepoType type) {
        IBaseRepo ret = null;
        switch (type) {
            case TYPE_MYVAULT:
                if (repoStore.containsKey(type)) {
                    ret = repoStore.get(type);
                } else {
                    ret = new MyVaultRepo();
                    repoStore.put(type, ret);
                }
                break;
            case TYPE_SHARED_WITH_ME:
                if (repoStore.containsKey(type)) {
                    ret = repoStore.get(type);
                } else {
                    ret = new SharedWithMeRepo();
                    repoStore.put(type, ret);
                }
                break;
            case TYPE_PROJECT:
                if (repoStore.containsKey(type)) {
                    ret = repoStore.get(type);
                } else {
                    ret = new ProjectRepo();
                    repoStore.put(type, ret);
                }
                break;
            case TYPE_WORKSPACE:
                if (repoStore.containsKey(type)) {
                    ret = repoStore.get(type);
                } else {
                    ret = new WorkSpaceRepo();
                    repoStore.put(type, ret);
                }
                break;
            case TYPE_LIBRARY:
                if (repoStore.containsKey(type)) {
                    ret = repoStore.get(type);
                } else {
                    ret = new LibraryRepo();
                    repoStore.put(type, ret);
                }
                break;
        }
        return ret;
    }

    public static long getCacheSize() {
        long ret = 0;
        Collection<IBaseRepo> repos = repoStore.values();
        if (repos.size() == 0) {
            return ret;
        }
        for (IBaseRepo repo : repos) {
            if (repo == null) {
                continue;
            }
            ret += repo.getCacheSize();
        }
        return ret;
    }

    public static void clearCache() {
        Collection<IBaseRepo> repos = repoStore.values();
        if (repos.size() == 0) {
            return;
        }
        for (IBaseRepo repo : repos) {
            if (repo == null) {
                continue;
            }
            repo.clearCache();
        }
    }

    public static void updateResetAllOperationStatus() {
        Collection<IBaseRepo> repos = repoStore.values();
        if (repos.size() == 0) {
            return;
        }
        for (IBaseRepo repo : repos) {
            if (repo == null) {
                continue;
            }
            repo.updateResetAllOperationStatus();
        }
    }
}
