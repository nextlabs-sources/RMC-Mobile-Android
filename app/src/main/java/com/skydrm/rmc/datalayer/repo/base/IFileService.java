package com.skydrm.rmc.datalayer.repo.base;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.io.File;
import java.util.List;

public interface IFileService<T extends IFileType> {
    List<T> listFile(String pathId, boolean recursively);

    List<T> syncFile(String pathId, boolean recursively)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    boolean deleteFile(String pathId, boolean recursively);

    boolean createFolder(String parentPathId, String name, boolean autoRename)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    boolean uploadFile(String pathId, File nxlFile)
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException;
}
