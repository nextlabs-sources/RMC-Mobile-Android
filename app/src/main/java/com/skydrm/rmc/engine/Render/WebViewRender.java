package com.skydrm.rmc.engine.Render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.skydrm.rmc.BuildConfig;
import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.utils.commonUtils.AvatarUtil;
import com.skydrm.sdk.rms.types.RemoteViewResult2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by aning on 1/10/2017.
 * <p>
 * used webView control to render the following file: text, image,
 * load converted Office & PDF viewerUrl and sharing link Url
 */

public class WebViewRender implements IFileRender {
    private static final DevLog log = new DevLog(WebViewRender.class.getSimpleName());
    private Context mContext;
    private RelativeLayout mMainLayout;
    private View mLayout;
    private File mFile;
    private WebView mWebView;
    // Office & pdf
    private List<String> mCookies;
    private String mViewerUrl;
    private ProgressBar mProgressBar;

    // load sharing link url
    public WebViewRender(Context context, RelativeLayout mainLayout) {
        mContext = context;
        mMainLayout = mainLayout;
        initLayout();
        initWebView();
    }

    // load text and image
    WebViewRender(Context context, RelativeLayout contentLayout, File file) {
        mContext = context;
        mMainLayout = contentLayout;
        mFile = file;
        initLayout();
        initWebView();
    }

    // load converted office & pdf url
    WebViewRender(Context context, RelativeLayout mainLayout, RemoteViewResult2.ResultsBean result) {
        mContext = context;
        mMainLayout = mainLayout;
        mCookies = result.getCookies();
        mViewerUrl = result.getViewerURL();

        initLayout();
        initWebView();
    }

    private void initLayout() {
        mLayout = LayoutInflater.from(mContext).inflate(R.layout.view_normal_file, null);
        mWebView = (WebView) mLayout.findViewById(R.id.normalView);
        mProgressBar = (ProgressBar) mLayout.findViewById(R.id.webview_load_progress);
        initLoadProgress();
    }

    private void initWebView() {
        mWebView.setBackgroundColor(Color.WHITE);
        // become big when double click, then become small double click again
        mWebView.getSettings().setUseWideViewPort(true);
        // support adaptive screen
        mWebView.getSettings().setLoadWithOverviewMode(true);
        // zoom by touch screen
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setBuiltInZoomControls(true);
        // enable js and support H5
        mWebView.getSettings().setJavaScriptEnabled(true);
        // set cache
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // start Dom storage API
        mWebView.getSettings().setDomStorageEnabled(true);
        // improve the render level
        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT >= 19) { // set hardware accelereate
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // mWebView.getSettings().setSupportMultipleWindows(true); // -- this must collaborate with onCreateWindow of  WebChromeClient.
        // support open new window by js (and disable pop up blocker)
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true); // load sharing link need this setting.
        // not be sure is needed for following settings.
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setAllowFileAccess(true);

        initLoadProgress();
        mMainLayout.addView(mLayout);
        ViewGroup.LayoutParams vc = mWebView.getLayoutParams();
        vc.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        vc.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        mWebView.setLayoutParams(vc);
        // listen the page when loading finish and if loading error.
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                log.e("onPageStarted:url " + url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                log.e("shouldOverrideUrlLoading:url " + url);
                //view.loadUrl(url);
                // as RMS required, when goto https://viewer.skydrm.com/viewer/help/Document_Viewer.htm
                // ues System browser to render it
                if (url.toLowerCase().contains("/help/Document_Viewer".toLowerCase())) {
                    log.v("filter out https://viewer.skydrm.com/viewer/help/Document_Viewer.htm");
                    return true;
                }
                return false;
            }

            //
            //  By Osmond,
            //  For Google Play, it denied you just call handler.proceed() in onReceivedSslError()
            //
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                log.e("onReceivedSslError");
                if (BuildConfig.DEBUG) {
                    handler.proceed();
                } else {
                    handler.cancel();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                log.e("onPageFinished:url " + url);
                super.onPageFinished(view, url);
            }
        });
    }

    private void initLoadProgress() {

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress == 100) {
                    try {
                        //Here cannot invoke new Handler.postDelayed(r,t) or webview.postDelayed(r,t) , put the following code inside r;
                        //because on samsung(SM-T715Y series) tablet will happen cash issues caused by java.lang.IllegalStateException: Unable to create layer for WebView
                        //the reason why issue above happened is that set parameter t non zero.[why we need set t is to wait the webview draw the complete layers then we can capture it to get its thumbnail]
                        //currently doesn't have a good solution to fix this issue we just give up using the way above[capture issue may happen].
                        if (mIWebViewLoadCallback != null) {
                            mIWebViewLoadCallback.onPageFinished(captureWebView(mWebView));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }
        });

    }

    /**
     * Client needs to set the cookie in cookie field under the domain received in viewerURL (such as: rmtest.nextlabs.solutions)
     * before trying to launch the webview. If this cookie is not set, file viewing will not be successful
     * <p>
     * /**
     * viewerURL : https://rmtest.nextlabs.solutions/viewer/DocViewer.jsp?documentid=149008594541046738476-6edf-4e11-9bde-d310ea1d328a&u=956880333@qq.com&t=fe7e52a7-4133-4288-bb66-16bf70d98a10&hideOperations=true
     * cookies : ["viewingSessionId=0f2e1198-817e-4c8c-9ea7-e54dfaf8ea99; Path=/viewer","JSESSIONID_V=8EAE1F81D922F0945F178C1914D553D7; Path=/viewer/; Secure; HttpOnly"]
     */
    private void setCookie() {
        CookieSyncManager.createInstance(mContext);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        // cookieManager.removeAllCookies();

        try {
            URL url = new URL(mViewerUrl);
            // set domain property for cookie
            String domain = url.getHost(); // rmtest.nextlabs.solutions
            // now set all cookies that returns from rms API, actually, only set "viewingSessionId" cookie, can view file
            for (String cookie : mCookies) {
                cookie += "; Domain=";
                cookie += domain;
                cookieManager.setCookie(mViewerUrl, cookie);
            }
            CookieSyncManager.getInstance().sync();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void fileRender() {
        if (mFile != null) { // load text and image
            Uri uri = Uri.fromFile(mFile);
            String type = RenderHelper.parseMimeType(uri.toString());
            if (type != null) {
                if (type.startsWith("image/")) {
                    loadImage(uri, type);
                } else if (type.startsWith("text/")) {
                    loadText(uri, type);
                }
            } else { // type is null
                String extension = RenderHelper.getFileExtension(uri.toString());
                if (TextUtils.isEmpty(extension)) {
                    return;
                }

                if (
                        extension.equalsIgnoreCase("log")
                                || extension.equalsIgnoreCase("py")
                                || extension.equalsIgnoreCase("md")
                                || extension.equalsIgnoreCase("m")
                                || extension.equalsIgnoreCase("swift")
                                || extension.equalsIgnoreCase("err")
                                || extension.equalsIgnoreCase("sql")
                                || extension.equalsIgnoreCase("vb")
                                || extension.equalsIgnoreCase("json")) {
                    loadText(uri, null);
                }
            }
        } else { // load converted Office & pdf url
            setCookie(); // need test using a different server url.
            mWebView.loadUrl(mViewerUrl);
        }
    }

    public WebView getmWebView() {
        return mWebView;
    }

    // load sharing link
    public void loadSharingLink(Uri uri) {
        mWebView.loadUrl(uri.toString());
        log.e("loadUri:  " + uri.toString());
    }

    private void loadImage(Uri uri, String type) {
//        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mMainLayout.getLayoutParams();
//        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
//        mMainLayout.addView(mLayout, lp);

//        mMainLayout.addView(mLayout);

        mWebView.setBackgroundColor(0);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setBuiltInZoomControls(true);


        if (type.startsWith("image/gif")) {
            String gifFilePath = "file://" + mFile.getPath();
            String data = "<HTML><Div align=\"center\"  margin=\"0px\"><IMG src=\"" + gifFilePath + "\" margin=\"0px\"/></Div>";
            mWebView.loadDataWithBaseURL(gifFilePath, data, "text/html", "utf-8", null);
        } else if (type.startsWith("image/x-ms-bmp")) {
            // Desired Bitmap and the html code, where you want to place it
            Bitmap bitmap = BitmapFactory.decodeFile(mFile.getPath());
            String html = "<html><body><Div align=\"center\"  margin=\"0px\"><img src='{IMAGE_PLACEHOLDER}' +\"\" margin=\"0px\" /></Div></body></html>";
            // Convert bitmap to Base64 encoded image for webView.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            String image = "data:image/png;base64," + imageBase64;
            // Use image for the img src parameter in your html and load to webView
            html = html.replace("{IMAGE_PLACEHOLDER}", image);
            String bmpFilePath = "file://" + mFile.getPath();
            mWebView.loadDataWithBaseURL(bmpFilePath, html, "text/html", "utf-8", "");
        } else {
            mWebView.loadUrl(uri.toString());
        }
    }


    private void loadText(Uri uri, String type) {
//        mMainLayout.addView(mLayout);
        mWebView.setBackgroundColor(Color.WHITE);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        ViewGroup.LayoutParams vc = mWebView.getLayoutParams();
        vc.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        vc.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        mWebView.setLayoutParams(vc);
        if (type != null)
            mWebView.loadDataWithBaseURL(null, RenderHelper.readTextFile(uri, true), type, "utf-8", null);
        else // other such as .js, .log and so on.
            mWebView.loadDataWithBaseURL(null, RenderHelper.readTextFile(uri, false), null, "utf-8", null);
    }

    private Bitmap captureWebView(final WebView webView) {
//        webView.measure(View.MeasureSpec.makeMeasureSpec(
//                View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        webView.measure(0, 0);
        webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();
        try {
            Bitmap bitmap = Bitmap.createBitmap(500,
                    800, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            webView.draw(canvas);
            return AvatarUtil.getInstance().getSmallBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            webView.setDrawingCacheEnabled(false);
            webView.destroyDrawingCache();
        }
        return null;
    }

    public interface IWebViewLoadCallback {
        void onPageFinished(Bitmap bitmap);
    }

    private IWebViewLoadCallback mIWebViewLoadCallback;

    public void setOnWebViewLoadListener(IWebViewLoadCallback callback) {
        this.mIWebViewLoadCallback = callback;
    }
}
