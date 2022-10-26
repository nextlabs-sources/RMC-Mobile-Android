package com.skydrm.rmc.ui.workspace;

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
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.ui.base.BaseContextMenu;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineFilter;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.service.offline.filter.OfflineFileFilter;

public class WorkSpaceFileContextMenu extends BaseContextMenu {
    private INxlFile mFile;
    private int mPosition;
    private boolean isOfflineView;

    private OnMarkOfflineClickListener mOnMarkOfflineClickListener;
    private OnModifyRightsClickListener mOnModifyRightsClickListener;
    private OnShareItemClickListener mOnShareItemClickListener;
    private OnDeleteButtonClickListener mOnDeleteListener;
    private IOfflineFilter mOfflineFilter = new OfflineFileFilter();

    public static WorkSpaceFileContextMenu newInstance() {
        return new WorkSpaceFileContextMenu();
    }

    public void setFile(INxlFile file) {
        this.mFile = file;
    }

    public void setOfflineView(boolean offline) {
        this.isOfflineView = offline;
    }

    public void setPosition(int pos) {
        this.mPosition = pos;
    }

    public void setOnMarkOfflineClickListener(OnMarkOfflineClickListener listener) {
        this.mOnMarkOfflineClickListener = listener;
    }

    public void setOnModifyRightsClickListener(OnModifyRightsClickListener listener) {
        this.mOnModifyRightsClickListener = listener;
    }

    public void setOnShareItemClickListener(OnShareItemClickListener listener) {
        this.mOnShareItemClickListener = listener;
    }

    public void setOnDeleteButtonClickListener(OnDeleteButtonClickListener listener) {
        this.mOnDeleteListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_workspace_file_ctx_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mFile == null) {
            return;
        }

        TextView tvFileName = view.findViewById(R.id.tv_file_name);
        Button btViewFile = view.findViewById(R.id.bt_view_file);
        Button btViewFileInfo = view.findViewById(R.id.bt_view_file_info);
        Button btAddToProject = view.findViewById(R.id.bt_add_to_project);
        Button btViewActivity = view.findViewById(R.id.bt_view_activity);
        Button btMakeOffline = view.findViewById(R.id.bt_make_offline);
        Button btModifyRights = view.findViewById(R.id.bt_modify_rights);
        Button btDelete = view.findViewById(R.id.bt_delete);

        tvFileName.setText(mFile.getName());

        boolean isTenantAdmin = isTenantAdmin();

        if (mFile.isFolder()) {
            btViewFile.setVisibility(View.GONE);
            btViewFileInfo.setVisibility(View.GONE);
            btAddToProject.setVisibility(View.GONE);
            btViewActivity.setVisibility(View.GONE);
            btMakeOffline.setVisibility(View.GONE);
            btModifyRights.setVisibility(View.GONE);

            btDelete.setVisibility(isTenantAdmin ? View.VISIBLE : View.GONE);
        } else {
            btAddToProject.setVisibility(View.GONE);
            if (isOfflineView) {
                btAddToProject.setVisibility(View.GONE);
                btModifyRights.setVisibility(View.GONE);
                btViewActivity.setVisibility(View.GONE);
                btDelete.setVisibility(View.GONE);
            } else {
                if (!isTenantAdmin) {
                    btViewActivity.setVisibility(View.GONE);
                    btModifyRights.setVisibility(View.GONE);
                    btDelete.setVisibility(View.GONE);
                }
            }
            try {
                if (!mOfflineFilter.accept(mFile.getName())) {
                    btMakeOffline.setEnabled(false);
                }
            } catch (OfflineException e) {
                btMakeOffline.setEnabled(false);
            }
            btMakeOffline.setText(((NxlDoc) mFile).isOffline() ?
                    mCtx.getString(R.string.offlined) :
                    mCtx.getString(R.string.Make_available_offline));
        }

        btViewFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFile();
            }
        });
        btViewFileInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFileInfo();
            }
        });

        if (isVisible(btAddToProject)) {
            btAddToProject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToProject();
                }
            });
        }

        if (btMakeOffline.isEnabled()) {
            btMakeOffline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    markAsOffline(((Button) v).getText().toString().equals(mCtx.getString(R.string.offlined)));
                }
            });
        }
        if (isVisible(btViewActivity)) {
            btViewActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewActivity();
                }
            });
        }
        if (isVisible(btModifyRights)) {
            btModifyRights.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modifyRights();
                }
            });
        }
        if (isVisible(btDelete)) {
            btDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFile();
                }
            });
        }
    }

    private void viewFile() {
        dismiss();
        // view file.
        NxlItemHelper.viewFile(mCtx, mFile);
    }

    private void viewFileInfo() {
        dismiss();
        NxlItemHelper.viewFileInfo(mCtx, (IFileInfo) mFile);
    }

    private void addToProject() {
        dismiss();
        if (mOnShareItemClickListener != null) {
            mOnShareItemClickListener.onAddToProject(mFile, mPosition);
        }
    }

    private void viewActivity() {
        dismiss();
        NxlDoc doc = (NxlDoc) mFile;
        NxlItemHelper.viewActivity(mCtx, doc.getName(), doc.getDuid());
    }

    private void markAsOffline(boolean offline) {
        dismiss();
        if (mOnMarkOfflineClickListener != null) {
            mOnMarkOfflineClickListener.onClick(mFile, offline, mPosition);
        }
    }

    private void modifyRights() {
        dismiss();
        if (mOnModifyRightsClickListener != null) {
            mOnModifyRightsClickListener.onModifyRights(mFile, mPosition);
        }
    }

    private void deleteFile() {
        dismiss();
        if (mOnDeleteListener != null) {
            mOnDeleteListener.onClick(mFile, mPosition);
        }
    }

    public interface OnMarkOfflineClickListener {
        void onClick(INxlFile f, boolean offline, int pos);
    }

    public interface OnModifyRightsClickListener {
        void onModifyRights(INxlFile f, int pos);
    }

    public interface OnShareItemClickListener {
        void onAddToProject(INxlFile f, int pos);
    }

    public interface OnDeleteButtonClickListener {
        void onClick(INxlFile f, int pos);
    }
}
