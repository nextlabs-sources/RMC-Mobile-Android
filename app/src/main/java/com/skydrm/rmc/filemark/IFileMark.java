package com.skydrm.rmc.filemark;

import com.skydrm.sdk.exception.RmsRestAPIException;

/**
 * Created by aning on 8/17/2017.
 */

public interface IFileMark {

    /**
     * Add the marked or un-marked file into the set
     * @param repoId the repository id that the file belong to
     * @param markItem the marked file
     */
    void addMarkFileCacheSet(String repoId, IMarkItem markItem);

    /**
     * Used to synchronize the marked & un-marked files into server
     * @throws RmsRestAPIException
     */
    void syncMarkedFileToRms() throws RmsRestAPIException;
}
