package com.skydrm.sdk.nxl;

import android.support.annotation.Nullable;

import com.skydrm.sdk.INxlExpiry;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.INxlObligations;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.INxlTags;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Rights;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
for version 1; total bytes is 804
    rootAgreementKey    [256]
    icaAgreementKey     [256]
    ownerid             [256];
    duid                [32]
    ml                  4;
 */
public class NxlFingerPrint implements INxlFileFingerPrint {
    public String ownerId;
    public String duid;
    public int ml = 0;
    public String rootAgreementKey;
    public String icaAgreementKey;
    public String otp;

    private INxlRights rights;
    private INxlObligations obligations;
    private INxlExpiry expiry;
    private INxlTags tags;

    private String mADHocSectionRaw;
    private String mCentralSectionRaw;

    private long mLastModifiedTime;
    private String mNormalFileName;

    public void setRights(INxlRights rights) {
        this.rights = rights;
    }

    public void setObligations(INxlObligations obligations) {
        this.obligations = obligations;
    }

    public void setExpiry(INxlExpiry expiry) {
        this.expiry = expiry;
    }

    public void setTags(INxlTags tags) {
        this.tags = tags;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    @Override
    public boolean hasRights() {
        return rights != null;
    }

    @Override
    public boolean hasObligations() {
        return obligations != null;
    }

    @Override
    public boolean hasExpiry() {
        return true;    // as defined, fingerprint always has expiry, default is Never
    }

    @Override
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    public void setADHocSectionRaw(String raw) {
        this.mADHocSectionRaw = raw;
    }

    public void setCentralSectionRaw(String raw) {
        this.mCentralSectionRaw = raw;
    }

    @Override
    public String getADHocSectionRaw() {
        return mADHocSectionRaw;
    }

    @Override
    public String getCentralSectionRaw() {
        return mCentralSectionRaw;
    }

    @Override
    public long getLastModifiedTime() {
        return mLastModifiedTime;
    }

    public void setNormalFileName(String mNormalFileName) {
        this.mNormalFileName = mNormalFileName;
    }

    @Override
    public String getNormalFileName() {
        return mNormalFileName;
    }

    @Override
    public String getDisplayWatermark() {
        if (obligations == null) {
            return "";
        }
        return obligations.getDisplayWatermark();
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.mLastModifiedTime = lastModifiedTime;
    }

    @Override
    public boolean isEmpty() {
        return tags == null || tags.isEmpty();
    }

    @Override
    public boolean isContain(String key) {
        return !isEmpty() && tags.isContain(key);
    }

    @Override
    public Set<String> find(String key) {
        if (isEmpty()) {
            return null;
        }
        return tags.find(key);
    }

    @Override
    public Map<String, Set<String>> getAll() {
        if (isEmpty()) {
            return null;
        }
        return tags.getAll();
    }

    @Override
    public boolean isExpired() {
        return expiry.isExpired();
    }

    @Override
    public boolean isExpired(long currentMills) {
        return expiry.isExpired(currentMills);
    }

    @Override
    public boolean isFuture() {
        return expiry.isFuture();
    }

    @Override
    public String formatString() {
        return expiry.formatString();
    }

    @Override
    public Expiry getExpiry() {
        if (expiry instanceof Expiry) {
            return (Expiry) expiry;
        }
        return null;
    }

    @Override
    public String getDUID() {
        return duid;
    }

    @Override
    public String getOwnerID() {
        return ownerId;
    }

    @Override
    public int getMaintenanceLevel() {
        return ml;
    }

    @Override
    public String getRootAgreementKey() {
        return rootAgreementKey;
    }

    @Override
    public String getICAAgreementKey() {
        return icaAgreementKey;
    }

    @Override
    public String getOtp() {
        return otp;
    }

    @Nullable
    @Override
    public Iterator<Map.Entry<String, String>> getIterator() {
        if (obligations == null) {
            return null;
        }
        return obligations.getIterator();
    }

    @Override
    public boolean hasWatermark() {
        return ((obligations != null) && obligations.hasWatermark())
                || ((rights != null && rights.hasWatermark())); // used to compatible with older.
    }

    @Override
    public boolean hasClassify() {
        if (rights == null) {
            return false;
        }
        return rights.hasClassify();
    }

    @Override
    public boolean hasView() {
        if (rights == null) {
            return false;
        }
        return rights.hasView();
    }

    @Override
    public boolean hasEdit() {
        if (rights == null) {
            return false;
        }
        return rights.hasEdit();
    }

    @Override
    public boolean hasPrint() {
        if (rights == null) {
            return false;
        }
        return rights.hasPrint();
    }

    @Override
    public boolean hasShare() {
        if (rights == null) {
            return false;
        }
        return rights.hasShare();
    }

    @Override
    public boolean hasDecrypt() {
        if (rights == null) {
            return false;
        }
        return rights.hasDecrypt();
    }

    @Override
    public boolean hasDownload() {
        if (rights == null) {
            return false;
        }
        return rights.hasDownload();
    }

    @Override
    public String toJsonFormat() {
        if (tags == null) {
            return "{}";
        }
        return tags.toJsonFormat();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("NxlFingerPrint:[");
        if (ownerId != null && !ownerId.isEmpty()) {
            sb.append("ownerId:" + ownerId + " ");
        } else {
            sb.append("ownerId:null" + " ");
        }
        if (duid != null && !duid.isEmpty()) {
            sb.append("duid:" + duid + " ");
        } else {
            sb.append("duid:null" + " ");
        }
        sb.append("ml:" + ml + " ");
        if (rights != null) {
            sb.append(rights.toString());
        } else {
            sb.append("rights:null ");
        }
        if (obligations != null) {
            sb.append(obligations.toString());
        }
        if (expiry != null) {
            sb.append(expiry.formatString());
        }
        if (tags != null) {
            sb.append(tags.toJsonFormat());
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int toInteger() {
        if (rights == null) {
            return -1;
        }
        return rights.toInteger();
    }

    @Override
    public List<String> toList() {
        if (rights == null) {
            return Collections.emptyList();
        }
        return rights.toList();
    }

    public byte[] toByteBuffer(int version) {
        int size = 0;
        if (version == 1) {
            size = 804;
        }
        if (version == 2) {
            size = 836;
        }
        return new byte[size];
    }

    public boolean fromByteBuffer(byte[] bytes, int version) {
        if (version == 1) {
            // fill all member data from bytes
            rootAgreementKey = bytesToHexString(Arrays.copyOfRange(bytes, 0, 256));
            icaAgreementKey = bytesToHexString(Arrays.copyOfRange(bytes, 256, 256 + 256));
            int endpos = 0;
            while (bytes[256 + 256 + endpos++] != 0) ;
            // filter out owerId's extra \u0000;
            ownerId = new String(bytes, 256 + 256, endpos - 1);
            duid = new String(bytes, 256 + 256 + 256, 32);
            int posml = 256 + 256 + 256 + 32;
            ml = (bytes[posml] & 0xff) | ((bytes[posml + 1] << 8) & 0xff00)
                    | ((bytes[posml + 2] << 24) >>> 8) | (bytes[posml + 3] << 24);
        }

        if (version == 2) {
            /*for version 2; total bytes is 836
                rootAgreementKey    [256] [0->256]
                icaAgreementKey     [256] [256->256+256]
                ownerid             [256];[256+256,256+256+256]
                duid                [32]  [256+256+256,256+256+256+32]
                ml                  4;
                otp                [32];*/
            // fill all member data from bytes
            rootAgreementKey = bytesToHexString(Arrays.copyOfRange(bytes, 0, 256));
            icaAgreementKey = bytesToHexString(Arrays.copyOfRange(bytes, 256, 256 + 256));

            int endpos = 0;
            while (bytes[256 + 256 + endpos++] != 0) ;
            // filter out owerId's extra \u0000;
            ownerId = new String(bytes, 256 + 256, endpos - 1);
            duid = new String(bytes, 256 + 256 + 256, 32);

            int posml = 256 + 256 + 256 + 32;
            ml = (bytes[posml] & 0xff) | ((bytes[posml + 1] << 8) & 0xff00)
                    | ((bytes[posml + 2] << 24) >>> 8) | (bytes[posml + 3] << 24);

            int endOtpPos = 0;
            while (bytes[256 + 256 + 256 + 32 + 4 + endOtpPos++] != 0) ;
            //filter out otp's extra \u0000;
            otp = new String(bytes, 256 + 256 + 256 + 32 + 4, endOtpPos - 1);
        }
        return true;
    }

    static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    static public boolean validFingerPrint(NxlFingerPrint fp) {
        if (fp.ownerId == null || fp.ownerId.isEmpty()) {
            return false;
        }
        if (fp.duid == null || fp.duid.isEmpty()) {
            return false;
        }
        if (fp.rootAgreementKey == null || fp.rootAgreementKey.isEmpty()) {
            return false;
        }
        return true;
    }

}
