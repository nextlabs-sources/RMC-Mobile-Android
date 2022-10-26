package com.skydrm.rmc.ui.common;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeFile;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.eventBusMsg.HomePageToSharePageEvent;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NxFileBase;
import com.skydrm.rmc.ui.activity.ProtectShareActivity;
import com.skydrm.rmc.ui.activity.ViewActivity;
import com.skydrm.rmc.ui.service.favorite.model.IFavoriteFile;
import com.skydrm.rmc.ui.service.fileinfo.FileInfoActivity;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.log.LogActivity;
import com.skydrm.rmc.ui.myspace.myvault.view.activity.MyVaultFileInfoActivity;
import com.skydrm.rmc.ui.myspace.myvault.view.activity.VaultFileShareActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

public class NxlItemHelper {

    public static void viewFile(Context ctx, INxlFile f) {
        Intent i = new Intent();
        if (f instanceof MyVaultFile) {
            MyVaultFile vf = (MyVaultFile) f;
            if (vf.isDeleted()) {
                showManageView(ctx, f);
                return;
            }
            i.setAction("NXMyVaultToView");
        } else if (f instanceof SharedWithMeFile) {
            i.setAction("NXSharedWithMeToView");
        } else if (f instanceof WorkSpaceFile) {
            i.setAction("NXWorkSpaceToView");
        }
        i.putExtra("file_entry", (NxlDoc) f);
        i.setClass(ctx, ViewActivity.class);
        ctx.startActivity(i);
    }

    public static void viewFile(Context ctx, int projectId, String projectName, INxlFile f) {
        Intent intent = new Intent();
        intent.setAction("NXProjectsToView");
        intent.putExtra(Constant.PROJECT_ID, projectId);
        intent.putExtra(Constant.PROJECT_NAME, projectName);
        intent.putExtra("file_entry", (NxlDoc) f);
        intent.setClass(ctx, ViewActivity.class);
        ctx.startActivity(intent);
    }

    public static void viewFavoriteFile(Context ctx, IFavoriteFile f) {
        if (f instanceof MyVaultFile) {
            viewFile(ctx, (MyVaultFile) f);
        } else {
            Intent intent = new Intent();
            intent.setAction("NXHomeToView");
            intent.putExtra("click_file", (NXDocument) f);
            intent.setClass(ctx, ViewActivity.class);
            ctx.startActivity(intent);
        }
    }

    public static void viewFileInfo(Context ctx, IFileInfo f) {
        Intent i = new Intent(ctx, FileInfoActivity.class);
        i.putExtra(Constant.FILE_INFO_ENTRY, (Parcelable) f);
        ctx.startActivity(i);
    }

    public static void reShareFile(Context ctx, INxlFile file) {
        Intent i = new Intent(ctx, VaultFileShareActivity.class);
        i.setAction(Constant.ACTION_RESHARE);
        i.putExtra(Constant.RESHARE_ENTRY, (NxlDoc) file);
        ctx.startActivity(i);
    }

    public static void viewActivity(Context ctx, String name, String duid) {
        Intent i = new Intent(ctx, LogActivity.class);
        i.putExtra("file_name", name);
        i.putExtra("duid", duid);
        ctx.startActivity(i);
    }

    public static void showDeleteDialog(Context ctx, final INxlFile f,
                                        final OnDeleteButtonClickListener listener) {
        String deleteMsg = "";
        if (f.isFolder()) {
            deleteMsg = String.format(Locale.getDefault(),
                    ctx.getString(R.string.hint_msg_delete_folder),
                    f.getName());
        } else {
            deleteMsg = String.format(Locale.getDefault(),
                    ctx.getString(R.string.hint_msg_delete_file),
                    f.getName());
        }

        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(ctx);
        deleteBuilder.setTitle(ctx.getString(R.string.app_name))
                .setMessage(deleteMsg)
                .setPositiveButton(ctx.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onClick(f);
                        }
                    }
                })
                .setNegativeButton(ctx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static void showManageView(Context ctx, INxlFile f) {
        Intent intent = new Intent();
        Bundle arguments = new Bundle();
        arguments.putParcelable(Constant.VAULT_FILE_ENTRY, (MyVaultFile) f);
        intent.putExtras(arguments);
        intent.setClass(ctx, MyVaultFileInfoActivity.class);
        ctx.startActivity(intent);
    }

    public static void viewMyVaultFileInfo(Context ctx, INxlFile f) {
//        MyVaultViewFileInfoEvent eventMsg = new MyVaultViewFileInfoEvent(entry, FileType.MY_VAULT_FILE, FileFrom.FILE_FROM_MYVAULT);
//        if (entry.isOffline()) {
//            File workingFile = tryGetMyVaultFile(entry);
//            if (workingFile != null) {
//                eventMsg.setWorkingFile(workingFile);
//            }
//        }
//        eventMsg.setOffline(entry.isOffline() & !isNetworkOffline());
//        EventBus.getDefault().postSticky(eventMsg);
//        launcher.startActivity(new Intent(launcher, MoreActivity.class));
    }

    public static void shareMyVaultFile(Context ctx, INxlFile f) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_SHARE);
        intent.setClass(ctx, VaultFileShareActivity.class);
        Bundle arguments = new Bundle();
        arguments.putParcelable(Constant.VAULT_FILE_ENTRY, (MyVaultFile) f);
        intent.putExtras(arguments);
        ctx.startActivity(intent);
    }

    public static void manageOrShareFavoriteFile(Context ctx, IFavoriteFile f, View v) {
        if (f instanceof MyVaultFile) {
            MyVaultFile nf = (MyVaultFile) f;
            if (((Button) v).getText().toString().equals(ctx.getString(R.string.manage))) {
                showManageView(ctx, nf);
            } else if (((Button) v).getText().toString().equals(ctx.getString(R.string.share))) {
                shareMyVaultFile(ctx, nf);
            }
        } else {
            INxFile nf = (NxFileBase) f;
            HomePageToSharePageEvent eventMsg = new HomePageToSharePageEvent(nf, CmdOperate.SHARE);
            EventBus.getDefault().postSticky(eventMsg);
            ctx.startActivity(new Intent(ctx, ProtectShareActivity.class));
        }
    }

    public static void protectOrViewLogFavoriteFile(Context ctx, IFavoriteFile f, View v) {
        if (f instanceof MyVaultFile) {
            MyVaultFile doc = (MyVaultFile) f;
            NxlItemHelper.viewActivity(ctx, doc.getName(), doc.getDuid());
        } else {
            INxFile nf = (NxFileBase) f;
            if (((Button) v).getText().toString().equals(ctx.getString(R.string.protect))) {
                HomePageToSharePageEvent eventMsg = new HomePageToSharePageEvent(nf, CmdOperate.PROTECT);
                EventBus.getDefault().postSticky(eventMsg);
                ctx.startActivity(new Intent(ctx, ProtectShareActivity.class));
            } else {
                Intent intent = new Intent();
                intent.setAction(Constant.VIEW_ACTIVITY_LOG_FROM_MAIN);
                intent.putExtra(Constant.SELECTED_ITEM, (NXDocument) nf);
                intent.setClass(ctx, LogActivity.class);
                ctx.startActivity(intent);
            }
        }
    }

    public interface OnDeleteButtonClickListener {
        void onClick(INxlFile f);
    }
}
