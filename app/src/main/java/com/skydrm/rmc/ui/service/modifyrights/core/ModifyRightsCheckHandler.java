package com.skydrm.rmc.ui.service.modifyrights.core;

import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.project.feature.service.core.RightsCheckHandler;
import com.skydrm.sdk.INxlFileFingerPrint;

public class ModifyRightsCheckHandler extends RightsCheckHandler {

    @Override
    protected void handleExactly(INxlFileFingerPrint fPrint, String name) throws MarkException {
        INxlFileFingerPrint fp = paramCheck(fPrint);
        if (fp.hasRights() && !fp.hasTags()) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_WRONG_POLICY_TYPE,
                    "This file is encrypted using user-defined rights and cannot be re-classified.");
        }
    }
}
