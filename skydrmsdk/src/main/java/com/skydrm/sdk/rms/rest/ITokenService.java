package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.util.Map;

public interface ITokenService {
    /**
     * Retrive 100 <duid,token> pairs by
     */
    @Deprecated
    Map<String, String> getEncryptionToken(byte[] dhAgreementKey) throws Exception;

    @Deprecated
    Map<String, String> getEncryptionToken(byte[] dhAgreementKey, int count) throws RmsRestAPIException;

    Map<String, String> getEncryptionToken(byte[] dhAgreementKey, String projectMembershipId, int count) throws RmsRestAPIException;


    String getDecryptionToken(String tenant, INxlFileFingerPrint nxlFingerPrint)
            throws RmsRestAPIException, TokenAccessDenyException;

    /**
     * @param tenant
     * @param fingerPrint
     * @param sharedSpaceType           Similar to fromSpace, it takes values 0,1,2 [ 0-MySpace, 1-Project Space, 2-Workspace which is not implemented yet ]
     * @param sharedSpaceId             In the case of sharedSpaceType =1, sharedSpaceId is the projectId to which file is shared to i.e. target project id
     * @param sharedSpaceUserMembership User membership for the project as specified in sharedSpaceId , will be used only for Central Policy File. i.e. user membership at the target project
     * @return
     * @throws RmsRestAPIException
     * @throws TokenAccessDenyException
     */
    String getDecryptionToken(String tenant, INxlFileFingerPrint fingerPrint,
                              int sharedSpaceType, int sharedSpaceId,
                              String sharedSpaceUserMembership)
            throws RmsRestAPIException, TokenAccessDenyException;

    /**
     * @param duid           represents target nxl file.
     * @param otp            prefetched token protected file should pass otp to active token matches duid in rms.
     * @param sectionRaw     either filePolicy or fileTags,
     *                       if protectionType passed 0 then sectionRaw should pass filePolicy which represents ADHoc file;
     *                       if protectionType passed 1 then sectionRaw should pass fileTags which represents Central policy file.
     * @param protectionType either 0[ADHco] or 1[Central policy].
     * @param ml             maintenanceLevel
     * @return success response json string.
     * @throws RmsRestAPIException
     */
    String updateNXLMetadata(String duid, String otp, String sectionRaw, int protectionType, int ml) throws RmsRestAPIException;
}
