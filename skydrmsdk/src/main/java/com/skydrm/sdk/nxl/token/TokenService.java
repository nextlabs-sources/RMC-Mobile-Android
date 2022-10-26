package com.skydrm.sdk.nxl.token;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.rms.NxlClient;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.user.IRmUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TokenService implements ITokenService {
    private final Map<String, EncryptTokenPool> mEncryptTokens = new HashMap<>();
    private final Map<String, DecryptTokenPool> mDecryptTokens = new HashMap<>();
    private final LocalDecryptTokenPool mLocalTokenPool = new LocalDecryptTokenPool();
    ;
    private final Object mELock = new Object();
    private final Object mDLock = new Object();

    private final IRmUser mUser;

    private final RestAPI mApi;
    private NxlClient.DH mDh;
    private int maintenanceLevel = 0;
    private String mTenantName;

    public TokenService(IRmUser usr, RestAPI api, String tenantName, NxlClient.DH dh) {
        this.mUser = usr;
        this.mApi = api;
        this.mTenantName = tenantName;
        this.mDh = dh;
    }

    @Override
    public void setSecurityCtx(Context ctx) {
        mLocalTokenPool.buildStore(ctx);
    }

    @Override
    public IToken getEncryptToken(String membershipId) throws TokenException, RmsRestAPIException {
        return getEncryptTokenInternal(membershipId);
    }

    @Override
    public IToken getDecryptToken(INxlFileFingerPrint fp, boolean allowOfflineLoad)
            throws TokenException, RmsRestAPIException, TokenAccessDenyException {
        return getDecryptTokenInternal(fp, allowOfflineLoad, true);
    }

    @Override
    public IToken getDecryptToken(int sharedSpaceType,
                                  int sharedSpaceId,
                                  String sharedSpaceUserMembership,
                                  INxlFileFingerPrint fp,
                                  boolean allowOfflineLoad)
            throws TokenException, RmsRestAPIException, TokenAccessDenyException {
        return getDecryptTokenInternal(sharedSpaceType, sharedSpaceId, sharedSpaceUserMembership,
                fp, allowOfflineLoad, true);
    }

    @Override
    public boolean prepareOfflineToken(INxlFileFingerPrint fp, boolean active)
            throws TokenException, RmsRestAPIException, TokenAccessDenyException {
        if (fp == null) {
            throw new TokenException("Param fp must not be null.");
        }
        String membershipId = fp.getOwnerID();
        String duid = fp.getDUID();
        if (active) {
            IToken tk = getDecryptTokenInternal(fp, false, false);
            return mLocalTokenPool.serialToken(membershipId, tk);
        } else {
            // If de-active a. clear token in mem.
            // b. clear token in local.
            DecryptTokenPool dtp = mDecryptTokens.get(membershipId);
            if (dtp != null) {
                dtp.removeOne(duid);
            }
            return mLocalTokenPool.remove(membershipId, duid);
        }
    }

    @Override
    public boolean prepareOfflineToken(int sharedSpaceType,
                                       int sharedSpaceId,
                                       String sharedSpaceUserMembership,
                                       INxlFileFingerPrint fp,
                                       boolean active)
            throws TokenException, RmsRestAPIException, TokenAccessDenyException {
        if (fp == null) {
            throw new TokenException("Param fp must not be null.");
        }
        String duid = fp.getDUID();
        if (active) {
            IToken tk = getDecryptTokenInternal(sharedSpaceType, sharedSpaceId, sharedSpaceUserMembership,
                    fp, false, false);
            return mLocalTokenPool.serialToken(sharedSpaceUserMembership, tk);
        } else {
            // If de-active a. clear token in mem.
            // b. clear token in local.
            DecryptTokenPool dtp = mDecryptTokens.get(sharedSpaceUserMembership);
            if (dtp != null) {
                dtp.removeOne(duid);
            }
            return mLocalTokenPool.remove(sharedSpaceUserMembership, duid);
        }
    }

    @Override
    public int getMaintenanceLevel() {
        return maintenanceLevel;
    }

    @Override
    public String getOtp(String ownerId, String duid) {
        synchronized (mDLock) {
            DecryptTokenPool decryptTokenPool = mDecryptTokens.get(ownerId);
            return decryptTokenPool == null ? "" : decryptTokenPool.getOtp(duid);
        }
    }

    private IToken getEncryptTokenInternal(String membershipId)
            throws TokenException, RmsRestAPIException {
        //Sanity check first.
        if (membershipId == null || membershipId.isEmpty()) {
            throw new TokenException("The required argument membership-id must not be null.");
        }
        //Read from local token pool first.
        IToken one = tryGetOneEKeyFromCache(membershipId);
        //Find one in cache.
        if (one != null) {
            return one;
        }

        EncryptTokenPool tp = new EncryptTokenPool().fillPool(membershipId);
        if (tp.isEmpty()) {
            throw new TokenException("Fatal error occurred when try to fill encrypt token pool.");
        }
        //Make sure the newly constructed t-pool exists in t-container.
        boolean rt = cacheEKey(membershipId, tp);
        if (!rt) {
            throw new TokenException("Fatal error occurred when invoke getEncryptToken.");
        }

        //Re-find one.
        return tp.findOne();
    }

    private void transferToDecryptPool(String membershipId, IToken internal)
            throws TokenException {
        if (containsDKey(membershipId)) {
            DecryptTokenPool dtp;

            synchronized (mDLock) {
                dtp = mDecryptTokens.get(membershipId);
            }

            if (dtp == null) {
                throw new TokenException("Fatal error,broken d-pool found.");
            }

            if (dtp.isEmpty()) {
                if (!dtp.setOne(internal)) {
                    throw new TokenException("Try transfer internal one from e-pool into d-pool failed.");
                }
                return;
            }

            if (dtp.contains(internal)) {
                throw new TokenException("Fatal error,the internal one you're transferring is duplicated");
            }

            if (!dtp.setOne(internal)) {
                throw new TokenException("Try transfer internal one from e-pool into d-pool failed.");
            }
        } else {
            DecryptTokenPool dtp = new DecryptTokenPool();
            if (!dtp.setOne(internal)) {
                throw new TokenException("Try transfer internal one from e-pool into d-pool failed.");
            }
            synchronized (mDLock) {
                mDecryptTokens.put(membershipId, dtp);
            }
        }
    }

    private IToken getDecryptTokenInternal(INxlFileFingerPrint fp,
                                           boolean localLoad,
                                           boolean checkValidity)
            throws TokenException, TokenAccessDenyException, RmsRestAPIException {
        if (fp == null) {
            throw new TokenException("Params INxlFileFingerPrint must not be null");
        }
        String membershipId = fp.getOwnerID();
        if (containsDKey(membershipId)) {
            DecryptTokenPool dtp;
            synchronized (mDLock) {
                dtp = mDecryptTokens.get(membershipId);
            }
            if (dtp == null) {
                throw new TokenException("Failed to get decrypt token pool.");
            }
            //Find in mem.
            IToken iLToken = dtp.findOne(fp.getDUID());
            if (iLToken == null) {
                //If local load mode. find in local.
                if (localLoad) {
                    iLToken = mLocalTokenPool.getToken(membershipId, fp.getDUID());
                }
            }
            if (iLToken == null) {
                //Fetch from remote.
                iLToken = dtp.fetchOne(mTenantName, fp);
            }
            if (checkValidity) {
                if (mLocalTokenPool.isOfflineToken(membershipId, fp.getDUID())) {
                    mLocalTokenPool.checkExpired(membershipId, fp.getDUID());
                }
            }
            return iLToken;
        } else {
            DecryptTokenPool dtp = new DecryptTokenPool();
            IToken iRToken = null;
            if (localLoad) {
                iRToken = mLocalTokenPool.getToken(membershipId, fp.getDUID());
            }
            if (iRToken == null) {
                iRToken = dtp.fetchOne(mTenantName, fp);
            }
            if (checkValidity) {
                if (mLocalTokenPool.isOfflineToken(membershipId, fp.getDUID())) {
                    mLocalTokenPool.checkExpired(membershipId, fp.getDUID());
                }
            }
            return iRToken;
        }
    }

    private IToken getDecryptTokenInternal(int sharedSpaceType,
                                           int sharedSpaceId,
                                           String sharedSpaceUserMembership,
                                           INxlFileFingerPrint fp,
                                           boolean localLoad,
                                           boolean checkValidity)
            throws TokenException, TokenAccessDenyException, RmsRestAPIException {
        if (fp == null) {
            throw new TokenException("Params INxlFileFingerPrint must not be null");
        }
        if (containsDKey(sharedSpaceUserMembership)) {
            DecryptTokenPool dtp;
            synchronized (mDLock) {
                dtp = mDecryptTokens.get(sharedSpaceUserMembership);
            }
            if (dtp == null) {
                throw new TokenException("Failed to get decrypt token pool.");
            }
            //Find in mem.
            IToken iLToken = dtp.findOne(fp.getDUID());
            if (iLToken == null) {
                //If local load mode. find in local.
                if (localLoad) {
                    iLToken = mLocalTokenPool.getToken(sharedSpaceUserMembership, fp.getDUID());
                }
            }
            if (iLToken == null) {
                //Fetch from remote.
                iLToken = dtp.fetchOne(mTenantName, fp,
                        sharedSpaceType, sharedSpaceId, sharedSpaceUserMembership);
            }
            if (checkValidity) {
                if (mLocalTokenPool.isOfflineToken(sharedSpaceUserMembership, fp.getDUID())) {
                    mLocalTokenPool.checkExpired(sharedSpaceUserMembership, fp.getDUID());
                }
            }
            return iLToken;
        } else {
            DecryptTokenPool dtp = new DecryptTokenPool();
            IToken iRToken = null;
            if (localLoad) {
                iRToken = mLocalTokenPool.getToken(sharedSpaceUserMembership, fp.getDUID());
            }
            if (iRToken == null) {
                iRToken = dtp.fetchOne(mTenantName, fp,
                        sharedSpaceType, sharedSpaceId, sharedSpaceUserMembership);
            }
            if (checkValidity) {
                if (mLocalTokenPool.isOfflineToken(sharedSpaceUserMembership, fp.getDUID())) {
                    mLocalTokenPool.checkExpired(sharedSpaceUserMembership, fp.getDUID());
                }
            }
            return iRToken;
        }
    }

    private IToken tryGetOneEKeyFromCache(String membershipId) {
        if (membershipId == null || membershipId.isEmpty()) {
            return null;
        }
        IToken ret = null;
        if (containsEKey(membershipId)) {
            synchronized (mELock) {
                ret = mEncryptTokens.get(membershipId).findOne();
            }
        }
        return ret;
    }

    private boolean containsEKey(String membershipId) {
        if (membershipId == null || membershipId.isEmpty()) {
            return false;
        }
        synchronized (mELock) {
            return mEncryptTokens.containsKey(membershipId);
        }
    }

    private boolean containsDKey(String membershipId) {
        if (membershipId == null || membershipId.isEmpty()) {
            return false;
        }
        synchronized (mDLock) {
            return mDecryptTokens.containsKey(membershipId);
        }
    }

    private boolean cacheEKey(String membershipId, EncryptTokenPool pool) {
        if (membershipId == null || membershipId.isEmpty()) {
            return false;
        }
        if (pool == null) {
            return false;
        }
        synchronized (mELock) {
            mEncryptTokens.put(membershipId, pool);
            return true;
        }
    }

    private class EncryptTokenPool {
        private final Set<IToken> mTokens = new HashSet<>();
        private final Object mTtLock = new Object();

        EncryptTokenPool() {
        }

        EncryptTokenPool fillPool(String membershipId) throws TokenException, RmsRestAPIException {
            Map<String, String> remoteEncryptToken = getRemoteEncryptToken(membershipId);
            if (remoteEncryptToken == null || remoteEncryptToken.isEmpty()) {
                throw new TokenException("Failed to find any encryption key from rms.");
            }
            for (Map.Entry<String, String> entry : remoteEncryptToken.entrySet()) {
                if (entry == null) {
                    continue;
                }
                try {
                    String duid = entry.getKey();
                    JSONObject tkObj = new JSONObject(entry.getValue());
                    String token = tkObj.optString("token");
                    String otp = tkObj.optString("otp");

                    synchronized (mTtLock) {
                        mTokens.add(new EncryptToken(duid, token, otp));
                    }

                } catch (JSONException e) {
                    throw new TokenException(e.getMessage(), e);
                }
            }
            return this;
        }

        boolean isEmpty() {
            synchronized (mTtLock) {
                return mTokens.isEmpty();
            }
        }

        IToken findOne() {
            if (!isEmpty()) {
                synchronized (mTtLock) {
                    IToken one = mTokens.iterator().next();
                    mTokens.remove(one);
                    return one;
                }
            }
            return null;
        }

        private Map<String, String> getRemoteEncryptToken(String membershipId)
                throws TokenException, RmsRestAPIException {
            try {
                return mApi.getTokenService(mUser)
                        .getEncryptionToken(mDh.getAgreementKey(), membershipId, 100);
            } catch (RmsRestAPIException e) {
                throw e;
            } catch (Exception e) {
                throw new TokenException(e.getMessage(), e);
            }
        }
    }

    private class DecryptTokenPool {
        private final Set<IToken> mTokens = new HashSet<>();
        private final Object mDtLock = new Object();

        DecryptTokenPool() {

        }

        boolean isEmpty() {
            synchronized (mDtLock) {
                return mTokens.isEmpty();
            }
        }

        boolean contains(IToken t) {
            return t != null && mTokens.contains(t);
        }

        boolean setOne(IToken t) {
            if (t == null) {
                return false;
            }
            String token = t.getTokenStr();
            if (token == null || token.isEmpty()) {
                return false;
            }
            synchronized (mDtLock) {
                return mTokens.add(t);
            }
        }

        String getOtp(String duid) {
            synchronized (mDtLock) {
                IToken one = null;
                for (IToken tk : mTokens) {
                    if (tk.getDuid().equals(duid)) {
                        one = tk;
                        break;
                    }
                }
                if (one != null) {
                    return one.getOtp();
                }
            }
            return "";
        }

        IToken findOne(String duid) {
            //Sanity check.
            if (duid == null || duid.isEmpty()) {
                return null;
            }

            IToken one = null;
            synchronized (mDtLock) {
                for (IToken t : mTokens) {
                    if (TextUtils.equals(t.getDuid(), duid)) {
                        one = t;
                        break;
                    }
                }
            }
            return one;
        }

        IToken fetchOne(String tenantName, INxlFileFingerPrint fp)
                throws RmsRestAPIException, TokenAccessDenyException {
            String remoteDecryptToken = getRemoteDecryptToken(tenantName, fp);
            return new DecryptToken(fp.getDUID(), remoteDecryptToken, fp.getOtp());
        }

        IToken fetchOne(String tenantName,
                        INxlFileFingerPrint fp,
                        int sharedSpaceType,
                        int sharedSpaceId,
                        String sharedSpaceUserMembership)
                throws RmsRestAPIException, TokenAccessDenyException {
            String remoteDecryptToken = getRemoteDecryptToken(tenantName, fp,
                    sharedSpaceType, sharedSpaceId, sharedSpaceUserMembership);
            return new DecryptToken(fp.getDUID(), remoteDecryptToken, fp.getOtp());
        }

        boolean removeOne(String duid) {
            if (duid == null || duid.isEmpty()) {
                return false;
            }

            synchronized (mDtLock) {
                if (mTokens.size() == 0) {
                    return false;
                }
                IToken target = null;
                for (IToken tk : mTokens) {
                    if (tk == null) {
                        continue;
                    }
                    if (tk.getDuid().equals(duid)) {
                        target = tk;
                        break;
                    }
                }

                if (target != null) {
                    return mTokens.remove(target);
                }
            }

            return false;
        }

        private String getRemoteDecryptToken(String tenantName, INxlFileFingerPrint fp)
                throws TokenAccessDenyException, RmsRestAPIException {
            return mApi.getTokenService(mUser).getDecryptionToken(tenantName, fp);
        }

        private String getRemoteDecryptToken(String tenantName,
                                             INxlFileFingerPrint fp,
                                             int sharedSpaceType,
                                             int sharedSpaceId,
                                             String sharedSpaceUserMembership)
                throws TokenAccessDenyException, RmsRestAPIException {
            return mApi.getTokenService(mUser).getDecryptionToken(tenantName, fp,
                    sharedSpaceType, sharedSpaceId, sharedSpaceUserMembership);
        }
    }

    private class LocalDecryptTokenPool {
        private final Map<String, List<IToken>> mLTokenStore = new HashMap<>();
        private final Object mLTLock = new Object();
        private Context mCtx;

        private void buildStore(Context ctx) {
            try {
                if (ctx == null) {
                    return;
                }
                this.mCtx = ctx;
                SharedPreferences localTokenPool = mCtx.getSharedPreferences("LocalTokenPool",
                        Context.MODE_PRIVATE);
                String security = localTokenPool.getString(mTenantName, "");
                if (security == null || security.isEmpty()) {
                    return;
                }
                JSONArray tenantTokenArr = new JSONArray(security);
                for (int i = 0; i < tenantTokenArr.length(); i++) {
                    JSONObject memberShipObj = tenantTokenArr.optJSONObject(i);
                    if (memberShipObj == null) {
                        continue;
                    }
                    String membership = memberShipObj.optString("membership");
                    JSONArray tokenArr = memberShipObj.optJSONArray("tokens");
                    if (tokenArr == null) {
                        continue;
                    }
                    if (mLTokenStore.containsKey(membership)) {
                        List<IToken> tokens = mLTokenStore.get(membership);
                        if (tokens == null) {
                            List<IToken> values = new ArrayList<>();
                            for (int j = 0; j < tokenArr.length(); j++) {
                                String tokenRaw = tokenArr.optString(j);
                                if (tokenRaw == null || tokenRaw.isEmpty()) {
                                    continue;
                                }
                                values.add(SecureToken.buildFromJson(tokenRaw));
                            }
                            mLTokenStore.put(membership, values);
                        } else {
                            for (int j = 0; j < tokenArr.length(); j++) {
                                String tokenRaw = tokenArr.optString(j);
                                if (tokenRaw == null || tokenRaw.isEmpty()) {
                                    continue;
                                }
                                tokens.add(SecureToken.buildFromJson(tokenRaw));
                            }
                        }
                    } else {
                        List<IToken> tokens = new ArrayList<>();
                        for (int j = 0; j < tokenArr.length(); j++) {
                            String tokenRaw = tokenArr.optString(j);
                            if (tokenRaw == null || tokenRaw.isEmpty()) {
                                continue;
                            }
                            tokens.add(SecureToken.buildFromJson(tokenRaw));
                        }
                        mLTokenStore.put(membership, tokens);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean serialToken(String membership, IToken token) throws TokenException {
            //Sanity check.
            if (membership == null || membership.isEmpty()) {
                throw new TokenException("Membership is required when performing serial token action.");
            }
            if (mCtx == null || token == null) {
                return false;
            }
            synchronized (mLTLock) {
                if (mLTokenStore.containsKey(membership)) {
                    List<IToken> iTokens = mLTokenStore.get(membership);
                    if (iTokens == null) {
                        List<IToken> values = new ArrayList<>();
                        values.add(token);
                        mLTokenStore.put(membership, values);
                    } else {
                        List<IToken> clearList = new ArrayList<>();
                        for (IToken tk : iTokens) {
                            if (tk.getDuid().equals(token.getDuid())) {
                                clearList.add(tk);
                            }
                        }
                        if (!clearList.isEmpty()) {
                            for (IToken tk : clearList) {
                                iTokens.remove(tk);
                            }
                        }
                        iTokens.add(SecureToken.buildSecureToken(token));
                    }
                } else {
                    List<IToken> values = new ArrayList<>();
                    values.add(SecureToken.buildSecureToken(token));
                    mLTokenStore.put(membership, values);
                }
            }
            return fireToSerialize(mLTokenStore);
        }

        private IToken getToken(String membership, String duid) {
            //Sanity check.
            if (membership == null || membership.isEmpty()) {
                return null;
            }
            if (duid == null || duid.isEmpty()) {
                return null;
            }
            synchronized (mLTLock) {
                if (mLTokenStore.containsKey(membership)) {
                    List<IToken> tokens = mLTokenStore.get(membership);
                    if (tokens == null || tokens.isEmpty()) {
                        return null;
                    }
                    for (IToken tk : tokens) {
                        if (tk.getDuid().equals(duid)) {
                            return tk;
                        }
                    }
                }
                return null;
            }
        }

        private boolean fireToSerialize(Map<String, List<IToken>> tokens) throws TokenException {
            if (tokens == null || tokens.isEmpty()) {
                return false;
            }
            JSONArray tenantTokenArr = new JSONArray();
            try {
                for (String m : tokens.keySet()) {
                    JSONObject memberShipTokenObj = new JSONObject();
                    if (m == null || m.isEmpty()) {
                        continue;
                    }
                    List<IToken> iTokens = tokens.get(m);
                    if (iTokens == null || iTokens.isEmpty()) {
                        continue;
                    }
                    JSONArray memberShipTokenArr = new JSONArray();
                    for (IToken tk : iTokens) {
                        if (tk instanceof SecureToken) {
                            memberShipTokenArr.put(SecureToken.serialToLocal((SecureToken) tk));
                        }
                    }
                    memberShipTokenObj.put("membership", m);
                    memberShipTokenObj.put("tokens", memberShipTokenArr);
                    tenantTokenArr.put(memberShipTokenObj);
                }
            } catch (JSONException e) {
                throw new TokenException(e.getMessage(), e);
            }
            SharedPreferences localTokenPool = mCtx.getSharedPreferences("LocalTokenPool",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = localTokenPool.edit();
            editor.clear();
            editor.apply();
            editor.putString(mTenantName, tenantTokenArr.toString());
            editor.apply();
            return true;
        }

        private void checkExpired(SecureToken expiry) throws TokenException {
            if (expiry == null) {
                throw new TokenException("The serialized token is missing.");
            }
            if (expiry.isExpired(getNow())) {
                //removeExpiry(expiry);
                throw new TokenException(TokenException.FAILED_TOKEN_EXPIRED, "The target token is expired. Please try it later.");
            }
        }

        private void checkExpired(String memberShip, String duid) throws TokenException {
            if (memberShip == null || memberShip.isEmpty()) {
                return;
            }
            if (duid == null || duid.isEmpty()) {
                return;
            }
            if (mLTokenStore.isEmpty()) {
                return;
            }
            IToken val = null;
            if (mLTokenStore.containsKey(memberShip)) {
                List<IToken> tokens = mLTokenStore.get(memberShip);
                if (tokens == null || tokens.isEmpty()) {
                    return;
                }
                for (IToken tk : tokens) {
                    if (tk.getDuid().equals(duid)) {
                        val = tk;
                        break;
                    }
                }
                if (val instanceof SecureToken) {
                    checkExpired((SecureToken) val);
                }
            }
        }

        private boolean removeExpiry(String memberShip, SecureToken expiry) throws TokenException {
            if (memberShip == null || memberShip.isEmpty()) {
                return false;
            }
            if (expiry == null) {
                return false;
            }
            if (mLTokenStore.containsKey(memberShip)) {
                List<IToken> tokens = mLTokenStore.get(memberShip);
                if (tokens == null || tokens.isEmpty()) {
                    return false;
                }
                tokens.remove(expiry);
                return fireToSerialize(mLTokenStore);
            }
            return false;
        }

        private boolean isOfflineToken(String memberShip, String duid) {
            if (memberShip == null || memberShip.isEmpty()) {
                return false;
            }
            if (mLTokenStore.isEmpty()) {
                return false;
            }
            if (duid == null || duid.isEmpty()) {
                return false;
            }
            if (mLTokenStore.containsKey(memberShip)) {
                List<IToken> tokens = mLTokenStore.get(memberShip);
                if (tokens == null || tokens.isEmpty()) {
                    return false;
                }
                for (IToken tk : tokens) {
                    if (tk == null) {
                        continue;
                    }
                    if (tk.getDuid().equals(duid)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean remove(String memberShip, String duid) throws TokenException {
            if (memberShip == null || memberShip.isEmpty()) {
                return false;
            }
            if (duid == null || duid.isEmpty()) {
                return false;
            }
            if (mLTokenStore.isEmpty()) {
                return false;
            }
            synchronized (mLTLock) {
                if (mLTokenStore.containsKey(memberShip)) {
                    List<IToken> tokens = mLTokenStore.get(memberShip);
                    if (tokens == null || tokens.isEmpty()) {
                        return false;
                    }
                    Iterator<IToken> it = tokens.iterator();
                    while (it.hasNext()) {
                        IToken next = it.next();
                        if (TextUtils.equals(duid, next.getDuid())) {
                            it.remove();
                            break;
                        }
                    }
                    return fireToSerialize(mLTokenStore);
                }
            }
            return false;
        }

        private long getNow() {
            return Calendar.getInstance(Locale.getDefault()).getTimeInMillis();
        }
    }
}
