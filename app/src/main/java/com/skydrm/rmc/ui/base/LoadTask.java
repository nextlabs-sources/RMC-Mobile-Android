package com.skydrm.rmc.ui.base;

import android.os.AsyncTask;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/**
 * Created by hhu on 4/28/2018.
 */

public abstract class LoadTask<Process, Result> extends AsyncTask<Void, Process, Result> implements Runnable {
    protected static final DevLog log = new DevLog(LoadTask.class.getSimpleName());
    private Executor mExecutor;

    public LoadTask() {
        this(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.NETWORK_TASK));
    }

    public LoadTask(Executor executor) {
        this.mExecutor = executor;
    }

    @Override
    public void run() {
        this.executeOnExecutor(mExecutor);
    }

    protected void checkNetwork() throws RmsRestAPIException {
        if (!isNetworkConnected()) {
            throw new RmsRestAPIException("Network is required to perform this action.",
                    RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }
    }

    protected boolean isNetworkConnected() {
        return SkyDRMApp.getInstance().isNetworkAvailable();
    }

    protected <T> T checkNoNull(WeakReference<T> contextWeakReference) {
        if (contextWeakReference == null) {
            throw new IllegalArgumentException("WeakReference cannot ne null.");
        }
        T t = contextWeakReference.get();
        if (t == null) {
            throw new IllegalArgumentException("content in WeakReference is nullable.");
        }
        return t;
    }

    public interface ITaskCallback<Result extends IResult, Reason> {
        void onTaskPreExecute();

        void onTaskExecuteSuccess(Result results);

        void onTaskExecuteFailed(Reason e);
    }

    public interface IResult {

    }
}
