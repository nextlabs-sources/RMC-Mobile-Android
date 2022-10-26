package com.skydrm.rmc.ui.activity.server.cache;

import android.support.annotation.NonNull;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.UserAccount;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class AccountCache {
    private static final String CACHE_NAME = "USER_ACCOUNT.CACHE";
    private File mUserAccountCachedSerializeFile;

    public AccountCache() {
        try {
            File rootFile = SkyDRMApp.getInstance().getCommonDirs().appRootFile();
            mUserAccountCachedSerializeFile = new File(rootFile, CACHE_NAME);
            if (!mUserAccountCachedSerializeFile.exists()) {
                mUserAccountCachedSerializeFile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeCache(@NonNull final List<UserAccount> caches) {
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI).execute(new Runnable() {
            @Override
            public void run() {
                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(new FileOutputStream(mUserAccountCachedSerializeFile));
                    oos.writeObject(caches);
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (oos != null) {
                        try {
                            oos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void readCache(ICacheCallback<UserAccount> callback) {
        if (mUserAccountCachedSerializeFile == null) {
            return;
        }
        AccountCacheWriteTask writeTask = new AccountCacheWriteTask(mUserAccountCachedSerializeFile, callback);
        writeTask.run();
    }
}
