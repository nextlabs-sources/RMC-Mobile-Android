package com.skydrm.rmc.ui.project.architecture;

import android.content.Context;
import android.support.annotation.Nullable;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.engine.DownloadManager;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.ui.project.architecture.IProjectFileInfoView;
import com.skydrm.sdk.INxlFileFingerPrint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hhu on 4/12/2018.
 */

@Deprecated
public class ProjectFileInfoPresenter {
    private static final DevLog log = new DevLog("ProjectFileInfo");
    private static final Map<String, String> mCaches = new HashMap<>();

    public static void present(final IProjectFileInfoView projectFileInfoView,
                               int projectId, final String pathId) {
        final String cacheKey = projectId + pathId;
        if (!mCaches.containsKey(cacheKey)) {
            //if cache does not exist in local.
//            ProjectFilesHelper3.requestProjectFileMataData(projectId, pathId,
//                    new ProjectFilesHelper3.IRequestFileMetadataCallback() {
//                        @Override
//                        public void onRequesting() {
//                            projectFileInfoView.onLoading(true);
//                        }
//
//                        @Override
//                        public void onRequested(String response) {
//                            projectFileInfoView.onLoading(false);
//                            mCaches.put(cacheKey, response);
//                            if (!TextUtils.isEmpty(getResults(response))) {
//                                projectFileInfoView.showFileInfo(response);
//                            }
//                        }
//
//                        @Override
//                        public void onRequestError(String errMsg) {
//                            projectFileInfoView.onRequestError(errMsg);
//                        }
//                    });
        } else {
            //get data from local cache.
            projectFileInfoView.showFileInfo(mCaches.get(cacheKey));
        }
    }

    public static void getProjectNxlFileFingerPrint(final Context context, int projectId, String projectName,
                                                    String pathId, String fileName,
                                                    final IGetFingerPrintCallback callback,
                                                    final IProjectFileInfoView projectFileInfoView) {
        File projectsMountPoint = RenderHelper.getProjectsMountPoint(projectName);
        if (projectsMountPoint == null) {
            return;
        }
        final String absPath = Helper.nxPath2AbsPath(projectsMountPoint, fileName);
        final File nxlFile = Helper.getLocalProjectFile(absPath);
        if (nxlFile != null && nxlFile.length() > 0) {
            //means file exists in local
            //extract finger print from nxl header
            getFingerPrint(context, nxlFile, true, "", new FileOperation.IGetFingerPrintCallback() {
                @Override
                public void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint) {
                    if (callback != null) {
                        callback.readFingerprint(fingerPrint);
                    }
                }
            });

        } else {
            if (projectFileInfoView != null) {
                projectFileInfoView.onLoading(true);
            }
            //should down project nxl file header from rms.[The nxl file header size is 16k]
            FileOperation.projectDownloadFile(context, projectId, pathId, absPath, 1,
                    new DownloadManager.IDownloadCallBack() {
                        @Override
                        public void onDownloadFinished(boolean taskStatus, String localPath,
                                                       @Nullable FileDownloadException e) {
                            if (projectFileInfoView != null) {
                                projectFileInfoView.onLoading(false);
                            }
                            getFingerPrint(context, new File(localPath), true, "",
                                    new FileOperation.IGetFingerPrintCallback() {
                                        @Override
                                        public void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint) {
                                            if (callback != null) {
                                                callback.readFingerprint(fingerPrint);
                                            }
                                        }
                                    });
                        }

                        @Override
                        public void onDownloadProgress(long value) {

                        }
                    }, 0, 16 * 1024);
        }
    }

    private static void getFingerPrint(Context context, File nxlFile, boolean bIsDisplayUi, String viewType, FileOperation.IGetFingerPrintCallback callback) {
        FileOperation.readNxlFingerPrint(context, nxlFile, bIsDisplayUi, viewType, callback);
    }

    private static String getResults(String response) {
        try {
            JSONObject responseObj = new JSONObject(response);
            if (responseObj.has("results")) {
                return responseObj.getJSONObject("results").toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public interface IGetFingerPrintCallback {
        void readFingerprint(INxlFileFingerPrint fingerPrint);
    }

    public static List<String> translateRights(int rights) {
        //List<String> rights = new ArrayList<>();
        List<String> rightsArray = new ArrayList<>();
        int right_view = 1;
        int right_print = 1 << 2;
        int right_download = 1 << 10;
        int right_watermark = 1 << 30;
        //if rights == 0 means no view rights.
        if (rights == 0) {
            return null;
        }
        //1<<30 represents watermark right.
        int remains = 0;
        if (rights >= right_watermark) {
            rightsArray.add("WATERMARK");
            //means have watermark right.
            remains = rights - right_watermark;
            if (remains >= right_download) {
                rightsArray.add("DOWNLOAD");
                remains = remains - right_download;
                if (remains >= right_print) {
                    rightsArray.add("PRINT");
                    remains = remains - right_print;
                    if (remains == right_view) {
                        rightsArray.add("VIEW");//watermark & download & print & view
                    }
                } else {
                    if (remains == right_view) {
                        rightsArray.add("VIEW");//watermark & download & view
                    }
                }
            } else {
                if (remains >= right_print) {
                    rightsArray.add("PRINT");
                    remains = remains - right_print;
                    if (remains == right_view) {
                        rightsArray.add("VIEW");//watermark&print&view
                    }
                } else {
                    if (remains == right_view) {
                        rightsArray.add("VIEW");
                    }
                }
            }
        } else {
            if (rights >= right_download) {
                rightsArray.add("DOWNLOAD");
                remains = rights - right_download;
                if (remains >= right_print) {
                    rightsArray.add("PRINT");
                    remains = remains - right_print;
                    if (remains == right_view) {
                        rightsArray.add("VIEW");//download & print & view
                    }
                } else {
                    if (remains == right_view) {
                        rightsArray.add("VIEW");//download & view
                    }
                }
            } else {
                if (rights >= right_print) {
                    rightsArray.add("PRINT");
                    remains = rights - right_print;
                    if (remains == right_view) {
                        rightsArray.add("VIEW");//print&view
                    }
                } else {
                    if (rights >= right_view) {
                        rightsArray.add("VIEW");//view
                    }
                }
            }
        }
        return rightsArray;
    }
}
