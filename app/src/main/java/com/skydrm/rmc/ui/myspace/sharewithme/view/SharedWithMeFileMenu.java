package com.skydrm.rmc.ui.myspace.sharewithme.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeFile;
import com.skydrm.rmc.ui.base.BaseContextMenu;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineFilter;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.service.offline.filter.OfflineFileFilter;

public class SharedWithMeFileMenu extends BaseContextMenu {
    private INxlFile mBaseFile;
    private int mPosition;
    private Context mCtx;
    private OnItemClickListener mOnItemClickListener;
    private IOfflineFilter mOfflineFilter = new OfflineFileFilter();

    public void setSharedWithMeFile(INxlFile file) {
        this.mBaseFile = file;
    }

    public void setPosition(int pos) {
        this.mPosition = pos;
    }

    public static SharedWithMeFileMenu newInstance() {
        return new SharedWithMeFileMenu();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCtx = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_shared_with_me_item_menu,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView fileName = view.findViewById(R.id.file_name);
        Button btViewFile = view.findViewById(R.id.view_file);
        Button btViewFileInfo = view.findViewById(R.id.view_file_info);
        Button btMarkOffline = view.findViewById(R.id.mark_offline);
        Button btShare = view.findViewById(R.id.share_file);
        Button btViewActivity = view.findViewById(R.id.view_activity);
        btViewActivity.setVisibility(View.GONE);

        final SharedWithMeFile f = (SharedWithMeFile) mBaseFile;
        fileName.setText(f.getName());

        if (f.isOffline()) {
            btMarkOffline.setText(mCtx.getString(R.string.offlined));
        } else {
            btMarkOffline.setTag(mCtx.getString(R.string.Make_available_offline));
        }

        btViewFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NxlItemHelper.viewFile(getContext(), f);
                dismiss();
            }
        });
        btViewFileInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NxlItemHelper.viewFileInfo(getContext(), f);
                dismiss();
            }
        });
        btMarkOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onSetOffline(mBaseFile, mPosition, f.isOffline());
                }
            }
        });

        try {
            if (!mOfflineFilter.accept(mBaseFile.getName())) {
                btMarkOffline.setEnabled(false);
            }
        } catch (OfflineException e) {
            btMarkOffline.setEnabled(false);
        }

        //For file owner will have all rights.[Fix Bug 59470]
        if (!f.isOwner()) {
            if (!f.hasShareRights()) {
                btShare.setVisibility(View.GONE);
            }
        }

        if (isVisible(btShare)) {
            btShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NxlItemHelper.reShareFile(getContext(), f);
                    dismiss();
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onSetOffline(INxlFile f, int pos, boolean offline);
    }
}
