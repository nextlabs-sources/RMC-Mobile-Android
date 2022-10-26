package com.skydrm.rmc.ui.base;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.List;

public interface IDataService {

    List<INxlFile> list(int type, String pathId, boolean recursively);

    List<INxlFile> sync(int type, String pathId, boolean recursively)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException;

}
