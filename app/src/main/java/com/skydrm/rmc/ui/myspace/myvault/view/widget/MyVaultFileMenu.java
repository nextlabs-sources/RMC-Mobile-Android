package com.skydrm.rmc.ui.myspace.myvault.view.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineFilter;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.service.offline.filter.OfflineFileFilter;

public class MyVaultFileMenu extends BottomSheetDialogFragment {
    private Context mCtx;
    private INxlFile mFile;
    private int mPosition;

    private OnItemClickListener mOnItemClickListener;
    private int mDeleteBtVisibility;
    private IOfflineFilter mOfflineFilter = new OfflineFileFilter();

    public static MyVaultFileMenu newInstance() {
        return new MyVaultFileMenu();
    }

    public void setFile(INxlFile f) {
        this.mFile = f;
    }

    public void setPosition(int pos) {
        this.mPosition = pos;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setDeleteBtVisibility(int visibility) {
        this.mDeleteBtVisibility = visibility;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mCtx = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_myvault_item_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvFileName = view.findViewById(R.id.file_name);
        Button btViewFile = view.findViewById(R.id.view_file);
        Button btViewFileInfo = view.findViewById(R.id.view_file_info);
        final Button btManage = view.findViewById(R.id.manage);
        final Button btMarkFavorite = view.findViewById(R.id.mark_favorite);
        Button btMarkOffline = view.findViewById(R.id.mark_offline);
        Button btViewActivity = view.findViewById(R.id.view_activity);
        Button btDelete = view.findViewById(R.id.delete);

        final MyVaultFile doc = (MyVaultFile) mFile;
        btDelete.setVisibility(mDeleteBtVisibility);
        if (!doc.isShared()) {//means the protected file
            if (doc.isDeleted()) {
                //only show activity log to the deleted protected item.
                btViewFile.setVisibility(View.GONE);
                btManage.setVisibility(View.GONE);
                btMarkFavorite.setVisibility(View.GONE);
                btMarkOffline.setVisibility(View.GONE);
                btDelete.setVisibility(View.GONE);
            }
        } else {//for shared deleted item
            //only show activity log and manage button for deleted shared button.
            if (doc.isDeleted()) {
                btViewFile.setVisibility(View.GONE);
                btMarkFavorite.setVisibility(View.GONE);
                btMarkOffline.setVisibility(View.GONE);
                btDelete.setVisibility(View.GONE);
            } else if (doc.isRevoked()) {
                btViewFile.setVisibility(View.VISIBLE);
                btMarkFavorite.setVisibility(View.VISIBLE);
                btMarkOffline.setVisibility(View.VISIBLE);
            }
        }

        if (doc.isShared() || doc.isDeleted() || doc.isRevoked()) {
            btManage.setText(mCtx.getString(R.string.manage));
        } else {
            btManage.setText(mCtx.getString(R.string.share));
        }
        if (doc.isFavorite()) {
            btMarkFavorite.setText(mCtx.getString(R.string.Favorited));
        } else {
            btMarkFavorite.setTag(mCtx.getString(R.string.Make_as_favorite));
        }
        if (doc.isOffline()) {
            btMarkOffline.setText(mCtx.getString(R.string.offlined));
            //when file is offline & network is unAvailable just dismiss manage|share button.
            if (!SkyDRMApp.getInstance().isNetworkAvailable()) {
                btManage.setVisibility(View.GONE);
            }
        } else {
            btMarkOffline.setTag(mCtx.getString(R.string.Make_available_offline));
        }
        tvFileName.setText(mFile.getName());

        btViewFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFile(mFile);
                dismiss();
            }
        });
        btViewFileInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFileInfo();
                dismiss();
            }
        });

        try {
            if (!mOfflineFilter.accept(mFile.getName())) {
                btMarkOffline.setEnabled(false);
            }
        } catch (OfflineException e) {
            btMarkOffline.setEnabled(false);
        }

        btManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btManage.getText().equals(mCtx.getString(R.string.share))) {
                    NxlItemHelper.shareMyVaultFile(mCtx, mFile);
                } else {
                    NxlItemHelper.showManageView(mCtx, mFile);
                }
                dismiss();
            }
        });

        btMarkFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onSetFavorite(mFile, mPosition, doc.isFavorite());
                }
            }
        });
        btMarkOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onSetOffline(mFile, mPosition, doc.isOffline());
                }
            }
        });
        btViewActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                viewActivity();
            }
        });
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onDeleteFile(mFile, mPosition);
                }
            }
        });
    }

    private void viewFile(INxlFile f) {
        NxlItemHelper.viewFile(mCtx, f);
    }

    private void viewFileInfo() {
        NxlItemHelper.viewFileInfo(mCtx, (IFileInfo) mFile);
    }

    private void viewActivity() {
        NxlItemHelper.viewActivity(mCtx, mFile.getName(), ((NxlDoc) mFile).getDuid());
    }

    public interface OnItemClickListener {
        void onSetFavorite(INxlFile f, int pos, boolean favorite);

        void onSetOffline(INxlFile f, int pos, boolean offline);

        void onDeleteFile(INxlFile f, int pos);
    }
}
