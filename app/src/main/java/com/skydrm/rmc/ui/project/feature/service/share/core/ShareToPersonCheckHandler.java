package com.skydrm.rmc.ui.project.feature.service.share.core;

import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.project.feature.service.core.RightsCheckHandler;
import com.skydrm.sdk.INxlFileFingerPrint;

public class ShareToPersonCheckHandler extends RightsCheckHandler {
    private int mProjectId;

    ShareToPersonCheckHandler(int projectId) {
        this.mProjectId = projectId;
    }

    @Override
    protected void handleExactly(INxlFileFingerPrint fPrint, String name) throws MarkException {
        INxlFileFingerPrint fp = paramCheck(fPrint);
        if (fp.hasRights() && !fp.hasTags()) {
            if (!fp.hasShare()) {
                throw new MarkException(MarkerStatus.STATUS_FAILED_UNAUTHORIZED,
                        "You are not authorized to perform this action.");
            }
            return;
        }
        throw new MarkException(MarkerStatus.STATUS_FAILED_WRONG_POLICY_TYPE,
                "This file is protected by company-defined rights,and cannot be shared.");
    }
}
