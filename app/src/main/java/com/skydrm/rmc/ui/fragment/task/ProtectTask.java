package com.skydrm.rmc.ui.fragment.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.IBaseRepo;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultRepo;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;

import java.io.FileNotFoundException;

public class ProtectTask extends LoadTask<Void, Boolean> {
    private String mPlainPath;
    private Rights mRights;
    private Obligations mObligations;
    private Expiry mExpiry;
    private INxFile mClickItem;
    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public ProtectTask(String plainPath,
                       Rights rights, Obligations obligations, Expiry expiry,
                       INxFile clickItem,
                       ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mPlainPath = plainPath;
        this.mRights = rights;
        this.mObligations = obligations;
        this.mExpiry = expiry;
        this.mClickItem = clickItem;
        this.mCallback = callback;
        mExp = new Exception("Unknown error");
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
        IBaseRepo repo = RepoFactory.getRepo(RepoType.TYPE_MYVAULT);
        if (repo instanceof MyVaultRepo) {
            MyVaultRepo vaultRepo = (MyVaultRepo) repo;
            try {
                return vaultRepo.protectFile(mPlainPath,
                        mRights, mObligations, mExpiry,
                        mClickItem);
            } catch (InvalidRMClientException e) {
                mExp = e;
            } catch (FileNotFoundException e) {
                mExp = e;
            } catch (RmsRestAPIException e) {
                mExp = e;
            } catch (SessionInvalidException e) {
                mExp = e;
            } catch (NotNxlFileException e) {
                mExp = e;
            } catch (TokenAccessDenyException e) {
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
                mCallback.onTaskExecuteFailed(mExp);
            }
        }
    }

    public class Result implements IResult {

    }
}
