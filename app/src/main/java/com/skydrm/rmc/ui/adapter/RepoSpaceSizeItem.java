package com.skydrm.rmc.ui.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.base.NavigationType;

import java.util.ArrayList;
import java.util.List;

public class RepoSpaceSizeItem {
    private int mRepoDrawableId;
    private String mRepoName;
    private long mRepoUsage;
    private long mRepoQuota;
    private int mRepoTotalFiles = -1;

    private RepoSpaceSizeItem(int drawableId, String name,
                              long usage, long quota,
                              int totalFiles) {
        this.mRepoDrawableId = drawableId;
        this.mRepoName = name;
        this.mRepoUsage = usage;
        this.mRepoQuota = quota;
        this.mRepoTotalFiles = totalFiles;
    }

    public int getRepoDrawableId() {
        return mRepoDrawableId;
    }

    public String getRepoName() {
        return mRepoName;
    }

    public long getRepoUsage() {
        return mRepoUsage;
    }

    public long getRepoQuota() {
        return mRepoQuota;
    }

    public int getRepoTotalFiles() {
        return mRepoTotalFiles;
    }

    public NavigationType toNavigationType(Context ctx) {
        if (ctx == null) {
            return NavigationType.TYPE_TO_MYDRIVE;
        }
        if (TextUtils.equals(mRepoName, ctx.getString(R.string.MyDrive))) {
            return NavigationType.TYPE_TO_MYDRIVE;
        }
        if (TextUtils.equals(mRepoName, ctx.getString(R.string.MyVault))) {
            return NavigationType.TYPE_TO_MYVAULT;
        }
        if (TextUtils.equals(mRepoName, ctx.getString(R.string.name_workspace))) {
            return NavigationType.TYPE_TO_WORKSPACE;
        }
        return NavigationType.TYPE_TO_MYDRIVE;
    }

    public static List<RepoSpaceSizeItem> createDriveItems(Context ctx,
                                                           long myDriveUsageSize,
                                                           long myVaultUsageSize) {
        List<RepoSpaceSizeItem> ret = new ArrayList<>();

        //construct myDrive space item.
        ret.add(new RepoSpaceSizeItem(R.drawable.bottom_sheet_my_drive,
                ctx.getString(R.string.MyDrive), myDriveUsageSize, -1, -1));

        //construct myVault space item.
        ret.add(new RepoSpaceSizeItem(R.drawable.icon_drawer_myvault,
                ctx.getString(R.string.MyVault), myVaultUsageSize, -1, -1));

        return ret;
    }

    public static List<RepoSpaceSizeItem> createWorkSpaceItem(Context ctx,
                                                              long usage,
                                                              int totals) {
        List<RepoSpaceSizeItem> ret = new ArrayList<>();

        ret.add(new RepoSpaceSizeItem(R.drawable.icon_drawer_workspace,
                ctx.getString(R.string.name_workspace), usage, -1, totals));

        return ret;
    }

}
