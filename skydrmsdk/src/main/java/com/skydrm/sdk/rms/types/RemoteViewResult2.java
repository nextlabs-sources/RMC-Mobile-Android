package com.skydrm.sdk.rms.types;

import java.util.List;

/**
 * Created by aning on 7/6/2017.
 */

public class RemoteViewResult2 {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1565778846237
     * results : {"owner":true,"permissions":1073743871,"viewerURL":"https://rms-centos7513.qapf1.qalab01.nextlabs.com:8445/viewer/temp/0b0c2050-d1dd-4f56-a203-ddb8cd96dd8e/1565778845736b831dbd8-68f0-4556-b739-259d065edc23/MyTest-2019-08-02-07-09-25.xls.html?d=1565778845736b831dbd8-68f0-4556-b739-259d065edc23&s=0b0c2050-d1dd-4f56-a203-ddb8cd96dd8e&operations=0","cookies":["JSESSIONID_V=94B9C3C3A2B5CC659008F35338D6E63A; Path=/viewer; Secure; HttpOnly","userId=11;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure","ticket=2075D407CCCCFC031E7870D2F8619FD4;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure","clientId=BAD8AFB24D495E1F6D7F6723D1727D0C;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure","idp=0;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure","platformId=800;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure","deviceId=Nexus+5X;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure"]}
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
         * owner : true
         * permissions : 1073743871
         * viewerURL : https://rms-centos7513.qapf1.qalab01.nextlabs.com:8445/viewer/temp/0b0c2050-d1dd-4f56-a203-ddb8cd96dd8e/1565778845736b831dbd8-68f0-4556-b739-259d065edc23/MyTest-2019-08-02-07-09-25.xls.html?d=1565778845736b831dbd8-68f0-4556-b739-259d065edc23&s=0b0c2050-d1dd-4f56-a203-ddb8cd96dd8e&operations=0
         * cookies : ["JSESSIONID_V=94B9C3C3A2B5CC659008F35338D6E63A; Path=/viewer; Secure; HttpOnly","userId=11;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure","ticket=2075D407CCCCFC031E7870D2F8619FD4;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure","clientId=BAD8AFB24D495E1F6D7F6723D1727D0C;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure","idp=0;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure","platformId=800;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure","deviceId=Nexus+5X;Version=1;Comment=;Domain=.rms-centos7513.qapf1.qalab01.nextlabs.com;Path=/;Max-Age=2499108;Secure"]
         */

        private boolean owner;
        private int permissions;
        private String viewerURL;
        private List<String> cookies;

        public boolean isOwner() {
            return owner;
        }

        public void setOwner(boolean owner) {
            this.owner = owner;
        }

        public int getPermissions() {
            return permissions;
        }

        public void setPermissions(int permissions) {
            this.permissions = permissions;
        }

        public String getViewerURL() {
            return viewerURL;
        }

        public void setViewerURL(String viewerURL) {
            this.viewerURL = viewerURL;
        }

        public List<String> getCookies() {
            return cookies;
        }

        public void setCookies(List<String> cookies) {
            this.cookies = cookies;
        }
    }
}
