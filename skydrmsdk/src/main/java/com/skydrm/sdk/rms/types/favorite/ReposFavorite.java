package com.skydrm.sdk.rms.types.favorite;

import java.util.List;

/**
 * All repository favorite files that getting from rms in an aggregated way.
 *  -- every repository has itself favorite file aggregation.
 */

public class ReposFavorite {
    private List<ReposBean> repos;

    public List<ReposBean> getRepos() {
        return repos;
    }

    public void setRepos(List<ReposBean> repos) {
        this.repos = repos;
    }

    public static class ReposBean {
        /**
         * repoId : 07ebed38-f23c-45af-96ea-5a16908266b4
         * repoName : myRepo
         * repoType : OneDrive
         * markedFavoriteFiles : [{"pathId":"id:bDWM0KRPeTAAAA56789ACA","pathDisplay":"/onedrive-document.docx"},{"pathId":"id:bDWMOKRzzTAAAA56789ACA","pathDisplay":"/myengine.ppt"}]
         * unmarkedFavoriteFiles : [{"pathId":"id:bDWM0KRPeTAAAA56789ACA","pathDisplay":"/onedrive-document.docx"},{"pathId":"id:bDWMOKRzzTAAAA56789ACA","pathDisplay":"/myengine.ppt"}]
         * isFullCopy : true
         */

        private String repoId;
        private String repoName;
        private String repoType;
        private boolean isFullCopy;
        private List<MarkedFavoriteFilesBean> markedFavoriteFiles;
        private List<UnmarkedFavoriteFilesBean> unmarkedFavoriteFiles;

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

        public boolean isIsFullCopy() {
            return isFullCopy;
        }

        public void setIsFullCopy(boolean isFullCopy) {
            this.isFullCopy = isFullCopy;
        }

        public List<MarkedFavoriteFilesBean> getMarkedFavoriteFiles() {
            return markedFavoriteFiles;
        }

        public void setMarkedFavoriteFiles(List<MarkedFavoriteFilesBean> markedFavoriteFiles) {
            this.markedFavoriteFiles = markedFavoriteFiles;
        }

        public List<UnmarkedFavoriteFilesBean> getUnmarkedFavoriteFiles() {
            return unmarkedFavoriteFiles;
        }

        public void setUnmarkedFavoriteFiles(List<UnmarkedFavoriteFilesBean> unmarkedFavoriteFiles) {
            this.unmarkedFavoriteFiles = unmarkedFavoriteFiles;
        }

        public static class MarkedFavoriteFilesBean {
            /**
             * pathId : id:bDWM0KRPeTAAAA56789ACA
             * pathDisplay : /onedrive-document.docx
             */

            private String pathId;
            private String pathDisplay;
            private boolean fromMyVault;

            public String getPathId() {
                return pathId;
            }

            public void setPathId(String pathId) {
                this.pathId = pathId;
            }

            public String getPathDisplay() {
                return pathDisplay;
            }

            public void setPathDisplay(String pathDisplay) {
                this.pathDisplay = pathDisplay;
            }

            public boolean isFromMyVault() {
                return fromMyVault;
            }

            public void setFromMyVault(boolean fromMyVault) {
                this.fromMyVault = fromMyVault;
            }
        }

        public static class UnmarkedFavoriteFilesBean {
            /**
             * pathId : id:bDWM0KRPeTAAAA56789ACA
             * pathDisplay : /onedrive-document.docx
             */

            private String pathId;
            private String pathDisplay;
            private boolean fromMyVault;

            public String getPathId() {
                return pathId;
            }

            public void setPathId(String pathId) {
                this.pathId = pathId;
            }

            public String getPathDisplay() {
                return pathDisplay;
            }

            public void setPathDisplay(String pathDisplay) {
                this.pathDisplay = pathDisplay;
            }

            public boolean isFromMyVault() {
                return fromMyVault;
            }

            public void setFromMyVault(boolean fromMyVault) {
                this.fromMyVault = fromMyVault;
            }
        }
    }
}
