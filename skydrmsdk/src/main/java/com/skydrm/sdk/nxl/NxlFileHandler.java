package com.skydrm.sdk.nxl;


import java.io.UnsupportedEncodingException;

public class NxlFileHandler {

    private NxlFileHandler() {
        { /* cannot be instantiated */ }
    }

    /**
     * this utils represents as a BRIDGE to c++ version of NXL2 through JNI
     */
    static {
        System.loadLibrary("openssl_fips_wrapper");
        System.loadLibrary("nxlformat");
    }

    static public boolean isNxlFile(String path, boolean fast) {
        return isMatchNxlFmt(path, fast);
    }

    static public boolean convert(String ownerId, String normalFile,
                                  String nxlFile, CryptoBlob tokenBlob,
                                  boolean overwrite) {
        return convertToNxlFile(ownerId, normalFile, nxlFile, tokenBlob.toByteBuffer(), overwrite);
    }

    static public boolean decrypt(String nxlFile, String normalFile,
                                  CryptoBlob tokenBlob,
                                  boolean overwrite) {
        return decryptToNormalFile(nxlFile, normalFile, tokenBlob.toByteBuffer(), overwrite);
    }

    static public boolean getNxlFingerPrint(String nxlFile, NxlFingerPrint fingerPrint) {
        return getNxlFingerPrint(nxlFile, fingerPrint, 2);
    }


    static public boolean getNxlFingerPrint(String nxlFile, NxlFingerPrint fingerPrint, int version) {
        byte[] buf = fingerPrint.toByteBuffer(version);
        return extractFingerPrint(nxlFile, buf, version) && fingerPrint.fromByteBuffer(buf, version);
    }

    /*
     * using this means you will be acknowledged and follow com.skydrm.rmc.nxl.format.v2 requirement:
         HMAC_SHA256(EncryptToken, CombinedData)
            Key: 64byte
            Message: the message is a combined data --- input data and its length (4 bytes).

            For example, if input data buffer is data, length is size, the combined data is:

            **Combined Data Buffer**

            |-------------------|-------------------------------------|
            |   size (4 bytes)  |                data                 |
            |___________________|_____________________________________|
     */
    static public String calcHmacSha256(String key, String message) {
        byte[] hash = new byte[64];
        byte[] k = new byte[64];
        if (key.length() <= 64) {
            for (int i = 0; i < key.length(); i++) {
                k[i] = (byte) key.charAt(i);
            }
        } else {
            return "";
        }
        byte[] m = message.getBytes();
        if (calcHmacSha256(k, m, hash)) {
            return new String(hash, 0, 64);
        } else {
            return "";
        }
    }

    static public boolean AdhocSectionInfo(String nxl, boolean set, CryptoBlob token, PolicyInfo info) {
        return policySectionInfo(nxl, ".FilePolicy", set, token, info);
    }

    static public boolean CentralSectionInfo(String nxl, boolean set, CryptoBlob token, PolicyInfo info) {
        return policySectionInfo(nxl, ".FileTag", set, token, info);
    }

    static public boolean FileInfoSectionInfo(String nxl, boolean set, CryptoBlob token, PolicyInfo info) {
        return policySectionInfo(nxl, ".FileInfo", set, token, info);
    }

    static private boolean policySectionInfo(String nxl, String sectionName, boolean set, CryptoBlob token, PolicyInfo info) {
        byte[] buf = new byte[0x1000];

        if (token == null) {
            return false;
        }

        if (set) {
            // for set policy info

            // put info into buf
            if (info.rawData.length() >= 0x1000) {
                return false;
            }
            try {
                byte[] b = info.rawData.getBytes("utf-8");
                for (int i = 0; i < b.length; i++) {
                    buf[i] = b[i];
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }

            if (sectionInfo(nxl, sectionName, true, token.toByteBuffer(), buf)) {
                return true;
            } else {
                return false;
            }

        } else {
            // for get
            if (sectionInfo(nxl, sectionName, false, token.toByteBuffer(), buf)) {
                info.rawData = new String(buf);
                info.rawData = info.rawData.substring(0, info.rawData.indexOf(0));
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean readADHocSection(String nxl, PolicyInfo info) {
        return readSection(nxl, ".FilePolicy", info);
    }

    public static boolean readCentralPolicySection(String nxl, PolicyInfo info) {
        return readSection(nxl, ".FileTag", info);
    }

    public static boolean readFileInfoSection(String nxl, PolicyInfo info) {
        return readSection(nxl, ".FileInfo", info);
    }

    private static boolean readSection(String nxl, String sectionName, PolicyInfo info) {
        byte[] buffer = new byte[0x1000];
        if (readSection(nxl, sectionName, buffer)) {
            info.rawData = new String(buffer);
            info.rawData = info.rawData.substring(0, info.rawData.indexOf(0));
            return true;
        }
        return false;
    }

    private static native boolean isMatchNxlFmt(String path, boolean fast);

    private static native boolean convertToNxlFile(String ownerId, String normalFile, String nxlFile,
                                                   byte[] tokenBlob, boolean overwrite);

    private static native boolean decryptToNormalFile(String nxlFile, String normalFile,
                                                      byte[] keyBlob, boolean overwrite);

    private static native boolean extractInfoFromNxlFile(String path, byte[] ownerId, byte[] DUID);

    private static native boolean extractFingerPrint(String nxl, byte[] FingerPrint, int version);

    private static native boolean calcHmacSha256(byte[] key, byte[] scr, byte outHash[]);

    private static native boolean sectionInfo(String nxl, String section, boolean set, byte[] tokenBlob, byte[] info);

    private static native boolean readSection(String nxl, String section, byte[] info);
}
