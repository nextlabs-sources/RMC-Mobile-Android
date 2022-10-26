package com.skydrm.rmc.ui.myspace;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.engine.enumData.FileType;
import com.skydrm.rmc.engine.eventBusMsg.HomePageToMorePageEvent;
import com.skydrm.rmc.engine.eventBusMsg.NxFileDeleteEvent;
import com.skydrm.rmc.reposystem.Utils;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity2;
import com.skydrm.rmc.ui.activity.MoreActivity;
import com.skydrm.rmc.ui.activity.ViewActivity;
import com.skydrm.rmc.ui.service.log.LogActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.Locale;

public class MySpaceFileItemHelper {

    public static void viewFile(Context ctx, NXDocument doc) {
        if (ctx == null || doc == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction("NXHomeToView");
        intent.putExtra("click_file", doc);
        intent.setClass(ctx, ViewActivity.class);
        ctx.startActivity(intent);
    }

    public static void viewFileInfo(Context ctx, INxFile f) {
        if (ctx == null || f == null) {
            return;
        }
        HomePageToMorePageEvent eventMsg = new HomePageToMorePageEvent(f, FileType.DRIVE_FILE,
                FileFrom.FILE_FROM_MYSPACE_PAGE);
        EventBus.getDefault().postSticky(eventMsg);
        ctx.startActivity(new Intent(ctx, MoreActivity.class));
    }

    public static void share(Context ctx, INxFile f) {
        if (ctx == null || f == null) {
            return;
        }
        Intent i = new Intent(ctx, CmdOperateFileActivity2.class);
        i.setAction(Constant.ACTION_SHARE);
        i.putExtra(Constant.LIBRARY_FILE_ENTRY, (Serializable) f);
        ctx.startActivity(i);
    }

    public static void protect(Context ctx, INxFile f) {
        if (ctx == null || f == null) {
            return;
        }
        Intent i = new Intent(ctx, CmdOperateFileActivity2.class);
        i.setAction(Constant.ACTION_PROTECT);
        i.putExtra(Constant.LIBRARY_FILE_ENTRY, (Serializable) f);
        ctx.startActivity(i);
    }

    public static void delete(Context ctx, final INxFile f, final int pos) {
        String fileName = f.getName();
        if (f.isSite()) {
            fileName = Utils.getFixedNameOfSite(f);
        }

        String deleteMsg = "";
        if (f.isFolder()) {
            deleteMsg = String.format(Locale.getDefault(),
                    ctx.getString(R.string.hint_msg_delete_folder),
                    fileName);
        } else {
            deleteMsg = String.format(Locale.getDefault(),
                    ctx.getString(R.string.hint_msg_delete_file),
                    fileName);
        }

        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(ctx);
        deleteBuilder.setTitle(ctx.getString(R.string.app_name))
                .setMessage(deleteMsg)
                .setPositiveButton(ctx.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            SkyDRMApp.getInstance().getRepoSystem().deleteFile(f);
                            EventBus.getDefault().post(new NxFileDeleteEvent(SkyDRMApp.getInstance()
                                    .getRepoSystem().isInSyntheticRoot(), f, pos));
                        } catch (Exception e) {
                            e.printStackTrace();
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

    public static void viewActivityLog(Context ctx, NXDocument doc) {
        if (ctx == null || doc == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Constant.VIEW_ACTIVITY_LOG_FROM_MAIN);
        intent.putExtra(Constant.SELECTED_ITEM, doc);
        intent.setClass(ctx, LogActivity.class);
        ctx.startActivity(intent);
    }
}
