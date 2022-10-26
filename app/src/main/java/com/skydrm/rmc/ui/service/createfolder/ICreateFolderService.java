package com.skydrm.rmc.ui.service.createfolder;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

public interface ICreateFolderService {
    boolean createFolder(String parentPathId, String name, boolean autoRename)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;
}
