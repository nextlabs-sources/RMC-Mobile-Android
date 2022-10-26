package com.skydrm.rmc.ui.common;

import android.content.Context;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.project.ProjectFile;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;

public class RightMenuItemClickListener implements NxlAdapter.OnRightMenuItemClickListener {
    private Context mCtx;
    private int projectId;
    private String projectName;

    public RightMenuItemClickListener(Context ctx) {
        this.mCtx = ctx;
    }

    public RightMenuItemClickListener(Context ctx, int id, String name) {
        this.mCtx = ctx;
        this.projectId = id;
        this.projectName = name;
    }

    @Override
    public void onButton01Click(INxlFile f, int pos, int type) {
        if (type == NxlAdapter.TYPE_SHARE) {
            shareFile(f, pos);
        } else if (type == NxlAdapter.TYPE_MANAGE) {
            manageFile(f, pos);
        } else if (type == NxlAdapter.TYPE_INFO) {
            if (f instanceof ProjectFile) {
                viewInfo(f);
                return;
            }
            viewInfo(f);
        }
    }

    @Override
    public void onButton02Click(INxlFile f, int pos, int type) {
        if (type == NxlAdapter.TYPE_VIEW_ACTIVITY) {
            viewActivity(f, pos);
        }
    }

    private void shareFile(INxlFile f, int pos) {
        NxlItemHelper.shareMyVaultFile(mCtx, f);
    }

    private void manageFile(INxlFile f, int pos) {
        NxlItemHelper.showManageView(mCtx, f);
    }

    private void viewInfo(INxlFile f) {
        NxlItemHelper.viewFileInfo(mCtx, (IFileInfo) f);
    }

    private void viewActivity(INxlFile f, int pos) {
        NxlItemHelper.viewActivity(mCtx, f.getName(), ((NxlDoc) f).getDuid());
    }
}
