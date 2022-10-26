package com.skydrm.sdk.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydrm.sdk.BuildConfig;
import com.skydrm.sdk.Factory;
import com.skydrm.sdk.R;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static com.skydrm.sdk.Factory.LOGIN_STATUS;
import static com.skydrm.sdk.Factory.RIGHTS_MANAGEMENT_ADDRESS;
import static com.skydrm.sdk.Factory.RIGHTS_MANAGEMENT_REGISTER_INTENT;
import static com.skydrm.sdk.Factory.RIGHTS_MANAGEMENT_TENANT;
import static com.skydrm.sdk.Factory.RIGHTS_MANAGEMENT_USER;

public class LoginActivity extends AppCompatActivity {
    private static final int MSG_LOGIN_ACTIVITY_LOGIN_OK = 0x001;
    final AjaxHandler ajaxHandle = new AjaxHandler(this, this);
    public WebView mWebView;
    private ProgressDialog mProgressDialog;
    private ProgressBar mProgressBar;
    //    private NxlTenant tenant = new NxlTenant();
    final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_LOGIN_ACTIVITY_LOGIN_OK) {
                onLoginSuccess();
                return true;
            }
            return false;
        }
    });
    private ImageView mRefreshIcon;
    private TextView mTextView;

    private boolean bIntentRegisterNewAccount = false;

    private DevLog log = new DevLog(LoginActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // init UI
        mRefreshIcon = (ImageView) findViewById(R.id.refresh);
        mRefreshIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryLogin();
            }
        });
        mTextView = (TextView) findViewById(R.id.loginEdit);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doEdit();
            }
        });
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mWebView = (WebView) findViewById(R.id.login_webView);
        // init config
        if (getIntent().getStringExtra(RIGHTS_MANAGEMENT_REGISTER_INTENT) != null) {
            log.i("LoginActivity:\nset as register mode");
            bIntentRegisterNewAccount = true;
            ((TextView) findViewById(R.id.SignIn)).setText(R.string.SignUp);
        } else {
            log.i("set as login mode");
            bIntentRegisterNewAccount = false;
            ((TextView) findViewById(R.id.SignIn)).setText(R.string.SignIn);
        }
//        // config tenant
//        String tenantStr = getIntent().getStringExtra(RIGHTS_MANAGEMENT_TENANT);
//        if (tenantStr == null) {
//            // use default
//            tenantStr = Factory.RM_TENANT_ID;
//        }
//        tenant.setTenantId(tenantStr);
//        trySPRestore();
//        log.i("tenant:" + tenant.getTenantId());

        initWebView();
        tryLogin();
    }

    @Override
    protected void onDestroy() {
        try {
            log.i("onDestroy, relase mWebView");
            super.onDestroy();
            if (mWebView != null) {
                // close browser and switch into main page
                mWebView.clearCache(false);
                mWebView.clearHistory();
                mWebView.loadUrl("about:blank");
                mWebView.destroy();
            }
        } catch (Exception e) {
            log.e(e);
        }
    }

    private Animation configRotateAnim(int repeatCount) {
        RotateAnimation animation = new RotateAnimation(
                0.0f, +359.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setRepeatCount(repeatCount);
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }

    private void trySPClear() {
        SharedPreferences sp = getSharedPreferences(LoginActivity.class.getName(), MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    @Override
    // Override onKeyDown(int keyCoder,KeyEvent event) of Activity.
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView != null && mWebView.canGoBack()) {
            // goBack() indicates back to previous page when user click other link address in one page.
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else {
        }
    }

    private void doEdit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View editView = LayoutInflater.from(this).inflate(R.layout.login_edit, null);
        final EditText et_tenantId = (EditText) editView.findViewById(R.id.ev_tenantId);
        et_tenantId.setText(R.string.default_tenant_name);
        builder.setCancelable(false);
        builder.setView(editView);
        builder.setPositiveButton(R.string.common_ok_uppercase, null);
        builder.setNegativeButton(R.string.Reset, null);
        builder.setTitle(R.string.app_name);
        builder.setMessage(getString(R.string.dialog_edit_msg));

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = et_tenantId.getText().toString();
                if (!input.isEmpty()) {
                    trySPClear();
                    tryLogin();
                    dialog.dismiss();
                }
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_tenantId.setText(R.string.default_tenant_name);
            }
        });

        final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        et_tenantId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    positiveButton.setEnabled(false);
                } else {
                    positiveButton.setEnabled(true);
                }
            }
        });
    }


    private void tryLogin() {
        if (bIntentRegisterNewAccount) {
            // for register
            mWebView.loadUrl(getRegisterURL());
        } else {
            fire_Login();
        }
    }

    private void fire_Login() {
        // for log-in
        try {
//            URL loginURL = new URL(tenant.getRmsAddress() + "/rs/tenant?tenant=" + tenant.getTenantId());
            URL loginURL = new URL(getLoginURL());
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            //
            // as RMS required, Each login must send phone's some unique ids as it's identity
            //
            cookieManager.setCookie(loginURL.toString(), "clientId=" + Factory.getClientId());
            cookieManager.setCookie(loginURL.toString(), "OsmondTest=" + Factory.getClientId());
            cookieManager.setCookie(loginURL.toString(), "platformId=" + Factory.getDeviceType());
            mWebView.loadUrl(loginURL.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private String getLoginURL() {
        return getServerURL() + "/login";
    }

    private String getRegisterURL() {
        return getServerURL() + "/register";
    }

    private String getServerURL() {
        return Factory.RM_SERVER;
    }

    private void initWebView() {

        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        {
            //
            //  in order to avoid Google Server bans imbedded WebView styles, use this section to trick it
            //
            // set User Agent -- for third party googleDrive login, use a fake user agent to request.
            String ua = "Mozilla/5.0 (Linux;Android 5.0.1;Nexus 6 Build/LRX22C)AppleWebKit/537.36(KHTML,like Gecko)Chrome/58.0.3029.83 Mobile Safari/537.36";
            mWebView.getSettings().setUserAgentString(ua);
        }
        {// set cache style
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            log.i("cache path:" + this.getExternalCacheDir().getAbsolutePath());
            mWebView.getSettings().setAppCachePath(this.getExternalCacheDir().getAbsolutePath());
            mWebView.getSettings().setAppCacheEnabled(true);
        }
        // Inject Ajax Listener, to intercept user login succeed event
        mWebView.addJavascriptInterface(ajaxHandle, "ajaxHandler");
        // clean cache cookies.
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();


        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                log.i("onLoadResource " + url);
                view.loadUrl("javascript:" +
                        "(function (open) {\n" +
                        "        XMLHttpRequest.prototype.open = function () {\n" +
                        "            this.addEventListener(\"readystatechange\", function () {\n" +
                        "                ajaxHandler.getAjaxResponse(this.responseText);\n" +
                        "            }, false);\n" +
                        "            open.apply(this, arguments);\n" +
                        "        };\n" +
                        "    }\n" +
                        ")\n" +
                        "(XMLHttpRequest.prototype.open);"
                );
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                log.i("shouldOverrideUrlLoading " + url);
                // fix a bug , if beginwieh mailto:,, use local intend to parse this
                // -----------"mailto:support@skydrm.com"
                if (url != null && url.toLowerCase().startsWith(MailTo.MAILTO_SCHEME)) {
                    log.i("intercept mailto:// scheme");
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse(url));
                    startActivity(emailIntent);
                    return true;
                }


                //
                // for account register/Login, we MUST NOT allow user back to Intro-page
                //
                if (url.contains("/rms/Intro".toLowerCase())) {
                    log.v("for account register/Login, we MUST NOT allow user back to Intro-page");
                    finish();
                    return true;
                }

                //
                // for account register, we MUST NOT allow user into Login-page
                //
//                if (bIntentRegisterNewAccount && url.contains("/rms/Login".toLowerCase())) {
//                    log.v("for account register, we MUST NOT allow user into Login-page");
//                    finish();
//                    return true;
//                }

                //
                // for account login, we MUST NOT allow user into Register
                //
//                if (!bIntentRegisterNewAccount && url.contains("/rms/Register".toLowerCase())) {
//                    log.v("for account login, we MUST NOT allow user into Register");
//                    finish();
//                    return true;
//                }


                // default http,https
                view.loadUrl(url);
                return false;
            }

            /*
             * special here to cache relevant res in LoginPage, some file may not stable (9.1.0152)
             *   :===  login.min.css login.min.js font/fira css/style
             *  for every new release, must check here
             *
             * if android-version above 21 , use the new shouldInterceptRequest
             *
             */
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                log.i("shouldInterceptRequest " + url);
                try {
                    // for woff section
                    if (url.endsWith("/rms/ui/css/font/woff/FiraSans-Regular.woff")) { // check
                        log.i("match");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/css/font/woff/FiraSans-Regular.woff");
                        return new WebResourceResponse("application/x-font-woff", "UTF-8", is);

                    } else if (url.endsWith("/rms/ui/css/font/woff2/FiraSans-Regular.woff2")) {
                        log.i("match");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/css/font/woff2/FiraSans-Regular.woff2");
                        return new WebResourceResponse("application/x-font-woff2", "UTF-8", is);
                    } else if (url.endsWith("/rms/ui/css/font/woff/FiraSans-Medium.woff")) { // check
                        log.i("match");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/css/font/woff/FiraSans-Medium.woff");
                        return new WebResourceResponse("application/x-font-woff", "UTF-8", is);

                    } else if (url.endsWith("/rms/ui/css/font/woff2/FiraSans-Medium.woff2")) { // check
                        log.i("match");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/css/font/woff2/FiraSans-Medium.woff2");
                        return new WebResourceResponse("application/x-font-woff2", "UTF-8", is);

                    } else if (url.endsWith("/rms/ui/css/font/woff/FiraSans-SemiBold.woff")) { // check
                        log.i("match");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/css/font/woff/FiraSans-SemiBold.woff");
                        return new WebResourceResponse("application/x-font-woff", "UTF-8", is);

                    } else if (url.endsWith("/rms/ui/css/font/woff/FiraSans-Bold.woff")) {  // check
                        log.i("match");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/css/font/woff/FiraSans-Bold.woff");
                        return new WebResourceResponse("application/x-font-woff", "UTF-8", is);
                    }
                    // for pic section
                    else if (url.endsWith("/rms/ui/img/loading-icon.gif")) { // check
                        log.i("match");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/img/loading-icon.gif");
                        return new WebResourceResponse("image/gif", "UTF-8", is);
                    } else if (url.endsWith("/rms/tenants/skydrm.com/images/login-screen-graphics.png")) { // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/tenants/login-screen-graphics.png");
                        return new WebResourceResponse("image/png", "UTF-8", is);

                    } else if (url.endsWith("/rms/ui/img/favicon.ico")) { // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/img/favicon.ico");
                        return new WebResourceResponse("image/x-icon", "UTF-8", is);
                    }
                    // for svg section
                    else if (url.endsWith("/rms/tenants/skydrm.com/images/logo-black.svg")) { // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/tenants/logo-black.svg");
                        return new WebResourceResponse("image/svg+xml", "UTF-8", is);
                    } else if (url.endsWith("/rms/ui/img/rms-logo-with-text.svg")) { // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/img/rms-logo-with-text.svg");
                        return new WebResourceResponse("image/svg+xml", "UTF-8", is);
                    } else if (url.endsWith("/rms/ui/img/GoogleSignIn_P.svg")) { // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/img/GoogleSignIn_P.svg");
                        return new WebResourceResponse("image/svg+xml", "UTF-8", is);
                    } else if (url.endsWith("/rms/ui/img/FacebookSignIn_P.svg")) { // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/img/FacebookSignIn_P.svg");
                        return new WebResourceResponse("image/svg+xml", "UTF-8", is);
                    } else if (url.endsWith("/rms/ui/img/Refresh_P.svg")) {  // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/img/Refresh_P.svg");
                        return new WebResourceResponse("image/svg+xml", "UTF-8", is);
                    }
                    // for json secton
                    else if (url.endsWith("/rms/ui/app/i18n/en.json?v=10.7.0107")) {
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/app/i18n/en.json");
                        return new WebResourceResponse("application/json", "UTF-8", is);
                    }
                    // for css section
                    else if (url.endsWith("/rms/ui/css/login.min.css?v=10.7.0107")) {  // check2222222
                        log.i("match");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/css/login.min.css");
                        return new WebResourceResponse("text/css", "UTF-8", is);

                    } else if (url.endsWith("/rms/ui/css/font/fira.css?v=10.7.0107")) {
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/css/font/fira.css");
                        return new WebResourceResponse("text/css", "UTF-8", is);

                    } else if (url.endsWith("/rms/tenants/skydrm.com/css/style.css?v=10.7.0107")) { // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/tenants/style.css");
                        return new WebResourceResponse("text/css", "UTF-8", is);

                    }
                    // for js section
                    else if (url.endsWith("/rms/ui/app/login.min.js?v=10.7.0107")) { // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/app/login.min.js");
                        return new WebResourceResponse("application/x-javascript", "UTF-8", is);

                    } else if (url.endsWith("/rms/ui/lib/3rdParty/core-min.js")) { // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/lib/3rdParty/core-min.js");
                        return new WebResourceResponse("application/x-javascript", "UTF-8", is);
                    } else if (url.endsWith("/rms/ui/lib/3rdParty/md5-min.js")) {  // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/lib/3rdParty/md5-min.js");
                        return new WebResourceResponse("application/x-javascript", "UTF-8", is);
                    } else if (url.endsWith("/rms/ui/lib/bootstrap/3.3.5/js/bootstrap.min.js")) {  // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/lib/bootstrap/3.3.5/js/bootstrap.min.js");
                        return new WebResourceResponse("application/x-javascript", "UTF-8", is);
                    }
                    // for jquery section
                    else if (url.endsWith("/rms/ui/lib/jquery/jquery-1.10.2.min.js")) {  // check
                        log.i("match ");
                        InputStream is = LoginActivity.this.getAssets().open("rms/ui/lib/jquery/jquery-1.10.2.min.js");
                        return new WebResourceResponse("application/x-javascript", "UTF-8", is);
                    }
                } catch (Exception e) {
                    log.e(e);
                }
                return null;
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                log.i("onPageStarted " + url);
                //
                // wait for new requirement,
                //
                if (url != null) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    String CookieStr = cookieManager.getCookie(url);
                    // set cookie for each page
                    //
                    // as RMS required, Each login must send phone's some unique ids as it's identity
                    //
                    cookieManager.setCookie(url, "clientId=" + Factory.getClientId());
                    cookieManager.setCookie(url, "OsmondTest=" + Factory.getClientId());
                    cookieManager.setCookie(url, "platformId=" + Factory.getDeviceType());
                }


                //
                // check if had get the user-string
                //
                if (ajaxHandle.bLoginSuccess) {
                    handler.sendEmptyMessage(MSG_LOGIN_ACTIVITY_LOGIN_OK);
                }


                mRefreshIcon.startAnimation(configRotateAnim(1));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                log.i("onPageFinished " + url);
                try {
                    mRefreshIcon.clearAnimation();
                    //
                    //  add feature for rms Login.jsp  path contain : /rms/login
                    //    inject js code to listen ajax result
                    //
                    if (url != null && url.toLowerCase().contains("/rms/login")) {
                        view.loadUrl("javascript:" +
                                "if(typeof $ != \"undefined\"){" +
                                "$(document).ajaxSuccess(function( event, xhr, settings ) {" +
                                "try{" +
                                "ajaxHandler.getAjaxResponse(xhr.responseText); " +
                                "}catch(e){}" +
                                "});" +
                                "}"
                        );
                    }


                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();

                    //
                    // check if had get the user-string
                    //
                    if (ajaxHandle.bLoginSuccess) {
                        handler.sendEmptyMessage(MSG_LOGIN_ACTIVITY_LOGIN_OK);
                    }


                } catch (Exception e) {
                    log.e(e);
                }
            }

            //
            //  By Osmond,
            //  For Google Play, it denied you just call handler.proceed() in onReceivedSslError()
            //
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                log.e("onReceivedSslError");
                // Ignore SSL certificate errors (that is the temprory method to handle this situation)
                // in normal case just  handler.cancel();
                if (BuildConfig.DEBUG) {
                    handler.proceed();
                } else {
                    handler.cancel();
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                    mRefreshIcon.startAnimation(configRotateAnim(1));
                }
            }
        });
    }

    private void onLoginSuccess() {
        mWebView.loadUrl("about:blank");

        String userStr = ajaxHandle.getRmUser();
        // put new generated class object to caller
        Intent resultIntent = new Intent();
        resultIntent.putExtra(LOGIN_STATUS, true);
        resultIntent.putExtra(RIGHTS_MANAGEMENT_USER, userStr);
        resultIntent.putExtra(RIGHTS_MANAGEMENT_TENANT, getCurrentUsrDefaultTenant(userStr));
        resultIntent.putExtra(RIGHTS_MANAGEMENT_ADDRESS, getServerURL());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private String getCurrentUsrDefaultTenant(String usrStr) {
        try {
            if (TextUtils.isEmpty(usrStr)) {
                return null;
            }
            JSONObject resultObj = new JSONObject(usrStr);
            JSONObject extraObj = resultObj.optJSONObject("extra");
            return extraObj.optString("defaultTenant");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    // our JavascriptInterface
    class AjaxHandler {

        private final Context context;
        public boolean bLoginSuccess = false;
        private LoginActivity loginActivity;
        private String rmUserStr;

        public AjaxHandler(Context context, LoginActivity activity) {
            this.context = context;
            this.loginActivity = activity;
        }


        /**
         * Be called by Javascript code from WebPage
         */
        @JavascriptInterface
        public void getAjaxResponse(String result) {
            if (result == null || result.isEmpty()) {
                return;
            }
            if (bLoginSuccess) {
                return;
            }
            // add by osmond,  json must begin with {
            if (!result.startsWith("{")) {
                return;
            }
            try {
                log.i("AjaxResponse: " + result);
                JSONObject jsonObject = new JSONObject(result);
                if (!jsonObject.has("statusCode")) {
                    return;
                }
                if (jsonObject.getInt("statusCode") == 200 && TextUtils.equals(jsonObject.getString("message"), "Authorized")) {
                    bLoginSuccess = true;
                    rmUserStr = result;
                }
            } catch (JSONException e) {
                log.e(e);
            }

        }

        public String getRmUser() {
            return rmUserStr;
        }
    }
}
