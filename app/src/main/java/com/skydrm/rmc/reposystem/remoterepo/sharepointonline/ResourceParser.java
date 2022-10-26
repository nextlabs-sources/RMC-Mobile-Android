package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import android.text.TextUtils;

import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.reposystem.types.NXSite;
import com.skydrm.rmc.reposystem.types.NxFileBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

class ResourceParser {
    /**
     * {
     * "__metadata": {
     * "id": "https://nextlabsdev.sharepoint.com/sites/rmscentos7303new/_api/Web/Lists(guid'3d8aaa0f-f42e-492e-9421-26d8f7feab61')",
     * "uri": "https://nextlabsdev.sharepoint.com/sites/rmscentos7303new/_api/Web/Lists(guid'3d8aaa0f-f42e-492e-9421-26d8f7feab61')",
     * "etag": "\"4\"",
     * "type": "SP.List"
     * },
     * "RootFolder": {
     * "__deferred": {
     * "uri": "https://nextlabsdev.sharepoint.com/sites/rmscentos7303new/_api/Web/Lists(guid'3d8aaa0f-f42e-492e-9421-26d8f7feab61')/RootFolder"
     * }
     * },
     * "Created": "2018-05-31T08:43:05Z",
     * "Title": "Documents"
     * }
     *
     * @param rt
     * @param file
     * @param results
     * @param sites
     */
    public static void parseRoots(NxFileBase rt, INxFile file, String results, boolean sites) {
        try {
            JSONObject resultsObj = new JSONObject(results);
            if (resultsObj.has("d")) {
                JSONObject dObj = resultsObj.getJSONObject("d");
                if (dObj.has("results")) {
                    JSONArray resultsArr = dObj.getJSONArray("results");
                    for (int i = 0; i < resultsArr.length(); i++) {
                        JSONObject itemObj = resultsArr.getJSONObject(i);
                        String cloudPathId = "";
                        if (itemObj.has("__metadata")) {
                            //parse metadata field
                            JSONObject metadataObj = itemObj.getJSONObject("__metadata");
                            cloudPathId = metadataObj.getString("uri");
                        }
                        String created = itemObj.getString("Created");
                        String title = itemObj.getString("Title");
                        if (sites) {
                            NxFileBase site = new NXSite();
                            if (file == null) {
                                fillParams(site, "/%" + title, cloudPathId, 0, "%" + title, created, cloudPathId);
                            } else {
                                fillParams(site, file.getLocalPath() + "/%" + title, cloudPathId, 0, "%" + title, created, cloudPathId);
                            }
                            rt.addChild(site);
                        } else {
                            NxFileBase folder = new NXFolder();
                            if (file == null) {
                                fillParams(folder, "/" + title, cloudPathId + "/RootFolder", 0, title, created, cloudPathId);
                            } else {
                                fillParams(folder, file.getLocalPath() + "/" + title, cloudPathId + "/RootFolder", 0, title, created, cloudPathId);
                            }
                            rt.addChild(folder);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void parseChildFolders(NxFileBase rt, INxFile file, String folders) {
        try {
            JSONObject foldersObj = new JSONObject(folders);
            if (foldersObj.has("d")) {
                JSONObject dObj = foldersObj.getJSONObject("d");
                if (dObj.has("results")) {
                    JSONArray resultsArr = dObj.getJSONArray("results");
                    for (int i = 0; i < resultsArr.length(); i++) {
                        JSONObject itemObj = resultsArr.getJSONObject(i);
                        String cloudPath = "";
                        if (itemObj.has("__metadata")) {
                            JSONObject metadataObj = itemObj.getJSONObject("__metadata");
                            cloudPath = metadataObj.getString("uri");
                        }
                        String name = itemObj.getString("Name");
                        //String timeLastModified = itemObj.getString("TimeLastModified");
                        String cloudPathId = itemObj.getString("ServerRelativeUrl");
                        NxFileBase folder = new NXFolder();
                        fillParams(folder, file.getLocalPath() + "/" + name, cloudPath, 0, name, "", cloudPathId);
                        rt.addChild(folder);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void parseChildFiles(NxFileBase rt, INxFile file, String files) {
        try {
            JSONObject filesObj = new JSONObject(files);
            if (filesObj.has("d")) {
                JSONObject dObj = filesObj.getJSONObject("d");
                if (dObj.has("results")) {
                    JSONArray resultsArr = dObj.getJSONArray("results");
                    for (int i = 0; i < resultsArr.length(); i++) {
                        JSONObject itemObj = resultsArr.getJSONObject(i);
                        String cloudPath = "";
                        if (itemObj.has("__metadata")) {
                            JSONObject metadataObj = itemObj.getJSONObject("__metadata");
                            cloudPath = metadataObj.getString("uri");
                        }
                        String name = itemObj.getString("Name");
                        long length = itemObj.getLong("Length");
                        String timeLastModified = itemObj.getString("TimeLastModified");
                        String cloudPathId = itemObj.getString("ServerRelativeUrl");
                        NxFileBase doc = new NXDocument();
                        fillParams(doc, file.getLocalPath() + "/" + name, cloudPath, length, name, timeLastModified, cloudPathId);
                        rt.addChild(doc);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void fillParams(NxFileBase base,
                                  String LocalPath,
                                  String CloudPath,
                                  long size,
                                  String name,
                                  String timeStr,
                                  String cloudPathId) {
        base.setDisplayPath(cloudPathId);
        base.setLocalPath(LocalPath);
        base.setCloudPath(CloudPath);
        base.setSize(size);
        base.setName(name);
        base.setmCloudPathID(cloudPathId);
        if (timeStr == null || timeStr.isEmpty()) {
            base.setLastModifiedTimeLong(System.currentTimeMillis());
        } else {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                base.setLastModifiedTimeLong(df.parse(timeStr).getTime());
            } catch (Exception e) {
                e.printStackTrace();
                base.setLastModifiedTimeLong(System.currentTimeMillis());
            }
        }
    }

    public static NxFileBase getBase(INxFile file) {
        NxFileBase rt;
        if (file.isSite()) {
            rt = new NXSite();
        } else {
            rt = new NXFolder();
        }
        fillParams(rt, file.getLocalPath(), file.getCloudPath(), file.getSize(), file.getName(), "", "");
        return rt;
    }

    /**
     * "Id": 9,
     *
     * @param usrInfo Json string retrieved from server.
     * @return Id of current usr which will send to server to get the detail usr info which includes username and user-email.
     */
    public static String parseUsrId(String usrInfo) {
        try {
            if (TextUtils.isEmpty(usrInfo)) {
                return null;
            }
            JSONObject usrInfoObj = new JSONObject(usrInfo);
            if (usrInfoObj.has("d")) {
                JSONObject dObj = usrInfoObj.getJSONObject("d");
                return dObj.getString("Id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * "Title": "Sudhindra Kumar",
     * "EMail": "skumar@nextlabsdev.onmicrosoft.com",
     *
     * @param usrDetailInfo Json string retrieved from server.
     * @return Map of contains title and email of current usr.
     */
    public static Map<String, String> parseUsrDetailInfo(String usrDetailInfo) {
        try {
            if (TextUtils.isEmpty(usrDetailInfo)) {
                return null;
            }
            JSONObject detailObj = new JSONObject(usrDetailInfo);
            if (detailObj.has("d")) {
                Map<String, String> detailMap = new HashMap<>();
                JSONObject dObj = detailObj.getJSONObject("d");
                String title = dObj.getString("Title");
                String email = dObj.getString("EMail");
                detailMap.put("username", title);
                detailMap.put("email", email);
                return detailMap;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> parseQuotaInfo(String siteQuotaInfo) {
        try {
            if (TextUtils.isEmpty(siteQuotaInfo)) {
                return null;
            }
            JSONObject quotaObj = new JSONObject(siteQuotaInfo);
            if (quotaObj.has("d")) {
                JSONObject dObj = quotaObj.getJSONObject("d");
                if (dObj.has("Usage")) {
                    Map<String, String> quotaMap = new HashMap<>();
                    JSONObject usageObj = dObj.getJSONObject("Usage");
                    String storage = usageObj.getString("Storage");
                    double storagePercentageUsed = usageObj.getDouble("StoragePercentageUsed");
                    quotaMap.put("Storage", storage);
                    quotaMap.put("StoragePercentageUsed", storagePercentageUsed + "");
                    return quotaMap;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
