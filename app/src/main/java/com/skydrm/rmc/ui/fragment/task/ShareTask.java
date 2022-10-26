package com.skydrm.rmc.ui.fragment.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultRepo;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.NotGrantedShareRights;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class ShareTask extends LoadTask<Void, Boolean> {
    private File mWorkingFile;
    private Rights mRights;
    private Obligations mObligations;
    private Expiry mExpiry;
    private List<String> mEmails;
    private String mComments;
    private ITaskCallback<Result, Exception> mCallback;

    private Exception mExp;

    public ShareTask(File workingFile,
                     Rights rights, Obligations obligations, Expiry expiry,
                     List<String> emails, String comments,
                     ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mWorkingFile = workingFile;
        this.mRights = rights;
        this.mObligations = obligations;
        this.mExpiry = expiry;
        this.mEmails = emails;
        this.mComments = comments;
        this.mCallback = callback;
    }

    public ShareTask(File workingFile,
                     List<String> emails, String comments,
                     ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mWorkingFile = workingFile;
        this.mEmails = emails;
        this.mComments = comments;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onTaskPreExecute();
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        MyVaultRepo repo = (MyVaultRepo) RepoFactory.getRepo(RepoType.TYPE_MYVAULT);

        String path = mWorkingFile.getPath();
        if (RenderHelper.isNxlFile(path)) {
            try {
                return repo.shareLocalFile(path, false, mEmails, mComments);
            } catch (NotGrantedShareRights e) {
                mExp = e;
            } catch (RmsRestAPIException e) {
                mExp = e;
            } catch (NotNxlFileException e) {
                mExp = e;
            } catch (TokenAccessDenyException e) {
                mExp = e;
            }
        } else {
            String filePathId = "Local:" + mWorkingFile.getName();
            try {
                return repo.shareLocalFile(path, filePathId, filePathId,
                        mRights, mObligations, mExpiry,
                        mEmails, mComments,
                        false);
            } catch (FileNotFoundException e) {
                mExp = e;
            } catch (RmsRestAPIException e) {
                mExp = e;
            } catch (Exception e) {
                mExp = e;
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result());
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mExp == null ? new Exception("Unknown error") : mExp);
            }
        }
    }

    public class Result implements IResult {

    }
}
