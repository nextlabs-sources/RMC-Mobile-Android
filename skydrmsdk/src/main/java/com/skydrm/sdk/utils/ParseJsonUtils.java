package com.skydrm.sdk.utils;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aning on 8/22/2017.
 */

public class ParseJsonUtils {

/*
                 "path":"/500K-2017-06-23-09-29-11.doc.nxl",
                         "name":"500K-2017-06-23-09-29-11.doc.nxl",
                         "pathId":"/500k-2017-06-23-09-29-11.doc.nxl",
                         "fileId":"/500k-2017-06-23-09-29-11.doc.nxl",
                         "isFolder":false,
                         "isRepo":false,
                         "favorited":true,
                         "fromMyVault":false,
                         "lastModifiedTime":1502183666000,
                         "fileSize":554496,
                         "repoId":"137ec706-b9d2-4634-aebf-a966628c37b5",
                         "repoName":"MyDrive",
                         "repoType":"S3",
                         "fileType":"doc",
                         "protectedFile":true,
                         "usePathId":false,
                         "children":[

                         ],
                         "deleted":false*/

    public static List<AllRepoFavoListBean> parseResultJson(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {

            List<AllRepoFavoListBean> resultList = new ArrayList<>();
            JSONObject results = (JSONObject) new JSONObject(json).get("results");
            JSONArray resultArray = results.getJSONArray("results");
            for(int i = 0; i< resultArray.length(); i++) {
                AllRepoFavoListBean resultBean = new AllRepoFavoListBean();
                JSONObject object = (JSONObject) resultArray.get(i);
                resultBean.setPath(object.getString("path"));
                resultBean.setName(object.getString("name"));
                resultBean.setPathId(object.getString("pathId"));
                resultBean.setFileId(object.getString("fileId"));
                resultBean.setIsFolder(object.getBoolean("isFolder"));
                resultBean.setIsRepo(object.getBoolean("isRepo"));
                resultBean.setFavorited(object.getBoolean("favorited"));
                resultBean.setFromMyVault(object.getBoolean("fromMyVault"));
                resultBean.setLastModifiedTime(object.getLong("lastModifiedTime"));
                resultBean.setFileSize(object.getInt("fileSize"));
                resultBean.setRepoId(object.getString("repoId"));
                resultBean.setRepoName(object.getString("repoName"));
                resultBean.setRepoType(object.getString("repoType"));
                resultBean.setFileType(object.getString("fileType"));
                resultBean.setProtectedFile(object.getBoolean("protectedFile"));
                resultBean.setUsePathId(object.getBoolean("usePathId"));

                /* now comment out this code, or else will occur error, because that the favorite feature only support file instead of folder, so no have children. */
//                List<?> list = (List<?>) object.get("children");
//                resultBean.setChildren(list);

                resultBean.setDeleted(object.getBoolean("deleted"));

                resultList.add(resultBean);
            }

            return resultList;

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static class AllRepoFavoListBean {
        /**
         * path : /500K-2017-06-23-09-29-11.doc.nxl
         * name : 500K-2017-06-23-09-29-11.doc.nxl
         * pathId : /500k-2017-06-23-09-29-11.doc.nxl
         * fileId : /500k-2017-06-23-09-29-11.doc.nxl
         * isFolder : false
         * isRepo : false
         * favorited : true
         * fromMyVault : false
         * lastModifiedTime : 1502183666000
         * fileSize : 554496
         * repoId : 137ec706-b9d2-4634-aebf-a966628c37b5
         * repoName : MyDrive
         * repoType : S3
         * fileType : doc
         * protectedFile : true
         * usePathId : false
         * children : []
         * deleted : false
         */

        private String path;
        private String name;
        private String pathId;
        private String fileId;
        private boolean isFolder;
        private boolean isRepo;
        private boolean favorited;
        private boolean fromMyVault;
        private long lastModifiedTime;
        private int fileSize;
        private String repoId;
        private String repoName;
        private String repoType;
        private String fileType;
        private boolean protectedFile;
        private boolean usePathId;
        private boolean deleted;
        private List<?> children;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPathId() {
            return pathId;
        }

        public void setPathId(String pathId) {
            this.pathId = pathId;
        }

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public boolean isIsFolder() {
            return isFolder;
        }

        public void setIsFolder(boolean isFolder) {
            this.isFolder = isFolder;
        }

        public boolean isIsRepo() {
            return isRepo;
        }

        public void setIsRepo(boolean isRepo) {
            this.isRepo = isRepo;
        }

        public boolean isFavorited() {
            return favorited;
        }

        public void setFavorited(boolean favorited) {
            this.favorited = favorited;
        }

        public boolean isFromMyVault() {
            return fromMyVault;
        }

        public void setFromMyVault(boolean fromMyVault) {
            this.fromMyVault = fromMyVault;
        }

        public long getLastModifiedTime() {
            return lastModifiedTime;
        }

        public void setLastModifiedTime(long lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
        }

        public int getFileSize() {
            return fileSize;
        }

        public void setFileSize(int fileSize) {
            this.fileSize = fileSize;
        }

        public String getRepoId() {
            return repoId;
        }

        public void setRepoId(String repoId) {
            this.repoId = repoId;
        }

        public String getRepoName() {
            return repoName;
        }

        public void setRepoName(String repoName) {
            this.repoName = repoName;
        }

        public String getRepoType() {
            return repoType;
        }

        public void setRepoType(String repoType) {
            this.repoType = repoType;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public boolean isProtectedFile() {
            return protectedFile;
        }

        public void setProtectedFile(boolean protectedFile) {
            this.protectedFile = protectedFile;
        }

        public boolean isUsePathId() {
            return usePathId;
        }

        public void setUsePathId(boolean usePathId) {
            this.usePathId = usePathId;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }

        public List<?> getChildren() {
            return children;
        }

        public void setChildren(List<?> children) {
            this.children = children;
        }

        @Override
        public String toString() {
            return "AllRepoFavoListBean{" +
                    "path='" + path + '\'' +
                    ", name='" + name + '\'' +
                    ", pathId='" + pathId + '\'' +
                    ", fileId='" + fileId + '\'' +
                    ", isFolder=" + isFolder +
                    ", isRepo=" + isRepo +
                    ", favorited=" + favorited +
                    ", fromMyVault=" + fromMyVault +
                    ", lastModifiedTime=" + lastModifiedTime +
                    ", fileSize=" + fileSize +
                    ", repoId='" + repoId + '\'' +
                    ", repoName='" + repoName + '\'' +
                    ", repoType='" + repoType + '\'' +
                    ", fileType='" + fileType + '\'' +
                    ", protectedFile=" + protectedFile +
                    ", usePathId=" + usePathId +
                    ", deleted=" + deleted +
                    ", children=" + children +
                    '}';
        }
    }

}
