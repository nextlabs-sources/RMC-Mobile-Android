package com.skydrm.rmc.engine.Render;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.utils.FileHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.skydrm.rmc.engine.Render.FileRenderProxy.FileType.FILE_TYPE_3D;
import static com.skydrm.rmc.engine.Render.FileRenderProxy.FileType.FILE_TYPE_AUDIO;
import static com.skydrm.rmc.engine.Render.FileRenderProxy.FileType.FILE_TYPE_IMAGE;
import static com.skydrm.rmc.engine.Render.FileRenderProxy.FileType.FILE_TYPE_INVALID_NXL;
import static com.skydrm.rmc.engine.Render.FileRenderProxy.FileType.FILE_TYPE_NOT_SUPPORT;
import static com.skydrm.rmc.engine.Render.FileRenderProxy.FileType.FILE_TYPE_OFFICE;
import static com.skydrm.rmc.engine.Render.FileRenderProxy.FileType.FILE_TYPE_PDF;
import static com.skydrm.rmc.engine.Render.FileRenderProxy.FileType.FILE_TYPE_REMOTE_VIEW;
import static com.skydrm.rmc.engine.Render.FileRenderProxy.FileType.FILE_TYPE_TXT;
import static com.skydrm.rmc.engine.Render.FileRenderProxy.FileType.FILE_TYPE_VIDEO;

/**
 * Created by aning on 11/22/2016.
 */

public class RenderHelper {
    // basic 3D file format
    static public final String FILE_EXTENSION_HSF = "hsf";
    static public final String FILE_EXTENSION_VDS = "vds";
    private static final DevLog log = new DevLog(RenderHelper.class.getSimpleName());
    // buffer size
    private static final int BUFFER_SIZE = 4096;
    // original, need rename them
    static private final String VIEW_TO_VIEW = "NXInitViewToView";
    static private final String LOGIN_TO_VIEW = "NXLoginToView";
    static private final String VIEWENCRYPTFILE = "NXViewEncryptFile";
    static private final String HOME_TO_VIEW = "NXHomeToView";
    static private final String MYVAULT_TO_VIEW = "NXMyVaultToView";
    static private final String SHARED_WITH_ME_TO_VIEW = "NXSharedWithMeToView";
    static private final String PROJECTS_TO_VIEW = "NXProjectsToView";
    static private final String WORKSPACE_TO_VIEW = "NXWorkSpaceToView";
    static private final String VIEW_TO_LOGIN = "NXInitViewToLogin";
    static private final String OFFLINE_TO_VIEW = "NXOfflineToView";
    static private final String VIEW_SHARE_LINK = "android.intent.action.VIEW";
    static private final String FILE_EXTENSION_PRT = "prt";
    static private final String FILE_EXTENSION_JT = "jt";
    // 3D pdf
    static private final String FILE_EXTENSION_PDF = "pdf";
    static private final String TMP_PATH = "tmp/convertFile";

    // google self-file format
    static private final String GOOGLE_FORMAT_DOC = "application/vnd.google-apps.document";
    static private final String GOOGLE_FORMAT_SHEET = "application/vnd.google-apps.spreadsheet";
    static private final String GOOGLE_FORMAT_SLIDE = "application/vnd.google-apps.presentation";
    static private final String GOOGLE_FORMAT_DRAW = "application/vnd.google-apps.drawing";

    // google file exported mimeType
    static private final String FILE_WORD_DOCUMENT = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    static private final String FILE_MS_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static private final String FILE_MS_POWERPOINT = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    static private final String FILE_DRAW_PNG = "image/png";

    private static final String[] FILE_TYPE_3DS = new String[]{
            "hsf", "stl", "obj", "vds",
            "pdf", "prc", "u3d", "step",
            "jt", "iges", "ifc", "ifczip",
            "x_b", "x_t", "x_mt", "xmt_txt"
    };

    public static FileSource judgeFileSource(Intent intent) {
        if (intent == null) {
            return FileSource.ERROR_SOURCE;
        }

        String strAction = intent.getAction();
        if (TextUtils.equals(strAction, HOME_TO_VIEW)) {
            return FileSource.FROM_HOME;
        }

        if (TextUtils.equals(strAction, VIEW_TO_LOGIN) || TextUtils.equals(strAction, VIEW_SHARE_LINK)) {
            return FileSource.OPEN_AS_THIRD_PARTY;
        }

        if (TextUtils.equals(strAction, VIEW_TO_VIEW) ||
                TextUtils.equals(strAction, LOGIN_TO_VIEW) ||
                TextUtils.equals(strAction, VIEWENCRYPTFILE)) {
            return FileSource.FROM_PROTECT_VIEW;
        }

        if (TextUtils.equals(strAction, MYVAULT_TO_VIEW)) {
            return FileSource.FROM_MYVAULT;
        }

        if (TextUtils.equals(strAction, PROJECTS_TO_VIEW)) {
            return FileSource.FROM_PROJECTS;
        }
        if (TextUtils.equals(strAction, SHARED_WITH_ME_TO_VIEW)) {
            return FileSource.FROM_SHARED_WITH_ME;
        }
        if (TextUtils.equals(strAction, OFFLINE_TO_VIEW)) {
            return FileSource.FROM_OFFLINE_VIEW;
        }
        if (TextUtils.equals(strAction, WORKSPACE_TO_VIEW)) {
            return FileSource.FROM_WORKSPACE_VIEW;
        }
        return FileSource.ERROR_SOURCE;
    }

    /**
     * judge the file if is com.skydrm.rmc.nxl file or not
     *
     * @param filePath
     */
    public static boolean isNxlFile(String filePath) {
        return SkyDRMApp.getInstance().getSession().getRmsClient().isNxlFile(filePath, false);
    }


    /**
     * Judge file type through file mimeType
     *
     * @param fileName file name
     * @param type     file mimeType
     * @return {@link FileRenderProxy.FileType}
     */
    private static FileRenderProxy.FileType dispatchByMimeType(String fileName, String type) {

        if (type == null) {
            return FILE_TYPE_NOT_SUPPORT;
        }

        if ((type.startsWith("image/"))) {
            if (type.startsWith("image/tiff")) {
                return FILE_TYPE_REMOTE_VIEW;
            }
            return FILE_TYPE_IMAGE;
        } else if (type.startsWith("text/")) {
            if (type.contains("rtf")) {
                return FILE_TYPE_REMOTE_VIEW;
            }
            return FILE_TYPE_TXT;
        } else if (type.endsWith("application/pdf")) {
            return FILE_TYPE_PDF;
        } else if (type.equals("application/msword")
                || type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || type.equals("application/vnd.ms-powerpoint")
                || type.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                || type.equals("application/vnd.ms-excel")
                || type.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || type.equals("application/vnd.visio") // vsd file also use remoteViewer
                || type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.template") // .dotx
                || type.equals("application/vnd.openxmlformats-officedocument.presentationml.template")  // .potx
                || type.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.template")) {  // .xltx
            return FILE_TYPE_OFFICE;
        } else if (type.startsWith("video/")) {
            return FILE_TYPE_VIDEO;
        } else if (type.startsWith("audio/")) {
            return FILE_TYPE_AUDIO;
        } else if (type.equals("model/iges") || type.equals("application/vnd.ms-pki.stl")) {
            // judge 3D
            if (isThreeD(fileName)) {
                return FILE_TYPE_3D;
            }
        }

        return FILE_TYPE_NOT_SUPPORT;
    }


    /**
     * @param fileName  file name
     * @param extension file extension
     * @return {@link FileRenderProxy.FileType}
     */
    private static FileRenderProxy.FileType dispatchByExtension(String fileName, String extension) {

        if (extension == null) {
            return FILE_TYPE_NOT_SUPPORT;
        }

        if (extension.equalsIgnoreCase("dwg")
                || extension.equalsIgnoreCase("vsdx")
                || extension.equalsIgnoreCase("dxf")
                || extension.equalsIgnoreCase("dotm")
                || extension.equalsIgnoreCase("potm")
                || extension.equalsIgnoreCase("xltm")
                || extension.equalsIgnoreCase("xlsb")
                || extension.equalsIgnoreCase("xlsm")
                || extension.equalsIgnoreCase("docm")
                || extension.equalsIgnoreCase("js")
                || extension.equalsIgnoreCase("model")
                || extension.equalsIgnoreCase("properties")) {
            return FILE_TYPE_REMOTE_VIEW;
        } else if (
                extension.equalsIgnoreCase("log")
                        || extension.equalsIgnoreCase("py")
                        || extension.equalsIgnoreCase("md")
                        || extension.equalsIgnoreCase("m")
                        || extension.equalsIgnoreCase("swift")
                        || extension.equalsIgnoreCase("err")
                        || extension.equalsIgnoreCase("sql")
                        || extension.equalsIgnoreCase("vb")
                        || extension.equalsIgnoreCase("json")) {
            return FILE_TYPE_TXT;
        } else {
            // judge 3D
            if (isThreeD(fileName)) {
                return FILE_TYPE_3D;
            }
        }

        return FILE_TYPE_NOT_SUPPORT;
    }


    /**
     * used to judge file type
     *
     * @param file
     */
    public static FileRenderProxy.FileType judgeFileType(File file) {
        if (file == null || !file.exists()) {
            throw new RuntimeException("file does not exist.");
        }

//        if (file.getName().toLowerCase().endsWith(".nxl")) {
//            return FILE_TYPE_INVALID_NXL;
//        }

        // try to get file mimeType
        String type = parseMimeType(Uri.fromFile(file).toString());
        log.d(type);
        if (!TextUtils.isEmpty(type)) {
            // can get file mimeType
            return dispatchByMimeType(file.getName(), type);
        } else { // mimeType is null
            // can't get file mimeType then use the file extension.
            String extension = RenderHelper.getFileExtension(Uri.fromFile(file).toString());
            return dispatchByExtension(file.getName(), extension);
        }
    }


    /**
     * Used to judge file name by file name
     *
     * @param fileName normal file name
     */
    private static FileRenderProxy.FileType judgeFileType(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("file name is empty");
        }

        if (fileName.toLowerCase().endsWith(".nxl")) {
            return FILE_TYPE_INVALID_NXL;
        }

        String type = parseMimeTypeByFileName(fileName);
        if (!TextUtils.isEmpty(type)) {
            // can get file mimeType
            return dispatchByMimeType(fileName, type);
        } else { // mimeType is null
            // can't get file mimeType then use the file extension.
            String extension = RenderHelper.getExtensionByName(fileName);
            return dispatchByExtension(fileName, extension);
        }

    }

    /**
     * For myDrive, myVault & project file which can't render in local, will use the simple remote view.
     */
    public static boolean isNeedSimpleRemoteView(@NonNull String fileName) {

        FileRenderProxy.FileType fileType = null;
        if (fileName.endsWith(".nxl")) {
            String normalFileName = fileName.substring(0, fileName.lastIndexOf("."));
            fileType = judgeFileType(normalFileName);
        } else {
            fileType = judgeFileType(fileName);
        }

        if (fileType == FILE_TYPE_OFFICE
//                || fileType == FILE_TYPE_PDF
                || fileType == FILE_TYPE_REMOTE_VIEW) {
            return true;
        }

        return false;
    }

    /**
     * judge if is three D file type
     *
     * @param fileName
     */
    private static boolean isThreeD(String fileName) {
        // sanity check
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        // for quick judging
        if (extension.equalsIgnoreCase(FILE_EXTENSION_HSF)
                || extension.equalsIgnoreCase(FILE_EXTENSION_JT)
                || extension.equalsIgnoreCase(FILE_EXTENSION_VDS)
                || extension.equalsIgnoreCase(FILE_EXTENSION_PDF)
                || extension.equalsIgnoreCase(FILE_EXTENSION_PRT)) {
            return true;
        }
        Set<String> localHPSSupportedTypes = getLocalHPSSupportedTypes();
        if (localHPSSupportedTypes.contains(extension)) {
            return true;
        }
        // RMS-supported conversion, it is also be regarded as 3d-file
        return is3DFileNeedConvertFormat(fileName);
    }

    private static Set<String> getLocalHPSSupportedTypes() {
        return new HashSet<>(Arrays.asList(FILE_TYPE_3DS));
    }

    /**
     * Judge the file if is Google file: google-doc, google-sheet, google-slide and google-draw.
     *
     * @return
     */
    public static boolean isGoogleFile(INxFile document) {
        return document.getUserDefinedStr().equals(GOOGLE_FORMAT_DOC)
                || document.getUserDefinedStr().equals(GOOGLE_FORMAT_SLIDE)
                || document.getUserDefinedStr().equals(GOOGLE_FORMAT_SHEET)
                || document.getUserDefinedStr().equals(GOOGLE_FORMAT_DRAW);
    }


    public static boolean isGoogleFile(String mimeType) {
        return mimeType.equals(GOOGLE_FORMAT_DOC)
                || mimeType.equals(GOOGLE_FORMAT_SLIDE)
                || mimeType.equals(GOOGLE_FORMAT_SHEET)
                || mimeType.equals(GOOGLE_FORMAT_DRAW);
    }

    /**
     * Get the google file exported file mimeType, can refer the site: https://developers.google.com/drive/v3/web/manage-downloads
     *
     * @return the exported file mime type.
     */
    public static String getGoogleExportedFormat(INxFile document) {
        if (document.getUserDefinedStr().equals(GOOGLE_FORMAT_DOC)) {
            return FILE_WORD_DOCUMENT;
        } else if (document.getUserDefinedStr().equals(GOOGLE_FORMAT_SLIDE)) {
            return FILE_MS_POWERPOINT;
        } else if (document.getUserDefinedStr().equals(GOOGLE_FORMAT_SHEET)) {
            return FILE_MS_EXCEL;
        } else if (document.getUserDefinedStr().equals(GOOGLE_FORMAT_DRAW)) {
            return FILE_DRAW_PNG;
        }

        return null;
    }

    /**
     * Used to get the google exported file name.
     *
     * @param document google document.
     * @return the file name of appended postfix.
     */
    public static String getGoogleExportFileName(INxFile document) {

        if (document.getUserDefinedStr().equals(GOOGLE_FORMAT_DOC) && !document.getName().toLowerCase().endsWith(".docx")) {
            return document.getName() + ".docx";
        }

        if (document.getUserDefinedStr().equals(GOOGLE_FORMAT_SHEET) && !document.getName().toLowerCase().endsWith(".xlsx")) {
            return document.getName() + ".xlsx";
        }

        if (document.getUserDefinedStr().equals(GOOGLE_FORMAT_SLIDE) && !document.getName().toLowerCase().endsWith(".pptx")) {
            return document.getName() + ".pptx";
        }

        if (document.getUserDefinedStr().equals(GOOGLE_FORMAT_DRAW) && !document.getName().toLowerCase().endsWith(".png")) {
            return document.getName() + ".png";
        }

        return document.getName();
    }

    /**
     * Used to append corresponding exported postfix for google file
     *
     * @param mimeType google file mimeType
     * @param filePath file path
     * @return the filePath of appended postfix.
     */
    public static String appendGoogleFileExportPostfix(String mimeType, String filePath) {

        if (mimeType.equals(GOOGLE_FORMAT_DOC) && !filePath.toLowerCase().endsWith(".docx")) {
            return filePath + ".docx";
        }

        if (mimeType.equals(GOOGLE_FORMAT_SHEET) && !filePath.toLowerCase().endsWith(".xlsx")) {
            return filePath + ".xlsx";
        }

        if (mimeType.equals(GOOGLE_FORMAT_SLIDE) && !filePath.toLowerCase().endsWith(".pptx")) {
            return filePath + ".pptx";
        }

        if (mimeType.equals(GOOGLE_FORMAT_DRAW) && !filePath.toLowerCase().endsWith(".png")) {
            return filePath + ".png";
        }

        return filePath;
    }

    public static boolean is3DFileNeedConvertFormat(String fileName) {
        // sanity check
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }

        Set<String> supportedCadFormats = SkyDRMApp.getInstance().getSupportedCadFormats();
        if (supportedCadFormats == null || supportedCadFormats.size() == 0) {
            // TODO: 11/22/2016  error dialog
            return false;
        }

        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")).toLowerCase() : "";
        return supportedCadFormats.contains(extension);
    }

    /**
     * handle the attachment of mail when as third party to open.
     *
     * @param context
     * @param uri
     */
    public static String handleFileOnMail(Context context, Uri uri) {

        Cursor cursor = null;
        String attachFileName = null;
        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            List<String> pathSegments = uri.getPathSegments();
            if (pathSegments.size() > 0) {
                attachFileName = pathSegments.get(pathSegments.size() - 1);
            }
        } else if (scheme.equals("content")) {
            cursor = context.getContentResolver().query(uri, new String[]{
                    MediaStore.MediaColumns.DISPLAY_NAME
            }, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    attachFileName = cursor.getString(nameIndex);
                }
            }
        } else {
            return null;
        }

        if (cursor != null) {
            cursor.close();
        }

        if (attachFileName == null) {
            return null;
        } else {
            return attachFileName;
        }

    }

    // judge the txt file char set
    private static String getCharset(String filePath) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(filePath));
        int p = (bin.read() << 8) + bin.read();
        String code = null;
        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            default:
                code = "GBK";
        }
        return code;
    }

    /**
     * read the content of text file
     *
     * @param uri
     * @param bWithCharset if takes with the charset.
     */
    public static String readTextFile(Uri uri, boolean bWithCharset) {
        String ret = "";

        try {
            File file = new File(uri.getPath());
            FileInputStream inputStream = new FileInputStream(file);

            try {
                InputStreamReader inputStreamReader = bWithCharset ? new InputStreamReader(inputStream, getCharset(file.getPath()))
                        : new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                    stringBuilder.append("\n");
                }
                ret = stringBuilder.toString();
            } catch (IOException e) {
                throw e;
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            Log.e("ViewFile activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("ViewFile activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    /**
     * wrapper that used to copy data
     *
     * @param context
     * @param uri
     * @param fileName
     * @param tmpPath
     */
    public static File copyData(Context context, Uri uri, String fileName, String tmpPath) {
        InputStream is = null;
        FileOutputStream os = null;
        File tmpFile = null;
        String tmpFilePath = null;

        try {
            File base = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                base = context.getExternalFilesDir(null);
                base = new File(base, tmpPath);
                if (!base.exists()) {
                    base.mkdirs();
                }
            }

            if (base == null) {
                return null;
            }

            tmpFilePath = base.toString() + "/" + fileName;
            try {
                tmpFile = new File(tmpFilePath);
                if (!tmpFile.exists())
                    tmpFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            is = context.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(tmpFile);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
            os.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        return tmpFile;
    }

    public static String saveFile(Context context, String fileName, byte[] convertedFileData, boolean isNxlFile) {

        String tmpFilePath = "";
        if (isNxlFile) {
            // for nxl 3d file, we cache the converted data into the internal storage(/data/data/SkyDRM/converted3D/xxx)
            // the expire is 24 hours, will clear it if timeout or user logout.
            File tmpFile = getConvertedNxl3DMountPoint(context);
            if (tmpFile != null) {
                tmpFilePath = tmpFile.getPath() + "/" + fileName;
            }
        } else {
            // for normal 3d file, we cache the converted data into local(external storage: sd card)
            // will clear it if user logout.
            File mountPoint = RenderHelper.getConvertedNormal3DMountPoint();
            if (mountPoint != null) {
                tmpFilePath = mountPoint.toString() + "/" + fileName;
            }
        }

        File tmpLocal = null;
        try {
            tmpLocal = new File(tmpFilePath);
            if (!tmpLocal.exists())
                tmpLocal.createNewFile();
        } catch (IOException e) {
            log.d("create temporary file failed!");
            e.printStackTrace();
            return null;
        }

        // write content data into file
        try {
            DataOutputStream d = new DataOutputStream(new FileOutputStream(tmpLocal));
            d.write(convertedFileData);
            d.flush();
        } catch (Exception e) {
            log.d("write data file failed!");
            e.printStackTrace();
            return null;
        }

        return tmpFilePath;
    }

    /**
     * parse the file mime type
     *
     * @param url
     */
    public static String parseMimeType(String url) {
        try {
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(URLEncoder.encode(url, "UTF-8"));
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            }

            return type;
        } catch (UnsupportedEncodingException e) {
            return null;
        }

    }

    /**
     * @param url file url
     * @return file extension.
     */
    public static String getFileExtension(String url) {
        try {
            return MimeTypeMap.getFileExtensionFromUrl(URLEncoder.encode(url, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param fileName file name
     * @return extension
     */
    public static String getExtensionByName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }

        int dot = fileName.lastIndexOf('.');
        if (dot > -1 && dot < fileName.length()) {
            return fileName.substring(dot + 1);
        }
        return "";
    }

    /**
     * get file mime type by file name
     *
     * @param fileName file name
     * @return type
     * note: may return null
     */
    public static String parseMimeTypeByFileName(String fileName) {

        if (!fileName.contains(".")) {
            return null;
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    }

    /**
     * converted the email list into a json string.
     */
    public static String emailList2Json(List<String> emailList) {
        String strRet = "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < emailList.size(); ++i) {
            if (i == 0) {
                sb.append(emailList.get(i));
            } else {
                sb.append(",");
                sb.append(emailList.get(i));
            }
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("recipients", sb.toString());
            strRet = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strRet;
    }

    public static void copyFileStreams(File source, File dest)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
    }

    // myVault files local mount point.
    public static File getMyVaultMountPoint() {
        try {
            File userRootFile = SkyDRMApp.getInstance().getCommonDirs().userRootFile();
            File myVaultMountPoint = new File(userRootFile, "myVault");
            if (!Helper.makeSureDirExist(myVaultMountPoint)) {
                throw new RuntimeException("make myVault point failed" + myVaultMountPoint.getPath());
            }
            return myVaultMountPoint;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // share link file local mount point
    public static File getShareLinkFileMountPoint() {
        try {
            File userRootFile = SkyDRMApp.getInstance().getCommonDirs().userRootFile();
            File myVaultMountPoint = new File(userRootFile, "sharedWithMe");
            if (!Helper.makeSureDirExist(myVaultMountPoint)) {
                throw new RuntimeException("make sharedWithMe point failed" + myVaultMountPoint.getPath());
            }
            return myVaultMountPoint;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clearMyVaultLocalCache() {
        try {
            File userRootFile = SkyDRMApp.getInstance().getCommonDirs().userRootFile();
            File myVaultMountPoint = new File(userRootFile, "myVault");
            if (myVaultMountPoint.exists() && myVaultMountPoint.isDirectory()) {
                File[] files = myVaultMountPoint.listFiles();
                for (File f : files) {
                    f.delete();
                }
            }
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
    }

    public static long calcMyVaultLocalCache() {
        long cacheSize = 0;
        File myVaultMountPoint = getMyVaultMountPoint();
        if (myVaultMountPoint != null && myVaultMountPoint.exists() && myVaultMountPoint.isDirectory()) {
            File[] files = myVaultMountPoint.listFiles();
            for (File f : files) {
                cacheSize += f.length();
            }
        }
        return cacheSize;
    }

    public static void clearSharedWithMeLocalCache() {
        //clear shared with me cache
        File rootCacheDir = RenderHelper.getShareLinkFileMountPoint();
        if (rootCacheDir != null && rootCacheDir.exists()) {
            File[] files = rootCacheDir.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static long calculateSharedWithMeLocalCache() {
        long cacheSize = 0;
        File rootCacheDir = RenderHelper.getShareLinkFileMountPoint();
        if (rootCacheDir != null && rootCacheDir.exists()) {
            File[] files = rootCacheDir.listFiles();
            for (File file : files) {
                cacheSize += file.length();
            }
        }
        return cacheSize;
    }

    // projects files local mount point. --- like: userEmail/projects/projectName/ROOT
    public static File getProjectsMountPoint(String projectName) {
        try {
            boolean dirMakeSucceed = true;
            File userRootFile = SkyDRMApp.getInstance().getCommonDirs().userRootFile();
            File projectsMountPoint = new File(userRootFile, "projects");
            if (!projectsMountPoint.exists()) {
                dirMakeSucceed = projectsMountPoint.mkdirs();
            }

            if (dirMakeSucceed) {
                projectsMountPoint = new File(projectsMountPoint, projectName);
                if (!projectsMountPoint.exists()) {
                    dirMakeSucceed = projectsMountPoint.mkdirs();
                }

                if (dirMakeSucceed) {
                    projectsMountPoint = new File(projectsMountPoint, "ROOT");
                    if (!Helper.makeSureDirExist(projectsMountPoint)) {
                        throw new RuntimeException("make projects point failed" + projectsMountPoint.getPath());
                    }
                }
            }

            return projectsMountPoint;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // converted normal CAD file mount point.
    public static File getConvertedNormal3DMountPoint() {
        try {
            File userRootFile = SkyDRMApp.getInstance().getCommonDirs().userRootFile();
            File converted3dMountPoint = new File(userRootFile, "converted3D");
            if (!Helper.makeSureDirExist(converted3dMountPoint)) {
                throw new RuntimeException("make converted normal 3D point failed" + converted3dMountPoint.getPath());
            }
            return converted3dMountPoint;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // converted nxl CAD file mount point.
    public static File getConvertedNxl3DMountPoint(Context context) {
        try {
            File internalDataCache = new File(context.getApplicationContext().getCacheDir().getPath());
            File converted3dMountPoint = new File(internalDataCache, "converted3D");
            if (!Helper.makeSureDirExist(converted3dMountPoint)) {
                throw new RuntimeException("make converted nxl 3D point failed" + converted3dMountPoint.getPath());
            }
            return converted3dMountPoint;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void cleanConvertedNxl3dCache(Context context) {
        // clean the cache of converted 3d nxl data(internal storage)
        File internalDataCacheMountPoint = getConvertedNxl3DMountPoint(context);
        if (internalDataCacheMountPoint != null && internalDataCacheMountPoint.isDirectory()) {
            String[] fileList = internalDataCacheMountPoint.list();
            for (String file : fileList) {
                if (!FileHelper.delFile(internalDataCacheMountPoint.getPath() + "/" + file)) {
                    log.d("delete converted nxl file failed.");
                }
            }
        }
    }

    // delete all converted 3d normal file when logout
    public static void cleanConverted3dCache(Context context) {
        // clean the cache of converted 3d normal data(external storage)
        File convertedMountPoint = getConvertedNormal3DMountPoint();
        if (convertedMountPoint != null && convertedMountPoint.isDirectory()) {
            String[] fileList = convertedMountPoint.list();
            for (String file : fileList) {
                if (!FileHelper.delFile(convertedMountPoint.getPath() + "/" + file)) {
                    log.d("delete converted normal file failed.");
                }
            }
        }

        cleanConvertedNxl3dCache(context);
    }

    // try to open file use the third party when don't support the file format for normal file.
    public static void tryOpenUseThirdParty(Context context, File file) {
        chooseThirdApp(context, file);
    }

    /**
     * get file Mime type when view not supported file by using third party
     * note: here we use the default mime "text/plain" if can't get the mime.
     *
     * @param url
     */
    public static String getMimeType(String url) {
        String defaultMime = "text/plain";
        try {
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(URLEncoder.encode(url, "UTF-8"));
            if (!TextUtils.isEmpty(extension)) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            }

            if (TextUtils.isEmpty(type)) {
                return defaultMime;
            }
            return type;
        } catch (UnsupportedEncodingException e) {
            return defaultMime;
        }
    }

    // choose third party app to open the not supported normal file by application chooser.
    private static void chooseThirdApp(Context context, File file) {
        String type = getMimeType(Uri.fromFile(file).toString());
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setDataAndType(Uri.fromFile(file), type);
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        if (resInfo != null && !resInfo.isEmpty()) {
            List<Intent> targetedViewIntents = new ArrayList<>();
            for (ResolveInfo info : resInfo) {
                Intent targeted = new Intent(Intent.ACTION_VIEW);
                targeted.setDataAndType(getUriFromFile(context, file), type);
                ActivityInfo activityInfo = info.activityInfo;
                // note: exclude our app to choose.
                if (activityInfo.packageName.contains(context.getPackageName())) {
                    continue;
                }
                targeted.setPackage(activityInfo.packageName);
                targetedViewIntents.add(targeted);
            }

            //Here will occur a exception when open unsupported files by 3rd app.
            //When we parse file path through intent using file:// will cause a exception named FileUriExposedException.
            //instead of using content://
            //This issue will happen when using our app is using in env >= android os 7.0
            try {
                if (targetedViewIntents.size() != 0) {
                    Intent chooserIntent = Intent.createChooser(targetedViewIntents.remove(0), context.getResources().getString(R.string.app_chooser));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedViewIntents.toArray(new Parcelable[]{}));
                    context.startActivity(chooserIntent);
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.no_app_choose), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Used to provide file uri generated by FileProvider which has been declared in AndroidManifest.xml
     * <provider>android.support.v4.content.FileProvider<provider/> see {@link FileProvider} section.
     *
     * @param context launcher
     * @param file    target file.
     * @return Uri of target file {@link Uri}.
     */
    private static Uri getUriFromFile(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    /**
     * judge the file needed to display source, i.e. from where jump into the viewActivity
     * including the third case:
     * - FROM_HOME: view the file when click the file items of home page.
     * FROM_MYVAULT: view the file when click the file items of myVault.
     * FROM_PROJECTS: view the nxl file when click the file items of one project.
     * - FROM_PROTECT_VIEW: view the protected file directly after protect normal file
     * - OPEN_AS_THIRD_PARTY: open file as third party, such as from attach etc.
     */
    public enum FileSource {
        FROM_HOME,
        FROM_PROTECT_VIEW,
        FROM_MYVAULT,
        FROM_PROJECTS,
        FROM_SHARED_WITH_ME,
        OPEN_AS_THIRD_PARTY,
        ERROR_SOURCE,
        FROM_OFFLINE_VIEW,
        FROM_WORKSPACE_VIEW
    }
}
