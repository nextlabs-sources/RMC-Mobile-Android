package com.skydrm.rmc.reposystem.remoterepo.googledrive2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.skydrm.rmc.DevLog;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Locale;

public class GoogleOAuth2 extends Activity {
    public static final String sClientID = "1021466473229-gfuljuu4spgkvs4vnk6hl48ah1rcpfre.apps.googleusercontent.com";
    public static final String sRedirect_URL = "com.googleusercontent.apps.1021466473229-gfuljuu4spgkvs4vnk6hl48ah1rcpfre";

    private static final DevLog log = new DevLog(GoogleOAuth2.class.getSimpleName());

    private static String LastAuthCode = null;

    String mAuthorizationCode = null;
    boolean isBeginOAuth = false;

    static public void startOAuth2Authentication(Activity context) {
        Intent intent = new Intent(context, GoogleOAuth2.class);
        context.startActivityForResult(intent, 0);
    }

    static public String getLastAuthCode() {
        return LastAuthCode;
    }


    static String buildUri(String host, String path) throws URISyntaxException {
        return new URI("https", host, path, null).toASCIIString();
    }

    static String encodeUrlParam(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8");
    }

    static String encodeUrlParams(/*@Nullable*/String userLocale,
                                          /*@Nullable*/String/*@Nullable*/[] params) throws UnsupportedEncodingException {
        StringBuilder buf = new StringBuilder();
        String sep = "";
        if (userLocale != null) {
            buf.append("locale=").append(userLocale);
            sep = "&";
        }

        if (params != null) {
            if (params.length % 2 != 0) {
                throw new IllegalArgumentException("'params.length' is " + params.length + "; expecting a multiple of two");
            }
            for (int i = 0; i < params.length; ) {
                String key = params[i];
                String value = params[i + 1];
                if (key == null) throw new IllegalArgumentException("params[" + i + "] is null");
                if (value != null) {
                    buf.append(sep);
                    sep = "&";
                    buf.append(encodeUrlParam(key));
                    buf.append("=");
                    buf.append(encodeUrlParam(value));
                }
                i += 2;
            }
        }

        return buf.toString();
    }

    static String buildUrlWithParams(/*@Nullable*/String userLocale,
                                     String host,
                                     String path,
                                            /*@Nullable*/String/*@Nullable*/[] params) throws UnsupportedEncodingException, URISyntaxException {
        return buildUri(host, path) + "?" + encodeUrlParams(userLocale, params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBeginOAuth) {
            Intent intent = getIntent();
            if (intent != null) {
                log.v("onResume--" + intent.toString());
                if (parseOAuthIntent(intent) && mAuthorizationCode != null) {
                    Intent outIntent= getIntent();
                    outIntent.putExtra("AuthorizationCode", mAuthorizationCode);
                    outIntent.putExtra("Repo","GoogleDrive");
                    setResult(RESULT_OK, outIntent);
                }
                finish();

            }
        } else {
            onConnectOAuth2();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            log.v("onNewIntent,intent---" + intent.toString());
            if (parseOAuthIntent(intent) && mAuthorizationCode != null) {
                Intent outIntent= getIntent();
                outIntent.putExtra("AuthorizationCode", mAuthorizationCode);
                outIntent.putExtra("Repo","GoogleDrive");
                setResult(RESULT_OK, outIntent);
            }
            finish();

        }
    }

    private boolean parseOAuthIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        log.v("parseOAuthIntent---" + intent.toString());
        Uri uri = intent.getData();
        if (uri == null) {
            return false;
        }
        log.v("parseOAuthIntent---" + uri.toString());
        String scheme = uri.getScheme();
        if (scheme == null) {
            return false;
        }
        if (!sRedirect_URL.equals(scheme)) {
            return false;
        }
        String part = uri.getSchemeSpecificPart();
        if (part == null) {
            return false;
        }
        // exctract access token
        // i.e.   //?state=oauth2:156da5e9576ad692a101a15e2ce53d57&code=4/a1pGXv5C0wCxHOXE0bSwkUvW-4Hd_Ujvlc9JnBZzySw
        int codeIndex = part.indexOf("code=");
        if (codeIndex == -1) {
            return false;
        }
        String code = part.substring(codeIndex + "code=".length());
        if (code == null) {
            return false;
        }
        if (code.isEmpty()) {
            return false;
        }
        log.v("parseOAuthIntent---retrived the token:" + code);
        mAuthorizationCode = code;
        LastAuthCode = mAuthorizationCode;
        return true;
    }

    private void onConnectOAuth2() {
        try {
            String host = "accounts.google.com";
            String path = "/o/oauth2/v2/auth";

            Locale locale = Locale.getDefault();

            String[] params = {
                    "scope", "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/drive",
                    "response_type", "code",
                    "redirect_uri", sRedirect_URL + "://",
                    "client_id", sClientID,
                    "state", "oauth2:156da5e9576ad692a101a15e2ce53d57"};

            String url = buildUrlWithParams(locale.toString(), host, path, params);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            isBeginOAuth = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
