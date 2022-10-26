package com.skydrm.rmc.reposystem.remoterepo.onedrive2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;

import java.util.Comparator;
import java.util.Locale;


/**
 * Only For Onedrive OAuth2.0
 * reference: https://msdn.microsoft.com/EN-US/library/dn659752.aspx
 */
public class OAuth2Activity extends Activity {
    private static String clientId = "8ea12ff6-4f3f-4f4d-a727-d02e2be5e15e";
    private static String redirectUri = "https://login.live.com/oauth20_desktop.srf";
    private static String scope = "wl.signin wl.basic wl.emails wl.offline_access wl.skydrive_update wl.skydrive wl.contacts_create";
    private static String LastAuthCode = null;
    DevLog log = new DevLog(OAuth2Activity.class.getSimpleName());
    Uri oAuthDesktopUri = Uri.parse("https://login.live.com/oauth20_desktop.srf");
    private WebView webView;
    private ProgressBar progressBar;


    static public void startOAuth2Authentication(Activity context) {
        Intent intent = new Intent(context, OAuth2Activity.class);
        context.startActivityForResult(intent, 0);
    }

    public static String getClientId() {
        return clientId;
    }

    public static String getRedirectUri() {
        return redirectUri;
    }

    public static String getLastAuthCode() {
        return LastAuthCode;
    }

    public static String getScope() {
        return scope;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        LastAuthCode = null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onedrive_oauth2);
        webView = (WebView) findViewById(R.id.webview);
        progressBar = (ProgressBar) findViewById(com.skydrm.sdk.R.id.progressBar);
        // config webView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDisplayZoomControls(false);
        // remove all cookies
        CookieManager manager = CookieManager.getInstance();
        manager.removeAllCookie();


        webView.setWebViewClient(getWebViewClient());
        // attach a progress bar fot web view to load res
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
        });

        // config OneDrive OAuth2
        Uri oauth = Uri.parse("https://login.live.com/oauth20_authorize.srf")
                .buildUpon()
                .appendQueryParameter("client_id", clientId)
                .appendQueryParameter("scope", scope)
                .appendQueryParameter("display", "android_phone")
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("locale", Locale.getDefault().toString())
                .appendQueryParameter("redirect_uri", redirectUri)
                .build();

        webView.loadUrl(oauth.toString());
    }

    private WebViewClient getWebViewClient() {
        return new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                log.i("onLoadResource " + url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                log.i("shouldOverrideUrlLoading " + url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                log.i("shouldInterceptRequest " + url);
                return super.shouldInterceptRequest(view, url);
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                log.i("onPageStarted " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                log.i("onPageFinished " + url);
                Uri uri = Uri.parse(url);
                // this is the only entrance-place to judge whether
                // we got ok or not
                // as for document, https://login.live.com/oauth20_desktop.srf
                Uri endUri = oAuthDesktopUri;
                boolean isEndUri = UriComparator.INSTANCE.compare(uri, endUri) == 0;
                if (!isEndUri) {
                    return;
                }
                OAuth2Activity.this.onEndUri(uri);
            }
        };
    }

    private void onEndUri(Uri endUri) {
        log.i("onEndUri" + endUri.toString());
        // on auth ok:  https://login.live.com/oauth20_desktop.srf?code=M2aa0ed32-cc13-443e-3a61-3e9c96a2b869&lc=1033
        // on auth failed: https://login.live.com/oauth20_desktop.srf?error=access_denied&error_description=The%20user%20has%20denied%20access%20to%20the%20scope%20requested%20by%20the%20client%20application.&lc=1033
        if (endUri.getQuery() != null) {
            String code = endUri.getQueryParameter("code");
            if (code != null) {
                log.i("onEndUri " + "code=" + code);
                LastAuthCode = code;
                Intent outIntent = getIntent();
                outIntent.putExtra("AuthorizationCode", LastAuthCode);
                outIntent.putExtra("Repo","OneDrive");
                setResult(RESULT_OK, outIntent);
                finish();
                return;
            }
            String error = endUri.getQueryParameter("error");
            if (error != null) {
                log.i("onEndUri " + "error=" + error);
                String errorDescription = endUri.getQueryParameter("error_description");
                if (errorDescription != null) {
                    log.i("onEndUri " + "errorDescription=" + errorDescription);
                }
                setResult(RESULT_CANCELED, null);
                finish();
                return;
            }
        }
    }

    private enum UriComparator implements Comparator<Uri> {
        INSTANCE;

        @Override
        public int compare(Uri lhs, Uri rhs) {
            String[] lhsParts = {lhs.getScheme(), lhs.getAuthority(), lhs.getPath()};
            String[] rhsParts = {rhs.getScheme(), rhs.getAuthority(), rhs.getPath()};

            for (int i = 0; i < lhsParts.length; i++) {
                int compare = lhsParts[i].compareTo(rhsParts[i]);
                if (compare != 0) {
                    return compare;
                }
            }

            return 0;
        }
    }

}
