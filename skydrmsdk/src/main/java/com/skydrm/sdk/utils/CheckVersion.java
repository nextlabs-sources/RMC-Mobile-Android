package com.skydrm.sdk.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by aning on 8/23/2017.
 */

public class CheckVersion {

    /**
     * @param url is the app page url that located in google play
     */
    public static String getLatestVersion(String url) {

        if (TextUtils.isEmpty(url)) {
            throw new RuntimeException("the url is null or empty");
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        String currentVersion = "";
        try {
            Response response = okHttpClient.newCall(request).execute();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            // now parse the current version from this page
            Pattern pattern = Pattern.compile("\"softwareVersion\"\\W*([\\d\\.]+)");
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    currentVersion = matcher.group(1);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return currentVersion;
    }
}
