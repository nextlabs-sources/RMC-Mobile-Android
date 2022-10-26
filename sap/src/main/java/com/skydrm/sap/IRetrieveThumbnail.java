package com.skydrm.sap;

import com.sap.ve.DVLTypes;
import com.sap.ve.SDVLProceduresInfo;

public interface IRetrieveThumbnail {
    void onDisplay(SDVLProceduresInfo proceduresInfo);

    void onInitializeError(DVLTypes.DVLRESULT dvlresult);
}
