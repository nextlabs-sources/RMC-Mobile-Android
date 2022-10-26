package com.skydrm.sdk.rms.rest.myVault;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hhu on 11/15/2017.
 */

public class MyVaultMetaDataResult implements Serializable {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1484527953665
     * results : {"detail":{"name":"Get Started with Dropbox-2017-01-13-14-30-28.pdf.nxl","recipients":["user1@nextlabs.com","user2@nextlabs.com"],"fileLink":"https://rmtest.nextlabs.solutions/rms/main.jsp#/personal/viewSharedFile?d=b8785cd6-632d-405e-973c-27eca6b905a7&c=EEF638B42F19D674D87FC8AE3ADF0C598818FECA6C49A9F34646A8E409F45359","protectedOn":1484289028934,"sharedOn":1484527940420,"rights":["VIEW"],"shared":true,"deleted":false,"revoked":false,"validity":{"startDate":1509638400000,"endDate":1512316799999}}}
     */

    private int statusCode;
    private String message;
    private long serverTime;
    private ResultsBean results;

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

    public ResultsBean getResults() {
        return results;
    }

    public void setResults(ResultsBean results) {
        this.results = results;
    }

    public static class ResultsBean implements Serializable {
        /**
         * detail : {"name":"Get Started with Dropbox-2017-01-13-14-30-28.pdf.nxl","recipients":["user1@nextlabs.com","user2@nextlabs.com"],"fileLink":"https://rmtest.nextlabs.solutions/rms/main.jsp#/personal/viewSharedFile?d=b8785cd6-632d-405e-973c-27eca6b905a7&c=EEF638B42F19D674D87FC8AE3ADF0C598818FECA6C49A9F34646A8E409F45359","protectedOn":1484289028934,"sharedOn":1484527940420,"rights":["VIEW"],"shared":true,"deleted":false,"revoked":false,"validity":{"startDate":1509638400000,"endDate":1512316799999}}
         */

        private DetailBean detail;

        public DetailBean getDetail() {
            return detail;
        }

        public void setDetail(DetailBean detail) {
            this.detail = detail;
        }

        public static class DetailBean implements Serializable{
            /**
             * name : Get Started with Dropbox-2017-01-13-14-30-28.pdf.nxl
             * recipients : ["user1@nextlabs.com","user2@nextlabs.com"]
             * fileLink : https://rmtest.nextlabs.solutions/rms/main.jsp#/personal/viewSharedFile?d=b8785cd6-632d-405e-973c-27eca6b905a7&c=EEF638B42F19D674D87FC8AE3ADF0C598818FECA6C49A9F34646A8E409F45359
             * protectedOn : 1484289028934
             * sharedOn : 1484527940420
             * rights : ["VIEW"]
             * shared : true
             * deleted : false
             * revoked : false
             * validity : {"startDate":1509638400000,"endDate":1512316799999}
             */

            private String name;
            private String fileLink;
            private long protectedOn;
            private long sharedOn;
            private boolean shared;
            private boolean deleted;
            private boolean revoked;
            private ValidityBean validity;
            private List<String> recipients;
            private List<String> rights;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getFileLink() {
                return fileLink;
            }

            public void setFileLink(String fileLink) {
                this.fileLink = fileLink;
            }

            public long getProtectedOn() {
                return protectedOn;
            }

            public void setProtectedOn(long protectedOn) {
                this.protectedOn = protectedOn;
            }

            public long getSharedOn() {
                return sharedOn;
            }

            public void setSharedOn(long sharedOn) {
                this.sharedOn = sharedOn;
            }

            public boolean isShared() {
                return shared;
            }

            public void setShared(boolean shared) {
                this.shared = shared;
            }

            public boolean isDeleted() {
                return deleted;
            }

            public void setDeleted(boolean deleted) {
                this.deleted = deleted;
            }

            public boolean isRevoked() {
                return revoked;
            }

            public void setRevoked(boolean revoked) {
                this.revoked = revoked;
            }

            public ValidityBean getValidity() {
                return validity;
            }

            public void setValidity(ValidityBean validity) {
                this.validity = validity;
            }

            public List<String> getRecipients() {
                return recipients;
            }

            public void setRecipients(List<String> recipients) {
                this.recipients = recipients;
            }

            public List<String> getRights() {
                return rights;
            }

            public void setRights(List<String> rights) {
                this.rights = rights;
            }

            public static class ValidityBean implements Serializable {
                /**
                 * startDate : 1509638400000
                 * endDate : 1512316799999
                 */

                private long startDate;
                private long endDate;

                public long getStartDate() {
                    return startDate;
                }

                public void setStartDate(long startDate) {
                    this.startDate = startDate;
                }

                public long getEndDate() {
                    return endDate;
                }

                public void setEndDate(long endDate) {
                    this.endDate = endDate;
                }

                @Override
                public String toString() {
                    return "ValidityBean{" +
                            "startDate=" + startDate +
                            ", endDate=" + endDate +
                            '}';
                }
            }

            @Override
            public String toString() {
                return "DetailBean{" +
                        "name='" + name + '\'' +
                        ", fileLink='" + fileLink + '\'' +
                        ", protectedOn=" + protectedOn +
                        ", sharedOn=" + sharedOn +
                        ", shared=" + shared +
                        ", deleted=" + deleted +
                        ", revoked=" + revoked +
                        ", validity=" + validity +
                        ", recipients=" + recipients +
                        ", rights=" + rights +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "ResultsBean{" +
                    "detail=" + detail +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MyVaultMetaDataResult{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", serverTime=" + serverTime +
                ", results=" + results +
                '}';
    }
}
