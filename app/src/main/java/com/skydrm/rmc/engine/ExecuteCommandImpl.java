package com.skydrm.rmc.engine;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.engine.intereface.IAddComplete;
import com.skydrm.rmc.engine.intereface.IExecuteCommand;
import com.skydrm.rmc.engine.intereface.IProtectComplete;
import com.skydrm.rmc.engine.intereface.IShareComplete;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.ui.DialogFactory;
import com.skydrm.rmc.ui.activity.ProtectShareActivity;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileResult;

import java.io.File;
import java.util.List;

/**
 * Created by aning on 5/12/2017.
 */

public class ExecuteCommandImpl implements IExecuteCommand {
    private static final DevLog log = new DevLog(ProtectShareActivity.class.getSimpleName());
    private static final long FILE_NAME_MAX_LENGTH = 128;

    @Deprecated
    @Override
    public void protect(final Context context,
                        File workingFile,
                        Rights rights,
                        Obligations obligations,
                        Expiry expiry,
                        final INxFile clickFileItem,
                        final IProtectComplete callback) {
        if (workingFile == null) {
            ToastUtil.showToast(context, context.getResources().getString(R.string.info_file_not_exist));
            return;
        }

        if (!RenderHelper.isNxlFile(workingFile.getPath())) { // normal file
            // can't protect empty file.
            if (workingFile.length() == 0) {
                ToastUtil.showToast(context, context.getResources().getString(R.string.info_empty_file));
                return;
            }
            // check file name length
            if (workingFile.getName().length() > FILE_NAME_MAX_LENGTH) {
                ToastUtil.showToast(context, context.getResources().getString(R.string.info_invalid_file_name));
                return;
            }
            // protect file
            FileOperation.protectFile(context, workingFile, rights, obligations, expiry, new FileOperation.IProtectFileFinish() {
                @Override
                public void onProtectFinished(String nxlPath) {
                    if (clickFileItem != null) {
                        // prepare param
                        BoundService service = clickFileItem.getService();
                        String pathId = clickFileItem.getDisplayPath();
                        String pathDisplayName = clickFileItem.getDisplayPath();
                        String repoId = service.rmsRepoId;
                        String repoNickName = service.rmsNickName;
                        String repoType = service.alias;
                        // param rectify
                        if (repoId == null) {
                            repoId = "";
                        }
                        if (repoNickName == null || repoNickName.isEmpty()) {
                            repoNickName = repoType;
                        }
                        // log
                        {
                            log.v("try to send file to myVault: " + nxlPath + "\n" +
                                    "pathIdName: " + pathDisplayName + "\n" +
                                    "repoId: " + repoId + "\n" +
                                    "repoNickName:" + repoNickName + "\n" +
                                    "repoType: " + repoType);
                        }

                        // note: the repoId and repoName is null of GoogleDriver
                        FileOperation.uploadFileToMyVault(
                                context,
                                new File(nxlPath),
                                pathId, // srcFilePathId
                                pathDisplayName, // srcFilePathDisplay
                                repoId,
                                repoNickName, // but the repoName can't is "", or else will upload failed.
                                repoType,
                                true,
                                new FileOperation.IUploadToMyVaultFinish() {
                                    @Override
                                    public void onUploadToMyVaultFinished(MyVaultUploadFileResult result) {
                                        callback.onProtectComplete(result);
                                    }
                                });
                    } else {  // as third part to open local file directly then protect.
                        FileOperation.uploadFileToMyVaultLocal(
                                context,
                                new File(nxlPath),
                                nxlPath,
                                true,
                                new FileOperation.IUploadToMyVaultFinish() {
                                    @Override
                                    public void onUploadToMyVaultFinished(MyVaultUploadFileResult result) {
                                        callback.onProtectComplete(result);
                                    }
                                });
                    }
                    // send log
                    LogSystem.sendProtectLog(null, false, new File(nxlPath), false);
                }
            });
        }
    }

    @Override
    public void shareLocalFile(Context context,
                               File workingFile,
                               List<String> emailList,
                               Rights rights,
                               Obligations obligations,
                               String comment,
                               @Nullable Expiry expiry,
                               final IShareComplete callback) {
        // check
        if (workingFile == null) {
            ToastUtil.showToast(context, context.getResources().getString(R.string.info_file_not_exist));
            return;
        }

        if (workingFile.length() == 0) {
            ToastUtil.showToast(context, context.getResources().getString(R.string.info_empty_file));
            return;
        }

        // check file name length
        if (workingFile.getName().length() > FILE_NAME_MAX_LENGTH) {
            ToastUtil.showToast(context, context.getResources().getString(R.string.info_invalid_file_name));
            return;
        }

        // do share
        if (RenderHelper.isNxlFile(workingFile.getPath())) {  // nxl file
            FileOperation.shareLocalFile(context, workingFile, emailList, null, null, false, comment, expiry, new FileOperation.IShareFileFinish() {
                @Override
                public void onShareFileFinished() {
                    callback.onShareComplete(true);
                }
            });
        } else { // normal file
            FileOperation.shareLocalFile(context, workingFile, emailList, rights, obligations, false, comment, expiry, new FileOperation.IShareFileFinish() {
                @Override
                public void onShareFileFinished() {
                    callback.onShareComplete(true);
                }
            });
        }
    }

    @Override
    public void shareRepoFile(Context context,
                              String fileName,
                              String repositoryId,
                              String filePathId,
                              String filePath,
                              int permissions,
                              List<String> emails,
                              @Nullable String comment,
                              @Nullable String watermark,
                              @Nullable Expiry expiry,
                              final IShareComplete callback) {
        // check
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(repositoryId) || TextUtils.isEmpty(filePathId) || TextUtils.isEmpty(filePath)) {
            log.e("file name, repository id, file path id, file path, at least is empty.");
            return;
        }

        if (emails.isEmpty()) {
            log.e("the email is empty");
            return;
        }

        FileOperation.shareRepoFile(context,
                fileName,
                repositoryId,
                filePathId,
                filePath,
                permissions,
                emails,
                false,
                comment,
                watermark,
                expiry,
                new FileOperation.IShareFileFinish() {
                    @Override
                    public void onShareFileFinished() {
                        callback.onShareComplete(true);
                    }
                });

    }

    @Override
    public void AddFile(Context context,
                        BoundService boundService,
                        INxFile parentFolder,
                        File addFile,
                        IAddComplete callback) {
        try {

            DialogFactory.getInstance().createDialog(context,
                    ProgressDialog.STYLE_HORIZONTAL, "",
                    context.getResources().getString(R.string.adding_file) + addFile.getName(),
                    100);

            SkyDRMApp.getInstance().getRepoSystem().uploadFile(boundService,
                    parentFolder,
                    addFile.getName(),
                    addFile,
                    new AddFileCallBack(context, callback));

        } catch (FileUploadException e) {
            log.e(e);
            DialogFactory.getInstance().dismissDialog();
            // exception handler
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

        } catch (Exception e) {
            log.e(e);
            DialogFactory.getInstance().dismissDialog();
        }
    }

    // Add file callback
    private static class AddFileCallBack implements IRemoteRepo.IUploadFileCallback {
        private Context context;
        private IAddComplete callback;

        AddFileCallBack(Context context, IAddComplete callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        public void cancelHandler(final ICancelable handler) {
            DialogFactory.getInstance().cancelDialog(new DialogFactory.IDialogCanceler() {
                @Override
                public void onCanceled() {
                    log.v("fire cancel upload");
                    handler.cancel();
                }
            });
        }

        @Override
        public void onFinishedUpload(boolean taskStatus, @Nullable NXDocument uploadedDoc, @Nullable FileUploadException e) {
            DialogFactory.getInstance().dismissDialog();
            callback.onAddFileComplete(taskStatus, uploadedDoc, e);
        }

        @Override
        public void progressing(long newValue) {
            log.v("progressing: " + newValue);
            DialogFactory.getInstance().setDialogProgress((int) newValue);
        }
    }
}
