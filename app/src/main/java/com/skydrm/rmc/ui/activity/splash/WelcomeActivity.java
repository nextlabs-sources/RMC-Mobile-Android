package com.skydrm.rmc.ui.activity.splash;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skydrm.rmc.BuildConfig;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.ui.activity.ViewActivity;
import com.skydrm.rmc.ui.activity.home.HomeActivity;
import com.skydrm.rmc.ui.activity.server.ServerConfig;
import com.skydrm.rmc.ui.activity.server.ServerTypeSelectActivity;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.project.service.InvitationLinkActivity;
import com.skydrm.rmc.ui.widget.autoscrollviewpager.AutoScrollViewPager;
import com.skydrm.rmc.ui.widget.customcontrol.SafeProgressDialog;
import com.skydrm.rmc.utils.SharePreferUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.Factory;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.project.ProjectMetaDataResult;

import java.util.ArrayList;
import java.util.List;

import static com.skydrm.sdk.Factory.RIGHTS_MANAGEMENT_ADDRESS;
import static com.skydrm.sdk.Factory.RIGHTS_MANAGEMENT_TENANT;

public class WelcomeActivity extends BaseActivity {
    private static final int IMAGE_MARGIN_RATIO = 8;
    private static final String AND = "and";
    // for features splash section
    private AutoScrollViewPager featuresViewPager;
    private List<View> featureViewLists = new ArrayList<>();
    private List<ImageView> scrollableDots = new ArrayList<>();
    private int mScreenHeight;
    private Intent mIntent;
    private boolean bIntercept = BuildConfig.FLAVOR.contains("pro");
    private boolean bRegister;
    private boolean bLogin;

    private String mRouterUrl;
    private boolean onPremise;
    private ProgressDialog mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initView();
        initEvent();

        mIntent = getIntent();
        parseIntent(mIntent);
    }

    private void parseIntent(Intent intent) {
        boolean redirect = intent.getBooleanExtra(Constant.COMMAND_REDIRECT, false);
        if (redirect) {
            onCommand_LogIn();
        }
    }

    /**
     * fix bug 44964, since this Activity launch mode is "singleTask", so will call this method when re-launch this activity,
     * which its instance has existed, then we can get this intent here.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mIntent = intent;
    }

    private void initView() {
        // get the width and height of screen.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenHeight = dm.heightPixels;
        featuresViewPager = findViewById(R.id.splash_view_pager);
        View featurePage1 = LayoutInflater.from(this).inflate(R.layout.layout_splash_feature_page1_2, null);
        TextView text_welcome_page1 = featurePage1.findViewById(R.id.welcome_page1_text1);
        SpannableString currentPdTitleStr = new SpannableString(getString(R.string.welcom_page1_text1));
        currentPdTitleStr.setSpan(new ForegroundColorSpan(Color.BLACK),
                currentPdTitleStr.toString().lastIndexOf(AND), currentPdTitleStr.toString().lastIndexOf(AND) + 3,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        currentPdTitleStr.setSpan(new StyleSpan(Typeface.NORMAL),
                currentPdTitleStr.toString().lastIndexOf(AND), currentPdTitleStr.toString().lastIndexOf(AND) + 3,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        currentPdTitleStr.setSpan(new StyleSpan(Typeface.ITALIC),
                0, currentPdTitleStr.toString().lastIndexOf(AND),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        currentPdTitleStr.setSpan(new StyleSpan(Typeface.ITALIC),
                currentPdTitleStr.toString().lastIndexOf(AND) + 3, currentPdTitleStr.toString().length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text_welcome_page1.setText(currentPdTitleStr);
        View featurePage2 = LayoutInflater.from(this).inflate(R.layout.layout_splash_feature_page2_2, null);
        View featurePage3 = LayoutInflater.from(this).inflate(R.layout.layout_splash_feature_page3_2, null);
        View featurePage4 = LayoutInflater.from(this).inflate(R.layout.layout_splash_feature_page4_2, null);
        View featurePage5 = LayoutInflater.from(this).inflate(R.layout.layout_splash_feature_page5_2, null);
//        ImageView mIvComIcon = (ImageView) featurePage1.findViewById(R.id.com_icon);
//        setControlMargin(mIvComIcon, IMAGE_MARGIN_RATIO);
        ImageView mIvFeaturePage2 = (ImageView) featurePage2.findViewById(R.id.iv_feature_page2);
//        setControlMargin(mIvFeaturePage2, IMAGE_MARGIN_RATIO);
        ImageView mIvFeaturePage3 = (ImageView) featurePage3.findViewById(R.id.iv_feature_page3);
//        setControlMargin(mIvFeaturePage3, IMAGE_MARGIN_RATIO);
        ImageView mIvFeaturePage4 = (ImageView) featurePage4.findViewById(R.id.iv_feature_page4);
//        setControlMargin(mIvFeaturePage4, IMAGE_MARGIN_RATIO);
        ImageView mIvFeaturePage5 = (ImageView) featurePage5.findViewById(R.id.iv_feature_page5);
//        setControlMargin(mIvFeaturePage5, IMAGE_MARGIN_RATIO);
        featureViewLists.add(featurePage1);
        featureViewLists.add(featurePage2);
        featureViewLists.add(featurePage3);
        featureViewLists.add(featurePage4);
        featureViewLists.add(featurePage5);
        ImageView mScrollDot1 = findViewById(R.id.scroll_dot1);
        ImageView mScrollDot2 = findViewById(R.id.scroll_dot2);
        ImageView mScrollDot3 = findViewById(R.id.scroll_dot3);
        ImageView mScrollDot4 = findViewById(R.id.scroll_dot4);
        ImageView mScrollDot5 = findViewById(R.id.scroll_dot5);
        scrollableDots.add(mScrollDot1);
        scrollableDots.add(mScrollDot2);
        scrollableDots.add(mScrollDot3);
        scrollableDots.add(mScrollDot4);
        scrollableDots.add(mScrollDot5);
        featuresViewPager.setAdapter(new SplashPagerAdapter(featureViewLists));
        featuresViewPager.setCurrentItem(0);
        scrollableDots.get(0).setVisibility(View.VISIBLE);
        featuresViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int newPositon = position % featureViewLists.size();
                for (int i = 0; i < scrollableDots.size(); i++) {
                    if (newPositon == i) {
                        scrollableDots.get(i).setVisibility(View.VISIBLE);
                    } else {
                        scrollableDots.get(i).setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        featuresViewPager.startAutoScroll();
    }

    private void setControlMargin(ImageView object, int ratio) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) object.getLayoutParams();
        layoutParams.setMargins(0, mScreenHeight / ratio, 0, 0);
    }

    private void initEvent() {
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bLogin = true;
                bRegister = false;
                onCommand_interceptLogin();
            }
        });
        findViewById(R.id.signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bLogin = false;
                bRegister = true;
                onCommand_interceptLogin();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (featuresViewPager != null) {
            featuresViewPager.startAutoScroll();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (featuresViewPager != null) {
            featuresViewPager.stopAutoScroll();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (featuresViewPager != null) {
            featuresViewPager.stopAutoScroll();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // for RM-SDK results
        if (requestCode == Factory.REQUEST_LOGIN && data != null) {
            // receive btnLogin result
            if (resultCode == Activity.RESULT_OK && Factory.authenticationSuccessfully(data)) {
                onUserLogin(data);
            }
        } else if (requestCode == Constant.CONFIG_SERVER && data != null) {
            if (resultCode == RESULT_OK) {
                onPremise = data.getBooleanExtra("on_premise", false);
                mRouterUrl = data.getStringExtra("router_url");
                configOnPremiseMode(onPremise);
                onLoadLoginPage();
            }
        }
    }

    private void onUserLogin(Intent data) {
        try {
            SkyDRMApp.getInstance().getDBProvider().upsertServerItem(mRouterUrl,
                    data.getStringExtra(RIGHTS_MANAGEMENT_ADDRESS),
                    data.getStringExtra(RIGHTS_MANAGEMENT_TENANT), onPremise);

            SkyDRMApp.getInstance().newSession(Factory.finishAuthenticating(data));

            // When open the external nxl file or email link
            if (mIntent != null && mIntent.getAction() != null && mIntent.getAction().equals("android.intent.action.VIEW")) {
                switchActivity();
                return;
            }

            // start home page
            startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            WelcomeActivity.this.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * When open the external nxl file(our skyDRM as third part to open file) or open share with me link, will switch to viewActivity directly;
     * when open the project invitation link, will switch to ClickEmailToProjects activity to handle it.
     */
    private void switchActivity() {
        if (mIntent == null) {
            return;
        }
        Uri uri = mIntent.getData();
        if (uri == null) {
            throw new RuntimeException("the uri is empty.");
        }

        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) { // open the email link
            dispatchUriLink(uri);
        } else { // open the external nxl file(our skyDRM as third part to open file) -- the scheme is file or content)
            Intent intent = new Intent();
            intent.putExtra("NXVIEW", uri.toString());
            intent.setAction("NXInitViewToLogin");
            intent.setClass(WelcomeActivity.this, ViewActivity.class);
            startActivity(intent);
            WelcomeActivity.this.finish();
        }
    }

    // parse the email url link then dispatch into different activity page.
    private void dispatchUriLink(Uri uri) {
        Intent intent = null;
        if (uri.toString().contains("/viewSharedFile")) { // open share with me link
            intent = mIntent;
            intent.putExtra("NXVIEW", uri.toString());
            intent.setClass(WelcomeActivity.this, ViewActivity.class);
        } else if (uri.toString().contains("/invitation")) { // open project invitation link
            intent = getIntent();
            intent.putExtra("INVITATION", uri.toString());
            intent.setClass(WelcomeActivity.this, InvitationLinkActivity.class);
        } else if (uri.toString().contains("/projects")) { // open project files link when project member upload file into some project
            String stringUri = uri.toString();
            String projectId = stringUri.replaceAll("\\D+", "");

            int parseProjectId = -1;
            try {
                parseProjectId = Integer.parseInt(projectId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            checkContainProject(this, parseProjectId);

            // start home page

            startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            WelcomeActivity.this.finish();
            return;
        }

        startActivity(intent);
        WelcomeActivity.this.finish();
    }


    private void checkContainProject(final Context context, int projectId) {
        FileOperation.getProjectMetaData(context, projectId, new FileOperation.IProjectMetaData() {
            @Override
            public void onProjectMetaDataFinished(ProjectMetaDataResult result) {

            }

            @Override
            public void onError(RmsRestAPIException e) {
                String hintMessage = "";
                try {
                    hintMessage = getString(R.string.You_are_not_authorized_to_access_this_project);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), hintMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onCommand_interceptLogin() {
        if (shouldIntercept()) {
            configServerUrl();
        } else {
            configServer(Factory.getPersonalCountURL());
        }
    }

    private void onLoadLoginPage() {
        if (bLogin) {
            onCommand_LogIn();
        }
        if (bRegister) {
            onCommand_SignUp();
        }
    }

    private void onCommand_SignUp() {
        Factory.registerNewAccount(this);
    }

    private void onCommand_LogIn() {
        Factory.startAuthenticating(this);
    }

    private boolean shouldIntercept() {
        return bIntercept;
    }

    private void configFactory(String url) {
        Factory.changeRMServer(url);
    }

    private String getStoredServerUrl() {
        return (String) SharePreferUtils.getParams(getApplicationContext(),
                Constant.CONFIGED_SERVER_URL, "www.skydrm.com");
    }

    private void configServerUrl() {
        Intent serverConfigIntent = new Intent(this, ServerTypeSelectActivity.class);
        startActivityForResult(serverConfigIntent, Constant.CONFIG_SERVER);
    }

    private void configOnPremiseMode(boolean onPremise) {
        SharePreferUtils.setParams(getApplicationContext(), Constant.ON_PREMISE, onPremise);
    }

    private void configServer(final String routerUrl) {
        ServerConfig.configServer(routerUrl, new ServerConfig.Callback() {

            @Override
            public void onInvoking() {
                showLoadingDialog();
            }

            @Override
            public void onInvoked(String serverURL) {
                dismissDialog();

                configFactory(serverURL);
                mRouterUrl = routerUrl;
                onPremise = false;
                SharePreferUtils.setParams(getApplicationContext(), Constant.INPUT_SERVER_URL, routerUrl);
                configOnPremiseMode(false);
                onLoadLoginPage();
            }

            @Override
            public void onFailed(String msg) {
                dismissDialog();
                ToastUtil.showToast(getApplicationContext(), msg);
            }
        });
    }

    private void showLoadingDialog() {
        mLoadingDialog = SafeProgressDialog.show(this,
                getString(R.string.wait_load), "", true);
    }

    private void dismissDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }
}
