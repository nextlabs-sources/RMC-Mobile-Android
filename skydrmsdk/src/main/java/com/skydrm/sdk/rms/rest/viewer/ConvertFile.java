package com.skydrm.sdk.rms.rest.viewer;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IConvertFileService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import java.io.File;

import okhttp3.OkHttpClient;


public class ConvertFile extends RestAPI.RestServiceBase implements IConvertFileService {

    public ConvertFile(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public byte[] convertCAD(IRmUser user, File file, RestAPI.IConvertListener convertListener) throws RmsRestAPIException {
//        String encodeFileName;
//        try {
//            encodeFileName = URLEncoder.encode(file.getName(), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            log.e("unsupported url encoding in convertCAD-" + e.toString(),e);
//            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
//        }
//
//        RequestBody body = RequestBody.create(
//                MediaType.parse("application/octet-stream"), file);
//        Request.Builder builder = new Request.Builder();
//        setCommonParas(builder);
//        Request request = builder
//                .url(config.getConvertCADURL().replace("{filename}", encodeFileName))
//                .addHeader("userId", user.getUserIdStr())
//                .addHeader("ticket", user.getTicket())
//                .post(body)
//                .build();
//
//        // simulate the progress of conversion
//        ConvertProgress convertThread = new ConvertProgress(file, convertListener);
//        convertThread.start();
//
//        Response response;
//        try {
//            mCall = httpClient.newCall(request);
//            response = mCall.execute();
////            if (!response.isSuccessful()) {
////                throw new RmsRestAPIException("network failed" + response.code() + response.message());
////            }
//        } catch (IOException e) {
//            if (mCall.isCanceled()) { // user cancel.
//                convertThread.cancel();
//                return null;
//            } else {
//                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
//            }
//        }
//
//        if (!response.isSuccessful()) {
//            if (response.code() == 401) {
//                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, 401);
//            } else {
//                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, response.code());
//            }
//        }
//
//        try {
//            byte[] convertedBinaries = response.body().bytes();
//            if (convertedBinaries.length > 0) {
//                return convertedBinaries;
//            }
//        } catch (IOException e) {
//            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileIOFailed);
//        }
//
        return null;
    }
}
