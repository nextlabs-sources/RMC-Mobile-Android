package com.skydrm.rmc.ui.activity.server;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.Factory;
import com.skydrm.sdk.exception.RmsRestAPIException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerConfig {

    public static void configServer(final String serverURL, final Callback callback) {
        class ServerConfigTask extends AsyncTask<Void, Void, String> {
            private RmsRestAPIException mRmsException;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (callback != null) {
                    callback.onInvoking();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                ServerConfigApi api = new ServerConfigApi();
                try {
                    return api.configServer(serverURL);
                } catch (RmsRestAPIException e) {
                    mRmsException = e;
                }
                return "";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (!TextUtils.isEmpty(result)) {
                    if (callback != null) {
                        callback.onInvoked(result);
                    }
                } else {
                    if (callback != null) {
                        callback.onFailed(mRmsException == null ? "Failed to get server url." : mRmsException.getMessage());
                    }
                }
            }
        }
        new ServerConfigTask().executeOnExecutor(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
    }

    private static class ServerConfigApi {
        String configServer(String serverURL) throws RmsRestAPIException {
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .readTimeout(Config.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(Config.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(Config.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .build();

            try {
                if (Factory.ignoreSSLCert) {
                    httpClient = buildWithIgnoreSSL();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Request.Builder builder = new Request.Builder();
            Request request = builder
                    .url(serverURL.concat("/router/rs/q/defaultTenant"))
                    .get()
                    .build();
            String responseString;
            try {
                Response response = httpClient.newCall(request).execute();
                responseString = response.body().string();
            } catch (IOException e) {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
            }
            // parse result
            try {
                // {"statusCode":200,"message":"OK","results":{"server":"https://rmtest.nextlabs.solutions/rms"}}
                JSONObject j = new JSONObject(responseString);
                if (!j.has("statusCode")) {
                    throw new RmsRestAPIException("Failed parse response.", RmsRestAPIException.ExceptionDomain.Common);
                }
                int code = j.getInt("statusCode");
                if (code == 200) {
                    if (j.has("results")) {
                        JSONObject results = j.getJSONObject("results");
                        if (results.has("server") && !results.isNull("server")) {
                            return results.getString("server");
                        }
                    }
                } else {
                    throw new RmsRestAPIException(j.has("message") ? j.getString("message") : "failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
                }
                // should never reach here
                throw new RmsRestAPIException("Failed parse response.", RmsRestAPIException.ExceptionDomain.Common);
            } catch (JSONException e) {
                throw new RmsRestAPIException("Failed parse response.", RmsRestAPIException.ExceptionDomain.Common);
            }
        }

        OkHttpClient buildWithIgnoreSSL() throws Exception {
//            //for debug only, ignore ssl certificate check
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null,
                    new TrustManager[]{new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            // Not Impl
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            // Not Impl
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }},
                    new java.security.SecureRandom());
//            HttpsUtils.SSLParams params = HttpsUtils.getSslSocketFactory(new InputStream[]{new Buffer().writeUtf8(CERT).inputStream()}, null, null);
            OkHttpClient client = new OkHttpClient.Builder()
//                    .sslSocketFactory(params.sSLSocketFactory, params.trustManager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .sslSocketFactory(sc.getSocketFactory(),
                            new X509TrustManager() {
                                @Override
                                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                                }

                                @Override
                                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                                }

                                @Override
                                public X509Certificate[] getAcceptedIssuers() {
                                    return new X509Certificate[0];
                                }
                            })
                    .readTimeout(Config.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(Config.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(Config.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            return client;
        }
    }

    public interface Callback {
        void onInvoking();

        void onInvoked(String serverURL);

        void onFailed(String msg);
    }
}
