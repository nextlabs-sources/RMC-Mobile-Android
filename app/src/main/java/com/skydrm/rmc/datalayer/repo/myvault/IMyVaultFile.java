package com.skydrm.rmc.datalayer.repo.myvault;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;
import com.skydrm.sdk.rms.rest.myVault.UpdateRecipientsResult;

import java.io.FileNotFoundException;
import java.util.List;

public interface IMyVaultFile {
    String getRepoId();

    long getSharedOn();

    List<String> getSharedWith();

    List<String> getRights();

    boolean isRevoked();

    boolean isDeleted();

    boolean isShared();

    String getSourceRepoType();

    String getSourceFilePathDisplay();

    String getSourceFilePathId();

    String getSourceRepoName();

    String getSourceRepoId();

    MyVaultMetaDataResult getMetadata()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    boolean share(List<String> rights, List<String> emails, String comments)
            throws FileNotFoundException, RmsRestAPIException;

    UpdateRecipientsResult updateRecipients(List<String> added, List<String> removed, String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    boolean revokeRights() throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException;
}
