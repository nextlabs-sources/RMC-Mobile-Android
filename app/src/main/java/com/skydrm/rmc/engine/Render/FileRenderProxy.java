package com.skydrm.rmc.engine.Render;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.RelativeLayout;

import com.skydrm.pdf.core.Utils;
import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.datalayer.repo.project.ProjectFile;
import com.skydrm.rmc.datalayer.repo.project.SharedWithProjectFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.LogSystem;
import com.skydrm.rmc.engine.eventBusMsg.ViewFileResultNotifyEvent;
import com.skydrm.rmc.engine.watermark.Overlay;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.errorHandler.IErrorResult;
import com.skydrm.rmc.exceptions.ErrorDialog;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.activity.ViewActivity;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.project.feature.centralpolicy.PolicyEvaluation;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.policy.Watermark;
import com.skydrm.sdk.rms.types.RemoteViewProjectFileParas;
import com.skydrm.sdk.rms.types.RemoteViewResult2;
import com.skydrm.sdk.rms.user.IRmUser;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by aning on 11/22/2016.
 */

public class FileRenderProxy implements IFileRender {
    private static DevLog log = new DevLog(FileRenderProxy.class.getSimpleName());
    private IFileRender mIFileRender = null;
    private Activity mActivity;
    private Context mContext;
    private RelativeLayout mMainLayout;
    private boolean bIsNxl = false;
    // for com.skydrm.rmc.nxl file decrypt
    private boolean bIsDecryptSucceed = false;
    private String mDecryptedFilePath;
    // for 3D convert
    private String mTmpConvertPath;
    private boolean bConvertSucceed = false;
    // for overlay
    private Overlay mOverlay;
    private File mWorkingFile;

    private IBuildRenderCallback mBuildRenderCallback;
    private IBuildRenderRemoteViewCallback mBuildRenderRemoteViewCallback;
    private INxlFileFingerPrint mNxlFileFingerPrint;
    // view type: normal view or preview
    private String mViewType;
    private String mFileName;

    private INxFile mClickFileItem;
    // for myVault remote view
    private INxlFile mFileBase;
    // for project remote view.
    private int mProjectId;

    // note: sometimes can't get the right file name by workingFile(case problem), so here incoming fileName.
    public FileRenderProxy(Context context, RelativeLayout mainLayout, boolean bIsNxlFile, final File workingFile, String fileName, String viewType) {
        mContext = context;
        mActivity = (Activity) context;
        mMainLayout = mainLayout;
        bIsNxl = bIsNxlFile;
        mWorkingFile = workingFile;
        mFileName = fileName;
        mViewType = viewType;
    }

    // used to simple remote view.
    public FileRenderProxy(Context context, RelativeLayout mainLayout, String viewType) {
        mContext = context;
        mActivity = (Activity) context;
        mMainLayout = mainLayout;
        mViewType = viewType;
    }

    public void setFileBase(INxlFile f) {
        this.mFileBase = f;
    }

    public void buildRender(final IBuildRenderCallback buildRenderCallback) {
        mBuildRenderCallback = buildRenderCallback;
        if (bIsNxl) {
            if (mFileBase instanceof SharedWithProjectFile) {
                SharedWithProjectFile swpf = (SharedWithProjectFile) mFileBase;
                int id = swpf.getId();
                String membershipId = "";
                try {
                    membershipId = swpf.getMembershipId();
                } catch (InvalidRMClientException e) {
                    e.printStackTrace();
                }
                FileOperation.decryptNxlFile(mContext, mWorkingFile, mViewType,
                        1, id, membershipId,
                        false,
                        new IDecryptCallback() {

                            @Override
                            public void onDecryptFinished(boolean status, String decryptedFilePath, INxlFileFingerPrint fingerPrint) {
                                File decryptedFile = new File(decryptedFilePath);
                                if (status) {
                                    mNxlFileFingerPrint = fingerPrint;
                                    //central policy.
                                    if (fingerPrint.hasTags()) {
                                        centralPolicyEvaluation(fingerPrint, decryptedFile, mWorkingFile, decryptedFilePath);
                                    } else if (fingerPrint.hasRights()) {
                                        adhocPolicyEvaluation(decryptedFilePath, fingerPrint, decryptedFile, mWorkingFile);
                                    } else if (!fingerPrint.hasRights() && !fingerPrint.hasTags()) {
                                        centralPolicyEvaluation(fingerPrint, decryptedFile, mWorkingFile, decryptedFilePath);
                                    }
                                } else {
                                    //send deny view log.
                                    LogSystem.sendDenyViewLog(mWorkingFile, mContext, mViewType);
                                }
                            }
                        });
            } else {
                FileOperation.decryptNxlFile(mContext, mWorkingFile, mViewType,
                        false,
                        new IDecryptCallback() {

                            @Override
                            public void onDecryptFinished(boolean status, String decryptedFilePath, INxlFileFingerPrint fingerPrint) {
                                File decryptedFile = new File(decryptedFilePath);
                                if (status) {
                                    mNxlFileFingerPrint = fingerPrint;
                                    //central policy.
                                    if (fingerPrint.hasTags()) {
                                        centralPolicyEvaluation(fingerPrint, decryptedFile, mWorkingFile, decryptedFilePath);
                                    } else if (fingerPrint.hasRights()) {
                                        adhocPolicyEvaluation(decryptedFilePath, fingerPrint, decryptedFile, mWorkingFile);
                                    } else if (!fingerPrint.hasRights() && !fingerPrint.hasTags()) {
                                        centralPolicyEvaluation(fingerPrint, decryptedFile, mWorkingFile, decryptedFilePath);
                                    }
                                } else {
                                    //send deny view log.
                                    LogSystem.sendDenyViewLog(mWorkingFile, mContext, mViewType);
                                }
                            }
                        });
            }

        } else {
            buildFileRender(mWorkingFile);
        }
    }

    public void buildOfflineRender(IBuildRenderCallback buildRenderCallback) {
        mBuildRenderCallback = buildRenderCallback;
        if (bIsNxl) {
            if (mFileBase instanceof SharedWithProjectFile) {
                SharedWithProjectFile swpf = (SharedWithProjectFile) mFileBase;
                int id = swpf.getId();
                String membershipId = "";
                try {
                    membershipId = swpf.getMembershipId();
                } catch (InvalidRMClientException e) {
                    e.printStackTrace();
                }
                FileOperation.decryptNxlFile(mContext, mWorkingFile, mViewType,
                        1, id, membershipId,
                        true, new IDecryptCallback() {
                            @Override
                            public void onDecryptFinished(boolean status, String decryptedFilePath, INxlFileFingerPrint fingerPrint) {
                                if (status) {
                                    mNxlFileFingerPrint = fingerPrint;
                                    if (fingerPrint.hasTags()) {
                                        if (viewOfflineFile(mFileBase, decryptedFilePath, fingerPrint)) {
                                            //record log.
                                            LogSystem.recordViewLog(mWorkingFile, fingerPrint.getDUID());
                                        }
                                    } else if (fingerPrint.hasRights()) {
                                        //adhoc policy.
                                        // init overlay  --  for com.skydrm.rmc.nxl file, have overlay obligation and is not steward, will display overlay
                                        if (fingerPrint.hasWatermark()) {
                                            buildWatermark(mContext, fingerPrint, mViewType);
                                        }
                                        //render file
                                        File decryptedFile = new File(decryptedFilePath);
                                        renderFile(decryptedFilePath, decryptedFile);

                                        //record log.
                                        LogSystem.recordViewLog(mWorkingFile, fingerPrint.getDUID());
                                    } else {
                                        if (viewOfflineFile(mFileBase, decryptedFilePath, fingerPrint)) {
                                            //record log.
                                            LogSystem.recordViewLog(mWorkingFile, fingerPrint.getDUID());
                                        }
                                    }
                                }
                            }
                        });
            } else {
                FileOperation.decryptNxlFile(mContext, mWorkingFile, mViewType,
                        true,
                        new IDecryptCallback() {
                            @Override
                            public void onDecryptFinished(boolean status, String decryptedFilePath, INxlFileFingerPrint fingerPrint) {
                                if (status) {
                                    mNxlFileFingerPrint = fingerPrint;
                                    if (fingerPrint.hasTags()) {
                                        if (viewOfflineFile(mFileBase, decryptedFilePath, fingerPrint)) {
                                            //record log.
                                            LogSystem.recordViewLog(mWorkingFile, fingerPrint.getDUID());
                                        }
                                    } else if (fingerPrint.hasRights()) {
                                        //adhoc policy.
                                        // init overlay  --  for com.skydrm.rmc.nxl file, have overlay obligation and is not steward, will display overlay
                                        if (fingerPrint.hasWatermark()) {
                                            buildWatermark(mContext, fingerPrint, mViewType);
                                        }
                                        //render file
                                        File decryptedFile = new File(decryptedFilePath);
                                        renderFile(decryptedFilePath, decryptedFile);

                                        //record log.
                                        LogSystem.recordViewLog(mWorkingFile, fingerPrint.getDUID());
                                    } else {
                                        if (viewOfflineFile(mFileBase, decryptedFilePath, fingerPrint)) {
                                            //record log.
                                            LogSystem.recordViewLog(mWorkingFile, fingerPrint.getDUID());
                                        }
                                    }
                                }
                            }
                        });
            }

        }
    }

    private boolean viewOfflineFile(INxlFile base, String decryptedFilePath, INxlFileFingerPrint fp) {
        if (base == null || fp == null) {
            return false;
        }

        if (checkOfflineViewRights(base)) {
            String obligation = getOfflineObligations(base);
            JSONArray obligations = null;
            try {
                obligations = new JSONArray(obligation);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (obligations != null && obligations.length() != 0) {
                buildWatermark(mContext, fp, obligations, mViewType);
            }

            File decryptedFile = new File(decryptedFilePath);
            //render file
            renderFile(decryptedFilePath, decryptedFile);

            return true;
        } else {
            ErrorDialog.showUI((Activity) mContext,
                    mContext.getResources().getString(R.string.hint_msg_error_token_access_deny),
                    true, false, true, null);

            //record log.
            LogSystem.recordDenyViewLog(mWorkingFile, fp.getDUID());
        }

        return false;
    }

    private boolean checkOfflineViewRights(INxlFile base) {
        if (base == null) {
            return false;
        }
        if (base instanceof NxlDoc) {
            NxlDoc doc = (NxlDoc) base;
            return doc.hasOfflineViewRights();
        }
        return false;
    }

    private String getOfflineObligations(INxlFile base) {
        String ret = "";
        if (base instanceof NxlDoc) {
            NxlDoc doc = (NxlDoc) base;
            ret = doc.getOfflineObligations();
        }
        return ret;
    }

    private void adhocPolicyEvaluation(String decryptedFilePath, INxlFileFingerPrint fingerPrint, File decryptedFile, File workingFile) {
        //adhoc policy.
        // init overlay  --  for com.skydrm.rmc.nxl file, have overlay obligation and is not steward, will display overlay
        if (fingerPrint.hasWatermark()) {
            buildWatermark(mContext, fingerPrint, mViewType);
        }
        //render file
        renderFile(decryptedFilePath, decryptedFile);
    }


    private void centralPolicyEvaluation(@NonNull final INxlFileFingerPrint fingerPrint,
                                         @NonNull final File decryptedFile,
                                         @NonNull final File workingFile,
                                         @NonNull final String decryptedFilePath) {
        if (mFileBase instanceof NxlDoc) {
            NxlDoc doc = (NxlDoc) mFileBase;
            doc.doPolicyEvaluation(fingerPrint, new IFileInfo.IPolicyCallback() {
                @Override
                public void onSuccess(List<String> rights, String obligations) {
                    if (rights != null && rights.contains(Constant.RIGHTS_VIEW)) {
                        if (obligations != null) {
                            try {
                                buildWatermark(mContext, fingerPrint, new JSONArray(obligations), mViewType);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //render file
                        renderFile(decryptedFilePath, decryptedFile);
                    } else {
                        //hint user with deny msg.
                        ToastUtil.showToast(mContext.getApplicationContext(),
                                mContext.getString(R.string.err_evaluate_denied));

                        if (mActivity != null) {
                            mActivity.finish();
                        }
                        // send deny view log
                        LogSystem.sendDenyViewLog(workingFile, null, null);
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    // send deny view log
                    ToastUtil.showToast(mContext.getApplicationContext(), e.getMessage());
                    if (mActivity != null) {
                        mActivity.finish();
                    }
                    LogSystem.sendDenyViewLog(workingFile, null, null);
                }
            });
            return;
        }
        //If has tags in nxl file header.Extract it send it to rms to evaluate.
        final int viewRights = 1;//defined by rms api.
        int evalType = 0;//defined by rms api.
        PolicyEvaluation.evaluate(PolicyEvaluation.buildEvalBean(fingerPrint, workingFile.getName(), viewRights, evalType),
                new PolicyEvaluation.IEvaluationCallback() {
                    @Override
                    public void onEvaluated(String result) {
                        PolicyEvaluation.parseResult(result, viewRights, new PolicyEvaluation.IParseEvalResultCallback() {
                            @Override
                            public void onAccessAllow(@Nullable JSONArray adhocObligations,
                                                      @Nullable JSONArray obligations) {
                                if (obligations != null) {
                                    buildWatermark(mContext, fingerPrint, obligations, mViewType);
                                }
                                //render file
                                renderFile(decryptedFilePath, decryptedFile);
                            }

                            @Override
                            public void onAccessDenied() {
                                //hint user with deny msg.
                                ToastUtil.showToast(mContext.getApplicationContext(),
                                        mContext.getString(R.string.err_evaluate_denied));

                                if (mActivity != null) {
                                    mActivity.finish();
                                }
                                // send deny view log
                                LogSystem.sendDenyViewLog(workingFile, null, null);
                            }
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        // send deny view log
                        ToastUtil.showToast(mContext.getApplicationContext(), e.getMessage());
                        if (mActivity != null) {
                            mActivity.finish();
                        }
                        LogSystem.sendDenyViewLog(workingFile, null, null);
                    }
                });
    }

    private void renderFile(String decryptedFilePath, File decryptedFile) {
        buildFileRender(decryptedFile);
        bIsDecryptSucceed = true;
        mDecryptedFilePath = decryptedFilePath;
    }

    // Generic build render for remote view repo -- myDrive, myVault, Files(Shared by me & Shared with me)
    public <T> void buildRemoteViewGeneric(T t, IBuildRenderRemoteViewCallback callback) {
        if (t instanceof INxFile) {
            mClickFileItem = (INxFile) t;
            mFileName = ((INxFile) t).getName();
            remoteViewRepo(RenderHelper.FileSource.FROM_HOME);
        } else if (t instanceof INxlFile) {
            mFileBase = (INxlFile) t;
            mFileName = mFileBase.getName();
            remoteViewRepo(RenderHelper.FileSource.FROM_MYVAULT);
        }
        // init callback
        mBuildRenderRemoteViewCallback = callback;
    }

    // used to remote view project
    public void buildRenderRemoteViewProject(int projectId, INxlFile f,
                                             IBuildRenderRemoteViewCallback callback) {
        mProjectId = projectId;
        mFileBase = f;
        mFileName = f.getName();
        mBuildRenderRemoteViewCallback = callback;
        remoteViewRepo(RenderHelper.FileSource.FROM_PROJECTS);
    }

    private void buildFileRender(final File workingFile) {
        FileType fileType = RenderHelper.judgeFileType(workingFile);
        switch (fileType) {
            case FILE_TYPE_TXT:
            case FILE_TYPE_IMAGE:
                mIFileRender = new WebViewRender(mContext, mMainLayout, workingFile);
                mBuildRenderCallback.onBuildRenderFinish();
                if (bIsNxl && mOverlay != null) {
                    mOverlay.showOverlay();
                }
                break;
            case FILE_TYPE_3D: // some need convert, some not
                dispatch3D(workingFile);
                break;
            case FILE_TYPE_AUDIO:
                mIFileRender = new Audio(mContext, mMainLayout, workingFile, mViewType);
                mBuildRenderCallback.onBuildRenderFinish();
                if (bIsNxl && mOverlay != null) {
                    mOverlay.showOverlay();
                }
                break;
            case FILE_TYPE_VIDEO:
                mIFileRender = new Video(mContext, mMainLayout, workingFile);
                mBuildRenderCallback.onBuildRenderFinish();
                if (bIsNxl && mOverlay != null) {
                    mOverlay.showOverlay();
                }
                break;
            case FILE_TYPE_OFFICE:
                remoteViewLocal(workingFile);
                break;
            // 2D or 3D pdf, for 3D pdf, sometimes the render is not fluent in some android device.
            case FILE_TYPE_PDF:
                if (Utils.is3DPdf(workingFile.getPath())) {
                    dispatch3D(workingFile);
                } else {
                    mIFileRender = new PDFRender(mMainLayout, workingFile);
                    mBuildRenderCallback.onBuildRenderFinish();
                    if (bIsNxl && mOverlay != null) {
                        mOverlay.showOverlay();
                    }
                }
                break;
            // other file format such as dwg,tif and tiff need to convert by rms.
            case FILE_TYPE_REMOTE_VIEW:
                remoteViewLocal(workingFile);
                break;
            case FILE_TYPE_NOT_SUPPORT:
                if (mViewType.equals(Constant.VIEW_TYPE_NORMAL)) {
                    if (bIsNxl) {
                        GenericError.showUI(mActivity, mContext.getString(R.string.file_format_not_supported), true, false, true, null);
                    } else {
                        GenericError.showUI(mActivity, mContext.getString(R.string.file_format_not_supported), true, false, true, new IErrorResult() {
                            @Override
                            public void cancelHandler() {
                            }

                            @Override
                            public void okHandler() {
                                // try to open file use third party app for not support normal file.
                                RenderHelper.tryOpenUseThirdParty(mContext, workingFile);
                            }
                        });
                    }
                } else { // preview file
                    EventBus.getDefault().post(new ViewFileResultNotifyEvent(false, mContext.getString(R.string.file_format_not_supported)));
                }
                break;
            case FILE_TYPE_INVALID_NXL:
                GenericError.showUI(mActivity, mContext.getString(R.string.hint_msg_nxl_invalid_type), true, false, true, null);
                break;
            default:
                break;
        }
    }

    /**
     * Remote view local, convert office file, 2d pdf, 3d pdf file, Microsoft Visio(vsd), dwg, tif and tiff by rms server.
     */
    private void remoteViewLocal(File workingFile) {
        FileOperation.remoteViewLocal(mContext, workingFile, mFileName, bIsNxl, mViewType, new FileRenderProxy.IRemoteViewerTask() {
            @Override
            public void onConvertFinish(RemoteViewResult2.ResultsBean result) {

                if (result != null) {
                    mIFileRender = new WebViewRender(mContext, mMainLayout, result);
                    // // TODO: 7/6/2017 should pass the parameter: result. ----- Note the result from rms is wrong: such as the isOwner always true.
                    mBuildRenderCallback.onBuildRenderFinish();
                    // display overlay
                    if (bIsNxl && mOverlay != null) {
                        mOverlay.showOverlay();
                    }
                }
            }
        });
    }

    /**
     * remote view repo -- only for myDrive and myVault some files which can't render in local
     */
    private void remoteViewRepo(RenderHelper.FileSource fileSource) {

        // myDrive
        if (fileSource == RenderHelper.FileSource.FROM_HOME && mClickFileItem != null) {
            BoundService bs = mClickFileItem.getService();
            FileOperation.remoteViewRepo(mContext,
                    bs.rmsRepoId,
                    mClickFileItem.getCloudPath(),
                    mClickFileItem.getDisplayPath(),
                    bs.alias,
                    bs.type.toRMSType(),
                    mClickFileItem.getLastModifiedTimeLong(),
                    mViewType,
                    false,
                    new IRemoteViewerTask() {
                        @Override
                        public void onConvertFinish(RemoteViewResult2.ResultsBean result) {
                            if (result != null) {
                                mIFileRender = new WebViewRender(mContext, mMainLayout, result);
                            }
                            // callback
                            mBuildRenderRemoteViewCallback.onBuildRenderRemoteViewFinish(result);
                        }
                    });
        }

        // myVault
        if (fileSource == RenderHelper.FileSource.FROM_MYVAULT && mFileBase != null) {
            MyVaultFile doc = (MyVaultFile) mFileBase;
            FileOperation.remoteViewRepo(mContext,
                    doc.getRepoId(),
                    doc.getPathId(),
                    doc.getPathDisplay(),
                    doc.getSourceRepoName(),
                    doc.getSourceRepoType(),
                    System.currentTimeMillis(), // lastModifiedDate -- is right?
                    mViewType,
                    true,
                    new IRemoteViewerTask() {
                        @Override
                        public void onConvertFinish(RemoteViewResult2.ResultsBean result) {
                            if (result != null) {
                                mIFileRender = new WebViewRender(mContext, mMainLayout, result);
                            }
                            // callback
                            mBuildRenderRemoteViewCallback.onBuildRenderRemoteViewFinish(result);
                        }
                    });
        }

        // project
        if (fileSource == RenderHelper.FileSource.FROM_PROJECTS && mFileBase != null) {
            ProjectFile doc = (ProjectFile) mFileBase;
            RemoteViewProjectFileParas paras = null;
            try {
                IRmUser user = SkyDRMApp.getInstance().getSession().getRmUser();
                String tenantId = SkyDRMApp.getInstance().getSession().getRmsClient().getTenant().getTenantId();
                paras = new RemoteViewProjectFileParas(mProjectId,
                        doc.getPathId(),
                        doc.getPathDisplay(),
                        user.getEmail(),
                        tenantId,
                        doc.getLastModifiedTime());
            } catch (InvalidRMClientException e) {
                e.printStackTrace();
            }

            FileOperation.remoteViewProject(mContext,
                    paras,
                    mViewType,
                    new IRemoteViewerTask() {
                        @Override
                        public void onConvertFinish(RemoteViewResult2.ResultsBean result) {
                            if (result != null) {
                                mIFileRender = new WebViewRender(mContext, mMainLayout, result);
                            }
                            // callback
                            mBuildRenderRemoteViewCallback.onBuildRenderRemoteViewFinish(result);
                        }
                    });
        }

    }

    /**
     * for some 3D format, need to convert.
     */
    private void dispatch3D(File workingFile) {
        if (RenderHelper.is3DFileNeedConvertFormat(workingFile.getName())) {

            File mountPoint = bIsNxl ? RenderHelper.getConvertedNxl3DMountPoint(mContext) : RenderHelper.getConvertedNormal3DMountPoint();
            try {
                File converted3d = new File(mountPoint, URLEncoder.encode(workingFile.getName(), "UTF-8") + ".hsf");
                // try to get the converted data from local if cache exist.
                if (converted3d.exists()) {
                    mIFileRender = new ThreeD(mContext, mMainLayout, converted3d, mViewType);
                    mBuildRenderCallback.onBuildRenderFinish();
                    if (bIsNxl && mOverlay != null) {
                        mOverlay.showOverlay();
                    }
                } else {
                    converted3D(workingFile);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            mIFileRender = new ThreeD(mContext, mMainLayout, workingFile, mViewType);
            mBuildRenderCallback.onBuildRenderFinish();
            if (bIsNxl && mOverlay != null) {
                mOverlay.showOverlay();
            }
        }
    }

    private void converted3D(final File workingFile) {
        FileOperation.Convert3dFiles(mContext, workingFile, mFileName, bIsNxl, false, mViewType, new IConvertAsyncTask() {
            @Override
            public void onConvertFinish(String ConvertPath) {
                if (!TextUtils.isEmpty(ConvertPath)) {
                    mTmpConvertPath = ConvertPath;
                    bConvertSucceed = true;
                    mIFileRender = new ThreeD(mContext, mMainLayout, new File(mTmpConvertPath), mViewType);
                    mBuildRenderCallback.onBuildRenderFinish();
                    if (bIsNxl && mOverlay != null) {
                        mOverlay.showOverlay();
                    }
                } else {
                    mActivity.finish();
                }
            }

        });
    }

    /**
     * including 2d PDF and 3D PDF, the latter also need to convert.
     */
//    @Deprecated
//    private void dispatchPdf(final File workingFile) {
//        final CharSequence[] pdfItems = mContext.getResources().getStringArray(R.array.pdf_format);
//        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setTitle(mContext.getResources().getString(R.string.app_name));
//        builder.setSingleChoiceItems(pdfItems, -1, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int item) {
//                if (item == 0) { // 2d pdf
//                    convertOfficePdf(workingFile);
//                } else if (item == 1) { // 3d pdf
//
//                    FileOperation.Convert3dFiles(mContext, workingFile, bIsNxl, false, new IConvertAsyncTask() {
//                        @Override
//                        public void onConvertFinish(String ConvertPath) {
//                            if (!TextUtils.isEmpty(ConvertPath)) {
//                                mTmpConvertPath = ConvertPath;
//                                bConvertSucceed = true;
//                                mIFileRender = new ThreeD(mContext, mMainLayout, new File(mTmpConvertPath));
//                                mBuildRenderCallback.onBuildRenderFinish();
//                                if (bIsNxl && mOverlay != null) {
//                                    mOverlay.showOverlay();
//                                }
//                            } else {
//                                mActivity.finish();
//                            }
//                        }
//                    });
//                }
//                dialog.dismiss();
//            }
//        });
//        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                mActivity.finish();
//            }
//        });
//        builder.setCancelable(false);
//        builder.show();
//    }
    @Override
    public void fileRender() {
        if (mIFileRender != null) {
            mIFileRender.fileRender();
        }
    }

    private void buildWatermark(Context context, INxlFileFingerPrint fingerPrint, JSONArray obligations, String viewType) {
        String ownerID = fingerPrint.getOwnerID();
        // here need to refactor
        if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {
            int projectOwnerId = ((ViewActivity) context).getmUserId();
            if (projectOwnerId != -1) { // for project nxl file.
                // For project nxl file, there is no owner concept.
                Watermark watermarkInfo = new Watermark(obligations);
                mOverlay = new Overlay(context, ((ViewActivity) context).getmOverlayFrameLayout(), watermarkInfo);
            } else {
                if (!SkyDRMApp.getInstance().isStewardOf(ownerID)) {
                    Watermark watermarkInfo = new Watermark(obligations);
                    mOverlay = new Overlay(context, ((ViewActivity) context).getmOverlayFrameLayout(), watermarkInfo);
                }
            }
        }
    }

    private void buildWatermark(@NonNull Context context, @NonNull INxlFileFingerPrint fingerPrint,
                                @NonNull String viewType) {
        String ownerID = fingerPrint.getOwnerID();
        // here need to refactor
        if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {
            int projectOwnerId = ((ViewActivity) context).getmUserId();
            if (projectOwnerId != -1) { // for project nxl file.
                // For project nxl file, there is no owner concept.
                Watermark watermarkInfo = SkyDRMApp.getInstance().getSession().getWatermark();
                // read watermark value from file header.
                String watermarkValue = "";
                Iterator<Map.Entry<String, String>> iterator = fingerPrint.getIterator();
                while (iterator != null && iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    if ("WATERMARK".equals(entry.getKey())) {
                        watermarkValue = entry.getValue();
                    }
                }
                // set watermark value.
                if (!TextUtils.isEmpty(watermarkValue)) {
                    watermarkInfo.setText(watermarkValue);
                }

                mOverlay = new Overlay(context, ((ViewActivity) context).getmOverlayFrameLayout(), watermarkInfo);
            } else { // for other nxl file.
                if (!SkyDRMApp.getInstance().isStewardOf(ownerID)) {
                    Watermark watermarkInfo = SkyDRMApp.getInstance().getSession().getWatermark();
                    // read watermark value from file header.
                    String watermarkValue = "";
                    Iterator<Map.Entry<String, String>> iterator = fingerPrint.getIterator();
                    while (iterator != null && iterator.hasNext()) {
                        Map.Entry<String, String> entry = iterator.next();
                        if ("WATERMARK".equals(entry.getKey())) {
                            watermarkValue = entry.getValue();
                        }
                    }
                    // set watermark value.
                    if (!TextUtils.isEmpty(watermarkValue)) {
                        watermarkInfo.setText(watermarkValue);
                    }

                    mOverlay = new Overlay(context, ((ViewActivity) context).getmOverlayFrameLayout(), watermarkInfo);
                }
            }
        } else if (viewType.equals(Constant.VIEW_TYPE_PREVIEW)) {
            // TODO: 5/16/2017, now ignore overlay for preview first.
        }
    }

    public INxlFileFingerPrint getmNxlFileFingerPrint() {
        return mNxlFileFingerPrint;
    }

    public IFileRender getIFileRender() {
        return mIFileRender;
    }

    public boolean isbIsDecryptSucceed() {
        return bIsDecryptSucceed;
    }

    public String getDecryptedFilePath() {
        return mDecryptedFilePath;
    }

    public boolean isbConvertSucceed() {
        return bConvertSucceed;
    }

    public String getTmpConvertPath() {
        return mTmpConvertPath;
    }

    // added for test print overlay.
    public Overlay getOverlay() {
        return mOverlay;
    }

    public enum FileType {
        FILE_TYPE_TXT,
        FILE_TYPE_IMAGE,
        FILE_TYPE_3D,
        FILE_TYPE_AUDIO,
        FILE_TYPE_VIDEO,
        FILE_TYPE_OFFICE,
        FILE_TYPE_PDF,
        FILE_TYPE_REMOTE_VIEW,

        FILE_TYPE_NOT_SUPPORT,
        FILE_TYPE_INVALID_NXL
    }

    public interface IDecryptCallback {
        void onDecryptFinished(boolean status, String decryptedFilePath, INxlFileFingerPrint fileFingerPrint);
    }

    public interface IConvertAsyncTask {
        // for CAD and 3D pdf
        void onConvertFinish(String ConvertPath);
    }

    public interface IRemoteViewerTask {
        // for convert by rms
        // void onConvertFinish(List<String> cookies, String viewerUrl);

        void onConvertFinish(RemoteViewResult2.ResultsBean result);
    }

    /**
     * invoke this after build Render, and then to render corresponding file with this render.
     */
    public interface IBuildRenderCallback {
        void onBuildRenderFinish();
    }

    /**
     * invoke this after build Render remote view, and then to render corresponding file with this render.
     */
    public interface IBuildRenderRemoteViewCallback {
        void onBuildRenderRemoteViewFinish(RemoteViewResult2.ResultsBean result);
    }

}
