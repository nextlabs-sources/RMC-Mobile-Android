package com.skydrm.rmc.ui.service.offline.downloader.core;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.service.offline.downloader.config.Config;
import com.skydrm.rmc.ui.service.offline.downloader.config.DefaultConfiguration;
import com.skydrm.rmc.ui.service.offline.downloader.config.DownloadInfo;
import com.skydrm.rmc.ui.service.offline.downloader.config.ThreadInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;

class SingleDownloadTask extends DownloadTask {

    SingleDownloadTask(DownloadInfo downloadInfo, ThreadInfo threadInfo, IDownloadListener listener) {
        super(downloadInfo, threadInfo, listener);
    }

    @Override
    protected OkHttpClient buildHttpClient() {
        return DefaultConfiguration.getHttpClient();
    }

    @Override
    protected Map<String, String> getRequestBody(DownloadInfo info) throws JSONException {
        Map<String, String> body = new HashMap<>();
        JSONObject postJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        String transactionId = info.getTransactionId();
        if (transactionId != null && !transactionId.isEmpty()) {
            parameters.put("transactionId", transactionId);
            parameters.put("transactionCode", info.getTransactionCode());
            parameters.put("forViewer", String.format(Locale.getDefault(), "%b",
                    info.getType() == 1));
            String spaceId = info.getSpaceId();
            if (spaceId != null && !spaceId.isEmpty()) {
                parameters.put("spaceId", spaceId);
            }
        } else {
            parameters.put("pathId", info.getPathId());
            parameters.put("type", info.getType());
        }
        parameters.put("start", String.format(Locale.getDefault(), "%d", info.getStart()));
        parameters.put("length", String.format(Locale.getDefault(), "%d", info.getLength()));
        postJson.put("parameters", parameters);

        body.put("application/json", postJson.toString());
        return body;
    }

    @Override
    protected Map<String, String> getHttpHeaders(ThreadInfo info) throws InvalidRMClientException {
        return Config.getCommonHeader();
    }

    @Override
    protected RandomAccessFile getFile(String localPath, long offset) throws IOException {
        File file = new File(localPath);
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(0);
        return raf;
    }
}
