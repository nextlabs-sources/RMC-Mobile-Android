package com.skydrm.rmc.ui.service.offline.downloader.config;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.sdk.Factory;
import com.skydrm.sdk.rms.user.IRmUser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static Map<String, String> getCommonHeader() throws InvalidRMClientException {
        IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
        return getCommonHeader(rmUser);
    }

    @NonNull
    private static Map<String, String> getCommonHeader(IRmUser rmUser) {
        Map<String, String> header = new HashMap<>();
        if (rmUser != null) {
            header.put("userId", rmUser.getUserIdStr());
            String ticket = rmUser.getTicket();
            if (!TextUtils.isEmpty(ticket)) {
                header.put("ticket", ticket);
            }
        }
        header.put("client_id", Factory.getClientId());
        header.put("clientId", Factory.getClientId());
        header.put("platformId", Factory.getDeviceType());
        header.put("deviceId", Factory.getClientId());
        try {
            header.put("client_id", URLEncoder.encode(Factory.getDeviceName(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return header;
    }
}
