package com.skydrm.sdk.rms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.INxlClient;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.INxlTenant;
import com.skydrm.sdk.IRecipients;
import com.skydrm.sdk.exception.NotGrantedShareRights;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RightsExpiredException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.nxl.CryptoBlob;
import com.skydrm.sdk.nxl.FileInfo;
import com.skydrm.sdk.nxl.NxlFileHandler;
import com.skydrm.sdk.nxl.NxlFingerPrint;
import com.skydrm.sdk.nxl.PolicyInfo;
import com.skydrm.sdk.nxl.token.IToken;
import com.skydrm.sdk.nxl.token.ITokenService;
import com.skydrm.sdk.nxl.token.TokenException;
import com.skydrm.sdk.nxl.token.TokenService;
import com.skydrm.sdk.policy.AdhocPolicy;
import com.skydrm.sdk.policy.CentralPolicy;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Policy;
import com.skydrm.sdk.rms.rest.IMembershipService;
import com.skydrm.sdk.rms.types.SharingLocalFilePara;
import com.skydrm.sdk.rms.types.SharingRepoFileParas;
import com.skydrm.sdk.rms.types.SharingRepoFileResult;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.skydrm.sdk.utils.NxCommonUtils.bytesToHexString;


public class NxlClient implements INxlClient {
    private static final DevLog log = new DevLog(NxlClient.class.getSimpleName());
    private IRmUser user;
    private NxlTenant tenant;
    private RestAPI api;
    private DH dh;

    private ITokenService mTService;

    public NxlClient(@NonNull IRmUser user, @NonNull NxlTenant tenant, @NonNull Config config) throws Exception {
        this.user = user;
        this.tenant = tenant;
        this.api = new RestAPI(config);
        this.dh = new DH();

        this.mTService = new TokenService(user, api, tenant.getTenantId(), dh);
    }

    public void setAPI(RestAPI api) {
        this.api = api;
    }

    public RestAPI getApi() {
        return api;
    }

    @Override
    public boolean isSessionExpired() {
        if (user == null) {
            log.e("in isSessionExpired user is null");
            return true;
        }
        long ttl = user.getTtl();
        long now = System.currentTimeMillis();

        log.v("Check if session expired:\n" +
                "Now:\t" + new Date(now).toString()
                + "\nExpired:\t" + new Date(ttl).toString()
                + "\nResult:\t" + (ttl < now));
        return ttl < now;
    }

    @Override
    public boolean signOut() {
        return false;
    }

    @Override
    public boolean isNxlFile(String path, boolean fast) {
        try {
            //sanity check
            if (path == null) {
                log.e("path is null in isNxlFile()");
                return false;
            }
            // -check if path end with .nxl
            if (!path.toLowerCase().endsWith(".nxl")) {
                log.e("path:" + path + " is not end with .nxl");
                return false;
            }
            // -check if file exist
            File nxFile = new File(path);
            if (!nxFile.exists()) {
                log.e("the file" + path + "\t is not exist in isNxlFile()");
                return false;
            }
            return NxlFileHandler.isNxlFile(path, fast);
        } catch (Exception ignored) {
            log.e("impossible situations in isNxlFile:" + ignored.toString());
        }
        return false;
    }

    @Override
    public boolean encryptToNxl(String srcPath, String nxlPath, Policy policy, boolean overwrite)
            throws FileNotFoundException, RmsRestAPIException {
        // sanity check
        if (srcPath == null || srcPath.isEmpty()) {
            log.e("srcPath is null or empty");
            return false;
        }
        if (!new File(srcPath).exists()) {
            log.e("srcPath is not exist");
            throw new FileNotFoundException(srcPath + "dose not exist");
        }
        if (nxlPath == null || nxlPath.isEmpty()) {
            log.e("nxlPath is null or empty");
            return false;
        }
        if (new File(nxlPath).exists() && !overwrite) {
            log.e(nxlPath + "has exsit but the param overwrite is false");
            return false;
        }
        // as current design, policy must be existed in NLX header
        if (policy == null) {
            log.e("policy is null");
            return false;
        }

        // prepare ownerID for who will create the NXL file, by default it is current log-in user
        String ownerId;
        CryptoBlob tokenBlob;
        // 4/12/2018, add new feature
        // for project upload a file, convert to nxl and at the same time, the nxl owner should be
        // project's membershipId
        if (policy.hasCentralPolicy()) {
            // for central policy
            ownerId = policy.getCentralPolicy().getMembershipId();
            if (ownerId == null || ownerId.isEmpty()) {
                log.e("owner-id is null or empty when converting nxl in Centrol Policy");
                return false;
            }
            // generate EncryptToken
            tokenBlob = generateBlob(ownerId);
        } else {
            // for adhoc policy
            ownerId = policy.getAdhocPolicy().getIssuer();// give client an chance to change ownerID by Adhoc's issuer
            if (ownerId == null || ownerId.isEmpty()) {
                ownerId = user.getMembershipId();
                tokenBlob = generateBlob(ownerId);
                if (tokenBlob == null) {
                    return false;
                }
            } else {
                tokenBlob = generateBlob(ownerId);
                if (tokenBlob == null) {
                    return false;
                }
            }
        }
        //log.i("call nxl-convert:{ownerId:" + ownerId + " src:" + srcPath + " " + tokenBlob.toString() + "}");
        // --convert src into nxl
        if (!NxlFileHandler.convert(ownerId, srcPath, nxlPath, tokenBlob, overwrite)) {
            log.e("failed, convert nomral to nxl");
            return false;
        }

        String sectionRaw = "";
        int protectionType = -1;

        if (policy.hasAdhocPolicy()) {
            // prepare policy section for the format of NXL header
            PolicyInfo p = new PolicyInfo(policy.getAdhocPolicy().generateJSON());
            sectionRaw = p.rawData;
            protectionType = 0;
            // --set policy for new converted nxl
            if (!NxlFileHandler.AdhocSectionInfo(nxlPath, true, tokenBlob, p)) {
                log.e("failed, write policy string into nxl");
                return false;
            }

            // you are kidding me, for adhoc policy we still need to set
            // central string as default json {}
            PolicyInfo defaultCentral = new PolicyInfo("{}");
            if (!NxlFileHandler.CentralSectionInfo(nxlPath, true, tokenBlob, defaultCentral)) {
                log.e("failed, write default adhoc josn string into nxl");
                return false;
            }
        }

        if (policy.hasCentralPolicy()) {
            PolicyInfo p = new PolicyInfo(policy.getCentralPolicy().generateJSON());
            sectionRaw = p.rawData;
            protectionType = 1;
            // --set policy for new converted nxl
            if (!NxlFileHandler.CentralSectionInfo(nxlPath, true, tokenBlob, p)) {
                log.e("failed, write policy string into nxl");
                return false;
            }
            // you are kidding me, for CentralPolicy we still need to set
            // Adhoc string as default json {}
            PolicyInfo defaultAdhoc = new PolicyInfo("{}");
            if (!NxlFileHandler.AdhocSectionInfo(nxlPath, true, tokenBlob, defaultAdhoc)) {
                log.e("failed, write default adhoc josn string into nxl");
                return false;
            }
        }
        if (policy.hasFileInfo()) {
            PolicyInfo p = new PolicyInfo(policy.getFileInfo().generateJSON());
            if (!NxlFileHandler.FileInfoSectionInfo(nxlPath, true, tokenBlob, p)) {
                log.e("failed, write file info json string into nxl.");
                return false;
            }
        }

        //Should call update nxl metadata in activate prefetched duid.
        api.getTokenService(user).updateNXLMetadata(
                tokenBlob.getDUID(),
                tokenBlob.getOtp(),
                sectionRaw,
                protectionType,
                tokenBlob.getMaintenanceLevel());

        return true;
    }

    @Override
    public INxlFileFingerPrint decryptFromNxl(
            String nxlPath,
            String plainPath,
            boolean overwrite,
            boolean allowOfflineLoad)
            throws NotNxlFileException, TokenAccessDenyException, RightsExpiredException, RmsRestAPIException {
        //sanity check
        if (!isNxlFile(nxlPath, false)) {
            log.e("nxlpath is not .nxl file");
            throw new NotNxlFileException("nxlpath is not .nxl file");
        }
        if (plainPath == null || plainPath.isEmpty()) {
            log.e("plainPath is null");
            throw new IllegalArgumentException("plainPath is null");
        }
        if (new File(plainPath).exists() && !overwrite) {
            log.e(plainPath + "has exsit but the param overwrite is false");
            throw new IllegalArgumentException(plainPath + "has exsit but the param overwrite is false");
        }

        // -extract fingerprint
        NxlFingerPrint fp = new NxlFingerPrint();
        if (!NxlFileHandler.getNxlFingerPrint(nxlPath, fp)) {
            log.e("failed,get finger print,in decryptFromNxl");
            return null;
        }

        // -check the validation of fingerprint
        if (!NxlFingerPrint.validFingerPrint(fp)) {
            log.e("failed,invalid finger print in decryptFromNxl");
            return null;
        }

        {
            // adhoc policy
            PolicyInfo policyInfo = new PolicyInfo();
            NxlFileHandler.readADHocSection(nxlPath, policyInfo);
            if (policyInfo.rawData != null && !policyInfo.rawData.isEmpty()) {
                log.e("fill with adhoc info");

                AdhocPolicy adhoc = AdhocPolicy.parseAdhocPolicyFromJSON(policyInfo.rawData);
                fp.setRights(adhoc.getRights());
                //Keep the section raw to upload to rms.
                fp.setADHocSectionRaw(policyInfo.rawData);
                fp.setObligations(adhoc.getObligations());
                fp.setExpiry(adhoc.getExpiry());
                // check rights expiry
                // --- note: for owner, still can view file even though rights expire(but don't have other rights, like "share".)
                if (fp.isExpired() && !user.isOwner(fp.getOwnerID())) {
                    log.e("the file rights has expired");
                    throw new RightsExpiredException("The file rights has expired.");
                }
            }
        }
        {
            // central policy
            PolicyInfo policyInfo = new PolicyInfo();
            NxlFileHandler.readCentralPolicySection(nxlPath, policyInfo);
            if (policyInfo.rawData != null && !policyInfo.rawData.isEmpty()) {
                log.e("fill with central policy info");
                CentralPolicy centralPolicy = new CentralPolicy.Builder()
                        .setMembershipId(fp.getOwnerID())
                        .addTagFromJSON(policyInfo.rawData)
                        .build();
                fp.setCentralSectionRaw(policyInfo.rawData);
                fp.setTags(centralPolicy.getTags());
            }
        }

        // -get token by finger print
        IToken TOKEN = null;
        try {
            TOKEN = mTService.getDecryptToken(fp, allowOfflineLoad);
        } catch (TokenException e) {
            if (e.getStatus() == TokenException.FAILED_TOKEN_EXPIRED) {
                throw new TokenAccessDenyException(e.getMessage(), e,
                        TokenAccessDenyException.TYPE_TOKEN_EXPIRED, nxlPath);
            }
            e.printStackTrace();
        }
        if (TOKEN == null) {
            log.e("failed,invalid decrypt token in decryptFromNxl");
            return null;
        }

        // -generate Decrypt Crypto Blob
        CryptoBlob blob = new CryptoBlob(fp.rootAgreementKey, fp.icaAgreementKey, fp.ml, fp.duid, TOKEN.getTokenStr());
        log.i("CryptoBlob for decyrpt:" + blob.toString());

        if (!NxlFileHandler.decrypt(nxlPath, plainPath, blob, overwrite)) {
            log.e("failed,in decrypt in decryptFromNxl");
            return null;
        }

        log.i("NxlFingerPrint:" + fp.toString());
        return fp;
    }

    @Override
    public INxlFileFingerPrint decryptFromNxl(String nxlPath, String plainPath,
                                              int sharedSpaceType, int sharedSpaceId,
                                              String sharedSpaceUserMembership,
                                              boolean overwrite, boolean allowOfflineLoad)
            throws NotNxlFileException, TokenAccessDenyException, RightsExpiredException, RmsRestAPIException {
        //sanity check
        if (!isNxlFile(nxlPath, false)) {
            log.e("nxlpath is not .nxl file");
            throw new NotNxlFileException("nxlpath is not .nxl file");
        }
        if (plainPath == null || plainPath.isEmpty()) {
            log.e("plainPath is null");
            throw new IllegalArgumentException("plainPath is null");
        }
        if (new File(plainPath).exists() && !overwrite) {
            log.e(plainPath + "has exsit but the param overwrite is false");
            throw new IllegalArgumentException(plainPath + "has exsit but the param overwrite is false");
        }

        // -extract fingerprint
        NxlFingerPrint fp = new NxlFingerPrint();
        if (!NxlFileHandler.getNxlFingerPrint(nxlPath, fp)) {
            log.e("failed,get finger print,in decryptFromNxl");
            return null;
        }

        // -check the validation of fingerprint
        if (!NxlFingerPrint.validFingerPrint(fp)) {
            log.e("failed,invalid finger print in decryptFromNxl");
            return null;
        }

        {
            // adhoc policy
            PolicyInfo policyInfo = new PolicyInfo();
            NxlFileHandler.readADHocSection(nxlPath, policyInfo);
            if (policyInfo.rawData != null && !policyInfo.rawData.isEmpty()) {
                log.e("fill with adhoc info");

                AdhocPolicy adhoc = AdhocPolicy.parseAdhocPolicyFromJSON(policyInfo.rawData);
                fp.setRights(adhoc.getRights());
                //Keep the section raw to upload to rms.
                fp.setADHocSectionRaw(policyInfo.rawData);
                fp.setObligations(adhoc.getObligations());
                fp.setExpiry(adhoc.getExpiry());
                // check rights expiry
                // --- note: for owner, still can view file even though rights expire(but don't have other rights, like "share".)
                if (fp.isExpired() && !user.isOwner(fp.getOwnerID())) {
                    log.e("the file rights has expired");
                    throw new RightsExpiredException("The file rights has expired.");
                }
            }
        }
        {
            // central policy
            PolicyInfo policyInfo = new PolicyInfo();
            NxlFileHandler.readCentralPolicySection(nxlPath, policyInfo);
            if (policyInfo.rawData != null && !policyInfo.rawData.isEmpty()) {
                log.e("fill with central policy info");
                CentralPolicy centralPolicy = new CentralPolicy.Builder()
                        .setMembershipId(fp.getOwnerID())
                        .addTagFromJSON(policyInfo.rawData)
                        .build();
                fp.setCentralSectionRaw(policyInfo.rawData);
                fp.setTags(centralPolicy.getTags());
            }
        }

        // -get token by finger print
        IToken TOKEN = null;
        try {
            TOKEN = mTService.getDecryptToken(sharedSpaceType, sharedSpaceId,
                    sharedSpaceUserMembership,
                    fp, allowOfflineLoad);
        } catch (TokenException e) {
            if (e.getStatus() == TokenException.FAILED_TOKEN_EXPIRED) {
                throw new TokenAccessDenyException(e.getMessage(), e,
                        TokenAccessDenyException.TYPE_TOKEN_EXPIRED, nxlPath,
                        sharedSpaceType, sharedSpaceId,
                        sharedSpaceUserMembership);
            }
            e.printStackTrace();
        }
        if (TOKEN == null) {
            log.e("failed,invalid decrypt token in decryptFromNxl");
            return null;
        }

        // -generate Decrypt Crypto Blob
        CryptoBlob blob = new CryptoBlob(fp.rootAgreementKey, fp.icaAgreementKey, fp.ml, fp.duid, TOKEN.getTokenStr());
        log.i("CryptoBlob for decyrpt:" + blob.toString());

        if (!NxlFileHandler.decrypt(nxlPath, plainPath, blob, overwrite)) {
            log.e("failed,in decrypt in decryptFromNxl");
            return null;
        }

        log.i("NxlFingerPrint:" + fp.toString());
        return fp;
    }

    @Override
    public INxlFileFingerPrint extractFingerPrint(String nxlPath)
            throws FileNotFoundException, NotNxlFileException, TokenAccessDenyException, RmsRestAPIException {
        //sanity check
        if (nxlPath == null) {
            throw new RuntimeException("nxlPath is null");
        }
        // -check if file exist
        File nxFile = new File(nxlPath);
        if (!nxFile.exists()) {
            throw new FileNotFoundException("the file" + nxlPath + "\t is not exist");
        }
        // -check is nxl file
        if (!isNxlFile(nxlPath, false)) {
            throw new NotNxlFileException("the file" + nxlPath + "\t is not .nxl format");
        }

        // extract finger print
        final NxlFingerPrint fp = new NxlFingerPrint();
        if (!NxlFileHandler.getNxlFingerPrint(nxlPath, fp)) {
            log.e("failed,get finger print,in decryptFromNxl");
            return null;
        }

        // check validation
        if (!NxlFingerPrint.validFingerPrint(fp)) {
            log.e("failed,invalid finger print in decryptFromNxl");
            return null;
        }

        fp.setOtp(mTService.getOtp(fp.ownerId, fp.duid));
        // 2018-04-09, required fp support both AdhocPolicy and CentralPolicy
        {
            // adhoc policy.
            PolicyInfo policyInfo = new PolicyInfo();
            NxlFileHandler.readADHocSection(nxlPath, policyInfo);
            if (policyInfo.rawData != null && !policyInfo.rawData.isEmpty()) {
                log.e("fill with adhoc info");

                AdhocPolicy adhoc = AdhocPolicy.parseAdhocPolicyFromJSON(policyInfo.rawData);
                fp.setADHocSectionRaw(policyInfo.rawData);
                fp.setRights(adhoc.getRights());
                fp.setObligations(adhoc.getObligations());
                fp.setExpiry(adhoc.getExpiry());
            }
        }
        {
            // central policy.
            PolicyInfo policyInfo = new PolicyInfo();
            NxlFileHandler.readCentralPolicySection(nxlPath, policyInfo);
            if (policyInfo.rawData != null && !policyInfo.rawData.isEmpty()) {
                log.e("fill with central policy info");
                CentralPolicy centralPolicy = new CentralPolicy.Builder()
                        .setMembershipId(fp.getOwnerID())
                        .addTagFromJSON(policyInfo.rawData)
                        .build();
                fp.setCentralSectionRaw(policyInfo.rawData);
                fp.setTags(centralPolicy.getTags());
            }
        }
        {
            // file info.
            PolicyInfo fileInfo = new PolicyInfo();
            if (NxlFileHandler.readFileInfoSection(nxlPath, fileInfo)) {
                String rawData = fileInfo.rawData;
                if (rawData != null && !rawData.isEmpty()) {
                    FileInfo info = FileInfo.fromRawJson(rawData);
                    fp.setLastModifiedTime(info.getDateModified());
                    fp.setNormalFileName(info.getFileName());
                }
            }
        }
        log.i("NxlFingerPrint:" + fp.toString());
        return fp;
    }

    @Override
    public void setSecurityCtx(Context ctx) {
        mTService.setSecurityCtx(ctx);
    }

    @Override
    public boolean updateOfflineStatus(String nxlPath, boolean active) throws FileNotFoundException,
            RmsRestAPIException, NotNxlFileException, TokenAccessDenyException, TokenException {
        //sanity check.
        if (nxlPath == null || nxlPath.isEmpty()) {
            return false;
        }
        return mTService.prepareOfflineToken(extractFingerPrint(nxlPath), active);
    }

    @Override
    public boolean updateOfflineStatus(String nxlPath,
                                       int sharedSpaceType, int sharedSpaceId,
                                       String sharedSpaceUserMembership,
                                       boolean active)
            throws FileNotFoundException, RmsRestAPIException, NotNxlFileException, TokenAccessDenyException, TokenException {
        //sanity check.
        if (nxlPath == null || nxlPath.isEmpty()) {
            return false;
        }
        return mTService.prepareOfflineToken(sharedSpaceType, sharedSpaceId,
                sharedSpaceUserMembership,
                extractFingerPrint(nxlPath), active);
    }

    @Override
    public String shareLocalPlainFileToMyVault(String plainPath, boolean bAsAttachment,
                                               AdhocPolicy policy, String filePathId, String filePath,
                                               IRecipients recipients, String comment)
            throws FileNotFoundException, RmsRestAPIException {
        // sanity check
        if (plainPath == null || plainPath.isEmpty()) {
            log.e("plain path is null or empty");
            throw new IllegalArgumentException("plain path is null or empty");
        }

        if (!new File(plainPath).exists()) {
            log.e("plain path is not exist");
            throw new FileNotFoundException("plain path is not exist");
        }

        if (recipients == null || recipients.iterator() == null || !recipients.iterator().hasNext()) {
            log.e("recipients is invalid");
            throw new IllegalArgumentException("recipients is invalid");
        }

        if (policy == null) {
            log.e("policy is null");
            throw new IllegalArgumentException("policy is null");
        }
        if (policy.getRights() == null || !policy.getRights().hasView()) {
            log.e("right in policy is null or not enough(without VIEW rights?)");
            throw new IllegalArgumentException("righst in policy is null or not enough(without VIEW rights?)");
        }

        INxlRights rights = policy.getRights();
        int permissions = rightsToInteger(rights);

        List<String> emails = new ArrayList<>();
        Iterator<String> iterator = recipients.iterator();
        while (iterator.hasNext()) {
            emails.add(iterator.next());
        }

        if (emails.isEmpty()) {
            throw new RuntimeException("mail list is empty, that's impossible");
        }

        String duid = null;
        String watermark = "";
        if (policy.getObligations().hasWatermark()) {
            watermark = ((Obligations) policy.getObligations()).getObligation().get("WATERMARK");
        }

        try {
            SharingLocalFilePara paras = new SharingLocalFilePara(new File(plainPath), permissions,
                    emails, comment, watermark, (Expiry) policy.getExpiry());
            paras.setFilePath(filePath);
            paras.setFilePathId(filePathId);
            duid = api.getSharingService(user).sharingLocalFile(paras);
        } catch (RmsRestAPIException e) {
            throw e;
        } catch (Exception e) {
            log.e("impossible situations", e);
            throw new RuntimeException(e);
        }

        return duid;
    }

    @Override
    public String shareLocalNxlFileToMyVault(String nxlPath, boolean bAsAttachment, IRecipients recipients, String comment)
            throws NotNxlFileException, NotGrantedShareRights, TokenAccessDenyException, RmsRestAPIException {
        // sanity check
        if (!isNxlFile(nxlPath, false)) {
            log.e("not .nxl");
            throw new NotNxlFileException("not .nxl");
        }
        if (recipients == null || recipients.iterator() == null || !recipients.iterator().hasNext()) {
            log.e("recipients is invalid");
            throw new IllegalArgumentException("recipients is invalid");
        }

        INxlFileFingerPrint fp = null;
        String ownerId = null;
        String duid = null;
        List<String> emails = new ArrayList<>();

        try {
            fp = extractFingerPrint(nxlPath);
            if (fp == null) {
                throw new RuntimeException("finger print is null");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        } catch (TokenAccessDenyException e) {
            throw new TokenAccessDenyException(e.getMessage());
        }

        ownerId = fp.getOwnerID();
        if (ownerId == null || ownerId.isEmpty()) {
            throw new RuntimeException("ownerId is null");
        }

        Iterator<String> iterator = recipients.iterator();
        while (iterator.hasNext()) {
            emails.add(iterator.next());
        }
        if (emails.isEmpty()) {
            throw new RuntimeException("mail list is empty");
        }

        if (!user.getMembershipId().toLowerCase().equals(ownerId)) {
            if (!fp.hasRights() || !fp.hasShare()) {
                log.e("current user" + user.getUserId() + "/"
                        + user.getEmail() + " is not granted to operate this file");
                throw new NotGrantedShareRights("current user" +
                        user.getUserId() + "/" + user.getEmail()
                        + " is not granted to operate this file");
            }
        }

        try {
            // for sharing nxl file, don't need to pass watermark and expiry params.
            SharingLocalFilePara paras = new SharingLocalFilePara(new File(nxlPath), rightsToInteger(fp), emails, comment);
            duid = api.getSharingService(user).sharingLocalFile(paras);
        } catch (RmsRestAPIException e) {
            throw e;
        } catch (Exception e) {
            log.e(e);
            throw new RuntimeException(e.getMessage());
        }

        return duid;
    }

    @Override
    public String shareRepoFileToMyVault(String fileName,
                                         boolean bAsAttachment,
                                         String repositoryId,
                                         String filePathId,
                                         String filePath, int permissions,
                                         IRecipients recipients,
                                         @Nullable String comment,
                                         @Nullable String watermark,
                                         @Nullable Expiry expiry)
            throws FileNotFoundException, RmsRestAPIException {

        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(repositoryId) || TextUtils.isEmpty(filePathId) || TextUtils.isEmpty(filePath)) {
            log.e("file name, repository id, file path id, file path, at least is empty.");
            throw new IllegalArgumentException("file name, repository id, file path id, file path, at least is empty.");
        }

        if (recipients == null || recipients.iterator() == null || !recipients.iterator().hasNext()) {
            log.e("recipients is invalid");
            throw new IllegalArgumentException("recipients is invalid");
        }

        List<String> emails = new ArrayList<String>();
        Iterator<String> iterator = recipients.iterator();
        while (iterator.hasNext()) {
            emails.add(iterator.next());
        }

        if (emails.isEmpty()) {
            throw new RuntimeException("mail list is empty, that's impossible");
        }

        SharingRepoFileResult result;
        try {
            SharingRepoFileParas paras = new SharingRepoFileParas(fileName, repositoryId, filePathId, filePath, permissions, emails, comment, watermark, expiry);
            result = api.getSharingService(user).sharingRepoFile(paras);
        } catch (RmsRestAPIException e) {
            throw e;
        } catch (Exception e) {
            log.e("impossible situations", e);
            throw new RuntimeException(e);
        }

        return result.getResults().getDuid();
    }

    @Override
    public IRmUser getUser() {
        return user;
    }

    @Override
    public INxlTenant getTenant() {
        return tenant;
    }

    public void setTenant(NxlTenant tenant) {
        this.tenant = tenant;
    }

    private boolean validDhClient() {
        return dh.validDhClient();
    }

    private void initDHClient() {
        dh.initDHClient();
    }

    private int rightsToInteger(INxlRights r) {
        return r.toInteger();
    }

    private CryptoBlob generateBlob(String membershipId) throws RmsRestAPIException {
        // init dh
        if (!validDhClient()) {
            initDHClient();
        }
        IToken tk;
        try {
            tk = mTService.getEncryptToken(membershipId);
        } catch (TokenException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }
        //Sanity check first.
        if (tk == null) {
            return null;
        }
        String DUID = tk.getDuid();
        if (DUID == null || DUID.isEmpty()) {
            return null;
        }
        String TOKEN = tk.getTokenStr();
        if (TOKEN == null || TOKEN.isEmpty()) {
            return null;
        }
        String OTP = tk.getOtp();

        int ML = mTService.getMaintenanceLevel();

        CryptoBlob rt;
        List<byte[]> keys = dh.getAgreementKeys();
        try {
            if (keys.size() == 3) {
                rt = new CryptoBlob(
                        bytesToHexString(keys.get(2)),
                        bytesToHexString(keys.get(1)),
                        ML,
                        DUID,
                        TOKEN,
                        OTP);
            } else if (keys.size() == 2) {
                rt = new CryptoBlob(
                        bytesToHexString(keys.get(1)),
                        bytesToHexString(keys.get(1)),
                        ML,
                        DUID,
                        TOKEN,
                        OTP);
            } else {
                log.e("logic error,certificates at lest has 2");
                throw new Exception("logic error,certificates at lest has 2");
            }
            log.v(rt.toString());
            return rt;
        } catch (RmsRestAPIException e) {
            throw e;
        } catch (Exception e) {
            log.e("failed,generateBlob", e);
            e.printStackTrace();
        }
        return null;
    }

    public class DH {
        // membership used
        private KeyPair dhClientKeyPair;
        private List<X509Certificate> certs;
        private byte[] agreementKey;    // from the last item in certs
        private List<byte[]> agreementKeys;

        public boolean validDhClient() {
            return dhClientKeyPair != null;
        }

        public void initDHClient() {
            if (dhClientKeyPair != null) {
                return;
            }
            try {
                dhClientKeyPair = api.getMembershipService(user).generateDHKeyPair(Config.p, Config.g);
            } catch (Exception e) {
                log.e(e);
                dhClientKeyPair = null;
            }
        }

        public List<byte[]> getAgreementKeys() throws RmsRestAPIException {
            if (agreementKeys != null && !agreementKeys.isEmpty()) {
                return agreementKeys;
            }
            // get again
            if (!validDhClient()) {
                initDHClient();
            }
            if (!validDhClient()) {
                return null;
            }
            try {
                IMembershipService service = api.getMembershipService(user);
                if (certs == null || certs.isEmpty()) {
                    certs = service.membership(dhClientKeyPair.getPublic().getEncoded());
                }
                // calc every agreementkey
                log.v("calc every agreementkey");
                // update new
                agreementKeys = null;
                agreementKeys = new ArrayList<>();
                for (X509Certificate cert : certs) {
                    byte[] k = service.calcDHAgreementKey(dhClientKeyPair.getPrivate().getEncoded(),
                            cert.getPublicKey().getEncoded());
                    agreementKeys.add(k);
                    log.v("k:" + bytesToHexString(k));
                }
                // set value to the last
                agreementKey = agreementKeys.get(agreementKeys.size() - 1);
                return agreementKeys;
            } catch (RmsRestAPIException e) {
                throw e;
            } catch (Exception e) {
                log.e("failed, getAgreementKeys");
            }
            return null;
        }


        public byte[] getAgreementKey() throws RmsRestAPIException {
            if (agreementKey != null) {
                return agreementKey;
            }
            // get again
            if (!validDhClient()) {
                initDHClient();
            }
            if (!validDhClient()) {
                return null;
            }

            try {
                IMembershipService service = api.getMembershipService(user);
                certs = service.membership(dhClientKeyPair.getPublic().getEncoded());
                // calc agreementkey
                agreementKey = service.calcDHAgreementKey(dhClientKeyPair.getPrivate().getEncoded(),
                        certs.get(certs.size() - 1).getPublicKey().getEncoded());
                return agreementKey;
            } catch (RmsRestAPIException e) {
                throw e;
            } catch (Exception e) {
                log.e(e);
            }

            return null;
        }
    }
}
