package com.skydrm.sdk.ui.uploadprogress;


import okhttp3.RequestBody;

/**
 * Created by jrzhou on 1/4/2017.
 */

public class ProgressHelper {

    public static ProgressRequestBody addProgressRequestListener(RequestBody requestBody, ProgressRequestListener progressRequestListener){

        return new ProgressRequestBody(requestBody,progressRequestListener);
    }

}
