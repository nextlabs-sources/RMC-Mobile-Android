package com.skydrm.rmc.datalayer.repo;

import com.skydrm.rmc.datalayer.repo.base.IBaseRepo;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.List;

public abstract class NxlRepo implements IBaseRepo {
    /**
     * Get nxl files from local.
     *
     * @return
     */
    public abstract List<INxlFile> list(int type);

    public abstract List<INxlFile> list(int type, String pathId, boolean recursively);

    /**
     * Get nxl files from remote.
     *
     * @return
     */
    public abstract List<INxlFile> sync(int type)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException;

    public abstract List<INxlFile> sync(int type, String pathId, boolean recursively)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException;
}
