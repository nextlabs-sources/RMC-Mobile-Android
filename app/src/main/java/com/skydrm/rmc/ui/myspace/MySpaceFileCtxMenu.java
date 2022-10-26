package com.skydrm.rmc.ui.myspace;

import android.view.View;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.ui.base.BaseFileCtxMenu;
import com.skydrm.rmc.ui.service.favorite.model.eventmsg.FavoriteFileUpdateFromMyDriveEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MySpaceFileCtxMenu extends BaseFileCtxMenu implements BaseFileCtxMenu.OnItemClickListener {
    private INxFile mINxFile;
    private int mPos;
    private OnFavoriteMarkerChangeListener mOnFavoriteMarkerChangeListener;

    public static MySpaceFileCtxMenu newInstance() {
        return new MySpaceFileCtxMenu();
    }

    public void setNxFile(INxFile file) {
        this.mINxFile = file;
    }

    public void setPosition(int position) {
        this.mPos = position;
    }

    public void setOnFavoriteMarkerChangeListener(OnFavoriteMarkerChangeListener listener) {
        this.mOnFavoriteMarkerChangeListener = listener;
    }

    @Override
    protected BaseFileCtxMenu.OnItemClickListener getItemClickListener() {
        return this;
    }

    @Override
    protected List<String> getData() {
        return getItemData();
    }

    @Override
    public void onItemClick(View v, String title, int pos) {
        if (pos == 0) {
            return;
        }

        if (mCtx.getString(R.string.view_file).equalsIgnoreCase(title)) {
            viewFile();
        }
        if (mCtx.getString(R.string.view_file_info).equalsIgnoreCase(title)) {
            viewFileInfo();
        }

        if (mCtx.getString(R.string.share).equalsIgnoreCase(title)) {
            shareFile();
        }

        if (mCtx.getString(R.string.protect).equalsIgnoreCase(title)) {
            protectFile();
        }

        if (mCtx.getString(R.string.Favorited).equalsIgnoreCase(title)) {
            unMarkAsFavorite();
        }
        if (mCtx.getString(R.string.Make_as_favorite).equalsIgnoreCase(title)) {
            markAsFavorite();
        }

        if (mCtx.getString(R.string.delete).equalsIgnoreCase(title)) {
            deleteFile();
        }

        dismiss();
    }

    private List<String> getItemData() {
        List<String> ret = new ArrayList<>();
        if (mINxFile == null) {
            return ret;
        }
        ret.add(mINxFile.getName());
        if (!mINxFile.isFolder()) {
            ret.add(mCtx.getString(R.string.view_file));
            if (isNxlFile()) {
                ret.add(mCtx.getString(R.string.view_file_info));
                ret.add(mCtx.getString(R.string.share));
            } else {
                if (isMyDrive()) {
                    ret.add(mCtx.getString(R.string.view_file_info));
                }
                ret.add(mCtx.getString(R.string.share));
                ret.add(mCtx.getString(R.string.protect));
            }
            if (isMyDrive()) {
                ret.add(mCtx.getString(isFavorite() ?
                        R.string.Favorited :
                        R.string.Make_as_favorite));
            }
            if (needShowOffline()) {
                ret.add(mCtx.getString(R.string.Make_available_offline));
            }
        }
        if (isMyDrive()) {
            ret.add(mCtx.getString(R.string.delete));
        }
        return ret;
    }

    private boolean isNxlFile() {
        if (mINxFile == null) {
            return false;
        }
        String name = mINxFile.getName();
        if (name == null || name.isEmpty()) {
            return false;
        }
        return name.endsWith(".nxl");
    }

    private boolean isMyDrive() {
        if (mINxFile == null) {
            return false;
        }
        BoundService service = mINxFile.getService();
        if (service == null) {
            return false;
        }
        return service.type == BoundService.ServiceType.MYDRIVE;
    }

    private boolean isFavorite() {
        if (mINxFile == null) {
            return false;
        }
        return mINxFile.isMarkedAsFavorite();
    }

    private boolean needShowOffline() {
        return false;
    }

    private void viewFile() {
        if (mINxFile == null) {
            return;
        }
        if (mINxFile instanceof NXDocument) {
            MySpaceFileItemHelper.viewFile(mCtx, (NXDocument) mINxFile);
        }
    }

    private void viewFileInfo() {
        MySpaceFileItemHelper.viewFileInfo(mCtx, mINxFile);
    }

    private void shareFile() {
        MySpaceFileItemHelper.share(mCtx, mINxFile);
    }

    private void protectFile() {
        MySpaceFileItemHelper.protect(mCtx, mINxFile);
    }

    private void markAsFavorite() {
        SkyDRMApp.getInstance().getRepoSystem().markAsFavorite(mINxFile);
        EventBus.getDefault().post(new FavoriteFileUpdateFromMyDriveEvent());
        if (mOnFavoriteMarkerChangeListener != null) {
            mOnFavoriteMarkerChangeListener.onChanged(mINxFile, mPos, true);
        }
    }

    private void unMarkAsFavorite() {
        SkyDRMApp.getInstance().getRepoSystem().unmarkAsFavorite(mINxFile);
        EventBus.getDefault().post(new FavoriteFileUpdateFromMyDriveEvent());
        if (mOnFavoriteMarkerChangeListener != null) {
            mOnFavoriteMarkerChangeListener.onChanged(mINxFile, mPos, false);
        }
    }

    private void deleteFile() {
        MySpaceFileItemHelper.delete(mCtx, mINxFile, -1);
    }

    public interface OnFavoriteMarkerChangeListener {
        void onChanged(INxFile f, int position, boolean favorite);
    }
}
