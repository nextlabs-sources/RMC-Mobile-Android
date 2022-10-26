package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.types.HeartbeatResponse;

public interface IHeartbeatService {
    HeartbeatResponse heartbeat() throws RmsRestAPIException;
}
