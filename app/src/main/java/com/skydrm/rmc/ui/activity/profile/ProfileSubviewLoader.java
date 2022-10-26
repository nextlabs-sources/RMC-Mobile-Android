package com.skydrm.rmc.ui.activity.profile;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.BuildConfig;
import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.presenter.IUserDataPresenter;
import com.skydrm.rmc.presenter.impl.UserDataPresenterImpl;
import com.skydrm.rmc.ui.widget.animation.AnimatorUtils;
import com.skydrm.rmc.ui.widget.avatar.AvatarView;
import com.skydrm.rmc.utils.commonUtils.AvatarUtil;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

/**
 * Created by hhu on 5/8/2017.
 */

public class ProfileSubviewLoader {
    private static final int REQUSE_CODE_LOGIN = 0xF001;
    private static final int ANIMATION_DURATION = 200;
    private static DevLog log = new DevLog(ProfileSubviewLoader.class.getSimpleName());
    private static final String HELP_URL = "help_users/index.html";
    private static final String GETTING_STARTED_URL = "https://help.skydrm.com/docs/android/start/1.0/en-us/index.htm";
    private static final String NEXTLABS_URL = "https://www.nextlabs.com/";
    private FrameLayout subviewContainer;
    private Context mContext;
    private SubviewType subviewType;
    private View mUserDetailView;
    private boolean isUserDetailViewLoaded;
    private boolean isUserHelpViewLoaded;
    private boolean bOnBackKeyHandling = false;
    private IUserDataPresenter presenter;
    private SwapView mSwapView;
    private TextView userName;
    private TextView userNameToolbar;
    private AvatarView avatarView;
    private PhotoSelectPopupWindow mSelectPopupWindow;
    private View mUserHelpView;
    private static final String TAG = "ProfileSubviewLoader";

    public ProfileSubviewLoader(Context context, SubviewType subviewType, FrameLayout container) {
        this.mContext = context;
        this.subviewType = subviewType;
        this.subviewContainer = container;
        presenter = new UserDataPresenterImpl(context);
        initSubview();
    }

    private void initSubview() {
        switch (subviewType) {
            case USER_DETAIL_VIEW:
                initDetailViewAndEvents();
                break;
            case LICENSE_VIEW:
                break;
            case HELP_VIEW:
                initHelpView(getHelpURL());
                break;
            case GETTING_START_VIEW:
                initHelpView(GETTING_STARTED_URL);
                break;
        }
    }

    private String getHelpURL() {
        String rmsURL = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryServerItem()
                .getRmsURL();
        if (rmsURL.endsWith("/")) {
            return rmsURL + HELP_URL;
        }
        return rmsURL + "/" + HELP_URL;
    }

    private void initHelpView(String loadUrl) {
        mUserHelpView = LayoutInflater.from(mContext).inflate(R.layout.layout_user_help_view, null);
        Toolbar toolbar = mUserHelpView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.icon_back_3);
        toolbar.setTitle(R.string.back);
        WebView webView = mUserHelpView.findViewById(R.id.webview);
        final ProgressBar loadingBar = mUserHelpView.findViewById(R.id.progressBar);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    if (null != loadingBar) {
                        loadingBar.setVisibility(View.GONE);
                    }
                } else {
                    loadingBar.setVisibility(View.VISIBLE);
                    loadingBar.setProgress(newProgress);
                }
            }
        });

        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setBuiltInZoomControls(true);
//        webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= 19) { // set hardware accelereate
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true); // load sharing link need this setting.
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);

        ViewGroup.LayoutParams vc = webView.getLayoutParams();
        vc.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        vc.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        webView.setLayoutParams(vc);

        if (mContext != null) {
            if (mContext instanceof AboutSkyDRMActivity) {
                webView.loadUrl(NEXTLABS_URL);
            } else {
                webView.loadUrl(loadUrl);
            }
        } else {
            webView.loadUrl(loadUrl);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserHelpView != null) {
                    dismissPage(mUserHelpView);
                }
            }
        });
    }

    private void initDetailViewAndEvents() {
        mUserDetailView = LayoutInflater.from(mContext).inflate(R.layout.layout_user_detail_view, null);
        mUserDetailView.findViewById(R.id.ib_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPage(mUserDetailView);
            }
        });
        userNameToolbar = mUserDetailView.findViewById(R.id.tv_user_name_toolbar);
        mUserDetailView.findViewById(R.id.tv_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(mContext);
            }
        });
        avatarView = mUserDetailView.findViewById(R.id.user_avatar_detail_page);
        mUserDetailView.findViewById(R.id.tv_select_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_ChangeAvatar();
            }
        });
        mUserDetailView.findViewById(R.id.rl_change_username_site).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeDisplayNameView();
            }
        });
        userName = mUserDetailView.findViewById(R.id.tv_user_name);
        TextView userEmail = mUserDetailView.findViewById(R.id.tv_user_email);
        // fill ui with data.
        presenter.setDisplayName(userNameToolbar);
        presenter.setUserAvatar(avatarView);
        presenter.setDisplayName(userName);
        presenter.setDisplayEmail(userEmail);
    }

    /**
     * dismiss current subview
     */
    private void dismissPage(View target) {
        if (target == null) {
            log.e("Error:target == null");
            return;
        }
        if (mSwapView == null) {
            mSwapView = new SwapView(target);
        }
        mSwapView.applyRotation(ANIMATION_DURATION, false);
    }

    /**
     * current user logout
     */
    public void logout(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.app_name)
                .setMessage(R.string.hint_msg_ask_log_out)
                .setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SkyDRMApp.getInstance().logout((Activity) context);
                    }
                })
                .setNegativeButton(R.string.common_cancel_initcap, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    /**
     * user select avatar site
     */
    private void onCMDUI_ChangeAvatar() {
        mSelectPopupWindow = new PhotoSelectPopupWindow(mContext, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_camera:
                        if (mContext != null) {
                            AvatarUtil.getInstance().takePhoto((Activity) mContext);
                        }
                        break;
                    case R.id.btn_photo:
                        if (mContext != null) {
                            AvatarUtil.getInstance().picPhoto((Activity) mContext);
                        }
                        break;
                }
            }
        });
        mSelectPopupWindow.showAtLocation(mUserDetailView, Gravity.BOTTOM, 0, 0);
    }

    /**
     * user change the current display name site
     */
    private void showChangeDisplayNameView() {
        ChangeUserNameDialogFragment changeUserNameDialogFragment = ChangeUserNameDialogFragment.newInstance();
        changeUserNameDialogFragment.show(((ProfileActivity) mContext).getSupportFragmentManager(), "username_fragment");
    }

    public void onNotify_NameChanged() {
        log.v("onNotify_NameChanged");
        try {
            presenter.setDisplayName(userName);
            presenter.setDisplayName(userNameToolbar);
        } catch (Exception e) {
            log.e(e);
        }
    }

    public void onNotify_AvatarChanged() {
        log.v("onNotify_AvatarChanged");
        try {
            presenter.setUserAvatar(avatarView);

            //
            //  as QA requried, change avater, close popWindow
            //
            mSelectPopupWindow.dismiss();
        } catch (Exception e) {
            log.e(e);
        }
    }

    /**
     * load current subview
     */
    void dispatchSubview() {
        subviewContainer.setVisibility(View.VISIBLE);
        subviewContainer.removeAllViews();
        switch (subviewType) {
            case USER_DETAIL_VIEW:
                loadView(mUserDetailView);
                break;
            case GETTING_START_VIEW:
                loadView(mUserHelpView);
                break;
            case LICENSE_VIEW:
                break;
            case HELP_VIEW:
                loadView(mUserHelpView);
                break;
            case ABOUT_VIEW:
                break;
        }
    }

    private void loadView(View view) {
        if (view != null) {
            subviewContainer.addView(view);
            mSwapView = new SwapView(view);
            mSwapView.applyRotation(ANIMATION_DURATION, true);
        }
    }

    boolean handleBackKey() {
        if (isUserDetailViewLoaded && !bOnBackKeyHandling) {
            mSwapView = new SwapView(mUserDetailView);
            mSwapView.applyRotation(ANIMATION_DURATION, false);
            bOnBackKeyHandling = true;
            return true;
        } else if (isUserHelpViewLoaded && !bOnBackKeyHandling) {
            mSwapView = new SwapView(mUserHelpView);
            mSwapView.applyRotation(ANIMATION_DURATION, false);
            bOnBackKeyHandling = true;
            return true;
        } else {
            return false;
        }
    }

    enum SubviewType {
        USER_DETAIL_VIEW,
        LICENSE_VIEW,
        HELP_VIEW,
        GETTING_START_VIEW,
        ABOUT_VIEW
    }

    private class SwapView implements Animator.AnimatorListener {
        private View mAnimateView;

        private SwapView(View animateView) {
            this.mAnimateView = animateView;
        }

        void applyRotation(int durationMillis, boolean bDisplayView) {
            if (bDisplayView) {
                mAnimateView.setVisibility(View.VISIBLE);
                AnimatorUtils.open(mAnimateView, durationMillis, new DecelerateInterpolator(), this);
            } else {
                AnimatorUtils.close(mAnimateView, durationMillis, new AccelerateInterpolator(), this);
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimateView.post(new Runnable() {
                @Override
                public void run() {
                    //user detail page
                    if (mUserDetailView != null) {
                        if (isUserDetailViewLoaded) {
                            subviewContainer.removeView(mUserDetailView);
                            subviewContainer.setClickable(false);
                            mUserDetailView = null;
                            isUserDetailViewLoaded = false;
                        } else {
                            mUserDetailView.setVisibility(View.VISIBLE);
                            subviewContainer.setClickable(true);
                            isUserDetailViewLoaded = true;
                        }
                    }
                    if (mUserHelpView != null) {
                        if (isUserHelpViewLoaded) {
                            subviewContainer.removeView(mUserHelpView);
                            subviewContainer.setClickable(false);
                            mUserHelpView = null;
                            isUserHelpViewLoaded = false;
                        } else {
                            mUserHelpView.setVisibility(View.VISIBLE);
                            subviewContainer.setClickable(false);
                            isUserHelpViewLoaded = true;
                        }
                    }
                }
            });
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
