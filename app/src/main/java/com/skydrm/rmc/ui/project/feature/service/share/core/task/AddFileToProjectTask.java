package com.skydrm.rmc.ui.project.feature.service.share.core.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.LogSystem;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.utils.FileHelper;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RightsExpiredException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class AddFileToProjectTask extends LoadTask<Void, Boolean> {
    private String mNxlPath;

    private String mMembershipId;
    private Map<String, Set<String>> mTags;

    private int mProjectId;
    private String mParentPathId;

    private ITaskCallback<Result, MarkException> mCallback;
    private MarkException mExp;

    public AddFileToProjectTask(String nxlPath,
                                String membershipId, Map<String, Set<String>> tags,
                                int projectId, String parentPathId,
                                ITaskCallback<Result, MarkException> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mNxlPath = nxlPath;
        this.mMembershipId = membershipId;
        this.mTags = tags;

        this.mProjectId = projectId;
        this.mParentPathId = parentPathId;

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
        // 1. try get the plain file.
        String decryptedPath = "";
        try {
            decryptedPath = decryptFile(mNxlPath);
        } catch (MarkException e) {
            mExp = e;
            releaseTmpFile(decryptedPath);
        }
        if (decryptedPath == null || decryptedPath.isEmpty()) {
            return false;
        }
        File tmpFile = new File(decryptedPath);
        if (!tmpFile.exists() || tmpFile.isDirectory()) {
            releaseTmpFile(tmpFile.getPath());
            return false;
        }
        // 2. encrypt target into nxl file.
        String encryptedPath = "";
        try {
            encryptedPath = encryptFile(mMembershipId, tmpFile, mTags);
        } catch (MarkException e) {
            releaseTmpFile(encryptedPath);
            mExp = e;
        }
        if (encryptedPath == null || encryptedPath.isEmpty()) {
            releaseTmpFile(tmpFile.getPath());
            return false;
        }
        // the new encrypted file.
        File ef = new File(encryptedPath);
        if (!ef.exists() || ef.isDirectory()) {
            releaseTmpFile(ef.getPath());
            return false;
        }

        releaseTmpFile(tmpFile.getPath());
        // 3. upload the newly protected file to rms.

        try {
            boolean uploadResult = uploadFile(ef, mProjectId, mParentPathId);
            if (uploadResult) {
                return sendLog(mNxlPath, ef);
            }
            return false;
        } catch (MarkException e) {
            mExp = e;
        } finally {
            releaseTmpFile(ef.getPath());
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

    private String decryptFile(String nxlPath) throws MarkException {
        if (nxlPath == null || nxlPath.isEmpty()) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_COMMON, "nxlPath must not be null");
        }
        String plainPath = generateTmpDecryptPathByNXLPath(nxlPath);
        if (plainPath.isEmpty()) {
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
                    .decryptFromNxl(nxlPath, plainPath, true,false);

        } catch (NotNxlFileException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_DECRYPT, e.getMessage(), e);
        } catch (TokenAccessDenyException e) {
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

    private String encryptFile(String membershipId, File targetFile, Map<String, Set<String>> tags)
            throws MarkException {
        try {
            return FileOperation.protectFile(targetFile, membershipId, tags);
        } catch (FileNotFoundException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_ENCRYPT, e.getMessage(), e);
        } catch (RmsRestAPIException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_ENCRYPT, e.getMessage(), e);
        }
    }

    private boolean uploadFile(File file, int projectId, String parentPathId) throws MarkException {
        try {
            return FileOperation.uploadProjectFile(projectId, file, parentPathId, null) != null;
        } catch (SessionInvalidException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_UPLOAD, e.getMessage(), e);
        } catch (InvalidRMClientException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_UPLOAD, e.getMessage(), e);
        } catch (RmsRestAPIException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_UPLOAD, e.getMessage(), e);
        }
    }

    private boolean sendLog(String sharedNxlPath, File newProtectedNxlFile) throws MarkException {
        return sendShareLog(sharedNxlPath) & sendProtectLog(newProtectedNxlFile);
    }

    private boolean sendProtectLog(File nxlFile) throws MarkException {
        try {
            if (nxlFile == null || nxlFile.isDirectory()) {
                return false;
            }
            if (!nxlFile.exists()) {
                return false;
            }
            return LogSystem.sendProtectLog(nxlFile);
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

    private boolean sendShareLog(String nxlFilePath) throws MarkException {
        if (nxlFilePath == null || nxlFilePath.isEmpty()) {
            return false;
        }
        File sharedTarget = new File(nxlFilePath);
        if (sharedTarget.isDirectory()) {
            return false;
        }
        if (!sharedTarget.exists()) {
            return false;
        }
        try {
            return LogSystem.sendShareLog(sharedTarget);
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

    private void releaseTmpFile(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        FileHelper.delFile(path);
    }

    public class Result implements IResult {

    }
}
