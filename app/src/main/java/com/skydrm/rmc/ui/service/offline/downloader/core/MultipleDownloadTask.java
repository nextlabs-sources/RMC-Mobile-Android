package com.skydrm.rmc.ui.service.offline.downloader.core;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.service.offline.downloader.config.Config;
import com.skydrm.rmc.ui.service.offline.downloader.config.DownloadInfo;
import com.skydrm.rmc.ui.service.offline.downloader.config.ThreadInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class MultipleDownloadTask extends DownloadTask {

    MultipleDownloadTask(DownloadInfo downloadInfo, ThreadInfo threadInfo, IDownloadListener listener) {
        super(downloadInfo, threadInfo, listener);
    }

    @Override
    protected OkHttpClient buildHttpClient() {
        return null;
    }

    @Override
    protected Map<String, String> getRequestBody(DownloadInfo info) throws JSONException {
        Map<String, String> body = new HashMap<>();
        // generate request json
        JSONObject postJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        parameters.put("start", info.getStart());
        if (info.getLength() != 0) {
            parameters.put("length", info.getLength());
        }
        parameters.put("pathId", info.getPathId());
        parameters.put("forViewer", info.isForViewer());
        postJson.put("parameters", parameters);
        body.put("application/json", postJson.toString());
        return body;
    }

    @Override
    protected Map<String, String> getHttpHeaders(ThreadInfo info) throws InvalidRMClientException {
        Map<String, String> headers = new HashMap<>(Config.getCommonHeader());
        long start = info.getStart() + info.getFinished();
        long end = info.getEnd();
        headers.put("Ranges", start + "-" + end);
        return headers;
    }

    @Override
    protected RandomAccessFile getFile(String localPath, long offset) throws IOException {
        File file = new File(localPath);
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(offset);
        return raf;
    }
}
