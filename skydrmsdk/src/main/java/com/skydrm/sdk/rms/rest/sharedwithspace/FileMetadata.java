package com.skydrm.sdk.rms.rest.sharedwithspace;

import java.util.List;

public class FileMetadata {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1581324892358
     * results : {"detail":{"duid":"3EF04EFB207D2FEEA52936D1E9E03927","name":"case_left_left-2020-02-06-07-52-51.png.nxl","fileType":"png","sharedDate":1580975585605,"sharedBy":"john.tyler@qapf1.qalab01.nextlabs.com","transactionId":"2705ea95-2da4-4ae1-8408-d9a3915c8ac9","transactionCode":"96E4D1C068D338A69CAACE8961E20FED0F31FC7B0DECA1997AF654C67755478A","sharedLink":"https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=2705ea95-2da4-4ae1-8408-d9a3915c8ac9&c=96E4D1C068D338A69CAACE8961E20FED0F31FC7B0DECA1997AF654C67755478A","rights":["VIEW","EDIT","PRINT","DECRYPT","SHARE","DOWNLOAD","WATERMARK"],"tags":{},"isOwner":false,"validity":{},"protectionType":0}}
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

    public static class ResultsBean {
        /**
         * detail : {"duid":"3EF04EFB207D2FEEA52936D1E9E03927","name":"case_left_left-2020-02-06-07-52-51.png.nxl","fileType":"png","sharedDate":1580975585605,"sharedBy":"john.tyler@qapf1.qalab01.nextlabs.com","transactionId":"2705ea95-2da4-4ae1-8408-d9a3915c8ac9","transactionCode":"96E4D1C068D338A69CAACE8961E20FED0F31FC7B0DECA1997AF654C67755478A","sharedLink":"https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=2705ea95-2da4-4ae1-8408-d9a3915c8ac9&c=96E4D1C068D338A69CAACE8961E20FED0F31FC7B0DECA1997AF654C67755478A","rights":["VIEW","EDIT","PRINT","DECRYPT","SHARE","DOWNLOAD","WATERMARK"],"tags":{},"isOwner":false,"validity":{},"protectionType":0}
         */

        private DetailBean detail;

        public DetailBean getDetail() {
            return detail;
        }

        public void setDetail(DetailBean detail) {
            this.detail = detail;
        }

        public static class DetailBean {
            /**
             * duid : 3EF04EFB207D2FEEA52936D1E9E03927
             * name : case_left_left-2020-02-06-07-52-51.png.nxl
             * fileType : png
             * sharedDate : 1580975585605
             * sharedBy : john.tyler@qapf1.qalab01.nextlabs.com
             * transactionId : 2705ea95-2da4-4ae1-8408-d9a3915c8ac9
             * transactionCode : 96E4D1C068D338A69CAACE8961E20FED0F31FC7B0DECA1997AF654C67755478A
             * sharedLink : https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=2705ea95-2da4-4ae1-8408-d9a3915c8ac9&c=96E4D1C068D338A69CAACE8961E20FED0F31FC7B0DECA1997AF654C67755478A
             * rights : ["VIEW","EDIT","PRINT","DECRYPT","SHARE","DOWNLOAD","WATERMARK"]
             * tags : {}
             * isOwner : false
             * validity : {}
             * protectionType : 0
             */

            private String duid;
            private String name;
            private String fileType;
            private long sharedDate;
            private String sharedBy;
            private String transactionId;
            private String transactionCode;
            private String sharedLink;
            private TagsBean tags;
            private boolean isOwner;
            private ValidityBean validity;
            private int protectionType;
            private List<String> rights;

            public String getDuid() {
                return duid;
            }

            public void setDuid(String duid) {
                this.duid = duid;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getFileType() {
                return fileType;
            }

            public void setFileType(String fileType) {
                this.fileType = fileType;
            }

            public long getSharedDate() {
                return sharedDate;
            }

            public void setSharedDate(long sharedDate) {
                this.sharedDate = sharedDate;
            }

            public String getSharedBy() {
                return sharedBy;
            }

            public void setSharedBy(String sharedBy) {
                this.sharedBy = sharedBy;
            }

            public String getTransactionId() {
                return transactionId;
            }

            public void setTransactionId(String transactionId) {
                this.transactionId = transactionId;
            }

            public String getTransactionCode() {
                return transactionCode;
            }

            public void setTransactionCode(String transactionCode) {
                this.transactionCode = transactionCode;
            }

            public String getSharedLink() {
                return sharedLink;
            }

            public void setSharedLink(String sharedLink) {
                this.sharedLink = sharedLink;
            }

            public TagsBean getTags() {
                return tags;
            }

            public void setTags(TagsBean tags) {
                this.tags = tags;
            }

            public boolean isIsOwner() {
                return isOwner;
            }

            public void setIsOwner(boolean isOwner) {
                this.isOwner = isOwner;
            }

            public ValidityBean getValidity() {
                return validity;
            }

            public void setValidity(ValidityBean validity) {
                this.validity = validity;
            }

            public int getProtectionType() {
                return protectionType;
            }

            public void setProtectionType(int protectionType) {
                this.protectionType = protectionType;
            }

            public List<String> getRights() {
                return rights;
            }

            public void setRights(List<String> rights) {
                this.rights = rights;
            }

            public static class TagsBean {
            }

            public static class ValidityBean {
            }
        }
    }
}
