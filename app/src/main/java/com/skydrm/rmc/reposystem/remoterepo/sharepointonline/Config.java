package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

public class Config {
    public String getRootFolderUrl(String serverSite) {
        return serverSite + "/_api/web/lists?$filter=BaseTemplate eq 101&$select=Title,Created,RootFolder";
    }

    public String getRootSitesUrl(String serverSite) {
        return serverSite + "/_api/web/webs?$select=Title,Created";
    }

    public String getChildSiteUrl(String cloudPath) {
        return cloudPath + "/webs";
    }

    public String getFileLists(String cloudPath) {
        return cloudPath + "/lists?$select=BaseTemplate,Title,Hidden,Id&$filter=BaseTemplate eq 101";
    }

    public String getFoldersUrl(String cloudPath) {
        return cloudPath + "/Folders?$filter=Name ne 'Forms'";
    }

    public String getFilesUrl(String cloudPath) {
        return cloudPath + "/Files";
    }

    public String getCurrentUsrInfoUrl(String serverSite) {
        return serverSite + "/_api/web/CurrentUser";
    }

    public String getCurrentUsrInfoDetailUrl(String serverSite, String usrId) {
        return serverSite + "/_api/web/SiteUserInfoList/Items" + "(" + usrId + ")";
    }

    public String getSiteQuotaUrl(String serverSite) {
        return serverSite + "/_api/site/usage";
    }

    /**
     * https://docs.microsoft.com/en-us/sharepoint/dev/sp-add-ins/complete-basic-operations-using-sharepoint-rest-endpoints
     * This method is used to get the url that retrieve all the lists in a specific SharePoint site.
     *
     * @param serverSite site url
     * @return site lists url
     */
    private String getSiteListsUrl(String serverSite) {
        return serverSite + "/_api/web/lists";
    }
}
