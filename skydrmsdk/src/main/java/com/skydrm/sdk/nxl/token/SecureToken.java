package com.skydrm.sdk.nxl.token;

import android.support.annotation.NonNull;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class SecureToken extends DecryptToken implements IToken.IExpiry, Serializable {
    private static final long serialVersionUID = 6353580421178770481L;

    private String mDuid;
    private String mToken;
    private String mOtp;
    private long mTtl;

    private SecureToken(String duid, String token, String otp, long ttl) {
        super(duid, token, otp);
        this.mDuid = duid;
        this.mToken = token;
        this.mOtp = otp;
        this.mTtl = ttl;
    }

    @Override
    public String getDuid() {
        return mDuid;
    }

    @Override
    public String getTokenStr() {
        return isExpired(System.currentTimeMillis()) ? "" : mToken;
    }

    @Override
    public String getOtp() {
        return mOtp;
    }

    public long getTtl() {
        return mTtl;
    }

    static SecureToken buildSecureToken(IToken tk) {
        return new SecureToken(tk.getDuid(), tk.getTokenStr(), tk.getOtp(), generateExpiryTime());
    }

    static String serialToLocal(SecureToken localToken) {
        if (localToken == null) {
            return "";
        }
        return toBase64Raw(localToken.toString());
    }

    static SecureToken buildFromJson(String tokenRaw) {
        //Sanity check.
        if (tokenRaw == null || tokenRaw.isEmpty()) {
            return new SecureToken("null", "null", "null", 0);
        }
        try {
            JSONObject serialObj = new JSONObject(fromBase64(tokenRaw));
            JSONObject tokenObj = serialObj.optJSONObject("local");
            String checksum = serialObj.optString("checksum");
            if (checksum.equals(generateCheckSum(tokenObj.toString()))) {
                String duid = tokenObj.optString("duid");
                String token = tokenObj.optString("token");
                String otp = tokenObj.optString("otp");
                long ttl = tokenObj.optLong("ttl");

                return new SecureToken(duid, token, otp, ttl);
            }
            return new SecureToken("error", "error", "error", -1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new SecureToken("empty", "empty", "empty", -2);
    }

    private static String toBase64Raw(String raw) {
        return Base64.encodeToString(raw.getBytes(), Base64.DEFAULT);
    }

    private static String fromBase64(String base64Raw) {
        return new String(Base64.decode(base64Raw, Base64.DEFAULT));
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject serialObj = new JSONObject();
        try {
            JSONObject tokenObj = new JSONObject();
            tokenObj.put("duid", mDuid);
            tokenObj.put("token", mToken);
            tokenObj.put("otp", mOtp);
            tokenObj.put("ttl", mTtl);

            serialObj.put("local", tokenObj);
            serialObj.put("checksum", generateCheckSum(tokenObj.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return serialObj.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecureToken that = (SecureToken) o;
        return Objects.equals(mDuid, that.mDuid) &&
                Objects.equals(mToken, that.mToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mDuid, mToken);
    }

    private static String generateCheckSum(String raw) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = raw.getBytes();
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }

    private static String bytes2Hex(byte[] bts) {
        StringBuilder des = new StringBuilder();
        String tmp = null;
        for (byte bt : bts) {
            tmp = (Integer.toHexString(bt & 0xFF));
            if (tmp.length() == 1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }

    @Override
    public boolean isExpired(long now) {
        if (now > mTtl) {
            return true;
        }
        return Math.abs(mTtl - now) > 7 * 24 * 3600 * 1000;
    }

    private static long generateExpiryTime() {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        int curYears = c.get(Calendar.YEAR);
        int curMonths = c.get(Calendar.MONTH);
        int curDays = c.get(Calendar.DAY_OF_MONTH);

        c.set(Calendar.YEAR, curYears);
        c.set(Calendar.MONTH, curMonths);
        c.set(Calendar.DAY_OF_MONTH, curDays + 6);
        c.set(Calendar.AM_PM, 1);
        c.set(Calendar.HOUR, c.getActualMaximum(Calendar.HOUR));
        c.set(Calendar.MINUTE, c.getActualMaximum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getActualMaximum(Calendar.SECOND));
        return c.getTimeInMillis();
    }
}
