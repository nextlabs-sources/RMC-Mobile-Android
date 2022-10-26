package com.skydrm.rmc.ui.project.feature.service.share.core.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultRepo;
import com.skydrm.rmc.engine.LogSystem;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RightsExpiredException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ShareToPersonTask extends LoadTask<Void, Boolean> {
    private String mNxlPath;
    private Rights mRights;
    private Obligations mObligations;
    private Expiry mExpiry;
    private List<String> mEmails;
    private String mComments;

    private String mFilePathId;
    private String mFilePath;
    private Exception mExp;
    private ITaskCallback<Result, Exception> mCallback;

    public ShareToPersonTask(String nxlpath, String filePathId, String filePath,
                             Rights rights, Obligations obligations, Expiry expiry,
                             List<String> emails, String cmts,
                             ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mNxlPath = nxlpath;
        this.mFilePathId = filePathId;
        this.mFilePath = filePath;

        this.mRights = rights;
        this.mObligations = obligations;
        this.mExpiry = expiry;
        this.mEmails = emails;
        this.mComments = cmts;

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
        String decryptedPath = "";
        try {
            decryptedPath = decryptFile(mNxlPath);
        } catch (MarkException e) {
            mExp = e;
        } catch (TokenAccessDenyException e) {
            mExp = new TokenAccessDenyException(e.getMessage(), e,
                    TokenAccessDenyException.TYPE_TOKEN_DENY_PROJECT_SHARE_TO_PERSON);
        }
        if (decryptedPath == null || decryptedPath.isEmpty()) {
            return false;
        }

        MyVaultRepo repo = (MyVaultRepo) RepoFactory.getRepo(RepoType.TYPE_MYVAULT);
        try {
            boolean shareResult = repo.shareLocalFile(decryptedPath, mFilePathId, mFilePath,
                    mRights, mObligations, mExpiry,
                    mEmails, mComments,
                    false);
            if (shareResult) {
                return sendShareLog(mNxlPath);
            }
            return false;
        } catch (FileNotFoundException e) {
            mExp = e;
        } catch (RmsRestAPIException e) {
            mExp = e;
        } catch (Exception e) {
            mExp = e;
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
                mCallback.onTaskExecuteFailed(mExp == null ? new MarkException(MarkerStatus.STATUS_FAILED_COMMON, "Unknown error.") : mExp);
            }
        }
    }

    private String decryptFile(String nxlPath) throws MarkException, TokenAccessDenyException {
        if (nxlPath == null || nxlPath.isEmpty()) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_COMMON, "nxlPath must not be null");
        }
        String plainPath = generateTmpDecryptPathByNXLPath(nxlPath);
        if (plainPath == null || plainPath.isEmpty()) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_COMMON, "Generate tmp decrypt path failed.");
        }

        //Should make sure normal file exists.
        File tmpF = new File(plainPath);
        if (!tmpF.exists() || !tmpF.isFile()) {
            try {
                tmpF.createNewFile();
            } catch (IOException e) {
                throw new MarkException(MarkerStatus.STATUS_FAILED_IO_EXCEPTION, "Failed  to create tmp normal file");
            }
        }

        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        INxlFileFingerPrint fPrint = null;
        try {
            fPrint = session.getRmsClient()
                    .decryptFromNxl(nxlPath, plainPath, true, false);

        } catch (NotNxlFileException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_DECRYPT, e.getMessage(), e);
        } catch (RightsExpiredException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_DECRYPT, e.getMessage(), e);
        } catch (RmsRestAPIException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_DECRYPT, e.getMessage(), e);
        }
        if (fPrint == null) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_DECRYPT, "Try to decrypt file failed.");
        }
        return plainPath;
    }

    private String generateTmpDecryptPathByNXLPath(String nxlpath) throws MarkException {
        if (nxlpath == null || nxlpath.isEmpty()) {
            return "";
        }
        StringBuilder fullPath = new StringBuilder();

        String rootPath = nxlpath.substring(0, nxlpath.lastIndexOf("/") + 1); //should contains character "/"
        fullPath.append(rootPath);

        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();

        try {
            INxlFileFingerPrint fp = session.getRmsClient().extractFingerPrint(nxlpath);
            fullPath.append(fp.getNormalFileName());

            return fullPath.toString();
        } catch (FileNotFoundException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_DECRYPT, e.getMessage(), e);
        } catch (NotNxlFileException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_DECRYPT, e.getMessage(), e);
        } catch (TokenAccessDenyException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_DECRYPT, e.getMessage(), e);
        } catch (RmsRestAPIException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_DECRYPT, e.getMessage(), e);
        }
    }

    private boolean sendShareLog(String nxlPath) throws MarkException {
        try {
            return LogSystem.sendShareLog(new File(nxlPath));
        } catch (FileNotFoundException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_SEND_LOG, e.getMessage(), e);
        } catch (RmsRestAPIException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_SEND_LOG, e.getMessage(), e);
        } catch (NotNxlFileException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_SEND_LOG, e.getMessage(), e);
        } catch (TokenAccessDenyException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_SEND_LOG, e.getMessage(), e);
        }
    }

    public class Result implements IResult {

    }
}
