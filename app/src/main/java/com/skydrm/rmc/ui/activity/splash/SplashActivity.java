package com.skydrm.rmc.ui.activity.splash;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.activity.ViewActivity;
import com.skydrm.rmc.ui.activity.home.HomeActivity;
import com.skydrm.rmc.ui.project.ProjectActivity;
import com.skydrm.rmc.ui.project.service.InvitationLinkActivity;
import com.skydrm.rmc.ui.project.service.SwitchProjectActivity;
import com.skydrm.rmc.ui.project.service.adapter.EmailLinkToProjectAdapter;
import com.skydrm.rmc.ui.common.ActivityManager;

import java.lang.ref.WeakReference;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;


/**
 * The first Activity will be displayed for user
 * - good point to do app level initialization tasks
 * - session recovery will be tried here
 * -- for Ok,       direct to Home Activity
 * -- for Failed,   direct to Welcome page for login or register
 * - 3rd app may open SkyDrm by file viewing or email-attachment/email-link viewing
 */
public class SplashActivity extends Activity {
    // for Messages
    private static final int MSG_SYSTEM_COMPONENTS_INIT_OK = 0X1001;
    private static final int MSG_SYSTEM_COMPONENTS_INIT_FAILED = 0X1002;
    private static final int MSG_SESSION_RECOVERY_OK = 0X1003;
    private static final int MSG_SESSION_RECOVERY_FAILED = 0X1004;
    private static DevLog log = new DevLog(SplashActivity.class.getSimpleName());
    private final Handler sHandler = new UIHandler(this);

    static public String nextlabs_oauth_result;
    private boolean mRedirect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        log.w("onCreate");


        // as for RMS design, we have to, at here, intercept
        // results from system browser, when user want to add a 3rd party repository
        // parse the intent here

        // and fire oauth2.0 by the url returned by RMS
        //
        //
        Intent intent = getIntent();
        log.w(intent.toString());
        // if is from Supported Cloud Activiy
        final String oauth = intent.getStringExtra("nextlabs_oauth");
        if (oauth != null && !oauth.isEmpty()) {
            Intent toSystemBrowser = new Intent();
            toSystemBrowser.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(oauth);
            toSystemBrowser.setData(content_url);
            startActivity(toSystemBrowser);
            finish();
            return;
        }
        // if is from System-browser to finish oauth
        final Uri uri = getIntent().getData();
        if (uri != null && uri.getScheme().equalsIgnoreCase("com.skydrm.rmc")) {
            // set the activity result and let the SupportedCloud activity to handle it
            nextlabs_oauth_result = uri.toString();
            finish();
            return;
        }
        mRedirect = intent.getBooleanExtra(Constant.COMMAND_REDIRECT, false);
        onEvent_BeginBrandSplash();
    }

    /**
     * Good point for
     * -- initializing system components
     * -- trying to recover last session
     */
    private void onEvent_BeginBrandSplash() {
        // app appInitialize
        ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new AppInitTask());
    }

    private void onEvent_SessionRecoveryOK() {
        log.v("onEvent_SessionRecoveryOK");
        // When open the external nxl file or email link
        Intent thisContextIntent = getIntent();
        if (util_IsContainViewAction(thisContextIntent)) {
            switchActivity();
            return;
        }

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void onEvent_SessionRecoveryFailed() {
        /*
           Failed to recover session,
           - direct user to Welcome activity for register/login

           - add new requirement:
             - if our app is stared by action of android.intent.action.VIEW,
               set the original intent's data and action to Welcome activity
         */
        log.v("onEvent_SessionRecoveryFailed");
        Intent newIntent = new Intent(this, WelcomeActivity.class);
        Intent thisContextIntent = getIntent();

        // pass intent action(android.intent.action.VIEW) into WelcomeActivity when user is not login.
        if (util_IsContainViewAction(thisContextIntent)) {
            newIntent.setAction(thisContextIntent.getAction());
            newIntent.setData(thisContextIntent.getData());
        }
        newIntent.putExtra(Constant.COMMAND_REDIRECT, mRedirect);
        startActivity(newIntent);
        finish();

    }

    private boolean util_IsContainViewAction(Intent intent) {
        if (intent == null) {
            return false;
        }
        return TextUtils.equals(intent.getAction(), "android.intent.action.VIEW");
    }


    /**
     * When open the external nxl file(our skyDRM as third part to open file) or open share with me link, will switch to viewActivity directly;
     * when open the project invitation link, will switch to InvitationLinkActivity to handle it.
     */
    private void switchActivity() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            throw new RuntimeException("the uri is empty.");
        }

        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) { // open the email link
            dispatchUriLink(uri);
        } else { // open the external nxl file(our skyDRM as third part to open file) -- the scheme is file or content)
            Intent intent = new Intent();
            intent.putExtra("NXVIEW", uri.toString());
            intent.setAction("NXInitViewToLogin");
            intent.setClass(SplashActivity.this, ViewActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }
    }

    // parse the email url link then dispatch into different activity page.
    private void dispatchUriLink(final Uri uri) {
        Intent intent = null;
        if (uri.toString().contains("/viewSharedFile")) { // open share with me link
            intent = getIntent();
            if (intent != null) {
                intent.putExtra("NXVIEW", uri.toString());
                intent.setClass(SplashActivity.this, ViewActivity.class);
            }
        } else if (uri.toString().contains("/invitation")) { // open project invitation link
            intent = getIntent();
            if (intent != null) {
                intent.putExtra("INVITATION", uri.toString());
                intent.setClass(SplashActivity.this, InvitationLinkActivity.class);
            }
        } else {
            if (uri.toString().contains("/projects")) { // open project files link when project member upload file into some project
                ActivityManager activityManager = ActivityManager.getDefault();
                if (activityManager.containClass(HomeActivity.class)) {
                    activityManager.finishActivityByClass(ProjectActivity.class);
                    activityManager.finishActivityByClass(SwitchProjectActivity.class);

                    String stringUri = uri.toString();
                    String projectId = stringUri.replaceAll("\\D+", "");

                    EmailLinkToProjectAdapter emailLinkToProjectAdapter = new EmailLinkToProjectAdapter(this, projectId);
                    emailLinkToProjectAdapter.toProjectActivity();
                } else {
                    intent = new Intent(this, HomeActivity.class);
                }
            }
        }

        if (intent != null) {
            startActivity(intent);
            SplashActivity.this.finish();
        }
    }


    private static class UIHandler extends Handler {
        private final WeakReference<SplashActivity> mActivity;

        UIHandler(SplashActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            final SplashActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_SYSTEM_COMPONENTS_INIT_OK:
                    log.v("MSG_SYSTEM_COMPONENTS_INIT_OK");
                    break;
                case MSG_SYSTEM_COMPONENTS_INIT_FAILED:
                    log.v("MSG_SYSTEM_COMPONENTS_INIT_FAILED");
                    System.exit(0);
                    break;
                case MSG_SESSION_RECOVERY_OK:
                    log.v("MSG_SESSION_RECOVERY_OK");
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            activity.onEvent_SessionRecoveryOK();
                        }
                    }, 200);
                    break;
                case MSG_SESSION_RECOVERY_FAILED:
                    log.v("MSG_SESSION_RECOVERY_FAILED");
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            activity.onEvent_SessionRecoveryFailed();
                        }
                    }, 200);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class AppInitTask implements Runnable {
        @Override
        public void run() {
            try {
                // app init
                SkyDRMApp app = SkyDRMApp.getInstance();
                sHandler.sendEmptyMessage(MSG_SYSTEM_COMPONENTS_INIT_OK);
                // trying to recover session
                app.recoverySession(new SkyDRMApp.SessionRecoverListener() {
                    @Override
                    public void onSuccess() {
                        log.v("session recover ok");
                        sHandler.sendEmptyMessage(MSG_SESSION_RECOVERY_OK);
                    }

                    @Override
                    public void onAlreadyExist() {
                        log.v("session recover ok");
                        sHandler.sendEmptyMessage(MSG_SESSION_RECOVERY_OK);
                    }

                    @Override
                    public void onFailed(String reason) {
                        log.v("session recover failed" + reason);
                        sHandler.sendEmptyMessage(MSG_SESSION_RECOVERY_FAILED);

                    }

                    @Override
                    public void onProcess(String hint) {
                        log.v("session recovering " + hint);
                    }
                });

            } catch (Exception e) {
                log.e(e);
                sHandler.sendEmptyMessage(MSG_SYSTEM_COMPONENTS_INIT_FAILED);
            }
        }
    }
}
