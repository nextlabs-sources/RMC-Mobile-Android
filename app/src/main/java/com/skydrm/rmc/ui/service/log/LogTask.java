package com.skydrm.rmc.ui.service.log;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.IVaultFileLog;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.VaultFileLogImpl;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.types.FetchLogRequestParas;
import com.skydrm.sdk.rms.types.FetchLogResult;
import com.skydrm.sdk.rms.user.IRmUser;

import java.util.ArrayList;
import java.util.List;

class LogTask extends LoadTask<Void, List<IVaultFileLog>> {
    private String duid;
    private FetchLogRequestParas mRequestParams;
    private ILoadCallback<List<IVaultFileLog>, LogException> mCallback;
    private LogException mLogException = new LogException("unknown error.");
    private int totalCount;

    LogTask(@NonNull String duid, @Nullable FetchLogRequestParas paras, @NonNull ILoadCallback<List<IVaultFileLog>, LogException> callback) {
        this.duid = duid;
        this.mRequestParams = paras == null ? new FetchLogRequestParas() : paras;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<IVaultFileLog> doInBackground(Void... voids) {
        try {
            checkNetwork();
            IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            FetchLogResult fetchLogResult = SkyDRMApp.getInstance()
                    .getSession()
                    .getRmsRestAPI()
                    .getLogService(rmUser)
                    .fetchActivityLog(duid, mRequestParams);
            List<FetchLogResult.ResultsBean.DataBean.LogRecordsBean> logRecords = fetchLogResult.getResults().getData().getLogRecords();
            List<IVaultFileLog> vaultFileLogs = new ArrayList<>();
            totalCount = fetchLogResult.getResults().getTotalCount();
            if (logRecords != null && logRecords.size() != 0) {
                for (FetchLogResult.ResultsBean.DataBean.LogRecordsBean logRecordsBean : logRecords) {
                    VaultFileLogImpl vaultFileLogImpl = new VaultFileLogImpl();
                    vaultFileLogImpl.setEmail(logRecordsBean.getEmail());
                    vaultFileLogImpl.setOperation(logRecordsBean.getOperation());
                    vaultFileLogImpl.setDeviceType(logRecordsBean.getDeviceType());
                    vaultFileLogImpl.setDeviceId(logRecordsBean.getDeviceId());
                    vaultFileLogImpl.setAccessTime(logRecordsBean.getAccessTime());
                    vaultFileLogImpl.setAccessResult(logRecordsBean.getAccessResult());
                    vaultFileLogs.add(vaultFileLogImpl);
                }
            }
            return vaultFileLogs;
        } catch (RmsRestAPIException e) {
            mLogException = new LogException(e.getMessage(), LogException.EXCEPTION_RMS_REST_API, e);
        } catch (SessionInvalidException e) {
            mLogException = new LogException(e.getMessage(), LogException.EXCEPTION_SESSION_INVALID, e);
        } catch (InvalidRMClientException e) {
            mLogException = new LogException(e.getMessage(), LogException.EXCEPTION_RMC_CLIENT_INVALID, e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<IVaultFileLog> results) {
        super.onPostExecute(results);
        if (results != null) {
            mCallback.onResult(results, totalCount);
        } else {
            mCallback.onError(mLogException);
        }
    }
}
