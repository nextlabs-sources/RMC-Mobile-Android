package com.skydrm.rmc.ui.common;

import android.content.Context;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;

@Deprecated
public class ItemClickListener implements NxlAdapter.OnItemClickListener {
    private Context mCtx;

    public ItemClickListener(Context ctx) {
        this.mCtx = ctx;
    }

    @Override
    public void onItemClick(INxlFile f, int pos) {
        NxlItemHelper.viewFile(mCtx, f);
    }
}
