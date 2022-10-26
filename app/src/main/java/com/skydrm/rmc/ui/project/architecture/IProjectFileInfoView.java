package com.skydrm.rmc.ui.project.architecture;

/**
 * Created by hhu on 4/12/2018.
 */

public interface IProjectFileInfoView {
    /**
     * This response is when file protected with central policy rms returns.
     * {
     * "fileInfo": {
     * "pathDisplay": "/FullSizeRender-2018-04-12-15-50-56.jpg.nxl",
     * "pathId": "/fullsizerender-2018-04-12-15-50-56.jpg.nxl",
     * "name": "FullSizeRender-2018-04-12-15-50-56.jpg.nxl",
     * "fileType": "jpg",
     * "lastModified": 1523519501000,
     * "size": 45056,
     * "rights": ["EDIT","SEND","CLASSIFY","PRINT","SCREENCAP","DECRYPT","DOWNLOAD","VIEW","SHARE","CLIPBOARD","SAVEAS"],
     * "owner": true,
     * "nxl": true,
     * "tags": {
     * "Sensitivity": ["GeneralBusiness","NoBusiness"],
     * "itar": ["itar01"]
     * }
     * }
     * }
     * This response is when file protected with adhoc policy rms returns.
     * {
     "statusCode": 200,
     "message": "OK",
     "serverTime": 1523531129318,
     "results": {
     "fileInfo": {
     "pathDisplay": "/户型布置-12-2018-04-11-11-55-25.jpg.nxl",
     "pathId": "/户型布置-12-2018-04-11-11-55-25.jpg.nxl",
     "name": "户型布置-12-2018-04-11-11-55-25.jpg.nxl",
     "fileType": "jpg",
     "lastModified": 1523447728000,
     "size": 199168,
     "rights": ["VIEW","DOWNLOAD","PRINT"],
     "owner": true,
     "nxl": true,
     "tags": {}
     }
     }
     }
     */
    void showFileInfo(String response);

    void onLoading(boolean show);

    void onRequestError(String errMsg);
}
