package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.errorHandler.ErrorCode;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.reposystem.RemoteRepoInfo;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.reposystem.types.NXSite;
import com.skydrm.rmc.reposystem.types.NxFileBase;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.SyncFailedException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;

/**
 * Created by aning on 6/2/2015.
 */

public class SharePointOnlineSdk {
    private static final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
    private static final String TAG = "SharePointOnlineSdk";
    private static final String LATEST_REV = null;
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static boolean bAuthSuccessful = false;
    private static Account AuthAccount = null;
    private Account mAccount = null;
    private boolean isCancel = false;
    private InputStream mSharepointonlineInputStream = null;
    private OutputStream mSharepointonlineOutputStream = null;
    private String mCookie;
    private String mfilePath = "";

    public SharePointOnlineSdk(String url, String userName, String token) {
        Account account = new Account();
        account.setUrl(url);
        account.setUsername(userName);
        account.setCookie(token);
        mAccount = account;
    }

    public static void startAuth(Activity mActivity) {
        resetAuthStatus();
        Intent intent = new Intent();
        intent.setClass(mActivity, LoginActivity.class);
        mActivity.startActivity(intent);
    }

    public static boolean getAuthStatus() {
        return bAuthSuccessful;
    }

    public static void setAuthStatus(boolean bAuthStatus) {
        bAuthSuccessful = bAuthStatus;
    }

    public static void resetAuthStatus() {
        bAuthSuccessful = false;
    }

    public static Account getAuthAccount() {
        return AuthAccount;
    }

    public static void setAuthAccount(Account account) {
        AuthAccount = account;
    }

    public NxFileBase listFiles(NXFolder file) {
        String path = file.getCloudPath();

        if (path.equals("/")) {
            return listRoot();  // get sites and rootsite list
        } else if (file.isSite()) {
            return listChildSiteAndLists(file); // get children sites and lists of default site
        } else {
            return listFoldersAndFiles(file); // get all folders and files of a list.
        }
    }

    public boolean getRepositoryInfo(RemoteRepoInfo info) {
        try {
            // get the user nad email
            String userInfo = getCurrentUserInfo();
            String userId = parseGetCurrentUserId(userInfo);
            String detailInfo = getCurrentUserInfoDetail(userId);
            Map<String, String> infoMap = parseGetUserDetailInfo(detailInfo);
            info.displayName = infoMap.get("user");
            info.email = infoMap.get("email");

            // get the total space and used space.
            String siteQuota = getSiteQuota();
            Map<String, String> quotaMap = parseGetSiteQuota(siteQuota);
            info.remoteTotalSpace = Long.valueOf(quotaMap.get("Storage"));
            info.remoteUsedSpace = (long) (info.remoteTotalSpace * Float.parseFloat(quotaMap.get("StoragePercentageUsed")));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void parseDocLists(String xmlDoc, NxFileBase rt, INxFile File) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new InputSource(new StringReader(xmlDoc)));

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();

        try {
            Element root = document.getDocumentElement();
            NodeList entryList = document.getElementsByTagName("entry");
            int size = entryList.getLength();
            for (int i = 0; i < entryList.getLength(); i++) {
                Element entry = (Element) entryList.item(i);
                String Time = xp.evaluate("content/properties/Created/text()", entry);
                String Name = xp.evaluate("content/properties/Title/text()", entry);
                String CloudPath = xp.evaluate("id/text()", entry);
                String cloudPathId = CloudPath;
                if (DEBUG) {
//                    Log.e(TAG, "----- parseDocLists: cloudPathId: " + cloudPathId);
                }
                NxFileBase folder = new NXFolder();
                if (File == null)
                    fillParmas(folder, "/" + Name, CloudPath + "/RootFolder", 0, Name, Time, cloudPathId);
                else
                    fillParmas(folder, File.getLocalPath() + "/" + Name, CloudPath + "/RootFolder", 0, Name, Time, cloudPathId);

                rt.addChild(folder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseSites(String xmlDoc, NxFileBase rt, INxFile File) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new InputSource(new StringReader(xmlDoc)));

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();

        try {
            Element root = document.getDocumentElement(); // get root node
            NodeList entryList = root.getElementsByTagName("entry");

            for (int i = 0; i < entryList.getLength(); i++) {
                Element entry = (Element) entryList.item(i);
                String Name = xp.evaluate("content/properties/Title/text()", entry);
                String Time = xp.evaluate("content/properties/Created/text()", entry);
                String CloudPath = xp.evaluate("id/text()", entry);
                String cloudPathId = CloudPath;
                if (DEBUG) {
//                    Log.e(TAG, "----- parseSites: cloudPathId: " + cloudPathId);
                }
                NxFileBase site = new NXSite();
                if (File == null)
                    fillParmas(site, "/%" + Name, CloudPath, 0, "%" + Name, Time, cloudPathId);
                else
                    fillParmas(site, File.getLocalPath() + "/%" + Name, CloudPath, 0, "%" + Name, Time, cloudPathId);

                rt.addChild(site);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void parseChildFolders(String xmlDoc, NxFileBase rt, INxFile File) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new InputSource(new StringReader(xmlDoc)));

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();

        try {
            Element root = document.getDocumentElement();
            NodeList entryList = root.getElementsByTagName("entry");

            for (int i = 0; i < entryList.getLength(); i++) {
                Element entry = (Element) entryList.item(i);
                String Name = xp.evaluate("content/properties/Name/text()", entry);
                String CloudPath = xp.evaluate("id/text()", entry);
                String CloudPathId = xp.evaluate("content/properties/ServerRelativeUrl/text()", entry);
                if (DEBUG) {
//                    Log.e(TAG, "-----parseChildFolders: CloudPathId: " + CloudPathId);
                }
                NxFileBase folder = new NXFolder();
                fillParmas(folder, File.getLocalPath() + "/" + Name, CloudPath, 0, Name, "", CloudPathId);
                rt.addChild(folder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseChildFiles(String xmlDoc, NxFileBase rt, INxFile File) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new InputSource(new StringReader(xmlDoc)));

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();

        try {
            Element root = document.getDocumentElement();
            NodeList entryList = root.getElementsByTagName("entry");

            for (int i = 0; i < entryList.getLength(); i++) {
                Element entry = (Element) entryList.item(i);
                String Name = xp.evaluate("content/properties/Name/text()", entry);
                String Length = xp.evaluate("content/properties/Length/text()", entry);
                String Time = xp.evaluate("content/properties/TimeLastModified/text()", entry);
                String CloudPath = xp.evaluate("id/text()", entry);
                // used to as file id when sync favorite & offline
                String cloudPathId = xp.evaluate("content/properties/ServerRelativeUrl/text()", entry);
                if (DEBUG) {
//                    Log.e(TAG, "----- parseChildFiles: cloudPathId: " + cloudPathId);
                }
                NxFileBase file = new NXDocument();
                fillParmas(file, File.getLocalPath() + "/" + Name, CloudPath, Long.valueOf(Length), Name, Time, cloudPathId);
                rt.addChild(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String parseGetCurrentUserId(String xmlDoc) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new InputSource(new StringReader(xmlDoc)));

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        String userId = null;
        try {
            Element root = document.getDocumentElement();
            NodeList entryList = root.getElementsByTagName("content");

            for (int i = 0; i < entryList.getLength(); i++) {
                Element entry = (Element) entryList.item(i);
                userId = xp.evaluate("properties/Id/text()", entry);
            }
            return userId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseGetUserDetailInfo(String xmlDoc) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new InputSource(new StringReader(xmlDoc)));

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();

        Map detailInfo = new HashMap<String, String>();
        try {
            Element root = document.getDocumentElement();
            NodeList entryList = root.getElementsByTagName("content");
            for (int i = 0; i < entryList.getLength(); i++) {
                Element entry = (Element) entryList.item(i);
                String user = xp.evaluate("properties/Title/text()", entry);
                String email = xp.evaluate("properties/EMail/text()", entry);
                detailInfo.put("user", user);
                detailInfo.put("email", email);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return detailInfo;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseGetSiteQuota(String xmlDoc) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new InputSource(new StringReader(xmlDoc)));

        Map quotaInfo = new HashMap<String, String>();
        try {
            Element root = document.getDocumentElement();
            NodeList childNodes = root.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeName().equals("d:Storage")) {
                    quotaInfo.put("Storage", childNodes.item(i).getTextContent());
                } else if (childNodes.item(i).getNodeName().equals("d:StoragePercentageUsed")) {
                    quotaInfo.put("StoragePercentageUsed", childNodes.item(i).getTextContent());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return quotaInfo;
    }

    private NxFileBase listRoot() {
        try {
            String xmlRootFolders = listRootFolders();
            String xmlRootSites = listRootSites();

            if (xmlRootFolders == null || xmlRootSites == null) {
                return null;
            }
            // new a root
            NxFileBase rt = new NXSite();
            fillParmas(rt, "/", "/", 0, "root", "", "");
            // parse its children
            parseSites(xmlRootSites, rt, null);
            parseDocLists(xmlRootFolders, rt, null);
            rt.updateRefreshTimeWisely();
            return rt;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return null;
    }

    private NxFileBase listChildSiteAndLists(com.skydrm.rmc.reposystem.types.INxFile File) {

        try {
            String xmlChildrenSites = getChildrenSites(File.getCloudPath());
            String xmlLists = getDocLists(File.getCloudPath());

            if (xmlLists == null || xmlChildrenSites == null) {
                return null;
            }
            NxFileBase rt = getBase(File);

            parseSites(xmlChildrenSites, rt, File);
            parseDocLists(xmlLists, rt, File);
            rt.updateRefreshTimeWisely();
            return rt;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }


    private NxFileBase listFoldersAndFiles(com.skydrm.rmc.reposystem.types.INxFile File) {
        try {
            String xmlFolders = getFolders(File.getCloudPath());
            String xmlFiles = getFiles(File.getCloudPath());

            if (xmlFolders == null || xmlFiles == null) {
                return null;
            }

            NxFileBase rt = getBase(File);

            parseChildFolders(xmlFolders, rt, File);
            parseChildFiles(xmlFiles, rt, File);
            rt.updateRefreshTimeWisely();
            return rt;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    public String GetAuthCookie() {
        return mCookie;
    }

    public void SetAuthCookie(String cookie) {
        mCookie = cookie;
    }

    private String listRootFolders() {
        return getResources(mAccount.getUrl()
                        + "/_api/web/lists?$filter=BaseTemplate eq 101&$select=Title,Created,RootFolder",
                METHOD_GET);
    }

    private String listRootSites() {
        return getResources(mAccount.getUrl() + "/_api/web/webs?$select=Title,Created", METHOD_GET);
    }

    private String getFolders(String CloudPath) {
        return getResources(CloudPath + "/Folders?$filter=Name ne 'Forms'", METHOD_GET);
    }

    private String getFiles(String CloudPath) {
        return getResources(CloudPath + "/Files", METHOD_GET);
    }

    private String getDocLists(String CloudPath) {
        return getResources(CloudPath + "/lists?$select=BaseTemplate,Title,Hidden,Id&$filter=BaseTemplate eq 101", METHOD_GET);
    }

    // used to get the user id (will be used to get detail info.)
    private String getCurrentUserInfo() {
        return getResources(mAccount.getUrl() + "/_api/web/CurrentUser", METHOD_GET);
    }

    // get the user and email
    private String getCurrentUserInfoDetail(String userId) {
        return getResources(mAccount.getUrl() + "/_api/web/SiteUserInfoList/Items" + "(" + userId + ")", METHOD_GET);
    }

    // get used Space and total space
    private String getSiteQuota() {
        return getResources(mAccount.getUrl() + "/_api/site/usage", METHOD_GET);
    }

    private String getChildrenSites(String CloudPath) {
        return getResources(CloudPath + "/webs", METHOD_GET);
    }

    public void delete(INxFile iNxFile) {
        deleteFileAndFolder(iNxFile.getCloudPath(), METHOD_POST);
    }

    public void create(INxFile iNxFile, String folderName) throws Exception {
        Log.d(TAG, "oye create: " + iNxFile.toString());
        String rootUrl = mAccount.getUrl();
        if (!iNxFile.isSite() && !iNxFile.getLocalPath().equals("/")) {
            String server;
            String uploadUrl;
            if (!rootUrl.endsWith("/")) {
                server = rootUrl + "/_api/web/folders";
                uploadUrl = rootUrl + iNxFile.getLocalPath();
            } else {
                server = rootUrl + "_api/web/folders";
                uploadUrl = rootUrl + iNxFile.getLocalPath().substring(1);
            }
            try {
                createFolder(server, uploadUrl, folderName, METHOD_POST);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String server;
            if (!rootUrl.endsWith("/")) {
                server = rootUrl + "/_api/web/lists";
            } else {
                server = rootUrl + "_api/web/lists";
            }
            try {
                createList(server, folderName, METHOD_POST);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private NxFileBase getBase(INxFile File) {
        NxFileBase rt = null;
        if (File.isSite()) {
            rt = new NXSite();
        } else {
            rt = new NXFolder();
        }
        fillParmas(rt, File.getLocalPath(), File.getCloudPath(), File.getSize(), File.getName(), "", "");
        return rt;
    }

    private String getResources(String Path, String method) {
        InputStream in = null;
        String ret = "";
        try {
            // path amend
            if (Path.contains(" ")) {
                Path = Path.replaceAll(" ", "%20");
            }
            URL url = new URL(Path);

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();

            String cookie = mAccount.getCookie();
            urlConn.setRequestProperty("Cookie", cookie);
            urlConn.setRequestMethod(method);

            Log.d("cookie", cookie);
            urlConn.setRequestProperty("Content-type", "application/atom+xml");
            urlConn.setDoInput(true);
            in = urlConn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        if (null == in) {
            return ret;
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        StringBuffer result = new StringBuffer();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ret = result.toString();
        return ret;
    }

    public void startDownloadFile(String CloudPath) {
        isCancel = false;
        try {
            mfilePath = CloudPath + "/$value";
            if (mfilePath.contains(" "))
                mfilePath = mfilePath.replaceAll(" ", "%20");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void startUploadFile(String CloudPath, String fileName, boolean bUpdate) {
        try {
            if (bUpdate) {
                mfilePath = CloudPath + "/Files/Add(overwrite=true,url='%@')";
            } else {
                mfilePath = CloudPath + "/Files/Add(overwrite=false,url='%@')";
            }
            mfilePath = mfilePath.replace("%@", fileName);
            if (mfilePath.contains(" "))
                mfilePath = mfilePath.replaceAll(" ", "%20");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public boolean downloadFile(String LocalPath, long fileSize, IUpdateDownLoadFile update) {
        boolean b = false;
        try {
            InputStream inputStream = openHttpsConnection(mfilePath);
            File local = new File(LocalPath);
            Helper.makeSureDocExist(local);
            //File output stream
            OutputStream outputStream = new FileOutputStream(local);
            b = copyStreamToOutput(inputStream, outputStream, fileSize, LocalPath, update);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return b;
    }

    private String parseDigestValue(String result) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse xml file
            Document document = db.parse(new InputSource(new StringReader(result)));
            // query xml document
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            Log.d("the text is: ", result);
            String value = xp.evaluate("//FormDigestValue/text()", document.getDocumentElement());

            return value;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getContextInfo() {
        return getResources(mAccount.getUrl() + "/_api/contextinfo", METHOD_POST);
    }

    public boolean uploadFile(File localFile, long fileSize, IUpdateUploadFile update) {
        // 1. get the FormDigestValue node first.
        String contextInfo = getContextInfo();
        String DigestValue = parseDigestValue(contextInfo);

        boolean b = false;
        long totalRead = 0;

        try {
            b = true;
            URL url = new URL(mfilePath);
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            String cookie = mAccount.getCookie();
            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Length", String.valueOf(fileSize));
            conn.setRequestProperty("X-RequestDigest", DigestValue);
            conn.setRequestProperty("Content-Type", "application/x-javascript; charset=" + "UTF-8");
            conn.setRequestMethod("POST");

            conn.connect();
            mSharepointonlineOutputStream = conn.getOutputStream();
            InputStream is = new FileInputStream(localFile);
            DataOutputStream dos = new DataOutputStream(mSharepointonlineOutputStream);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                dos.write(bytes, 0, len);

                totalRead += len;
                long newValue = (long) (totalRead / (double) fileSize * 100);
                if (newValue > 100) {
                    newValue = 100;
                }
                update.onUpdate(newValue);
            }
            is.close();
            dos.flush();

            String result = "";
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    result += inputLine;
                }
                in.close();
                Log.d(TAG, result);
            } else {
                b = false;
                ErrorCode.SHARE_POINT_ONLINE_UPLOAD_REQUEST_ERROR = conn.getResponseCode();
            }

        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            b = false;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            b = false;
        }

        return b;
    }

    private boolean copyStreamToOutput(InputStream input,
                                       OutputStream output,
                                       long fileSize,
                                       String localPath,
                                       IUpdateDownLoadFile update) {
        BufferedOutputStream bos = null;
        long totalRead = 0;
        long lastListened = 0;

        boolean bException = false;
        boolean b = true;

        try {
            bos = new BufferedOutputStream(output);

            byte[] buffer = new byte[4096];
            int read;
            while (true) {
                read = input.read(buffer);
                if (read < 0) {
                    break;
                }

                bos.write(buffer, 0, read);

                totalRead += read;

                long newValue = (long) (totalRead / (double) fileSize * 100);
                if (newValue > 100) {
                    newValue = 100;
                }

                if (isCancel) {
                    try {
                        // close stream at non-UI thread
                        if (mSharepointonlineInputStream != null) {
                            mSharepointonlineInputStream.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                update.onUpdate(newValue);
            }

            bos.flush();
            output.flush();
            // Make sure it's flushed out to disk
            try {
                if (output instanceof FileOutputStream) {
                    ((FileOutputStream) output).getFD().sync();
                }
            } catch (SyncFailedException e) {
                b = false;
            }

        } catch (IOException e) {
            b = false;
            bException = true;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                }
            }
            try {
                output.close();
            } catch (IOException e) {
            }
            try {
                input.close();
            } catch (IOException e) {
            }

            if (bException) {
                Helper.deleteFile(new File(localPath));
            }
        }

        return b;
    }

    public boolean deleteFileAndFolder(String cloudPath, String method) {
        if (DEBUG) {
            Log.d(TAG, "deleteFile: " + cloudPath);
        }
        // 1. get the FormDigestValue node first.
        String contextInfo = getContextInfo();
        String DigestValue = parseDigestValue(contextInfo);
        boolean result = false;
        try {
            if (cloudPath.contains(" ")) {
                cloudPath = cloudPath.replaceAll(" ", "%20");
            }
            URL url = new URL(cloudPath);
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(false);
            conn.setDoInput(false);
            conn.setUseCaches(false);
            String cookie = mAccount.getCookie();
//            conn.setRequestProperty("Authorization ", "Bearer" + cookie);
            conn.setRequestProperty("Cookie ", cookie);
            conn.setRequestProperty("X-RequestDigest", DigestValue);
            conn.setRequestProperty("IF-MATCH ", "*");
            conn.setRequestProperty("X-HTTP-Method", "DELETE");
            conn.setRequestMethod(method);

            conn.connect();
            Log.d(TAG, "deleteFile: " + conn.getResponseCode());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                result = true;
            } else {
                result = false;
            }

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            result = false;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    /**
     * This method is used to creat folder in remote repo
     */
    public boolean createFolder(String serverRoot, String cloudPath, String folderName, String method) throws Exception {
        if (DEBUG) {
            Log.e(TAG, "createFolderAsync: " + serverRoot);
        }
        // 1. get the FormDigestValue node first.
        String contextInfo = getContextInfo();
        String digestValue = parseDigestValue(contextInfo);
        boolean result = false;
        if (serverRoot.contains(" ")) {
            serverRoot = serverRoot.replaceAll(" ", "%20");
        }
        // fix cloudpath;
        String newFilePath = cloudPath + "/" + folderName;
        if (newFilePath.contains(" ")) {
            newFilePath = newFilePath.replaceAll(" ", "%20");
        }

        URL url = new URL(serverRoot);
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Cookie", mAccount.getCookie());
        conn.setRequestProperty("X-RequestDigest", digestValue);
        conn.setRequestProperty("accept", "application/json;odata=verbose");
        conn.setRequestProperty("content-type", "application/json;odata=verbose");
//            conn.setRequestProperty("content-length", String.valueOf(fileSize));
        conn.setRequestMethod(method);
        conn.connect();
//            body: { '__metadata': { 'type': 'SP.Folder' }, 'ServerRelativeUrl': '/document library relative url/folder name'}
        JSONObject requestBody = new JSONObject();
        JSONObject object = new JSONObject();
        object.put("type", "SP.Folder");
        requestBody.put("__metadata", object);
        requestBody.put("ServerRelativeUrl", newFilePath);
        if (DEBUG) {
            Log.e(TAG, "createFolderAsync: File Cloud Path:--->" + newFilePath);
        }
        OutputStream outputStream = conn.getOutputStream();

        byte[] outputInBytes = requestBody.toString().getBytes();
        if (DEBUG) {
            Log.e(TAG, "createFolderAsync: " + outputInBytes.length);
        }
        outputStream.write(outputInBytes);
        outputStream.close();


        int connCode = conn.getResponseCode();
        if (DEBUG) {
            Log.e(TAG, "createFolderAsync: " + connCode);
        }

        if (connCode == HttpURLConnection.HTTP_OK || connCode == HttpURLConnection.HTTP_CREATED || connCode == HttpURLConnection.HTTP_ACCEPTED) {
            result = true;
        } else {
            throw new IOException("change another name");
        }

        return result;
    }

    public boolean createList(String localPath, String folderName, String methodPost) throws Exception {
        if (DEBUG) {
            Log.e(TAG, "createList: " + localPath);
        }
        // 1. get the FormDigestValue node first.
        String contextInfo = getContextInfo();
        String digestValue = parseDigestValue(contextInfo);
        boolean result = false;
        if (localPath.contains(" ")) {
            localPath = localPath.replaceAll(" ", "%20");
        }

        URL url = new URL(localPath);
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Cookie", mAccount.getCookie());
        conn.setRequestProperty("X-RequestDigest", digestValue);
        conn.setRequestProperty("accept", "application/json;odata=verbose");
        conn.setRequestProperty("content-type", "application/json;odata=verbose");
        conn.setRequestMethod(methodPost);
        conn.connect();
        //fill requestParams
        JSONObject requestParams = new JSONObject();
        JSONObject object = new JSONObject();
        object.put("type", "SP.List");
        requestParams.put("__metadata", object);
        requestParams.put("AllowContentTypes", true);
        requestParams.put("BaseTemplate", 101);
        requestParams.put("ContentTypesEnabled", true);
        requestParams.put("Description", "henry");
        requestParams.put("Title", folderName);
        if (DEBUG) {
            Log.e(TAG, "createList: " + requestParams.toString());
        }
        byte[] requestBytes = requestParams.toString().getBytes();
        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(requestBytes);
        outputStream.close();
        if (DEBUG) {
            Log.e(TAG, "createList: " + conn.getResponseCode());
        }
        int connCode = conn.getResponseCode();
        //connect success will return 201 that means Request Sevser Success And Creat Folder
        if (connCode == HttpURLConnection.HTTP_OK || connCode == HttpURLConnection.HTTP_CREATED || connCode == HttpURLConnection.HTTP_ACCEPTED) {
            result = true;
        } else {
            throw new IOException("change another name");
        }
        return result;
    }

    public void abortTask() {
        isCancel = true;
    }

    public void abortUploadTask() {
        try {
            if (mSharepointonlineOutputStream != null) {
                mSharepointonlineOutputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private InputStream openHttpsConnection(String stringUrl) {

        int response = -1;
        try {

            URL url = new URL(stringUrl);

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();

            String cookie = mAccount.getCookie();
            urlConn.setRequestProperty("Cookie", cookie);
            urlConn.setDoInput(true);
            response = urlConn.getResponseCode();
            if (response == HttpsURLConnection.HTTP_OK) {
                mSharepointonlineInputStream = urlConn.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return mSharepointonlineInputStream;
    }

    private void fillParmas(NxFileBase Base,
                            String LocalPath,
                            String CloudPath,
                            long size,
                            String name,
                            String timeStr,
                            String cloudPathId) {
        Base.setDisplayPath(LocalPath);
        Base.setLocalPath(LocalPath);
        Base.setCloudPath(CloudPath);
        Base.setSize(size);
        Base.setName(name);
        Base.setmCloudPathID(cloudPathId);
        if (timeStr == null || timeStr.isEmpty()) {
            Base.setLastModifiedTimeLong(System.currentTimeMillis());
        } else {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Base.setLastModifiedTimeLong(df.parse(timeStr).getTime());
            } catch (Exception e) {
                e.printStackTrace();
                Base.setLastModifiedTimeLong(System.currentTimeMillis());
            }
        }
    }

    public interface IUpdateDownLoadFile {
        void onUpdate(long newValue);
    }

    public interface IUpdateUploadFile {
        void onUpdate(long newValue);
    }

    public static class Account {

        private String mUsername;
        private String mUrl;
        private String mCookie;

        public Account() {
            mUsername = "";
            mUrl = "";
            mCookie = "";
        }

        public String getUsername() {
            return this.mUsername;
        }

        public void setUsername(String Username) {
            this.mUsername = Username;
        }

        public String getAccountName() {
            return this.mUsername;
        }

        public String getUrl() {
            return this.mUrl;
        }

        public void setUrl(String Url) {
            this.mUrl = Url;
        }

        public String getAccountId() {
            return this.mUrl;
        }

        public String getCookie() {
            return this.mCookie;
        }

        public void setCookie(String Cookie) {
            this.mCookie = Cookie;
        }

        public String getToken() {
            return this.mCookie;
        }
    }

    public static class AuthenticationManager {

        private static final String TAG = "AuthenticationManager";
        private static final int CYCLE_COUNT = 3;
        /* Microsoft Online Security Token Service (STS) */
        private static final String STS = "https://login.microsoftonline.com/extSTS.srf";
        private static final String LOGIN = "/_forms/default.aspx?wa=wsignin1.0";
        //    private static final String SHAREPOINT_ONLINE_URL = "https://nextlabsdev.sharepoint.com/ProjectNova";
        private static final String SHAREPOINT_ONLINE_URL = "https://nextlabsdev.sharepoint.com"; // only use host
        private static final String GET_ROOT_FOLDER = "/_api/web/lists?$filter=BaseTemplate eq 101&$select=Title,Created,RootFolder";
        private static final String REQUEST_XML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:u=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><s:Header><a:Action s:mustUnderstand=\"1\">http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue</a:Action><a:ReplyTo><a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address></a:ReplyTo><a:To s:mustUnderstand=\"1\">https://login.microsoftonline.com/extSTS.srf</a:To><o:Security s:mustUnderstand=\"1\" xmlns:o=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"><o:UsernameToken><o:Username>[username]</o:Username><o:Password>[password]</o:Password></o:UsernameToken></o:Security></s:Header><s:Body><t:RequestSecurityToken xmlns:t=\"http://schemas.xmlsoap.org/ws/2005/02/trust\"><wsp:AppliesTo xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"><a:EndpointReference><a:Address>[endpoint]</a:Address></a:EndpointReference></wsp:AppliesTo><t:KeyType>http://schemas.xmlsoap.org/ws/2005/05/identity/NoProofKey</t:KeyType><t:RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</t:RequestType><t:TokenType>urn:oasis:names:tc:SAML:1.0:assertion</t:TokenType></t:RequestSecurityToken></s:Body></s:Envelope>";

        private String mUsername;
        private String mPassword;
        private String mUrl;

        public String sharepointOnlineAuth() {

            try {
                String result = requestToken();
                String token = extractToken(result);
                int lengh = token.length(); // 1085
                String cookie = extractCookie(token);
                int len = cookie.length(); // 1198
                return cookie;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        // fill in the soap package with user name, password and url
        private String generateSAML() {
            mUsername = LoginActivity.getmInstance().getUsername();
            mPassword = LoginActivity.getmInstance().getPassword();
            mUrl = LoginActivity.getmInstance().getUrl();
            // fill in the soap package.
            String saml = REQUEST_XML.replace("[username]", mUsername);
            saml = saml.replace("[password]", mPassword);
            saml = saml.replace("[endpoint]", mUrl);
            return saml;
        }

        // Send SAML Request to STS（Microsoft online Security Token Service）
        private String requestToken() throws XPathExpressionException, SAXException,
                ParserConfigurationException, IOException {

            String saml = generateSAML();
            URL url = new URL(STS);

            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();

            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            urlConn.setRequestMethod("POST");
            urlConn.addRequestProperty("Content-Type", "text/xml; charset=utf-8");

            // write the SAML info into server
            OutputStream out = urlConn.getOutputStream();
            Writer wout = new OutputStreamWriter(out);
            wout.write(saml);

            wout.flush();
            wout.close();

            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("POST request not worked" + "-" + String.valueOf(urlConn.getResponseCode()));
            }

            // receive SAML response including security token
            InputStream in = urlConn.getInputStream();
            int c;
            StringBuilder sb = new StringBuilder("");
            while ((c = in.read()) != -1)
                sb.append((char) (c));
            in.close();
            String result = sb.toString();

            return result;
        }

        // parse token from the SAML response
        private String extractToken(String result) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse xml file
            Document document = db.parse(new InputSource(new StringReader(result)));
            // query xml document
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            Log.d("the text is: ", result);
            String token = xp.evaluate("//BinarySecurityToken/text()", document.getDocumentElement());
            return token;
        }

        // send security token to SPO (SharePoint Online)
        private String extractCookie(String token) throws IOException {

            URL url = new URL(SHAREPOINT_ONLINE_URL + LOGIN);

            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            String Cookies = "";
            boolean reDirect = false;
            int cycleCount = 0;
            do {
                cycleCount++;
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.addRequestProperty("Accept", "application/x-www-form-urlencoded");
                connection.addRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0)");
                connection.addRequestProperty("Content-Type", "text/xml; charset=utf-8");
                // true means the system handle the redirect automatically; true means manual handle the redirect, in this case
                // we can get some useful data such as: cookie and Location.
                connection.setInstanceFollowRedirects(false);

                // write the token into server
                OutputStream out = connection.getOutputStream();
                Writer wout = new OutputStreamWriter(out);
                wout.write(token);
                wout.flush();
                wout.close();
                // 302 redirect -- Temporarily Moved: need to optimize!

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    int code = connection.getResponseCode();
                    Map<String, List<String>> head = connection.getHeaderFields();
                    // throw new RuntimeException("POST request not worked" + "-" + String.valueOf(connection.getResponseCode()));
                }

                // get and parse the cookie
                List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
                for (String str : cookies) {
                    if (str.contains("rtFa")) {
                        String[] str1 = str.split(";");
                        Cookies += str1[0];
                        Cookies += ";";
                    } else if (str.contains(("FedAuth"))) {
                        String[] str2 = str.split(";");
                        Cookies += str2[0];
                    }
                }
                if (Cookies.equals("")) {
                    reDirect = true;
                    continue;
                } else
                    break;
            } while (reDirect && cycleCount <= CYCLE_COUNT);

            return Cookies;
        }

        public boolean verifySiteUrl(String siteUrl, String cookie) {
            String Path = siteUrl + GET_ROOT_FOLDER;
            boolean isRightUrl = true;
            try {
                if (Path.contains(" ")) {
                    Path = Path.replaceAll(" ", "%20");
                }
                URL url = new URL(Path);

                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
                HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();

                urlConn.setRequestProperty("Cookie", cookie);
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Content-type", "application/atom+xml");
                urlConn.setDoInput(true);
                urlConn.getInputStream();
            } catch (IOException e) {
                if (e.toString().contains("FileNotFoundException")) {
                    isRightUrl = false;
                }
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            return isRightUrl;
        }

        private class MyHostnameVerifier implements HostnameVerifier {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                // TODO Auto-generated method stub
                return true;
            }
        }

        private class MyTrustManager implements X509TrustManager {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                // Auto-generated method stub
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                // TODO Auto-generated method stub
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                // TODO Auto-generated method stub
                return null;
            }
        }
    }

    public static class LoginActivity extends Activity {
        static private final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
        private static WeakReference<LoginActivity> mInstance;
        public EditText mEditTextUsername;
        private Button mButton;
        private EditText mEditTextPassword;
        private EditText mEditTextUrl;
        private AuthenticationManager mAuthenticationManager;
        private View mView;
        private ProgressDialog mProgressDialog;
        private Account account;
        private TextView mBack;

        public static LoginActivity getmInstance() {
            return mInstance.get();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_sharepoint_online);

            mBack = (TextView) findViewById(R.id.tv_back);
            mBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LoginActivity.this.finish();
                }
            });

            mInstance = new WeakReference<>(this);
            mButton = (Button) findViewById(R.id.sharepointonline_sign_in_button);

            mEditTextUrl = (EditText) findViewById(R.id.sharepointonline_url);
            mEditTextUsername = (EditText) findViewById(R.id.sharepointonline_username);
            mEditTextPassword = (EditText) findViewById(R.id.sharepointonline_password);

            if (DEBUG) {
//                mEditTextUrl.setText("https://nextlabsdev.sharepoint.com/ProjectNova/");
//                mEditTextUsername.setText("mxu@nextlabsdev.onmicrosoft.com");
//                mEditTextPassword.setText("123blue!");
                mEditTextUrl.setText("http://rms-sp2013.qapf1.qalab01.nextlabs.com/sites/iosdev");
                mEditTextUsername.setText("abraham.lincoln@qapf1.qalab01.nextlabs.com");
                mEditTextPassword.setText("abraham.lincoln");
            }

            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bind();
                }
            });
        }

        private void bind() {
            // check input
            boolean paraError = false;
            View focusView = null;
            if (TextUtils.isEmpty(mEditTextUrl.getText().toString())) {
                GenericError.showUI(LoginActivity.this, getString(R.string.hint_msg_require_input_url), true, false, false, null);
                focusView = mEditTextUrl;
                paraError = true;
            } else if (TextUtils.isEmpty(mEditTextUsername.getText().toString())) {
                GenericError.showUI(LoginActivity.this, getString(R.string.hint_msg_require_input_userName), true, false, false, null);
                focusView = mEditTextUsername;
                paraError = true;
            } else if (TextUtils.isEmpty(mEditTextPassword.getText().toString())) {
                GenericError.showUI(LoginActivity.this, getString(R.string.hint_msg_require_input_password), true, false, false, null);
                focusView = mEditTextPassword;
                paraError = true;
            }
            if (paraError) {
                focusView.requestFocus();
                return;
            }

            account = new Account();
            account.setUsername(getUsername());
            account.setUrl(getUrl());
            new GetCookieAsyncTask().executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI));
        }

        public String getUsername() {
            return mEditTextUsername.getText().toString();
        }

        public String getPassword() {
            return mEditTextPassword.getText().toString();
        }

        public String getUrl() {
            String url = mEditTextUrl.getText().toString();
            if (url.endsWith("/")) {
                return url;
            } else {
                return url + "/";
            }
        }

        private class GetCookieAsyncTask extends AsyncTask<Void, Void, Boolean> {
            private boolean isNetworkAvailable = true;
            private boolean canGetCookie = true;
            private boolean isRightUrl = true;

            @Override
            protected Boolean doInBackground(Void... params) {
                if (SkyDRMApp.getInstance().isNetworkAvailable()) {
                    mAuthenticationManager = new AuthenticationManager();
                    String cookie = mAuthenticationManager.sharepointOnlineAuth();
                    if (!TextUtils.isEmpty(cookie)) {
                        account.setCookie(cookie);
                        if (mAuthenticationManager.verifySiteUrl(account.getUrl(), cookie)) {
                            return isRightUrl;
                        } else {
                            return isRightUrl = false;
                        }
                    } else {
                        return canGetCookie = false;
                    }
                } else {
                    return isNetworkAvailable = false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                mProgressDialog.dismiss();
                if (result) {
                    finish();
                    setAuthAccount(account);
                    setAuthStatus(true);
                } else if (!isRightUrl) {
                    GenericError.showUI(LoginActivity.this, getString(R.string.error_sharePointOnline_site_url), true, false, false, null);
                } else if (!isNetworkAvailable) {
                    GenericError.showUI(LoginActivity.this, getString(R.string.error_network_unreach_msg), true, false, false, null);
                } else {
                    GenericError.showUI(LoginActivity.this, getString(R.string.error_sharePointOnline_login), true, false, false, null);
                }
            }

            @Override
            protected void onPreExecute() {
                mProgressDialog = ProgressDialog.show(LoginActivity.this, "", getString(R.string.add_account));
            }
        }

    }

    private class MyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            return true;
        }
    }

    private class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // Auto-generated method stub
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // TODO Auto-generated method stub
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
