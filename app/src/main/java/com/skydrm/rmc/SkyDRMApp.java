package com.skydrm.rmc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.skydrm.rmc.database.Database;
import com.skydrm.rmc.database.RepoEntry;
import com.skydrm.rmc.datalayer.HeartBeatManager;
import com.skydrm.rmc.datalayer.heartbeat.CommonPolicy;
import com.skydrm.rmc.datalayer.heartbeat.HeartbeatPolicyGenerator;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatPolicy;
import com.skydrm.rmc.dbbridge.DBProvider;
import com.skydrm.rmc.dbbridge.IServer;
import com.skydrm.rmc.dbbridge.IUser;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.exceptions.AppCorruptedException;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.filemark.FavoriteMarkImpl;
import com.skydrm.rmc.reposystem.IRepoSystem;
import com.skydrm.rmc.reposystem.RepoSystem;
import com.skydrm.rmc.reposystem.RunningMode;
import com.skydrm.rmc.reposystem.UserReposSyncPolicy;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.RmsRepoInfo;
import com.skydrm.rmc.ui.activity.splash.SplashActivity;
import com.skydrm.rmc.ui.common.ActivityManager;
import com.skydrm.rmc.ui.service.offline.db.OfflineLog;
import com.skydrm.rmc.ui.service.offline.downloader.DownloadManager;
import com.skydrm.rmc.utils.AppVersionHelper;
import com.skydrm.rmc.utils.NetworkStatus;
import com.skydrm.rmc.utils.NxCommonUtils;
import com.skydrm.rmc.utils.SendLogHelper2;
import com.skydrm.rmc.utils.SharePreferUtils;
import com.skydrm.rmc.utils.commonUtils.AvatarUtil;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;
import com.skydrm.sdk.Factory;
import com.skydrm.sdk.INxlClient;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.policy.Watermark;
import com.skydrm.sdk.rms.NxlClient;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.types.HeartbeatResponse;
import com.skydrm.sdk.rms.types.SendLogRequestValue;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.RmUser;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;
import com.skydrm.sdk.utils.ParseJsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;
import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;
import static com.skydrm.rmc.ExecutorPools.Select_Type.REGULAR_BACK_GROUND;

/**
 * This class is designed to manage all lifecycle of app and hold Global instances
 * -            holds repository system
 * -            holds database
 * -            holds session for current login user
 * -               - use SharedPreference to store and recover significant fields of current login user
 * -            get rights and labels for current login user
 */
public class SkyDRMApp extends Application {
    static private final String FIRST_SIGN_FLAG = "firstSignFlagCache";
    //used to check whether the page from view page.
    static public boolean isFromViewPage = false;
    static public DevLog log = new DevLog(SkyDRMApp.class.getSimpleName());
    static private SkyDRMApp singleton;
    private NetworkStatus networkStatus;
    private IRepoSystem repoSystem;
    private Session2 session2 = new Session2();
    private DataBaseWithCache db;
    private CommonDir commonDir;
    private Handler mGlobalUIHandler;
    private Config config;
    private DBProvider mDBProvider;

    static public SkyDRMApp getInstance() {
        return singleton;
    }

    public boolean isDebug() {
        return config.isDebug();
    }

    public boolean isNetworkAvailable() {
        return networkStatus.isNetworkAvailable();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        // for debug : .setAppDebugVer()
        // for release: .setAppReleaseVer()
        config = new Config.Builder()
                .setDebug(false)
                .setTurnOnLog(true)
                .setOnPremise(isOnPremise())
//              .setAppReleaseVer()
//              .setAppReleaseStagingVer()
//              .setAppDebugVer()
//              .setCentralOsDebugVer()
                .build();
        try {
            appInitialize();
        } catch (AppCorruptedException e) {
            log.e(e);
            // can not allow app to be returned
        }
    }

    /**
     * Initialize dependent components. database,repo-system,rms-api
     *
     * @throws AppCorruptedException
     */
    public void appInitialize() throws AppCorruptedException {
        try {
            log.v(config.toString());
            // - system level component init
            // config log

            // - init Global UI handler
            if (mGlobalUIHandler == null) {
                mGlobalUIHandler = new Handler();
            }
            //dataBase2 = new Database(this);
            db = new DataBaseWithCache();
            networkStatus = new NetworkStatus(this);
            commonDir = new CommonDir();

            // make sure SD card exists
            // make sure folder exists again
            if (!commonDir.repoSystemRootFile().exists()) {
                log.e("can not create Folder on SD card");
                throw new AppCorruptedException("can not create Folder on SD card");
            }

            repoSystem = new RepoSystem();
            //init data base
            try {
                initDataBase();
            } catch (Exception e) {
                e.printStackTrace();
            }

            DownloadManager.getInstance().init(this, null);
            // config SkyDRM sdk
            Factory.config(config.isDebug(),
                    config.isTurnOnLog(),
                    config.getDeviceID(),
                    config.getDeviceType(),
                    config.getDeviceName(),
                    config.getClientId(),
                    config.getRmsServer(),
                    config.getRmsTenantID(),
                    ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND));
            Factory.ignoreSSLCertStatus(isIgnoreSSLCert());

            // recover session
            recoverySession(new SessionRecoverListener() {
                @Override
                public void onSuccess() {
                    log.v("recover session success");
                }

                @Override
                public void onAlreadyExist() {
                    log.v("recover session success, session already exist");
                }

                @Override
                public void onFailed(String reason) {
                    log.v("recover session failed");
                }

                @Override
                public void onProcess(String hint) {
                    log.v("recover session:" + hint);
                }
            });

        } catch (AppCorruptedException e) {
            throw e;
        } catch (Exception e) {
            log.e(e.getMessage(), e);
            throw new AppCorruptedException("failed to appInitialize important components");
        }
    }

    private void initDataBase() {
        mDBProvider = new DBProvider(this);
        mDBProvider.initDB();
    }

    public DBProvider getDBProvider() {
        return mDBProvider;
    }

    /**
     * In SaaS situation set ignoreSSLCert = false, in PaaS situation will set ignoreSSLCert = true
     */
    private boolean isIgnoreSSLCert() {
        return (boolean) SharePreferUtils.getParams(getApplicationContext(),
                Constant.IGNORE_SSL_CERT, false);
    }

    public boolean isOnPremise() {
        return (boolean) SharePreferUtils.getParams(getApplicationContext(),
                Constant.ON_PREMISE, false);
    }

    public Session2 getSession() {
        return session2;
    }

    public void recoverySession(@NonNull SessionRecoverListener listener) {
        /*
        On failed ,clear seeson
         */
        listener.onProcess("check session validation");
        if (session2.isValid()) {
            listener.onProcess("session has existed");
            listener.onAlreadyExist();
            return;
        }

        listener.onProcess("session recovering...");
        if (!session2.recoverSession(listener)) {
            session2.clearSession();
            return;
        }

        // session recovery OK!
        session2.configBuildInRepoMyDrive();
        // close exist
        try {
            // make sure Mount Point has good
            String relativePath = session2.getUserTenantName() + "/" + session2.getUserEmail();
            repoSystem.create(commonDir.repoSystemRootFile(), relativePath);//require session has initialized
            repoSystem.attach(getUserLinkedRepos());
            repoSystem.activate();
        } catch (Exception e) {
            // todo: by Osmond  fatal error, kill process
            e.printStackTrace();
        }
        // repo sync, may be ServerImpl has modified the repos
        UserReposSyncPolicy.syncing(ExecutorPools.SelectSmartly(FIRED_BY_UI), new UserReposSyncPolicy.UIListener() {
            @Override
            public void progressing(@NonNull String msg) {
            }

            @Override
            public void result(boolean status) {
            }
        });

        listener.onProcess("session check ttl");
        // session ttl test
        boolean sessionExpired = session2.isExpired();

        if (!sessionExpired) {
            HeartbeatPolicyGenerator.configureOne(HeartbeatPolicyGenerator.TYPE_COMMON, new CommonPolicy(IHeartBeatPolicy.TYPE_USER_RECOVER, 120));
            session2.startHeartBeatTask();
            listener.onProcess("session recovered");
            listener.onSuccess();
        } else {
            // session exists,but expired
            session2.clearSession();
            listener.onFailed("session has recovered, but expired");
        }
    }

    public void newSession(INxlClient rmClient) {
        session2.newSession(rmClient);
    }

    public void logout(Activity activity) {
        try {
            // give FavSync last change to send sync to RMS
            ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        FavoriteMarkImpl.getInstance().syncMarkedFileToRms();
                    } catch (RmsRestAPIException e) {
                        log.e(e);
                    }
                }
            });
            // release repoSystem
            repoSystem.close();
            // delete the tmp converted 3d file
            RenderHelper.cleanConverted3dCache(activity);
            //clear avatar cache
            AvatarUtil.getInstance().clearAvatarCache();
            // release this session's res
            session2.closeSession();
            // restart app (with flag_clear_top)
            Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            // close all activity before launching Splash activity
            ActivityManager.getDefault().clearAllActivity();
        } catch (Exception e) {
            log.e(e);
        }
    }

    public void redirectToLoginPageAfterChangeServer(Context context) {
        try {
            // give FavSync last change to send sync to RMS
            ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        FavoriteMarkImpl.getInstance().syncMarkedFileToRms();
                    } catch (RmsRestAPIException e) {
                        log.e(e);
                    }
                }
            });
            // release repoSystem
            repoSystem.close();
            // delete the tmp converted 3d file
            RenderHelper.cleanConverted3dCache(context);
            //clear avatar cache
            AvatarUtil.getInstance().clearAvatarCache();
            // release this session's res
            session2.closeSession();
            //redirect to login page
            // restart app (with flag_clear_top)
            Intent intent = new Intent(context, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.COMMAND_REDIRECT, true);
            startActivity(intent);
            // close all activity before launching Splash activity
            ActivityManager.getDefault().clearAllActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Handler getGlobalUIHandler() {
        return mGlobalUIHandler;
    }

    public ICommonDirectories getCommonDirs() {
        return commonDir;
    }

    public IRepoSystem getRepoSystem() {
        return repoSystem;
    }

    private boolean isVersionChange() {
        int currentVersionCode = AppVersionHelper.getVersionCode(getApplicationContext());
        SharedPreferences sp = getSharedPreferences(FIRST_SIGN_FLAG, MODE_PRIVATE);
        int oldVersionCode = sp.getInt("VersionCode", 0);
        return currentVersionCode != oldVersionCode;
    }

    public void changeRunningMode(RunningMode newMode) {
        switch (newMode) {
            case Normal: {
                int size = repoSystem.getSizeOfLivingRepo();
                if (size > 1 || size == 0) {
                    repoSystem.changeState(RepoSystem.RunningMode.SYNTHETIC);
                } else {
                    repoSystem.changeState(RepoSystem.RunningMode.FOCUSED);
                }
                break;
            }
            case Favorite: {
                repoSystem.changeState(RepoSystem.RunningMode.FAVORITE);
                break;
            }
            case Offline: {
                repoSystem.changeState(RepoSystem.RunningMode.OFFLINE);
                break;
            }
        }
    }

    public void onUpdateFileMark(List<ParseJsonUtils.AllRepoFavoListBean> list) {
        // notify MyDrive repo
        try {
            repoSystem.updateFilesMark3(list);
        } catch (Exception e) {
            //ignore
        }
        //EventBus.getDefault().post(new FavoriteSetEvent());
    }

    //
    //  section database operate
    //
    public List<BoundService> getUserLinkedRepos() {
        // sanity check
        if (session2.getRmsClient() == null) {
            log.e("rmsClient is null");
            throw new RuntimeException("rmsClient is null");
        }
        if (session2.getRmsClient().getUser() == null) {
            log.e("rmuser in rmsClient is null");
            throw new RuntimeException("rmuser in rmsClient is null");
        }
        if (session2.getRmsClient().getTenant() == null) {
            log.e("tenant in rmsClient is null");
            throw new RuntimeException("tenant in rmsClient is null");
        }

        String tenant = session2.getRmsClient().getTenant().getTenantId();
        int userId = session2.getRmsClient().getUser().getUserId();
        List<BoundService> rt = db.queryFromCacheBy(tenant, userId);
        return rt;
    }

    public void dbAddRepo(BoundService.ServiceType type, String alias, String account, String accountId, String accountToken, int selected) {
        // sanity check
        String tenant = session2.getRmsClient().getTenant().getTenantId();
        int userId = session2.getRmsClient().getUser().getUserId();

        db.addRepo(tenant, userId, type, alias, account, accountId, accountToken, selected);
    }

    public void dbInsertRepo(BoundService s) {
        String tenant = session2.getRmsClient().getTenant().getTenantId();
        int userId = session2.getRmsClient().getUser().getUserId();

        db.insertRepo(tenant, userId, s);

    }

    public BoundService dbFindRepo(BoundService.ServiceType type, String account, String accountId) {
        String tenant = session2.getRmsClient().getTenant().getTenantId();
        int userId = session2.getRmsClient().getUser().getUserId();

        return db.queryService(tenant, userId, type.value(), account, accountId);
    }

    public void dbDelRepo(BoundService boundService) {
        String tenant = session2.getRmsClient().getTenant().getTenantId();
        int userId = session2.getRmsClient().getUser().getUserId();

        db.delService(tenant, userId, boundService);
    }

    public void dbUpdateRepoToken(BoundService service) {
        String tenant = session2.getRmsClient().getTenant().getTenantId();
        int userId = session2.getRmsClient().getUser().getUserId();
        db.updateRepoToken(tenant, userId, service);

    }

    public void dbUpdateRepoSelected(BoundService service) {
        String tenant = session2.getRmsClient().getTenant().getTenantId();
        int userId = session2.getRmsClient().getUser().getUserId();
        db.updateRepoSelected(tenant, userId, service);
    }

    public void dbUpdateRepoNickName(BoundService service) {
        String tenant = session2.getRmsClient().getTenant().getTenantId();
        int userId = session2.getRmsClient().getUser().getUserId();
        db.updateRepoNickName(tenant, userId, service);
    }

    public void dbUpdateRepoRmsId(BoundService service) {
        String tenant = session2.getRmsClient().getTenant().getTenantId();
        int userId = session2.getRmsClient().getUser().getUserId();
        db.updateRepoRmsID(tenant, userId, service);
    }

    public boolean dbExistRepoOfCurrentUser(BoundService.ServiceType type, String account) {
        String tenant = session2.getRmsClient().getTenant().getTenantId();
        int userId = session2.getRmsClient().getUser().getUserId();
        return db.exist(tenant, userId, type.value(), account);
    }

    public Set<String> getSupportedCadFormats() {
        return Factory.getSupportedCadFormats();
    }

    /**
     * Judge current user whether is the nxl file's owner
     * Note: only apply to the nxl file not in project, to compare by membership id.
     *
     * @param nxlFileOwnerID the ownerId that read from nxl file header.
     */
    public boolean isStewardOf(@NonNull String nxlFileOwnerID) {
        try {
            return nxlFileOwnerID.equals(session2.getRmUser().getMembershipId());
        } catch (InvalidRMClientException e) {
            throw new RuntimeException("Invalid RMClient Exception");
        }
    }

    /**
     * Judge current user whether is the nxl file's owner
     * Note: only apply to the nxl file in project, to compare by membership id.
     *
     * @param nxlFileOwnerID the ownerId that read from nxl file header.
     * @param projectId      current project user entered,need use this to find membershipId exists in memberships lists
     * @return
     */
    @Deprecated
    public boolean isStewardOf(@NonNull String nxlFileOwnerID, int projectId) {
        try {
            String memberShipId = "";
            List<IMemberShip> memberships = session2.getRmUser().getMemberships();
            for (IMemberShip membership : memberships) {
                if (membership instanceof ProjectMemberShip) {
                    ProjectMemberShip pms = (ProjectMemberShip) membership;
                    if (pms.getProjectId() == projectId) {
                        memberShipId = membership.getId();
                        break;
                    }
                }
            }
            return TextUtils.equals(nxlFileOwnerID, memberShipId);
        } catch (InvalidRMClientException e) {
            throw new RuntimeException("Invalid RMClient Exception");
        }
    }

    /**
     * Judge current user whether is the nxl file's owner
     * Note: only apply to the project nxl file, to compare by userId
     *
     * @param userId the userId of "owner" field of one project.
     */
    public boolean isStewardOf(int userId) {
        try {
            return session2.getRmUser().getUserId() == userId;
        } catch (InvalidRMClientException e) {
            throw new RuntimeException("Invalid RMClient Exception");
        }
    }

    public interface SessionRecoverListener {
        void onSuccess();

        void onAlreadyExist();

        void onFailed(String reason);

        void onProcess(String hint);
    }

    public interface ClearCacheListener {
        void finished();
    }

    public interface ICommonDirectories {
        //  /sdcard/Android/data/com.skydrm.rmc/
        String appRoot();

        File appRootFile();

        //  /sdcard/Android/data/com.skydrm.rmc/repo-system/
        String repoSystemRoot();

        File repoSystemRootFile();

        //  for current login used
        String userRoot() throws SessionInvalidException;

        File userRootFile() throws SessionInvalidException;
    }

    public interface ISession {
        /* Sync time means when did this session has synchronized with RMS*/
        long getSyncTime();


        /**
         * {@param millis} will be saved immediately
         *
         * @param millis new sync time
         */
        void setSyncTime(long millis);

        String getSyncTimeFriendly();


        /**
         * @return true: use has login and not expired, else return false
         */
        boolean isValid();

        boolean isExpired();


        /**
         * @return RMS defined time out / expireation point, use Unix Epoch Time
         * <p>
         * 0 : 00:00:00 Coordinated Universal Time (UTC), Thursday, 1 January 1970
         */
        long getExpiredTimeMills();

        /**
         * @return Intervals between (Expired - Current) with formatted Days:Hours:Minutes:Seconds:Millis
         */
        String getExpiredTimeFriendly();


        /**
         * new feature that support to change user name,
         * if newName is same with previous, return directly
         *
         * @param newName
         */
        void updateRmUserName(String newName);

        RemoteViewerOfficePdfCache getRemoteViwerCache();

        IUserPreference getUserPreference();

        void savePreference(String preference);

        void savePreference(User.IExpiry expiry, String watermark);
    }

    public interface IUserPreference {
        String getWatermarkValue();

        User.IExpiry getExpiry();
    }

    private static class UserPreference implements IUserPreference {
        private String watermarkValue = "$(User)$(Break)$(Date)$(Time)";
        private User.IExpiry expiry = new User.IExpiry() {
            @Override
            public int getOption() {
                return 0;
            }
        };

        public void saveWatermark(String watermark) {
            this.watermarkValue = watermark;
        }

        public void saveIExpiry(User.IExpiry expiry) {
            this.expiry = expiry;
        }

        @Override
        public String getWatermarkValue() {
            return watermarkValue;
        }

        @Override
        public User.IExpiry getExpiry() {
            return expiry;
        }
    }

    static public class RemoteViewerOfficePdfCache {

        static private long TIME_OUT_MILLIS = 20 * 60 * 1000; // MAX HOLD TIME, 20 MINS

        private Map<String, Value> cache;


        public RemoteViewerOfficePdfCache() {
            cache = new HashMap<>();
        }

        public void addCache(String filePath, Value value) {
            cache.put(filePath, value);
        }

        public
        @Nullable
        Value getCache(String filePath) {
            Value rt = cache.get(filePath);
            if (rt == null) {
                return null;
            }
            // check if time-out
            if (System.currentTimeMillis() - rt.createdMillis >= TIME_OUT_MILLIS) {
                cache.remove(filePath);
                return null;
            } else {
                return rt;
            }
        }

        static public class Value {
            private String viewerURL;
            private List<String> cookies;
            private boolean isOwner;
            private int permissions;
            private long createdMillis;

            public Value(String viewerURL, List<String> cookies, int permissions, boolean isOwner) {
                this.viewerURL = viewerURL;
                this.cookies = cookies;
                this.permissions = permissions;
                this.isOwner = isOwner;
                // record time
                createdMillis = System.currentTimeMillis();
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

            public boolean isOwner() {
                return isOwner;
            }

            public void setOwner(boolean owner) {
                this.isOwner = owner;
            }

            public int getPermissions() {
                return permissions;
            }

            public void setPermissions(int permissions) {
                this.permissions = permissions;
            }
        }
    }

    /**
     * ues this to represent all that need to control app's behaviors
     */
    static class Config {
        final boolean isDebug;
        final boolean isTurnOnLog;
        final String deviceID;
        final String deviceType;
        final String deviceName;
        final String clientId;
        final String rmsServer;
        final String rmsTenantID;
        final boolean onPremise;

        private Config(Builder builder) {
            isDebug = builder.isDebug;
            isTurnOnLog = builder.isTurnOnLog;
            deviceID = builder.deviceID;
            deviceName = builder.deviceName;
            deviceType = builder.deviceType;
            clientId = builder.clientId;
            rmsServer = builder.rmsServer;
            rmsTenantID = builder.rmsTenantID;
            onPremise = builder.onPremise;
        }

        public boolean isDebug() {
            return isDebug;
        }

        public boolean isTurnOnLog() {
            return isTurnOnLog;
        }

        public String getDeviceID() {
            return deviceID;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getClientId() {
            return clientId;
        }

        public String getRmsServer() {
            return rmsServer;
        }

        public String getRmsTenantID() {
            return rmsTenantID;
        }

        public boolean isOnPremise() {
            return onPremise;
        }

        @Override
        public String toString() {
            String str = "device:\n" +
                    "{\n " +
                    "device-id:" + deviceID + "\n " +
                    "device-type: " + deviceType + "\n " +
                    "device-name: " + deviceName + "\n " +
                    "client-id: " + clientId + "\n " +
                    "}";
            return str;
        }

        public static class Builder {
            boolean isDebug;
            boolean isTurnOnLog;
            private String deviceID;
            private String deviceType;
            private String deviceName;
            private String clientId;
            private String rmsServer;
            private String rmsTenantID;
            private boolean onPremise;

            public Builder() {
                isDebug = false;
                isTurnOnLog = false;
                deviceID = NxCommonUtils.getDeviceId();
                deviceName = NxCommonUtils.getDeviceName();
                deviceType = NxCommonUtils.getDeviceType();
                clientId = NxCommonUtils.getClientId();
                // by default with official
                rmsServer = Factory.RM_SERVER_RELEASE;
                rmsTenantID = Factory.DEFAULT_TENANTID;
            }

            public Builder setDebug(boolean flag) {
                this.isDebug = flag;
                return this;
            }

            public Builder setTurnOnLog(boolean flag) {
                this.isTurnOnLog = flag;
                DevLog.setTurnOn(flag);
                return this;
            }

            public Builder setAppReleaseVer() {
                this.rmsServer = Factory.RM_SERVER_RELEASE;
                this.rmsTenantID = Factory.DEFAULT_TENANTID;
                return this;
            }

            public Builder setAppReleaseStagingVer() {
                this.rmsServer = Factory.RM_SERVER_DEBUG_TESTDRM;
                this.rmsTenantID = Factory.TENANT_ID_TESTDRM;
                return this;
            }

            public Builder setAppDebugVer() {
                this.rmsServer = Factory.RM_SERVER_DEBUG;
                this.rmsTenantID = Factory.DEFAULT_TENANTID;
                return this;
            }

            public Builder setOnPremise(boolean onPremise) {
                this.onPremise = onPremise;
                return this;
            }

            public Config build() {
                return new Config(this);
            }
        }
    }

    private class CommonDir implements ICommonDirectories {
        @Override
        public String appRoot() {
            return appRootFile().getAbsolutePath();
        }

        @Override
        public File appRootFile() {
            return getExternalFilesDir(null);
        }

        @Override
        public String repoSystemRoot() {
            return repoSystemRootFile().getAbsolutePath();
        }

        @Override
        public File repoSystemRootFile() {
            File root = new File(commonDir.appRootFile(), "repo-system");
            if (!root.exists()) {
                root.mkdirs();
            }
            return root;

        }

        @Override
        public String userRoot() throws SessionInvalidException {
            return userRootFile().getAbsolutePath();
        }

        @Override
        public File userRootFile() throws SessionInvalidException {
            if (!session2.isValid()) {
                throw new SessionInvalidException("invalid session");
            }
            String tenant = session2.getRmsClient().getTenant().getTenantId();
            String email = session2.getRmsClient().getUser().getEmail();
            File sessionRoot = new File(appRootFile(), "sessions" + "/" + tenant + "/" + email);
            if (!sessionRoot.exists()) {
                sessionRoot.mkdirs();
            }
            return sessionRoot;
        }
    }

    // new session.
    public class Session2 implements ISession {
        static private final String SESSION_CACHE = "sessionCache";
        static private final String APP_NAME = "RMC Android";
        static private final String APP_PATH = "RMC Android";
        static private final String APP_PUBLISHER = "Nextlabs";

        private long lastSyncTimeWithRMS_Mills = 0;

        private INxlClient rmsClient;

        // this background thread is used to periodically communicate with RMS to get heartbeat response.
        private Thread heartbeatTask;

        private Watermark watermark = Watermark.buildDefault();
        private RemoteViewerOfficePdfCache remoteViewerOfficePdfCache;

        /*------ Begin: used to clean the converted 3d nxl file periodically --------*/
        private Handler mcleanConverted3dHandler = new Handler();
        private Runnable mcleanConverted3dRunnable = new Runnable() {
            @Override
            public void run() {
                // clean up every day
                RenderHelper.cleanConvertedNxl3dCache(getApplicationContext());
                mcleanConverted3dHandler.postDelayed(this, 24 * 3600 * 1000); // one day
            }
        };

        private IUserPreference userPreference;

        @Override
        public long getSyncTime() {
            return lastSyncTimeWithRMS_Mills;
        }

        @Override
        public void setSyncTime(long millis) {
            boolean needUpdate = (lastSyncTimeWithRMS_Mills != millis);
            lastSyncTimeWithRMS_Mills = millis;
            if (needUpdate) {
                // save new value
                try {
                    SharedPreferences sp = getSharedPreferences(SESSION_CACHE, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    {
                        editor.putLong("sync_time_with_RMS_mills", lastSyncTimeWithRMS_Mills);
                    }
                    editor.apply();
                } catch (Exception e) {
                    log.e(e);
                }
            }
        }

        @Override
        public String getSyncTimeFriendly() {
            if (lastSyncTimeWithRMS_Mills == 0) {
                return "Never";
            }
            return TimeUtil.getProtectTime(lastSyncTimeWithRMS_Mills, System.currentTimeMillis());
        }

        @Override
        public boolean isValid() {
            return rmsClient != null && !isExpired();
        }

        @Override
        public boolean isExpired() {
            try {
                return (rmsClient == null || rmsClient.isSessionExpired());
            } catch (Exception ignored) {
                log.e(ignored);
            }
            return true;
        }

        @Override
        public long getExpiredTimeMills() {
            return rmsClient.getUser().getTtl();
        }

        @Override
        public String getExpiredTimeFriendly() {
            return CommonUtils.format(getExpiredTimeMills() - System.currentTimeMillis());
        }

        /**
         * for client to modify its login user name at local
         *
         * @param newName
         */
        @Override
        public void updateRmUserName(String newName) {
            try {
                // sanity check
                if (newName == null || newName.isEmpty()) {
                    return;
                }
                if (TextUtils.equals(getRmUser().getName(), newName)) {
                    return;
                }
                // set new value
                getRmUser().setName(newName);
                // save session immediately
                SharedPreferences sp = getSharedPreferences(SESSION_CACHE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                {
                    editor.putString("raw_rm_user_json", rmsClient.getUser().toString());
                }
                editor.apply();
            } catch (Exception e) {
                log.e(e);
            }
        }

        @Override
        public RemoteViewerOfficePdfCache getRemoteViwerCache() {
            return remoteViewerOfficePdfCache == null ? remoteViewerOfficePdfCache = new RemoteViewerOfficePdfCache() : remoteViewerOfficePdfCache;
        }

        @Override
        public IUserPreference getUserPreference() {
            return userPreference != null ? userPreference : new UserPreference();
        }

        @Override
        public void savePreference(String preference) {
            UserPreference userPreference = new UserPreference();
            if (!TextUtils.isEmpty(preference)) {
                try {
                    JSONObject jsonObject = new JSONObject(preference);
                    if (jsonObject.has("results")) {
                        JSONObject results = jsonObject.getJSONObject("results");
                        if (results.has("watermark")) {
                            userPreference.saveWatermark(results.getString("watermark"));
                        }
                        if (results.has("expiry")) {
                            User.IExpiry iExpiry = null;
                            JSONObject expiry = results.getJSONObject("expiry");
                            if (expiry.has("option")) {
                                int option = expiry.getInt("option");
                                switch (option) {
                                    case 0:
                                        iExpiry = new User.IExpiry() {
                                            @Override
                                            public int getOption() {
                                                return 0;
                                            }
                                        };
                                        break;
                                    case 1:
                                        final JSONObject relativeDay = expiry.getJSONObject("relativeDay");
                                        final int mYears = relativeDay.optInt("year");
                                        final int mMonths = relativeDay.optInt("month");
                                        final int mWeeks = relativeDay.optInt("week");
                                        final int mDays = relativeDay.optInt("day");
                                        iExpiry = new User.IRelative() {
                                            @Override
                                            public int getYear() {
                                                return mYears;
                                            }

                                            @Override
                                            public int getMonth() {
                                                return mMonths;
                                            }

                                            @Override
                                            public int getWeek() {
                                                return mWeeks;
                                            }

                                            @Override
                                            public int getDay() {
                                                return mDays;
                                            }

                                            @Override
                                            public int getOption() {
                                                return 1;
                                            }
                                        };
                                        break;
                                    case 2:
                                        final long absoluteEndDate = expiry.getLong("endDate");
                                        iExpiry = new User.IAbsolute() {
                                            @Override
                                            public long endDate() {
                                                return absoluteEndDate;
                                            }

                                            @Override
                                            public int getOption() {
                                                return 2;
                                            }
                                        };
                                        break;
                                    case 3:
                                        final long rangeStartDate = expiry.getLong("startDate");
                                        final long rangeEndDate = expiry.getLong("endDate");
                                        iExpiry = new User.IRange() {
                                            @Override
                                            public long startDate() {
                                                return rangeStartDate;
                                            }

                                            @Override
                                            public long endDate() {
                                                return rangeEndDate;
                                            }

                                            @Override
                                            public int getOption() {
                                                return 3;
                                            }
                                        };
                                        break;
                                }
                                userPreference.saveIExpiry(iExpiry);
                                this.userPreference = userPreference;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void savePreference(User.IExpiry expiry, String watermark) {
            UserPreference userPreference = new UserPreference();
            userPreference.saveIExpiry(expiry);
            userPreference.saveWatermark(watermark);
            this.userPreference = userPreference;
        }

        private void startCleanConverted3dTask() {
            mcleanConverted3dHandler.postDelayed(mcleanConverted3dRunnable, 24 * 3600 * 1000);
        }

        private void stopCleanConverted3dTask() {
            mcleanConverted3dHandler.removeCallbacks(mcleanConverted3dRunnable);
        }
        /*------ End: used to clean the converted 3d nxl file periodically--------*/

        public INxlClient getRmsClient() {
            return rmsClient;
        }

        /**
         * session expired(such as user has changed password or ticket expire)handler,
         * will popup a dialog to tip user switch into logon page.
         *
         * @param context must be activity context, not be application context.
         */
        public void sessionExpiredHandler(final Context context) {
            if (context == null || ((Activity) context).isFinishing())
                return;

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.hint_msg_session_expired)
                    .setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SkyDRMApp.getInstance().logout((Activity) context);
                            // close the dialog.
                            dialog.dismiss();
                        }
                    });

            builder.setCancelable(false);
            builder.show();
        }

        @NonNull
        public Watermark getWatermark() {
            return watermark;
        }

        @NonNull
        public RestAPI getRmsRestAPI() throws SessionInvalidException {
            try {
                if (rmsClient == null) {
                    throw new InvalidRMClientException("rmsClient is null");
                }
                if (rmsClient.isSessionExpired()) {
                    throw new InvalidRMClientException("rmClient has exipired");
                }
                if (rmsClient instanceof NxlClient) {
                    return ((NxlClient) rmsClient).getApi();
                } else {
                    throw new InvalidRMClientException("rmClient cast failed");
                }
            } catch (Exception e) {
                log.e(e);
                throw new SessionInvalidException(e.getMessage());
            }
        }

        /**
         * used to send the activity logs for documents in personal space
         * Can backward compatibility.
         *
         * @param operationId:  {@link com.skydrm.sdk.rms.types.SendLogRequestValue.OperationType}
         * @param accessResult: {@link com.skydrm.sdk.rms.types.SendLogRequestValue.AccessResult}
         */
        public void sendLogToServer(String duid, int operationId, String fileName, String filePath, int accessResult, String activityData) {
            SendLogRequestValue logRequestValue = null;
            try {
                logRequestValue = new SendLogRequestValue(duid,
                        rmsClient.getUser().getMembershipId(),
                        rmsClient.getUser().getUserId(),
                        operationId,
//                        URLEncoder.encode(NxCommonUtils.getDeviceName(), StandardCharsets.UTF_8.name()),
                        NxCommonUtils.getDeviceName(),
                        Integer.valueOf(NxCommonUtils.getDeviceType()),
                        "", // repositoryId
                        "", // filePathId
                        fileName,
                        filePath,
                        APP_NAME,
                        APP_PATH,
                        APP_PUBLISHER,
                        accessResult,
                        System.currentTimeMillis(),
                        activityData);
            } catch (Exception e) {
                e.printStackTrace();
            }

            SendLogHelper2 sendLogHelper = new SendLogHelper2(logRequestValue);
            sendLogHelper.reportToRMS();
        }

        private void batchProcessViewLog(List<OfflineLog> offlineLogs) {
            if (offlineLogs == null || offlineLogs.size() == 0) {
                return;
            }
            List<SendLogRequestValue> requestValues = new ArrayList<>();
            for (OfflineLog log : offlineLogs) {
                SendLogRequestValue logRequestValue = new SendLogRequestValue(log.getDuid(),
                        rmsClient.getUser().getMembershipId(),
                        rmsClient.getUser().getUserId(),
                        log.getOperationId(),
//                        URLEncoder.encode(NxCommonUtils.getDeviceName(), StandardCharsets.UTF_8.name()),
                        NxCommonUtils.getDeviceName(),
                        Integer.valueOf(NxCommonUtils.getDeviceType()),
                        "", // repositoryId
                        "", // filePathId
                        log.getFileName(),
                        log.getFilePath(),
                        APP_NAME,
                        APP_PATH,
                        APP_PUBLISHER,
                        log.getAccessResult(),
                        log.getAccessTime(),
                        log.getActivityData());
                requestValues.add(logRequestValue);
            }

            if (requestValues.size() == 0) {
                return;
            }
            SendLogHelper2 sendLogHelper2 = new SendLogHelper2();
            sendLogHelper2.batchReportToRMS(requestValues);
        }

        public void newSession(INxlClient rmClient) {
            this.rmsClient = rmClient;
            configBuildInRepoMyDrive();
            try {
                // make sure Mount Point has good
                String relativePath = session2.getUserTenantName() + "/" + session2.getUserEmail();
                repoSystem.create(commonDir.repoSystemRootFile(), relativePath);//require session has initialized
                repoSystem.attach(getUserLinkedRepos());
                repoSystem.activate();
            } catch (Exception e) {
                // todo: by Osmond  fatal error, kill process
                e.printStackTrace();
            }
            // repo sync, may be ServerImpl has modified the repos
            UserReposSyncPolicy.syncing(ExecutorPools.SelectSmartly(FIRED_BY_UI), new UserReposSyncPolicy.UIListener() {
                @Override
                public void progressing(@NonNull String msg) {
                }

                @Override
                public void result(boolean status) {
                }
            });
            saveSession();
            HeartbeatPolicyGenerator.configureOne(HeartbeatPolicyGenerator.TYPE_COMMON, new CommonPolicy(IHeartBeatPolicy.TYPE_NEW_USER_LOGIN, 120));
            startHeartBeatTask();
            startCleanConverted3dTask();
            // give this session a RMS build-in repository called myDrive
            rmClient.setSecurityCtx(SkyDRMApp.getInstance());
        }

        // session has an attached obligation to create RMS-myDrive repo before init repo_system
        private void configBuildInRepoMyDrive() {
            try {
                String ACCOUNT = getUserEmail();
                BoundService.ServiceType REPO_TYPE = BoundService.ServiceType.MYDRIVE;
                String ACCESTT_TOKEN = "MY_DRIVE_DOSE_NOT_HAVE_THIS";
                String ALIAS = BoundService.MYDRIVE;
                int SET_SELECTED = 1;
                if (!dbExistRepoOfCurrentUser(REPO_TYPE, ACCOUNT)) {
                    dbAddRepo(REPO_TYPE, ALIAS, ACCOUNT, ACCOUNT, ACCESTT_TOKEN, SET_SELECTED);
                    // rmd_repo_id of myDrive must be determined by RMS
                    log.i("Seesion:" + ACCOUNT + " does not has myDrive inbuild ,create one");
                    ExecutorPools.SelectSmartly(NETWORK_TASK).execute(configRepoIdOfMyDrive());
                } else {
                    // if rmsID is invalid, must tu refresh it
                    BoundService myDrive = dbFindRepo(REPO_TYPE, ACCOUNT, ACCOUNT);
                    if (myDrive != null) {
                        if (myDrive.rmsRepoId == null) {
                            // rmd_repo_id of myDrive must be determined by RMS
                            ExecutorPools.SelectSmartly(NETWORK_TASK).execute(configRepoIdOfMyDrive());
                        }
                    }
                }
            } catch (Exception e) {
                log.e(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        private Runnable configRepoIdOfMyDrive() {
            return new Runnable() {
                @Override
                public void run() {
                    try {
                        String rmsRepoId = null;
                        // get myDrive repo id from RMS
                        List<RmsRepoInfo> repos = RmsRepoInfo.fromResultBean(getRmsRestAPI().getRepositoryService(getRmUser()).repositoryGet());
                        for (RmsRepoInfo i : repos) {
                            if (RmsRepoInfo.matchMyDrive(i)) {
                                rmsRepoId = i.rmsRepoId;
                                break;
                            }
                        }
                        if (rmsRepoId == null) {
                            log.e("rmsRepoId can not be found");
                            return;
                        }
                        // update database
                        BoundService myDrive = dbFindRepo(BoundService.ServiceType.MYDRIVE, getUserEmail(), getUserEmail());
                        if (myDrive == null) {
                            log.e("myDrive can not be found");
                            return;
                        }
                        myDrive.rmsRepoId = rmsRepoId;
                        // update new id into db
                        dbUpdateRepoRmsId(myDrive);

                        // update myDrive into RepoSystem
                        repoSystem.updateRepo(myDrive);

                    } catch (Exception e) {
                        log.e(e);
                    }
                }
            };
        }

        public void closeSession() {
            stopHeartBeatTask();
            stopCleanConverted3dTask();
            clearSession();
        }

        public IRmUser getRmUser() throws InvalidRMClientException {
            if (rmsClient == null) {
                log.e("rmsClient is null in Session.getUserInfo()");
                throw new InvalidRMClientException("rmsClient is null in Session.getUserInfo()");
            }
            IRmUser user = rmsClient.getUser();
            if (user == null) {
                log.e("user is null in Session.getUserInfo()");
                throw new InvalidRMClientException("user is null in Session.getUserInfo()");
            }
            return user;
        }

        public String getUserEmail() throws InvalidRMClientException {
            return getRmUser().getEmail();
        }

        public String getUserTenantName() {
            return getRmsClient().getTenant().getTenantId();
        }

        private void startHeartBeatTask() {
            heartbeatTask = new Thread(new BackGroundHearBeat(), "NEXTLABS_HEARTBEAT_THREAD");
            heartbeatTask.start();
        }

        private void stopHeartBeatTask() {
            heartbeatTask.interrupt();
        }

        public synchronized void saveSession() {
            IRmUser u = rmsClient.getUser();
            String name = u.getName();
            String email = u.getEmail();
            int userId = u.getUserId();
            int idpType = u.getIdpType();
            long ttl = u.getTtl();
            String ticket = u.getTicket();
            String tenantId = u.getTenantId();
            String tokenGroupName = u.getTokenGroupName();
            String defaultTenant = u.getDefaultTenant();
            String defaultTenantUrl = "";
            String preferencesRawJson = com.skydrm.rmc.database.table.user.User.toRawJson(lastSyncTimeWithRMS_Mills);
            String userRawJson = u.toString();

            mDBProvider.upsertUserItem(name, email, userId, idpType, ttl, ticket, tenantId, tokenGroupName,
                    defaultTenant, defaultTenantUrl, preferencesRawJson, userRawJson);
        }

        private synchronized boolean recoverSession(SessionRecoverListener listener) {
            // recover common login profile
            String userJson;
            String serverURL;
            String tenantID;

            // retrieve session values
            try {
                IServer server = mDBProvider.queryServerItem();
                IUser user = mDBProvider.queryUserItem();

                userJson = user.getUserRawJson();
                if (userJson.equals("NULL")) {
                    listener.onFailed("failed recovering session");
                    return false;
                }
                serverURL = server.getRmsURL();
                if (serverURL.equals("NULL")) {
                    listener.onFailed("failed recovering session");
                    return false;
                }
                tenantID = server.getTenantId();
                if ("NULL".equals(tenantID)) {
                    listener.onFailed("failed recovering session");
                    return false;
                }
                lastSyncTimeWithRMS_Mills = user.getPreference().getSyncWithRmsTimeMillis();

                rmsClient = Factory.recoverClient(userJson, tenantID, serverURL);
                IRmUser rmUser = rmsClient.getUser();
                if (rmUser instanceof RmUser) {
                    RmUser usr = (RmUser) rmUser;
                    usr.setTenantAdmin(user.isTenantAdmin());
                    usr.setProjectAdmin(user.isProjectAdmin());
                }
                rmsClient.setSecurityCtx(SkyDRMApp.getInstance());
                listener.onProcess("success recovering session!");
                // make sure session folder has been created
                commonDir.userRootFile();
                return true;
            } catch (Exception e) {
                log.e(e.getMessage(), e);
                listener.onFailed("failed recovering session,exception:" + e.toString());
            }
            return false;
        }

        private synchronized void clearSession() {
            mDBProvider.onUserLogout();
            rmsClient = null;
            // clear remoteViewer cache
            remoteViewerOfficePdfCache = null;
            //clear user preference information stored in Application.
            userPreference = null;
        }

        public void syncUserPreference() throws SessionInvalidException, RmsRestAPIException {
            getRmsRestAPI()
                    .getUserService(rmsClient.getUser())
                    .retrieveUserPreference(new RestAPI.IRequestCallBack<String>() {
                        @Override
                        public void onSuccess(String result) {
                            log.d("syncUserPreferenceTask-->" + result);
                            savePreference(result);
                        }

                        @Override
                        public void onFailed(int statusCode, String errorMsg) {

                        }
                    });
        }

        class BackGroundHearBeat implements Runnable {
            static private final int HeartBeat_Intervals = 120;

            @Override
            public void run() {
                boolean isWait30sec = false;
                while (!Thread.interrupted()) {
                    try {
                        if (isWait30sec) {
                            isWait30sec = false;
                            TimeUnit.SECONDS.sleep(30);
                        }
                        if (!networkStatus.isNetworkAvailable()) {
                            isWait30sec = true;
                            continue;
                        }
                        //do task
                        doHeartBeatTask();
                        TimeUnit.SECONDS.sleep(HeartBeat_Intervals);
                    } catch (InterruptedException e) {
                        log.i("task thread is been interrupted!");
                        break;
                    } catch (Exception e) {
                        log.e(e.getMessage(), e);
                        isWait30sec = true;
                    }
                }// end while
                log.i("user interrupt");
            }

            private void doHeartBeatTask() throws RmsRestAPIException, InvalidRMClientException, SessionInvalidException {
                try {
                    rmsHeartbeatTask();
                } catch (Exception e) {
                    log.e(e);
                }
                try {
                    rmsLogTask();
                } catch (Exception e) {
                    log.e(e);
                }
                try {
                    syncFavoriteOfflineTask();
                } catch (RmsRestAPIException e) {
                    log.e(e);
                }
                try {
                    log.v("In Heartbeat, save repos in repo system to local ");
                    syncRepoSystemTask();
                } catch (Exception e) {
                    log.e(e);
                }
                try {
                    log.d("syncUserPreferenceTask-->In Heartbeat, syncUserPreferenceTask ");
                    syncUserPreferenceTask();
                } catch (Exception e) {
                    log.e(e);
                }
                HeartBeatManager.getInstance().onHeartBeat();
            }

            private void rmsHeartbeatTask() throws RmsRestAPIException, InvalidRMClientException, SessionInvalidException {
                HeartbeatResponse heartbeatResponse = session2
                        .getRmsRestAPI()
                        .getHeartbeatService(rmsClient.getUser())
                        .heartbeat();
                watermark = heartbeatResponse.getWatermark();
            }

            private void rmsLogTask() {
                try {
                    // re-send the log to server that have not sent successfully because of network problem.
                    SendLogHelper2 sendLogHelper = new SendLogHelper2();
                    sendLogHelper.reSubmitLogToRMS();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                batchProcessViewLog(mDBProvider.queryAllActivityLogItem());
                mDBProvider.deleteAllActivityLogItem();
            }

            private void syncFavoriteOfflineTask() throws RmsRestAPIException {

//                // post
                FavoriteMarkImpl.getInstance().syncMarkedFileToRms();
//                // get
//                List<ParseJsonUtils.AllRepoFavoListBean> repoList = FavoriteMarkImpl.getInstance().getAllRepoFavFileList(new AllRepoFavFileRequestParas());
//                // update
//               onUpdateFileMark(repoList);
                log.v("test fav");
            }

            private void syncRepoSystemTask() {
                repoSystem.saveToLocal();
            }

            private void syncUserPreferenceTask() throws RmsRestAPIException, SessionInvalidException {
                session2.syncUserPreference();
            }
        }

    }

    public class DataBaseWithCache {
        private final Object repos_sync = new Object();
        private List<RepoEntry> repos;
        private Database db;
        //status
        private boolean initialized = false;

        public DataBaseWithCache() {
            if (initialized) {
                throw new RuntimeException("can be created only once");
            }
            db = new Database(SkyDRMApp.this);
            repos = new ArrayList<>();
            generateCache();
            initialized = true;
        }

        private void generateCache() {
            repos = db.queryAll();
            if (config.isDebug()) {
                log.v("++++++++++dump repos in db++++++++++");
                for (RepoEntry e : repos) {
                    log.v(e.toString());
                }
                log.v("------------------------------------");
            }
        }

        private List<BoundService> queryFromCacheBy(String tenant, int userId) {
            List<BoundService> services = new ArrayList<>();
            synchronized (repos_sync) {
                for (RepoEntry e : repos) {
                    if (TextUtils.equals(e.getTenant(), tenant) && e.getUserId() == userId) {
                        services.add(e.convert());
                    }
                }
            }
            return services;
        }

        void addRepo(String tenant,
                     int userId,
                     BoundService.ServiceType type,
                     String alias,
                     String account,
                     String accountId,
                     String accountToken,
                     int selected) {
            // add to cache first
            final RepoEntry entry = new RepoEntry();
            entry.tenant = tenant;
            entry.userId = userId;
            entry.repo_type = type.value();
            entry.repo_alias = alias;
            entry.repo_account = account;
            entry.repo_account_id = accountId;
            entry.repo_account_token = accountToken;
            entry.repo_select_status = selected == 1;

            synchronized (repos_sync) {
                repos.add(entry);
            }

            // tell db
            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(new Runnable() {
                RepoEntry e = entry;

                @Override
                public void run() {
                    db.addService(
                            e.userId,
                            e.tenant,
                            BoundService.ServiceType.valueOf(e.repo_type),
                            e.repo_alias,
                            e.repo_account,
                            e.repo_account_id,
                            e.repo_account_token,
                            e.repo_select_status ? 1 : 0
                    );
                    // check status, maybe need to amend e.id
                }
            });
        }

        void insertRepo(String tenant, int userId, BoundService s) {
            // add to cache first
            final RepoEntry entry = new RepoEntry();
            entry.tenant = tenant;
            entry.userId = userId;
            entry.repo_type = s.type.value();
            entry.repo_alias = s.alias;
            entry.repo_account = s.account;
            entry.repo_account_id = s.accountID;
            entry.repo_account_token = s.accountToken;
            entry.repo_select_status = (s.selected == 1);
            entry.rms_repo_id = s.rmsRepoId;
            entry.rms_nick_name = s.rmsNickName;
            entry.rms_repo_token = s.rmsToken;
            entry.rms_is_shared = s.rmsIsShared;
            entry.rms_is_perference = s.rmsPreference;
            entry.rms_creation_time = s.rmsCreationTime;
            entry.rms_updated_time = s.rmsUpdatedTime;

            synchronized (repos_sync) {
                repos.add(entry);
            }

            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(new Runnable() {
                RepoEntry e = entry;

                @Override
                public void run() {
                    db.insert(e);
                }
            });
        }

        public BoundService queryService(String tenant,
                                         int userId,
                                         int type,
                                         String account,
                                         String accountId) {
            synchronized (repos_sync) {
                for (RepoEntry e : repos) {
                    if (TextUtils.equals(e.getTenant(), tenant)
                            && e.getUserId() == userId
                            && e.repo_type == type
                            && TextUtils.equals(account, e.repo_account)
                            && TextUtils.equals(accountId, e.repo_account_id)) {
                        return e.convert();
                    }
                }
            }
            return null;
        }


        public void delService(String tenant, int userId, BoundService s) {
            // del from cache first
            Iterator<RepoEntry> iterator = repos.iterator();
            RepoEntry toBeDel = null;
            synchronized (repos_sync) {
                while (iterator.hasNext()) {
                    toBeDel = iterator.next();
                    if (TextUtils.equals(toBeDel.getTenant(), tenant)
                            && toBeDel.getUserId() == userId
                            && toBeDel.repo_type == s.type.value()
                            && TextUtils.equals(s.rmsRepoId, toBeDel.rms_repo_id)) {
                        iterator.remove();
                        break;
                    }
                }
            }
            if (toBeDel == null) {
                return;
            }
            // tell db
            final RepoEntry finalToBeDel = toBeDel;
            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(new Runnable() {
                RepoEntry e = finalToBeDel;

                @Override
                public void run() {
                    db.delService(e.userId, e.tenant, e.repo_type, e.repo_account_token);
                }
            });
        }

        public void updateRepoToken(String tenant, int userId, BoundService s) {
            // update cache first
            Iterator<RepoEntry> iterator = repos.iterator();
            RepoEntry toBeUpdate = null;
            synchronized (repos_sync) {
                while (iterator.hasNext()) {
                    toBeUpdate = iterator.next();
                    if (TextUtils.equals(toBeUpdate.getTenant(), tenant)
                            && toBeUpdate.getUserId() == userId
                            && toBeUpdate.repo_type == s.type.value()
                            && TextUtils.equals(s.accountID, toBeUpdate.repo_account_id)
                    ) {
                        toBeUpdate.repo_account_token = s.accountToken;
                        toBeUpdate.rms_repo_token = s.rmsToken;
                        break;
                    }
                }
            }
            if (toBeUpdate == null) {
                return;
            }
            // tell db
            final RepoEntry finalToBeDel = toBeUpdate;
            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(new Runnable() {
                RepoEntry e = finalToBeDel;

                @Override
                public void run() {
                    db.updateRepoToken(e.userId, e.tenant, e.convert());
                }
            });
        }

        public void updateRepoSelected(String tenant, int userId, BoundService s) {
            // update cache first
            Iterator<RepoEntry> iterator = repos.iterator();
            RepoEntry toBeUpdate = null;
            synchronized (repos_sync) {
                while (iterator.hasNext()) {
                    toBeUpdate = iterator.next();
                    if (TextUtils.equals(toBeUpdate.getTenant(), tenant)
                            && toBeUpdate.getUserId() == userId
                            && toBeUpdate.repo_type == s.type.value()
                            && TextUtils.equals(s.accountToken, toBeUpdate.repo_account_token)
                    ) {
                        toBeUpdate.repo_select_status = s.selected == 1;
                        break;
                    }
                }
            }
            if (toBeUpdate == null) {
                return;
            }
            // tell db
            final RepoEntry finalToBeDel = toBeUpdate;
            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(new Runnable() {
                RepoEntry e = finalToBeDel;

                @Override
                public void run() {
                    db.updateRepoSelected(e.userId, e.tenant, e.convert());
                }
            });
        }

        public void updateRepoNickName(String tenant, int userId, BoundService s) {
            // update cache first
            Iterator<RepoEntry> iterator = repos.iterator();
            RepoEntry toBeUpdate = null;
            synchronized (repos_sync) {
                while (iterator.hasNext()) {
                    toBeUpdate = iterator.next();
                    if (TextUtils.equals(toBeUpdate.getTenant(), tenant)
                            && toBeUpdate.getUserId() == userId
                            && toBeUpdate.repo_type == s.type.value()
                            && TextUtils.equals(s.accountToken, toBeUpdate.repo_account_token)
                    ) {
                        toBeUpdate.rms_nick_name = s.rmsNickName;
                        break;
                    }
                }
            }
            if (toBeUpdate == null) {
                return;
            }
            // tell db
            final RepoEntry finalToBeDel = toBeUpdate;
            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(new Runnable() {
                RepoEntry e = finalToBeDel;

                @Override
                public void run() {
                    db.updateRepoNickName(e.userId, e.tenant, e.convert());
                }
            });
        }

        public void updateRepoRmsID(String tenant, int userId, BoundService s) {
            // update cache first
            Iterator<RepoEntry> iterator = repos.iterator();
            RepoEntry toBeUpdate = null;
            synchronized (repos_sync) {
                while (iterator.hasNext()) {
                    toBeUpdate = iterator.next();
                    if (TextUtils.equals(toBeUpdate.getTenant(), tenant)
                            && toBeUpdate.getUserId() == userId
                            && toBeUpdate.repo_type == s.type.value()
                            && TextUtils.equals(s.accountToken, toBeUpdate.repo_account_token)
                    ) {
                        toBeUpdate.rms_repo_id = s.rmsRepoId;
                        break;
                    }
                }
            }
            if (toBeUpdate == null) {
                return;
            }
            // tell db
            final RepoEntry finalToBeDel = toBeUpdate;
            ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND).execute(new Runnable() {
                RepoEntry e = finalToBeDel;

                @Override
                public void run() {
                    db.updateRepoRmsID(e.userId, e.tenant, e.convert());
                }
            });
        }

        public boolean exist(String tenant, int userId, int type, String account) {
            synchronized (repos_sync) {
                for (RepoEntry e : repos) {
                    if (TextUtils.equals(e.getTenant(), tenant)
                            && e.getUserId() == userId
                            && e.repo_type == type) {
                        if (type != BoundService.ServiceType.SHAREPOINT_ONLINE.value()) {
                            if (TextUtils.equals(account, e.repo_account)) {
                                return true;
                            }
                        } else {
                            // special for sharepoint online
                            String predict = e.repo_account_id + e.repo_account;
                            if (TextUtils.equals(account.toLowerCase(), predict.toLowerCase())) {
                                return true;
                            }
                        }

                    }
                }
            }
            return false;
        }

    }
}
