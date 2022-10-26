package com.skydrm.sdk.ui.uploadprogress;

import java.io.IOException;

/**
 * Created by jrzhou on 1/4/2017.
 */

public interface ProgressRequestListener {
    void onRequestProgress(long bytesWritten, long contentLength, boolean done) throws IOException;
}
