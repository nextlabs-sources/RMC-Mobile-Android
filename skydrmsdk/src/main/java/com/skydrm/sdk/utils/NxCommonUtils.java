package com.skydrm.sdk.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPOutputStream;

/**
 * Created by aning on 2/15/2017.
 */

public class NxCommonUtils {
    private static final String TAG = "NxCommonUtils";

    public static byte[] md5(byte[] data) throws NoSuchAlgorithmException {
        // sanity check
        if (data == null) {
            throw new RuntimeException("invalid data");
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(data);
    }

    public static String hexifyMD5(byte[] data, boolean uppercase) throws NoSuchAlgorithmException {

        byte[] checksum = md5(data);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < checksum.length; i++) {
            int val = ((int) checksum[i]) & 0xff;
            if (val < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }

        return uppercase ? sb.toString().toUpperCase() : sb.toString().toLowerCase();
    }

     public static String bytesToHexString(byte[] src) {
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

    /**
     * this function used to do gzip compress
     */
    public static byte[] gzipCompress(String strData) {
        if (strData == null || strData.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(strData.getBytes("UTF-8"));
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     *  convert tags string to jsonObject.
     */
    public static JSONObject stringTags2Json(String tags) {
        JSONObject tagsJson = new JSONObject();
        if (TextUtils.isEmpty(tags)) {
            return tagsJson; // empty jsonObject
        }

        try {
            if (tags.contains("|")) {
                String[] tagArray = tags.split("\\|");
                for(String oneTag: tagArray) {
                    String tagName = oneTag.substring(0, oneTag.indexOf("="));
                    String tagValue = oneTag.substring(oneTag.indexOf("=") + 1);
                    if (tagValue.contains(",")) {
                        String[] values = tagValue.split(",");
                        JSONArray valueJsonArray = new JSONArray();
                        for( String oneValue: values) {
                            valueJsonArray.put(oneValue);
                        }
                        tagsJson.put(tagName, valueJsonArray);
                    } else {
                        JSONArray valueJsonArray = new JSONArray();
                        valueJsonArray.put(tagValue);
                        tagsJson.put(tagName, valueJsonArray);
                    }
                }
            } else { // only one tag
                String tagName = tags.substring(0, tags.indexOf("="));
                String tagValue = tags.substring(tags.indexOf("=") + 1);
                if (tagValue.contains(",")) {
                    String[] values = tagValue.split(",");
                    JSONArray valueJsonArray = new JSONArray();
                    for( String oneValue: values) {
                        valueJsonArray.put(oneValue);
                    }
                    tagsJson.put(tagName, valueJsonArray);
                } else {
                    JSONArray valueJsonArray = new JSONArray();
                    valueJsonArray.put(tagValue);
                    tagsJson.put(tagName, valueJsonArray);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "parse tags failed in stringTags2Json--\n");
            return null;
        }

        return tagsJson;
    }
}
