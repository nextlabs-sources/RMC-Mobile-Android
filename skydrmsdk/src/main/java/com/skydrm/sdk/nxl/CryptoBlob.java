package com.skydrm.sdk.nxl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*
try to encapsulate c++ struct into to Java byte[]
----------------------------------
struct _NXL_CRYPTO_TOKEN{
    unsigned char PublicKey[256];   //The public key between member and Root CA
    unsigned char PublicKeyWithiCA[256];  //The public key between member and iCA
    uint32_t      ml;            token's maintenance level
    unsigned char UDID[32];      UDID, unique document id HEX string
    unsigned char EncryptToken[64];     token, used to encrypt cek or decrypt cek HEX string
        };

 sizeof(_NXL_CRYPTO_TOKEN) = 0n612,0x264
----------------------------------
 */
public final class CryptoBlob {

    private String mPublicKeyRootCA;    // -> unsigned char PublicKey[256]
    private String mPublicKeyWithiCA;   // -> unsigned char PublicKeyWithiCA[256];
    private int mMaintenanceLevel;      // -> uint32_t
    private String mOtp;                 // -> unsigned char otp[32];
    private String mDUID;               // -> unsigned char DUID[32]
    private String mToken;              // -> unsigned char EncryptToken[64]

    public CryptoBlob(String agreementKey, String publicKeyInCA, int maintenanceLevel, String duid, String token) {
        this.mPublicKeyRootCA = agreementKey;
        this.mPublicKeyWithiCA = publicKeyInCA;
        this.mMaintenanceLevel = maintenanceLevel;
        this.mDUID = duid;
        this.mToken = token;
    }

    public CryptoBlob(String agreementKey, String publicKeyInCA, int maintenanceLevel, String duid, String token, String otp) {
        this.mPublicKeyRootCA = agreementKey;
        this.mPublicKeyWithiCA = publicKeyInCA;
        this.mMaintenanceLevel = maintenanceLevel;
        this.mDUID = duid;
        this.mToken = token;
        this.mOtp = otp;
    }

    public String getDUID() {
        return mDUID;
    }

    public String getOtp() {
        return mOtp;
    }

    public int getMaintenanceLevel() {
        return mMaintenanceLevel;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CryptoBlob:[");
        sb
                .append("RootCA: ").append(mPublicKeyRootCA)
                .append("\tica: ").append(mPublicKeyWithiCA)
                .append("\totp:").append(mOtp)
                .append("\tDUID: ").append(mDUID)
                .append("\tTOKEN ").append(mToken)
                .append("\tMantanenceLevel: ").append(mMaintenanceLevel)
                .append("]");
        return sb.toString();
    }

    public byte[] toByteBuffer() {
        // sanity check
        // prepare
        /*
         unsigned char PublicKey[256];          |  0
         unsigned char PublicKeyWithiCA[256];   |
         uint32_t      ml;                      |
         unsigned char otp[32];                 |
         unsigned char UDID[32];                |
         unsigned char EncryptToken[64];        |
         */
        // encapsulate
        ByteBuffer blob = ByteBuffer.allocate(644); // 612 for 32bit is ok, but 616 for 64bit is ok
        try {
            blob.order(ByteOrder.LITTLE_ENDIAN);
            if (null == mPublicKeyRootCA) {
                blob.position(256);
            } else {
                blob.put(hexStringToByteArray(mPublicKeyRootCA), 0, 256);
            }
            if (null == mPublicKeyWithiCA) {
                blob.position(512);
            } else {
                blob.put(hexStringToByteArray(mPublicKeyWithiCA), 0, 256);
            }
            blob.putInt(mMaintenanceLevel);
            if (null == mOtp || mOtp.isEmpty()) {
                blob.position(548);
            } else {
                blob.put(mOtp.getBytes("UTF-8"), 0, 32);
            }
            blob.put(mDUID.getBytes("UTF-8"), 0, 32);
            blob.put(mToken.getBytes("UTF-8"), 0, 64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] rt = blob.array();
        return rt;
    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
