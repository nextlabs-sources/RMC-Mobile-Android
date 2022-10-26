package com.skydrm.sdk.nxl.token;

import android.content.Context;

import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

public interface ITokenService {
    IToken getEncryptToken(String membershipId)
            throws TokenException, RmsRestAPIException;

    IToken getDecryptToken(INxlFileFingerPrint fp,
                           boolean allowOfflineLoad)
            throws TokenException, RmsRestAPIException, TokenAccessDenyException;

    IToken getDecryptToken(int sharedSpaceType,
                           int sharedSpaceId,
                           String sharedSpaceUserMembership,
                           INxlFileFingerPrint fp,
                           boolean allowOfflineLoad)
            throws TokenException, RmsRestAPIException, TokenAccessDenyException;

    void setSecurityCtx(Context ctx);

    boolean prepareOfflineToken(INxlFileFingerPrint fp,
                                boolean active)
            throws TokenException, RmsRestAPIException, TokenAccessDenyException;

    boolean prepareOfflineToken(int sharedSpaceType,
                                int sharedSpaceId,
                                String sharedSpaceUserMembership,
                                INxlFileFingerPrint fp,
                                boolean active)
            throws TokenException, RmsRestAPIException, TokenAccessDenyException;

    int getMaintenanceLevel();

    String getOtp(String ownerId, String duid);
}
