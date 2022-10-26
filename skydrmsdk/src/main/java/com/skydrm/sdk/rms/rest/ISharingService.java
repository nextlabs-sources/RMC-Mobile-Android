package com.skydrm.sdk.rms.rest;

import android.support.annotation.Nullable;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.myVault.UpdateRecipientsResult;
import com.skydrm.sdk.rms.types.SharingLocalFilePara;
import com.skydrm.sdk.rms.types.SharingRepoFileParas;
import com.skydrm.sdk.rms.types.SharingRepoFileResult;
import com.skydrm.sdk.rms.types.UpdateProjectRecipientsResult;

import java.io.File;
import java.util.List;

public interface ISharingService {
    boolean share(String duid, String encryptionToken, String fileName,
                  String deviceId, String deviceType, int rights,
                  List<String> emails, long expireMillis) throws RmsRestAPIException;

    /**
     * This API is used to share a local nxl file; the rms will immediately send emails to recipients if
     * the request is successful.
     *
     * @return duid
     */
    String share(File nxlFile, String tags, boolean bAsAttachment,
                 String deviceId, String deviceType, int rights,
                 List<String> emails, long expireMillis) throws RmsRestAPIException;

    /**
     * This API is used to share a local nxl file; the rms will immediately send emails to recipients if
     * the request is successful.
     *
     * @param file
     * @param tags: the style is like: tagName1=tagValue1|tagName2=tagValue2 (the value can is single or multiple, can split with comma when multiple.)
     *              Classification=ITAR|Clearance=Confidiential,Top Secret
     * @return duid
     */
    String share2(File file, String tags, boolean bAsAttachment,
                  int rights, List<String> emails, long expireMillis) throws RmsRestAPIException;

    /**
     * This API used to share a local file
     *
     * @param paras {@link SharingLocalFilePara}, the request parameters
     * @return String duid
     * @throws RmsRestAPIException
     */
    String sharingLocalFile(SharingLocalFilePara paras) throws RmsRestAPIException;

    /**
     * This API used to share a repository file
     *
     * @param paras {@link SharingRepoFileParas}, the request parameters
     * @return {@link SharingRepoFileResult}
     * @throws RmsRestAPIException
     */
    SharingRepoFileResult sharingRepoFile(final SharingRepoFileParas paras) throws RmsRestAPIException;

    /**
     * This API is used to update(remove or add) recipients from a shared document.
     *
     * @param duid
     * @param newRecipientList:    the new added recipients
     * @param removeRecipientList: the removed recipients
     */
    UpdateRecipientsResult updateRecipients(String duid,
                                            @Nullable List<String> newRecipientList,
                                            @Nullable List<String> removeRecipientList,
                                            @Nullable String comments) throws RmsRestAPIException;

    UpdateProjectRecipientsResult updateProjectRecipients(String duid,
                                                   List<Integer> newRecipients,
                                                   List<Integer> removedRecipients,
                                                   String comments) throws RmsRestAPIException;

    /**
     * This API is used to revoke a shared document.
     * note:  Only the document owner/steward can revoke the document.
     */
    boolean revokingDocument(String duid) throws RmsRestAPIException;
}
