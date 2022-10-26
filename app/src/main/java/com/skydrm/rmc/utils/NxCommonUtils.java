package com.skydrm.rmc.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.sdk.rms.user.membership.IMemberShip;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

public class NxCommonUtils {
    private static final String NXL_FILE_SUFFIX = ".nxl";

    /**
     * the devicde id must be a uuid
     * note: the uuid can't be too long,or else will get the 500 error code from server.
     */
    public static String getDeviceId() {
        StringBuilder deviceId = new StringBuilder();
        Context context = SkyDRMApp.getInstance().getApplicationContext();
        // label
        deviceId.append("a");
        try {
            //wifi mac address
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            String wifiMac = info.getMacAddress();
            if (!wifiMac.isEmpty()) {
                // can't get this when don't have wifi, also need the permission: android.permission.ACCESS_WIFI_STATE
                deviceId.append("wifi");
                deviceId.append(wifiMac);
                //Log.e("getDeviceId : ", deviceId.toString());
                return deviceId.toString();
            }
            //IMEI（imei）
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if (!imei.isEmpty()) {
                // need the permission: <uses-permission android:name="android.permission.READ_PHONE_STATE" />
                // and not friendly for user to supply this right
                deviceId.append("imei");
                deviceId.append(imei);
                //Log.e("getDeviceId : ", deviceId.toString());
                return deviceId.toString();
            }
            //serelize（sn）
            String sn = tm.getSimSerialNumber();
            if (!sn.isEmpty()) {
                deviceId.append("sn");
                deviceId.append(sn);
                //Log.e("getDeviceId : ", deviceId.toString());
                return deviceId.toString();
            }
            // get current millis if above not.
            String uuid = String.valueOf(System.currentTimeMillis());
            if (!uuid.isEmpty()) {
                deviceId.append("id");
                deviceId.append(uuid);
                //Log.e("getDeviceId : ", deviceId.toString());
                return deviceId.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            String uuid = UUID.randomUUID().toString();
            deviceId.append("id").append(uuid.substring(0, uuid.length()));
        }
        //Log.e("getDeviceId : ", deviceId.toString());
        return deviceId.toString();
    }

    /**
     * Get device name by access blueTooth name(this is the same with device name),
     * Will use device model number as the device name if can't get it.
     */
    @SuppressWarnings("static-access")
    public static String getDeviceName() {
        try {
            if (!TextUtils.isEmpty(BluetoothAdapter.getDefaultAdapter().getName())) {
                return BluetoothAdapter.getDefaultAdapter().getName();
            } else {
                return new Build().MODEL; // device model number
            }
        } catch (Exception e) {
            //ignored
        }
        // BluetoothAdapter.getDefaultAdapter() may null, so return default
        return new Build().MODEL; // device model number

    }

    /**
     * In order identify each RMC client installation, a 32 bytes UUID should be assigned to each client.
     * This ID will be used for analytics purpose. Each request should include this header.
     */
    public static String getClientId() {
        return hexifyMD5(getUniquePseudoID().getBytes(), true);
    }


    /**
     * Can get the unique id by the field "Build.SERIAL" when API>=9;
     * for API<9, we can composite it by hardware info
     */
    private static String getUniquePseudoID() {

        String serial = null;
        String devIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13 bit

        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            //API>=9
            return new UUID(devIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            // need an initialize
            serial = "serial";
        }
        // API<9
        return new UUID(devIDShort.hashCode(), serial.hashCode()).toString();
    }

    /**
     * judge current device is phone or tablet
     *
     * @return true is tablet,else is phone
     */
    private static boolean isTablet() {
        Context context = SkyDRMApp.getInstance().getApplicationContext();
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * android phone range is 800 - 899, android tablet range is 900 - 999
     * also: platformId
     */
    public static String getDeviceType() {
        long nStart = 0;
        if (isTablet()) {
            nStart = 900;
        } else {
            nStart = 800;
        }

        long nEnd = 0;
        String systemVersion = android.os.Build.VERSION.RELEASE;
        if (systemVersion.startsWith("7")) {
            nEnd = 7;
        } else if (systemVersion.startsWith("6")) {
            nEnd = 6;
        } else if (systemVersion.startsWith("5")) {
            nEnd = 5;
        } else if (systemVersion.startsWith("4")) {
            nEnd = 4;
        } else if (systemVersion.startsWith("3")) {
            nEnd = 3;
        }

        return String.valueOf(nStart + nEnd);
    }

    /**
     * this function used to pad checksum when doing sharing rest api request
     *
     * @param key: is the encryption token.
     * @param msg: is a json text for shareDocument
     */
    public static String hMacSha256(String key, String msg) throws Exception {
        byte[] result = CryptoHelper.hmacSha256(key.getBytes(), msg.getBytes());
        return CryptoHelper.bytesToHexString(result);
    }

    /**
     * used to judge current user if is the steward of the com.skydrm.rmc.nxl file.
     */
    public static boolean isSteward(String ownerId) {
        boolean bIsSteward = false;
        try {
            List<IMemberShip> memberships = SkyDRMApp.getInstance().getSession().getRmUser().getMemberships();
            for (IMemberShip m : memberships) {
                if (m.getId() != null && m.getId().equals(ownerId)) {
                    bIsSteward = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bIsSteward;
    }

    /**
     * the com.skydrm.rmc.nxl file is renamed according to original file name-date-time-postfix.com.skydrm.rmc.nxl after protect.
     */
    public static String reNameNxlFile(String originalFilePath) {
        //obtain current time
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String date = sDateFormat.format(new java.util.Date());
        date = date.replace(" ", "-");
        date = date.replace(":", "-");

        String oldFileName = originalFilePath.substring(originalFilePath.lastIndexOf('/') + 1);
        String fileParent = originalFilePath.substring(0, originalFilePath.lastIndexOf('/'));
        String newFileName = "";
        if (oldFileName.contains(".")) {
            String name = oldFileName.substring(0, oldFileName.lastIndexOf("."));
            String postfix = oldFileName.substring(oldFileName.lastIndexOf(".") + 1);
            newFileName = name + "-" + date + "." + postfix + NXL_FILE_SUFFIX;
        } else {
            newFileName = oldFileName + "-" + date + NXL_FILE_SUFFIX;
        }

        return fileParent + "/" + newFileName;
    }

    /**
     * this function used to do gzip compress
     */
    public static byte[] gzipCompress(String strData) {
        if (strData == null || strData.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(strData.getBytes("UTF-8"));
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * set textView width dynamically to adapter different machine size.
     */
    public static void setTextViewWidth(Activity activity, TextView textView) {
        DisplayMetrics screenMetrics = getScreenMetrics(activity);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) textView.getLayoutParams();
        lp.width = screenMetrics.widthPixels * 3 / 5;
        textView.setLayoutParams(lp);
    }

    public static DisplayMetrics getScreenMetrics(Activity activity) {
        // get screen width
        WindowManager windowManager = (WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics;
    }

    /**
     * read the text content from asset
     *
     * @param fileName text file name that need to read
     * @param context  context of a activity.
     */
    public static String getContentByAsset(String fileName, Context context) {
        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open(fileName);
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder("");
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * used to get parent folder path by current working path
     */
    public static String getParentFolderPath(String pathId) {
        if (TextUtils.isEmpty(pathId)) {
            return null;
        }
        if (pathId.equals("/")) {
            return "/";
        }

//        if (pathId.endsWith("/")) { // remove the end "/" if ends with "/"
//            pathId = pathId.substring(0, pathId.lastIndexOf("/"));
//        }
//
//        if (pathId.contains("/") && pathId.length() >= pathId.lastIndexOf("/") + 1) {
//            pathId = pathId.substring(0, pathId.lastIndexOf("/") + 1);
//        }

        if (pathId.endsWith("/")) {
            int FirstLastIndexOf = pathId.lastIndexOf("/");
            String FirstNewPathId = pathId.substring(0, FirstLastIndexOf);
            int secondLastIndexOf = FirstNewPathId.lastIndexOf("/");
            String newPathId = FirstNewPathId.substring(0, secondLastIndexOf);
            return newPathId + "/";
        } else {
            int FirstLastIndexOf = pathId.lastIndexOf("/");
            String FirstNewPathId = pathId.substring(0, FirstLastIndexOf);
            return FirstNewPathId + "/";
        }
    }

    private static byte[] md5(byte[] data) {
        // sanity check
        if (data == null) {
            throw new RuntimeException("invalid data");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String hexifyMD5(byte[] data, boolean uppercase) {

        byte[] checksum = md5(data);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < checksum.length; i++) {
            int val = ((int) checksum[i]) & 0xff;
            if (val < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }

        return uppercase ? sb.toString().toUpperCase() : sb.toString().toLowerCase();
    }
}
