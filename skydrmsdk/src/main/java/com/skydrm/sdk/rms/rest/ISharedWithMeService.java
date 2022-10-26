package com.skydrm.sdk.rms.rest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeListFileRequestParams;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeListFileResult;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeReshareResult;

import java.io.File;
import java.util.List;

public interface ISharedWithMeService {
    SharedWithMeListFileResult listFile(@NonNull SharedWithMeListFileRequestParams params) throws RmsRestAPIException;

    SharedWithMeReshareResult reShareFile(@NonNull String transactionId, @NonNull String transactionCode, @NonNull List<String> members,
                                          @Nullable String comment) throws RmsRestAPIException;

    /**
     * @param id
     * @param code
     * @param bForViewer should set it as true when user click "view icon" to view file, in this case, ignoring the check for download rights
     *                   should set it as false when user click "download icon" to download file(for android client, now not has this icon).
     * @param f
     * @param listener
     * @return the fileName that stored at RMS
     * @throws RmsRestAPIException
     */
    String download(String id, String code, boolean bForViewer, File f, int start, int length, RestAPI.DownloadListener listener) throws RmsRestAPIException;
}
