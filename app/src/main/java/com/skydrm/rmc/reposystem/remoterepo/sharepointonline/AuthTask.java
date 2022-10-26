package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.skydrm.rmc.ExecutorPools;

import java.security.InvalidParameterException;

class AuthTask extends AsyncTask<Void, Void, Boolean> implements Runnable {
    private SharePointAuthManager.IAuthCallback mCallback;
    private String mSu;
    private String mUn;
    private String mPd;
    private Exception mException;

    AuthTask(SharePointAuthManager.IAuthCallback callback) {
        this.mCallback = callback;
    }

    public void setServerUrl(String url) {
        this.mSu = url;
    }

    public void setUsername(String username) {
        this.mUn = username;
    }

    public void setPassword(String password) {
        this.mPd = password;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (TextUtils.isEmpty(mSu) || TextUtils.isEmpty(mUn) || TextUtils.isEmpty(mPd)) {
            mException = new InvalidParameterException("Invalid params was configured.");
            return false;
        }
        NXSharePointOnPremise onPremise = new NXSharePointOnPremise(mSu, mUn, mPd);
        try {
            return onPremise.tryAuth();
        } catch (Exception e) {
            mException = e;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            mCallback.onSuccess();
        } else {
            mCallback.onFailed(mException == null ? "Authentication failed." : mException.getMessage());
        }
    }

    @Override
    public void run() {
        executeOnExecutor(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.NETWORK_TASK));
    }
}
