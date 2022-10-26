package com.skydrm.sdk.rms.rest.sharedwithspace;

import java.util.List;

public class ListFileResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1581057173488
     * results : {"detail":{"totalFiles":2,"files":[{"duid":"C365CCA9965FC4A4EF0FE7F6DBE8F1A2","name":"XMLde- test plan-2020-02-06-04-02-02.xml.nxl","size":291328,"fileType":"xml","sharedDate":1580976003029,"sharedBy":"john.tyler@qapf1.qalab01.nextlabs.com","transactionId":"abfe0d42-1479-4a6d-b254-630ab56bddfb","transactionCode":"DB98FE368EAF931D2A199F15D7067F076448ABB7C18DAD10D2096D2C9C7E08E4","sharedLink":"https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=abfe0d42-1479-4a6d-b254-630ab56bddfb&c=DB98FE368EAF931D2A199F15D7067F076448ABB7C18DAD10D2096D2C9C7E08E4","rights":[],"isOwner":false,"protectionType":1,"sharedByProject":"6"},{"duid":"994C513F55ED66090A331A39A282DE5D","name":"Tools used for SkyDRM Product-2020-02-03-08-42-21.docx.nxl","size":588288,"fileType":"docx","sharedDate":1580719350391,"sharedBy":"john.tyler@qapf1.qalab01.nextlabs.com","transactionId":"b9961744-4bf3-402e-bd88-7270f2766063","transactionCode":"31021AD7247EF8482473C827389773BCF9E63B2E6C68B8ABC5C920FA417B3208","sharedLink":"https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=b9961744-4bf3-402e-bd88-7270f2766063&c=31021AD7247EF8482473C827389773BCF9E63B2E6C68B8ABC5C920FA417B3208","rights":["VIEW","PRINT","SHARE","WATERMARK"],"isOwner":false,"protectionType":0,"sharedByProject":"4"}]}}
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
         * detail : {"totalFiles":2,"files":[{"duid":"C365CCA9965FC4A4EF0FE7F6DBE8F1A2","name":"XMLde- test plan-2020-02-06-04-02-02.xml.nxl","size":291328,"fileType":"xml","sharedDate":1580976003029,"sharedBy":"john.tyler@qapf1.qalab01.nextlabs.com","transactionId":"abfe0d42-1479-4a6d-b254-630ab56bddfb","transactionCode":"DB98FE368EAF931D2A199F15D7067F076448ABB7C18DAD10D2096D2C9C7E08E4","sharedLink":"https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=abfe0d42-1479-4a6d-b254-630ab56bddfb&c=DB98FE368EAF931D2A199F15D7067F076448ABB7C18DAD10D2096D2C9C7E08E4","rights":[],"isOwner":false,"protectionType":1,"sharedByProject":"6"},{"duid":"994C513F55ED66090A331A39A282DE5D","name":"Tools used for SkyDRM Product-2020-02-03-08-42-21.docx.nxl","size":588288,"fileType":"docx","sharedDate":1580719350391,"sharedBy":"john.tyler@qapf1.qalab01.nextlabs.com","transactionId":"b9961744-4bf3-402e-bd88-7270f2766063","transactionCode":"31021AD7247EF8482473C827389773BCF9E63B2E6C68B8ABC5C920FA417B3208","sharedLink":"https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=b9961744-4bf3-402e-bd88-7270f2766063&c=31021AD7247EF8482473C827389773BCF9E63B2E6C68B8ABC5C920FA417B3208","rights":["VIEW","PRINT","SHARE","WATERMARK"],"isOwner":false,"protectionType":0,"sharedByProject":"4"}]}
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
             * totalFiles : 2
             * files : [{"duid":"C365CCA9965FC4A4EF0FE7F6DBE8F1A2","name":"XMLde- test plan-2020-02-06-04-02-02.xml.nxl","size":291328,"fileType":"xml","sharedDate":1580976003029,"sharedBy":"john.tyler@qapf1.qalab01.nextlabs.com","transactionId":"abfe0d42-1479-4a6d-b254-630ab56bddfb","transactionCode":"DB98FE368EAF931D2A199F15D7067F076448ABB7C18DAD10D2096D2C9C7E08E4","sharedLink":"https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=abfe0d42-1479-4a6d-b254-630ab56bddfb&c=DB98FE368EAF931D2A199F15D7067F076448ABB7C18DAD10D2096D2C9C7E08E4","rights":[],"isOwner":false,"protectionType":1,"sharedByProject":"6"},{"duid":"994C513F55ED66090A331A39A282DE5D","name":"Tools used for SkyDRM Product-2020-02-03-08-42-21.docx.nxl","size":588288,"fileType":"docx","sharedDate":1580719350391,"sharedBy":"john.tyler@qapf1.qalab01.nextlabs.com","transactionId":"b9961744-4bf3-402e-bd88-7270f2766063","transactionCode":"31021AD7247EF8482473C827389773BCF9E63B2E6C68B8ABC5C920FA417B3208","sharedLink":"https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=b9961744-4bf3-402e-bd88-7270f2766063&c=31021AD7247EF8482473C827389773BCF9E63B2E6C68B8ABC5C920FA417B3208","rights":["VIEW","PRINT","SHARE","WATERMARK"],"isOwner":false,"protectionType":0,"sharedByProject":"4"}]
             */

            private int totalFiles;
            private List<FilesBean> files;

            public int getTotalFiles() {
                return totalFiles;
            }

            public void setTotalFiles(int totalFiles) {
                this.totalFiles = totalFiles;
            }

            public List<FilesBean> getFiles() {
                return files;
            }

            public void setFiles(List<FilesBean> files) {
                this.files = files;
            }

            public static class FilesBean {
                /**
                 * duid : C365CCA9965FC4A4EF0FE7F6DBE8F1A2
                 * name : XMLde- test plan-2020-02-06-04-02-02.xml.nxl
                 * size : 291328
                 * fileType : xml
                 * sharedDate : 1580976003029
                 * sharedBy : john.tyler@qapf1.qalab01.nextlabs.com
                 * transactionId : abfe0d42-1479-4a6d-b254-630ab56bddfb
                 * transactionCode : DB98FE368EAF931D2A199F15D7067F076448ABB7C18DAD10D2096D2C9C7E08E4
                 * sharedLink : https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=abfe0d42-1479-4a6d-b254-630ab56bddfb&c=DB98FE368EAF931D2A199F15D7067F076448ABB7C18DAD10D2096D2C9C7E08E4
                 * rights : []
                 * isOwner : false
                 * protectionType : 1
                 * sharedByProject : 6
                 */

                private String duid;
                private String name;
                private int size;
                private String fileType;
                private long sharedDate;
                private String sharedBy;
                private String transactionId;
                private String transactionCode;
                private String sharedLink;
                private boolean isOwner;
                private int protectionType;
                private String sharedByProject;
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

                public int getSize() {
                    return size;
                }

                public void setSize(int size) {
                    this.size = size;
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

                public boolean isIsOwner() {
                    return isOwner;
                }

                public void setIsOwner(boolean isOwner) {
                    this.isOwner = isOwner;
                }

                public int getProtectionType() {
                    return protectionType;
                }

                public void setProtectionType(int protectionType) {
                    this.protectionType = protectionType;
                }

                public String getSharedByProject() {
                    return sharedByProject;
                }

                public void setSharedByProject(String sharedByProject) {
                    this.sharedByProject = sharedByProject;
                }

                public List<String> getRights() {
                    return rights;
                }

                public void setRights(List<String> rights) {
                    this.rights = rights;
                }
            }
        }
    }
}
