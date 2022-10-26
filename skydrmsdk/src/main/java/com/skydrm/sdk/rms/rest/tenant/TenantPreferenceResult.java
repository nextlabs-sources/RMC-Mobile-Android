package com.skydrm.sdk.rms.rest.tenant;

import java.util.List;

public class TenantPreferenceResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1575880146871
     * extra : {"CLIENT_HEARTBEAT_FREQUENCY":"5","SYSTEM_BUCKET_NAME":"skydrm245_policytest_system","ADHOC_ENABLED":true,"PROJECT_ADMIN":["aa@aa.com","jimmy.carter@qapf1.qalab01.nextlabs.com","abraham.lincoln@qapf1.qalab01.nextlabs.com"],"RMC_CURRENT_VERSION":"","RMC_UPDATE_URL_32BITS":"","RMC_CRC_CHECKSUM_32BITS":"","RMC_SHA1_CHECKSUM_32BITS":"","RMC_UPDATE_URL_64BITS":"","RMC_CRC_CHECKSUM_64BITS":"","RMC_SHA1_CHECKSUM_64BITS":"","RMC_MAC_CURRENT_VERSION":"","RMC_CRC_CHECKSUM_MAC":"","RMC_SHA1_CHECKSUM_MAC":"","RMC_FORCE_DOWNGRADE":false,"RMD_WIN_32_DOWNLOAD_URL":"","RMD_WIN_64_DOWNLOAD_URL":"","RMD_MAC_DOWNLOAD_URL":"","RMC_IOS_DOWNLOAD_URL":"","RMC_ANDROID_DOWNLOAD_URL":""}
     */

    private int statusCode;
    private String message;
    private long serverTime;
    private ExtraBean extra;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public ExtraBean getExtra() {
        return extra;
    }

    public void setExtra(ExtraBean extra) {
        this.extra = extra;
    }

    public static class ExtraBean {
        /**
         * CLIENT_HEARTBEAT_FREQUENCY : 5
         * SYSTEM_BUCKET_NAME : skydrm245_policytest_system
         * ADHOC_ENABLED : true
         * PROJECT_ADMIN : ["aa@aa.com","jimmy.carter@qapf1.qalab01.nextlabs.com","abraham.lincoln@qapf1.qalab01.nextlabs.com"]
         * RMC_CURRENT_VERSION :
         * RMC_UPDATE_URL_32BITS :
         * RMC_CRC_CHECKSUM_32BITS :
         * RMC_SHA1_CHECKSUM_32BITS :
         * RMC_UPDATE_URL_64BITS :
         * RMC_CRC_CHECKSUM_64BITS :
         * RMC_SHA1_CHECKSUM_64BITS :
         * RMC_MAC_CURRENT_VERSION :
         * RMC_CRC_CHECKSUM_MAC :
         * RMC_SHA1_CHECKSUM_MAC :
         * RMC_FORCE_DOWNGRADE : false
         * RMD_WIN_32_DOWNLOAD_URL :
         * RMD_WIN_64_DOWNLOAD_URL :
         * RMD_MAC_DOWNLOAD_URL :
         * RMC_IOS_DOWNLOAD_URL :
         * RMC_ANDROID_DOWNLOAD_URL :
         */

        private String CLIENT_HEARTBEAT_FREQUENCY;
        private String SYSTEM_BUCKET_NAME;
        private boolean ADHOC_ENABLED;
        private String RMC_CURRENT_VERSION;
        private String RMC_UPDATE_URL_32BITS;
        private String RMC_CRC_CHECKSUM_32BITS;
        private String RMC_SHA1_CHECKSUM_32BITS;
        private String RMC_UPDATE_URL_64BITS;
        private String RMC_CRC_CHECKSUM_64BITS;
        private String RMC_SHA1_CHECKSUM_64BITS;
        private String RMC_MAC_CURRENT_VERSION;
        private String RMC_CRC_CHECKSUM_MAC;
        private String RMC_SHA1_CHECKSUM_MAC;
        private boolean RMC_FORCE_DOWNGRADE;
        private String RMD_WIN_32_DOWNLOAD_URL;
        private String RMD_WIN_64_DOWNLOAD_URL;
        private String RMD_MAC_DOWNLOAD_URL;
        private String RMC_IOS_DOWNLOAD_URL;
        private String RMC_ANDROID_DOWNLOAD_URL;
        private List<String> PROJECT_ADMIN;

        public String getCLIENT_HEARTBEAT_FREQUENCY() {
            return CLIENT_HEARTBEAT_FREQUENCY;
        }

        public void setCLIENT_HEARTBEAT_FREQUENCY(String CLIENT_HEARTBEAT_FREQUENCY) {
            this.CLIENT_HEARTBEAT_FREQUENCY = CLIENT_HEARTBEAT_FREQUENCY;
        }

        public String getSYSTEM_BUCKET_NAME() {
            return SYSTEM_BUCKET_NAME;
        }

        public void setSYSTEM_BUCKET_NAME(String SYSTEM_BUCKET_NAME) {
            this.SYSTEM_BUCKET_NAME = SYSTEM_BUCKET_NAME;
        }

        public boolean isADHOC_ENABLED() {
            return ADHOC_ENABLED;
        }

        public void setADHOC_ENABLED(boolean ADHOC_ENABLED) {
            this.ADHOC_ENABLED = ADHOC_ENABLED;
        }

        public String getRMC_CURRENT_VERSION() {
            return RMC_CURRENT_VERSION;
        }

        public void setRMC_CURRENT_VERSION(String RMC_CURRENT_VERSION) {
            this.RMC_CURRENT_VERSION = RMC_CURRENT_VERSION;
        }

        public String getRMC_UPDATE_URL_32BITS() {
            return RMC_UPDATE_URL_32BITS;
        }

        public void setRMC_UPDATE_URL_32BITS(String RMC_UPDATE_URL_32BITS) {
            this.RMC_UPDATE_URL_32BITS = RMC_UPDATE_URL_32BITS;
        }

        public String getRMC_CRC_CHECKSUM_32BITS() {
            return RMC_CRC_CHECKSUM_32BITS;
        }

        public void setRMC_CRC_CHECKSUM_32BITS(String RMC_CRC_CHECKSUM_32BITS) {
            this.RMC_CRC_CHECKSUM_32BITS = RMC_CRC_CHECKSUM_32BITS;
        }

        public String getRMC_SHA1_CHECKSUM_32BITS() {
            return RMC_SHA1_CHECKSUM_32BITS;
        }

        public void setRMC_SHA1_CHECKSUM_32BITS(String RMC_SHA1_CHECKSUM_32BITS) {
            this.RMC_SHA1_CHECKSUM_32BITS = RMC_SHA1_CHECKSUM_32BITS;
        }

        public String getRMC_UPDATE_URL_64BITS() {
            return RMC_UPDATE_URL_64BITS;
        }

        public void setRMC_UPDATE_URL_64BITS(String RMC_UPDATE_URL_64BITS) {
            this.RMC_UPDATE_URL_64BITS = RMC_UPDATE_URL_64BITS;
        }

        public String getRMC_CRC_CHECKSUM_64BITS() {
            return RMC_CRC_CHECKSUM_64BITS;
        }

        public void setRMC_CRC_CHECKSUM_64BITS(String RMC_CRC_CHECKSUM_64BITS) {
            this.RMC_CRC_CHECKSUM_64BITS = RMC_CRC_CHECKSUM_64BITS;
        }

        public String getRMC_SHA1_CHECKSUM_64BITS() {
            return RMC_SHA1_CHECKSUM_64BITS;
        }

        public void setRMC_SHA1_CHECKSUM_64BITS(String RMC_SHA1_CHECKSUM_64BITS) {
            this.RMC_SHA1_CHECKSUM_64BITS = RMC_SHA1_CHECKSUM_64BITS;
        }

        public String getRMC_MAC_CURRENT_VERSION() {
            return RMC_MAC_CURRENT_VERSION;
        }

        public void setRMC_MAC_CURRENT_VERSION(String RMC_MAC_CURRENT_VERSION) {
            this.RMC_MAC_CURRENT_VERSION = RMC_MAC_CURRENT_VERSION;
        }

        public String getRMC_CRC_CHECKSUM_MAC() {
            return RMC_CRC_CHECKSUM_MAC;
        }

        public void setRMC_CRC_CHECKSUM_MAC(String RMC_CRC_CHECKSUM_MAC) {
            this.RMC_CRC_CHECKSUM_MAC = RMC_CRC_CHECKSUM_MAC;
        }

        public String getRMC_SHA1_CHECKSUM_MAC() {
            return RMC_SHA1_CHECKSUM_MAC;
        }

        public void setRMC_SHA1_CHECKSUM_MAC(String RMC_SHA1_CHECKSUM_MAC) {
            this.RMC_SHA1_CHECKSUM_MAC = RMC_SHA1_CHECKSUM_MAC;
        }

        public boolean isRMC_FORCE_DOWNGRADE() {
            return RMC_FORCE_DOWNGRADE;
        }

        public void setRMC_FORCE_DOWNGRADE(boolean RMC_FORCE_DOWNGRADE) {
            this.RMC_FORCE_DOWNGRADE = RMC_FORCE_DOWNGRADE;
        }

        public String getRMD_WIN_32_DOWNLOAD_URL() {
            return RMD_WIN_32_DOWNLOAD_URL;
        }

        public void setRMD_WIN_32_DOWNLOAD_URL(String RMD_WIN_32_DOWNLOAD_URL) {
            this.RMD_WIN_32_DOWNLOAD_URL = RMD_WIN_32_DOWNLOAD_URL;
        }

        public String getRMD_WIN_64_DOWNLOAD_URL() {
            return RMD_WIN_64_DOWNLOAD_URL;
        }

        public void setRMD_WIN_64_DOWNLOAD_URL(String RMD_WIN_64_DOWNLOAD_URL) {
            this.RMD_WIN_64_DOWNLOAD_URL = RMD_WIN_64_DOWNLOAD_URL;
        }

        public String getRMD_MAC_DOWNLOAD_URL() {
            return RMD_MAC_DOWNLOAD_URL;
        }

        public void setRMD_MAC_DOWNLOAD_URL(String RMD_MAC_DOWNLOAD_URL) {
            this.RMD_MAC_DOWNLOAD_URL = RMD_MAC_DOWNLOAD_URL;
        }

        public String getRMC_IOS_DOWNLOAD_URL() {
            return RMC_IOS_DOWNLOAD_URL;
        }

        public void setRMC_IOS_DOWNLOAD_URL(String RMC_IOS_DOWNLOAD_URL) {
            this.RMC_IOS_DOWNLOAD_URL = RMC_IOS_DOWNLOAD_URL;
        }

        public String getRMC_ANDROID_DOWNLOAD_URL() {
            return RMC_ANDROID_DOWNLOAD_URL;
        }

        public void setRMC_ANDROID_DOWNLOAD_URL(String RMC_ANDROID_DOWNLOAD_URL) {
            this.RMC_ANDROID_DOWNLOAD_URL = RMC_ANDROID_DOWNLOAD_URL;
        }

        public List<String> getPROJECT_ADMIN() {
            return PROJECT_ADMIN;
        }

        public void setPROJECT_ADMIN(List<String> PROJECT_ADMIN) {
            this.PROJECT_ADMIN = PROJECT_ADMIN;
        }
    }
}
