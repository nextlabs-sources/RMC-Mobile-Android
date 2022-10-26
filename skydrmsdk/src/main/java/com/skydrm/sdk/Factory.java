package com.skydrm.sdk;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.skydrm.sdk.rms.NxlClient;
import com.skydrm.sdk.rms.NxlTenant;
import com.skydrm.sdk.rms.user.RmUser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Factory {
    // as Activity request code
    public static final int REQUEST_LOGIN = 0X1001;
    public static final int REQUEST_REGISTER = 0X1002;

    public static final String LOGIN_STATUS = "LOGIN_STATUS";
    public static final String RIGHTS_MANAGEMENT_USER = "NEXTALBS_RMUSER";
    public static final String RIGHTS_MANAGEMENT_TENANT = "NEXTALBS_TENANT";
    public static final String RIGHTS_MANAGEMENT_ADDRESS = "NEXTLABS_RM_ADDRESS";
    public static final String RIGHTS_MANAGEMENT_REGISTER_INTENT = "NEXTLABS_REGISTER_ACCOUNT";
    // for tenant id, RMS required
    static public final String DEFAULT_TENANTID = "skydrm.com";
    static public final String TENANT_ID_TESTDRM = "testdrm.com";
    public static final String RM_SERVER_DEBUG = "https://rmtest.nextlabs.solutions";
    public static final String RM_SERVER_DEBUG_TESTDRM = "https://r.testdrm.com";
    public static final String RM_SERVER_RELEASE = "https://r.skydrm.com";
    public static final String RM_WWW_SKYDRM_SERVER_RELEASE = "https://www.skydrm.com";
    //used to maintain which suffix of 3d-files, app can render it
    private static final Set<String> sRMSSupportedCadFormats = new HashSet<>();
    public static boolean DEBUG = false;
    // control whether sky will output log
    public static boolean TURN_ON_LOG = true;
    public static String DEVICE_ID = null;
    public static String DEVICE_TYPE = null;
    public static String DEVICE_NAME = null;
    public static String CLIENT_ID = null;
    public static String RM_SERVER = null;
    public static String RM_TENANT_ID = null;
    private static ExecutorService DEFAULT_Executor = null;
    public static boolean ignoreSSLCert = false;

    static {
        // cad formats that RMS can be supported
        String[] fs = new String[]{
                ".prt",
                ".sldprt",
                ".sldasm",
                ".catpart",
                ".catshape",
                ".cgr",
                ".neu",
                ".par",
                ".psm",
                ".ipt",
                ".igs",
                ".stp",
                ".3dxml",
                ".vsd"
        };
        Collections.addAll(sRMSSupportedCadFormats, fs);
    }

    public static void config(boolean isDebug, boolean isTurnOnLog,
                              String deviceId, String deviceType, String deviceName, String clientId,
                              String rmServer,
                              String rmTenantID,
                              ExecutorService executorService) {
        DEBUG = isDebug;
        TURN_ON_LOG = isTurnOnLog;
        DEVICE_ID = deviceId;
        DEVICE_TYPE = deviceType;
        DEVICE_NAME = deviceName;
        CLIENT_ID = clientId;
        DEFAULT_Executor = executorService;
        RM_SERVER = rmServer;
        RM_TENANT_ID = rmTenantID;
    }

    public static void changeRMServer(String rmServer) {
        RM_SERVER = rmServer;
    }

    public static void ignoreSSLCertStatus(boolean ignore) {
        ignoreSSLCert = ignore;
    }

    public static Executor getDefaultExecutor() {
        if (DEFAULT_Executor == null) {
            return Executors.newFixedThreadPool(4);
        } else {
            return DEFAULT_Executor;
        }
    }

    public static String getDeviceId() {
        return DEVICE_ID;
    }

    public static String getDeviceName() {
        return DEVICE_NAME;
    }

    public static String getClientId() {
        return CLIENT_ID;
    }

    public static String getDeviceType() {
        return DEVICE_TYPE;
    }

    public static Set<String> getSupportedCadFormats() {
        return sRMSSupportedCadFormats;
    }

    public static String getSDKVersion() {
        return "1.0.0";
    }

    public static void startAuthenticating(Activity activity) {
        if (DEVICE_ID == null || DEVICE_TYPE == null) {
            throw new RuntimeException("device_id or device_type not configured");
        }
        startAuthenticating(activity, Factory.RM_TENANT_ID);
    }

    public static void registerNewAccount(Activity activity) {
        if (DEVICE_ID == null || DEVICE_TYPE == null) {
            throw new RuntimeException("device_id or device_type not configured");
        }
        Intent intent = new Intent(activity, com.skydrm.sdk.ui.LoginActivity.class);
        intent.putExtra(RIGHTS_MANAGEMENT_TENANT, Factory.RM_TENANT_ID);
        intent.putExtra(RIGHTS_MANAGEMENT_REGISTER_INTENT, RIGHTS_MANAGEMENT_REGISTER_INTENT);
        activity.startActivityForResult(intent, Factory.REQUEST_REGISTER);
    }

    public static void startAuthenticating(Activity activity, String tenantId) {
        Intent intent = new Intent(activity, com.skydrm.sdk.ui.LoginActivity.class);
        intent.putExtra(RIGHTS_MANAGEMENT_TENANT, tenantId);
        activity.startActivityForResult(intent, Factory.REQUEST_LOGIN);
    }

    public static boolean authenticationSuccessfully(Intent data) {
        if (data == null) {
            return false;
        }
        return data.getBooleanExtra(LOGIN_STATUS, false);
    }

    public static INxlClient finishAuthenticating(Intent data) throws Exception {
        // sanity check
        if (data == null) {
            return null;
        }
        String userStr = data.getStringExtra(RIGHTS_MANAGEMENT_USER);
        if (userStr == null) {
            return null;
        }
        String tenantStr = data.getStringExtra(RIGHTS_MANAGEMENT_TENANT);
        if (tenantStr == null) {
            return null;
        }
        String rmsStr = data.getStringExtra(RIGHTS_MANAGEMENT_ADDRESS);
        if (rmsStr == null) {
            return null;
        }
        return new NxlClient(RmUser.buildFromJson(userStr), new NxlTenant(tenantStr, rmsStr), new Config(rmsStr));
    }

    public static INxlClient recoverClient(@NonNull String rmUserJSON,
                                           @NonNull String tenantId,
                                           @NonNull String rmsAddress) throws Exception {
        if (DEVICE_ID == null || DEVICE_TYPE == null) {
            throw new RuntimeException("device_id or device_type not configured");
        }
        return new NxlClient(RmUser.buildFromJson(rmUserJSON), new NxlTenant(tenantId, rmsAddress), new Config(rmsAddress));
    }

    public static String getPersonalCountURL() {
        return RM_WWW_SKYDRM_SERVER_RELEASE;
    }
}
