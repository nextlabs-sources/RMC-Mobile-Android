package com.skydrm.sdk;

import android.content.Context;
import android.support.annotation.Nullable;

import com.skydrm.sdk.exception.NotGrantedShareRights;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RightsExpiredException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.nxl.token.TokenException;
import com.skydrm.sdk.policy.AdhocPolicy;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Policy;
import com.skydrm.sdk.rms.user.IRmUser;

import java.io.FileNotFoundException;

public interface INxlClient {
    IRmUser getUser();

    INxlTenant getTenant();

    boolean isSessionExpired();

    boolean signOut();

    boolean isNxlFile(String path, boolean fast);

    boolean encryptToNxl(String srcPath, String nxlPath, Policy policy, boolean overwrite)
            throws FileNotFoundException, RmsRestAPIException;

    INxlFileFingerPrint decryptFromNxl(String nxlPath, String plainPath, boolean overwrite, boolean allowOfflineLoad)
            throws NotNxlFileException, TokenAccessDenyException, RightsExpiredException, RmsRestAPIException;

    INxlFileFingerPrint decryptFromNxl(String nxlPath, String plainPath,
                                       int sharedSpaceType, int sharedSpaceId, String sharedSpaceUserMembership,
                                       boolean overwrite, boolean allowOfflineLoad)
            throws NotNxlFileException, TokenAccessDenyException, RightsExpiredException, RmsRestAPIException;

    INxlFileFingerPrint extractFingerPrint(String nxlPath)
            throws FileNotFoundException, NotNxlFileException, TokenAccessDenyException, RmsRestAPIException;

    void setSecurityCtx(Context ctx);

    boolean updateOfflineStatus(String nxlPath, boolean active) throws FileNotFoundException,
            RmsRestAPIException, NotNxlFileException, TokenAccessDenyException, TokenException;

    boolean updateOfflineStatus(String nxlPath,
                                int sharedSpaceType, int sharedSpaceId, String sharedSpaceUserMembership,
                                boolean active) throws FileNotFoundException,
            RmsRestAPIException, NotNxlFileException, TokenAccessDenyException, TokenException;

    /**
     * Used to sharing local plain file
     *
     * @param plainPath     the plain file path
     * @param bAsAttachment whether sharing file as attachment
     * @param policy        adHoc policy -- rights & obligations & expiry
     * @param recipients    the email address will be shared
     * @param comment       the comment about share(optional)
     * @return String duid
     */
    String shareLocalPlainFileToMyVault(String plainPath,
                                        boolean bAsAttachment,
                                        AdhocPolicy policy,
                                        String filePathId, String filePath,
                                        IRecipients recipients,
                                        @Nullable String comment)
            throws FileNotFoundException, RmsRestAPIException;

    /**
     * Used to share local nxl file --- don't need to pass watermark value & expiry
     *
     * @param nxlPath       the nxl file path
     * @param bAsAttachment whether sharing file as attachment
     * @param recipients    the email address will be shared
     * @param comment       the comment about share(optional)
     * @return String duid
     */
    String shareLocalNxlFileToMyVault(String nxlPath,
                                      boolean bAsAttachment,
                                      IRecipients recipients,
                                      @Nullable String comment)
            throws NotNxlFileException, TokenAccessDenyException, NotGrantedShareRights, TokenAccessDenyException, RmsRestAPIException;

    /**
     * Used to share repository file
     *
     * @param fileName      file name.
     * @param bAsAttachment whether sharing file as attachment
     * @param repositoryId  repository id
     * @param filePathId    file path id
     * @param filePath      file path
     * @param permissions   the rights value
     * @param recipients    the email address will be shared
     * @param comment       the comment about share(optional)
     * @param watermark     watermark value
     * @param expiry        expiry date, for nxl file, we can get it from the nxl header first.
     * @return String duid
     */
    String shareRepoFileToMyVault(String fileName,
                                  boolean bAsAttachment,
                                  String repositoryId,
                                  String filePathId,
                                  String filePath,
                                  int permissions,
                                  IRecipients recipients,
                                  @Nullable String comment,
                                  @Nullable String watermark,
                                  @Nullable Expiry expiry) throws FileNotFoundException, RmsRestAPIException;

}
