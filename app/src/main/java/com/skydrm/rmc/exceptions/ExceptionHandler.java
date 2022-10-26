package com.skydrm.rmc.exceptions;

import android.app.Activity;
import android.content.Context;

import com.sap.ve.DVLTypes;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.errorHandler.IErrorResult;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.ui.activity.CreateFolderActivity;
import com.skydrm.rmc.ui.activity.ViewActivity;
import com.skydrm.rmc.ui.service.fileinfo.FileInfoActivity;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;
import com.skydrm.rmc.ui.service.offline.core.OfflineTokenHandler;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.project.ProjectActivity;
import com.skydrm.rmc.ui.project.common.MsgFileNotFound;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
import com.skydrm.rmc.ui.project.service.message.MsgNotFound;
import com.skydrm.rmc.ui.service.search.SearchActivity;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.exception.NotGrantedShareRights;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RightsExpiredException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import org.greenrobot.eventbus.EventBus;

import java.io.FileNotFoundException;

/**
 * Created by aning on 12/7/2016.
 */
public class ExceptionHandler {

    public static void handleException(Context ctx, Exception e) {
        if (e == null || ctx == null) {
            return;
        }
        if (e instanceof FileNotFoundException) {
            ErrorDialog.showSimpleUI(ctx, ctx.getResources().getString(R.string.excep_file_not_find));
        } else if (e instanceof NotNxlFileException) {
            ErrorDialog.showSimpleUI(ctx, ctx.getResources().getString(R.string.excep_not_nxl_file));
        } else if (e instanceof TokenAccessDenyException) {
            handleTokenAccessDenyException(ctx, (TokenAccessDenyException) e);
        } else if (e instanceof NotGrantedShareRights) {
            ErrorDialog.showSimpleUI(ctx, ctx.getResources().getString(R.string.excep_no_share_right));
        } else if (e instanceof RightsExpiredException) {
            ErrorDialog.showUI((Activity) ctx, ctx.getResources().getString(R.string.excep_nxl_rights_expired), true, false, true, null);
        } else if (e instanceof RmsRestAPIException) {
            handleRmsRestAPIException(ctx, (RmsRestAPIException) e);
        } else if (e instanceof OfflineException) {
            handleOfflineException(ctx, (OfflineException) e);
        } else if (e instanceof MarkException) {
            handleShareException(ctx, (MarkException) e);
        } else if (e instanceof FileUploadException) {
            handleFileUploadException(ctx, (FileUploadException) e);
        } else {
            ErrorDialog.showSimpleUI(ctx, e.getMessage());
        }
    }

    public static void handleDVLRESULT(final Context ctx, final DVLTypes.DVLRESULT dvlresult) {
        if (ctx == null) {
            return;
        }
        CommonUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dvlresult == DVLTypes.DVLRESULT.BADFORMAT) {
                    ErrorDialog.showUI((Activity) ctx, ctx.getResources().getString(R.string.hint_msg_error_while_process_bad_format_file),
                            true, false, true, null);
                } else {
                    ErrorDialog.showUI((Activity) ctx, ctx.getResources().getString(R.string.hint_msg_error_while_process),
                            true, false, true, null);
                }
            }
        });
    }

    private static void handleTokenAccessDenyException(final Context ctx, final TokenAccessDenyException e) {
        if (e.getType() == TokenAccessDenyException.TYPE_TOKEN_EXPIRED) {
            ErrorDialog.showUI((Activity) ctx, ctx.getResources().getString(R.string.excep_offline_token_expired),
                    true, true, true, new IErrorResult() {
                        @Override
                        public void cancelHandler() {
                            ((Activity) ctx).finish();
                        }

                        @Override
                        public void okHandler() {
                            String sharedSpaceUserMembership = e.getSharedSpaceUserMembership();
                            if (sharedSpaceUserMembership == null || sharedSpaceUserMembership.isEmpty()) {
                                OfflineTokenHandler.reActiveToken(ctx, e.getTargetPath());
                            } else {
                                OfflineTokenHandler.reActiveToken(ctx, e.getTargetPath(),
                                        e.getSharedSpaceType(), e.getSharedSpaceId(),
                                        sharedSpaceUserMembership);
                            }
                        }
                    });
            return;
        }
        if (e.getType() == TokenAccessDenyException.TYPE_TOKEN_DENY_PROJECT_SHARE_TO_PERSON) {
            ErrorDialog.showUI((Activity) ctx, ctx.getResources().getString(R.string.err_evaluate_denied),
                    true, false, true, null);
            return;
        }
        ErrorDialog.showUI((Activity) ctx, ctx.getResources().getString(R.string.excep_token_access_deny),
                true, false, true, null);
    }

    private static void handleOfflineException(Context ctx, OfflineException e) {
        switch (e.getErrCode()) {
            case OfflineStatus.STATUS_FAILED:
                if (e.getErrMsg().contains("No address associated with hostname")
                        || e.getErrMsg().contains("Unable to resolve host")) {
                    ErrorDialog.showSimpleUI(ctx, ctx.getResources().getString(R.string.excep_network_unavailable));
                } else if (e.getErrMsg().contains("connection abort")) {
                    ErrorDialog.showSimpleUI(ctx, ctx.getResources().getString(R.string.excep_network_unavailable));
                } else {
                    ErrorDialog.showSimpleUI(ctx, e.getMessage());
                }
                break;
            case OfflineStatus.STATUS_REST_API_EXCEPTION:
                if (e.getErrMsg().contains("No address associated with hostname")
                        || e.getErrMsg().contains("Unable to resolve host")) {
                    ErrorDialog.showSimpleUI(ctx, ctx.getResources().getString(R.string.excep_network_unavailable));
                } else {
                    ErrorDialog.showSimpleUI(ctx, e.getMessage());
                }
                break;
            case OfflineStatus.STATUS_TOKEN_PROCESS_FAILED:
                ErrorDialog.showSimpleUI(ctx, ctx.getResources().getString(R.string.excep_token_access_deny_offline));
                break;
            case OfflineStatus.STATUS_TOKEN_ACCESS_DENY:
                ErrorDialog.showSimpleUI(ctx, ctx.getResources().getString(R.string.excep_token_access_deny_offline));
                break;
            case OfflineStatus.STATUS_INVALID_NXL_FILE:
                ErrorDialog.showSimpleUI(ctx, ctx.getResources().getString(R.string.excep_not_nxl_file));
                break;
            case OfflineStatus.STATUS_FILE_NOT_FOUND:
                ErrorDialog.showUI((Activity) ctx, ctx.getString(R.string.invalid_file), true, false,
                        inSearchStatus(ctx), null);

                if (inSearchStatus(ctx)) {
                    return;
                }

                EventBus.getDefault().post(new MsgFileNotFound());
                break;
            case OfflineStatus.STATUS_UNAUTHORIZED:
                ErrorDialog.showSimpleUI(ctx, ctx.getResources().getString(R.string.err_evaluate_denied));
                break;
            default:
                ErrorDialog.showSimpleUI(ctx, e.getMessage());
                break;
        }
    }

    private static void handleShareException(Context ctx, MarkException e) {
        ErrorDialog.showUI((Activity) ctx, e.getMessage(), true, false,
                true, null);
    }

    private static void handleRmsRestAPIException(Context ctx, RmsRestAPIException e) {
        switch (e.getDomain()) {
            case AuthenticationFailed:
                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(ctx);
                break;
            case AccessDenied:
                ErrorDialog.showUI((Activity) ctx, ctx.getResources().getString(R.string.err_evaluate_denied),
                        true, false, true, null);
                break;
            case NetWorkIOFailed:
                ToastUtil.showToast(ctx, ctx.getResources().getString(R.string.excep_network_unavailable));
                break;
            case InvalidProjectName:
                ToastUtil.showToast(ctx, ctx.getResources().getString(R.string.excep_invalid_name));
                break;
            case InvalidProjectDescription:
                ToastUtil.showToast(ctx, ctx.getResources().getString(R.string.excep_invalid_description));
                break;
            case ProjectNameAlreadyExist:
                ToastUtil.showToast(ctx, ctx.getResources().getString(R.string.excep_project_already_exist));
                break;
            case NotFound:
                ErrorDialog.showUI((Activity) ctx, e.getMessage(), true, false,
                        true, null);

                if (ctx instanceof ProjectActivity) {
                    return;
                }

                EventBus.getDefault().post(new MsgNotFound());
                break;
            case FileNotFound:
                ErrorDialog.showUI((Activity) ctx, e.getMessage(), true, false,
                        needFinish(ctx), null);

                EventBus.getDefault().post(new MsgFileNotFound());
                break;
            case UNSUPPORTED_WORKSPACE_UPLOAD_FILE:
                ErrorDialog.showUI((Activity) ctx, ctx.getString(R.string.hint_unsupported_nxl_file_upload_to_workspace), true, false,
                        true, null);
                break;
            default:
                ToastUtil.showToast(ctx, e.getMessage());
                break;
        }
    }

    private static void handleFileUploadException(Context context, FileUploadException e) {
        switch (e.getErrorCode()) {
            case AuthenticationFailed:
                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                break;
            case NetWorkIOFailed:
                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                break;
            case NamingCollided:
                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_file_existed));
                break;
            case NameTooLong:
                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_file_name_too_long));
                break;
            case DriveStorageExceed:
                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_storage_exceed));
                break;
            default:
                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                break;
        }
    }

    private static boolean needFinish(Context ctx) {
        if (ctx == null) {
            return false;
        }
        return ctx instanceof ProjectOperateActivity
                || ctx instanceof ViewActivity
                || ctx instanceof FileInfoActivity
                || ctx instanceof CreateFolderActivity
                || ctx instanceof SearchActivity;
    }

    private static boolean inSearchStatus(Context ctx) {
        if (ctx == null) {
            return false;
        }
        return ctx instanceof SearchActivity;
    }
}
