package com.skydrm.rmc.ui.activity.server.cache;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.domain.UserAccount;
import com.skydrm.rmc.ui.base.LoadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;

class AccountCacheWriteTask extends LoadTask<Void, List<UserAccount>> {
    private File mUserAccountCachedSerializeFile;
    private ICacheCallback<UserAccount> mCacheCallback;

    AccountCacheWriteTask(File cacheFile, ICacheCallback<UserAccount> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.REGULAR_BACK_GROUND));
        this.mUserAccountCachedSerializeFile = cacheFile;
        this.mCacheCallback = callback;
    }

    @Override
    protected List<UserAccount> doInBackground(Void... voids) {
        ObjectInputStream ois = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(mUserAccountCachedSerializeFile);
            if (fis.available() != 0) {
                ois = new ObjectInputStream(new FileInputStream(mUserAccountCachedSerializeFile));
                return (List<UserAccount>) ois.readObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    protected void onPostExecute(List<UserAccount> accounts) {
        super.onPostExecute(accounts);
        if (accounts != null) {
            if (mCacheCallback != null) {
                mCacheCallback.onCacheLoad(accounts);
            }
        }
    }
}
