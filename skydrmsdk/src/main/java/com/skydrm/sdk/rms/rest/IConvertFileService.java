package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.user.IRmUser;

import java.io.File;

public interface IConvertFileService {
    byte[] convertCAD(IRmUser user, final File file,
                      final RestAPI.IConvertListener convertListener) throws RmsRestAPIException;
}
