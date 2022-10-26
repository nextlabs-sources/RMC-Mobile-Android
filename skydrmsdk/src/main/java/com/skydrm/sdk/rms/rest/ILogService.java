package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.types.FetchLogRequestParas;
import com.skydrm.sdk.rms.types.FetchLogResult;
import com.skydrm.sdk.rms.types.SendLogRequestValue;

public interface ILogService {
    /**
     * This API is used log operations (ACTIVITY_LOG) performed through RMC client
     *
     * @param logRequestValue: the request values that needed.
     * @return: true if send log to rms succeed, or else false
     */
    boolean sendLogToRms(SendLogRequestValue logRequestValue) throws RmsRestAPIException;

    /**
     * The API used to fetch activity logs
     *
     * @param duid
     * @param requestValue
     * @return {@link FetchLogResult}
     */
    FetchLogResult fetchActivityLog(String duid, FetchLogRequestParas requestValue) throws RmsRestAPIException;
}
