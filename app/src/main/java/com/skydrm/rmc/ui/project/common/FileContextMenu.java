package com.skydrm.rmc.ui.project.common;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.base.SharedWithBase;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineFilter;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.service.offline.filter.OfflineFileFilter;
import com.skydrm.sdk.rms.user.IRmUser;

public class FileContextMenu extends BottomSheetDialogFragment {
    private Context mCtx;
    private INxlFile mFile;

    private int mProjectId;
    private String mProjectName;
    private boolean isOwnerByMe;
    private int mPosition;
    private int viewActivityVisibility;
    private int deleteVisibility;

    private boolean offlineView;

    private NxlItemHelper.OnDeleteButtonClickListener mListener;
    private OnMarkOfflineClickListener mOnMarkOfflineClickListener;
    private OnShareItemClickListener mOnShareItemClickListener;
    private OnModifyRightsClickListener mOnModifyRightsClickListener;
    private IOfflineFilter mOfflineFilter = new OfflineFileFilter();

    public static FileContextMenu newInstance() {
        return new FileContextMenu();
    }

    public void setFile(INxlFile file) {
        this.mFile = file;
    }

    public void setProjectId(int id) {
        this.mProjectId = id;
    }

    public void setProjectName(String name) {
        this.mProjectName = name;
    }

    public void setOwnerByMe(boolean ownerByMe) {
        isOwnerByMe = ownerByMe;
    }

    public void setOfflineView(boolean offlineView) {
        this.offlineView = offlineView;
    }

    public void setViewActivityItemVisibility(int visibility) {
        this.viewActivityVisibility = visibility;
    }

    public void setDeleteVisibility(int visibility) {
        this.deleteVisibility = visibility;
    }

    public void setOnDeleteButtonClickListener(NxlItemHelper.OnDeleteButtonClickListener listener) {
        this.mListener = listener;
    }

    public void setOnMarkOfflineClickListener(OnMarkOfflineClickListener listener) {
        this.mOnMarkOfflineClickListener = listener;
    }

    public void setOnShareItemClickListener(OnShareItemClickListener listener) {
        this.mOnShareItemClickListener = listener;
    }

    public void setOnModifyRightsClickListener(OnModifyRightsClickListener listener) {
        this.mOnModifyRightsClickListener = listener;
    }

    public void setPosition(int pos) {
        this.mPosition = pos;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCtx = context;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (isAdded()) {
            return;
        }
        super.show(manager, tag);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_project_files_context_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mFile == null) {
            return;
        }

        TextView tvFileName = view.findViewById(R.id.project_files_menu_file_name);
        Button btViewFile = view.findViewById(R.id.project_files_menu_view_file);
        Button btViewFileInfo = view.findViewById(R.id.project_files_menu_view_file_info);
        Button btAddToProject = view.findViewById(R.id.bt_add_to_project);
        Button btViewActivity = view.findViewById(R.id.project_files_menu_view_activity);
        Button btMarkAsOffline = view.findViewById(R.id.project_files_menu_mark_offline);
        Button btShare = view.findViewById(R.id.bt_share);
        Button btModifyRights = view.findViewById(R.id.bt_modify_rights);
        Button btDelete = view.findViewById(R.id.project_files_menu_delete);

        tvFileName.setText(mFile.getName());

        if (!isOwnerByMe) {
            btViewActivity.setVisibility(View.GONE);
            btDelete.setVisibility(View.GONE);
        } else {
            btViewActivity.setVisibility(viewActivityVisibility);
            btDelete.setVisibility(deleteVisibility);
        }

        if (mFile.isFolder()) {
            btViewFile.setVisibility(View.GONE);
            btViewFileInfo.setVisibility(View.GONE);
            btAddToProject.setVisibility(View.GONE);
            btViewActivity.setVisibility(View.GONE);
            btShare.setVisibility(View.GONE);
            btModifyRights.setVisibility(View.GONE);
            btMarkAsOffline.setVisibility(View.GONE);
        } else {
            btAddToProject.setVisibility(View.GONE);
            if (offlineView) {
                btAddToProject.setVisibility(View.GONE);
                btShare.setVisibility(View.GONE);
                btModifyRights.setVisibility(View.GONE);
            } else {
                try {
                    IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
                    if (rmUser.isProjectAdmin()) {
                        btModifyRights.setVisibility(View.VISIBLE);
                    }
                } catch (InvalidRMClientException e) {
                    e.printStackTrace();
                }
            }
            btMarkAsOffline.setText(((NxlDoc) mFile).isOffline() ?
                    mCtx.getString(R.string.offlined) :
                    mCtx.getString(R.string.Make_available_offline));

            try {
                if (!mOfflineFilter.accept(mFile.getName())) {
                    btMarkAsOffline.setEnabled(false);
                }
            } catch (OfflineException e) {
                btMarkAsOffline.setEnabled(false);
            }

            if (mFile instanceof SharedWithBase) {
                btShare.setText(R.string.Re_share);
                btModifyRights.setVisibility(View.GONE);
                btViewActivity.setVisibility(View.GONE);
                btDelete.setVisibility(View.GONE);
            }
        }

        btViewFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                viewFile();
            }
        });

        btViewFileInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                viewFileInfo();
            }
        });
        btAddToProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                addToProject();
            }
        });
        btMarkAsOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                markAsOffline(((Button) v).getText().toString().equals(mCtx.getString(R.string.offlined)));
            }
        });

        btViewActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                viewActivity();
            }
        });
        btShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                shareToPerson();
            }
        });
        if (btModifyRights.getVisibility() == View.VISIBLE) {
            btModifyRights.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (mOnModifyRightsClickListener != null) {
                        mOnModifyRightsClickListener.onModifyRights(mFile, mPosition);
                    }
                }
            });
        }
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                showDeleteDialog();
            }
        });
    }

    private void viewFile() {
        NxlItemHelper.viewFile(mCtx, mProjectId, mProjectName, mFile);
    }

    private void viewFileInfo() {
        NxlItemHelper.viewFileInfo(mCtx, (IFileInfo) mFile);
    }

    private void addToProject() {
        if (mOnShareItemClickListener != null) {
            mOnShareItemClickListener.onAddToProjectItemClick(mFile, mPosition);
        }
    }

    private void viewActivity() {
        NxlDoc doc = (NxlDoc) mFile;
        NxlItemHelper.viewActivity(mCtx, doc.getName(), doc.getDuid());
    }

    private void markAsOffline(boolean offline) {
        if (mOnMarkOfflineClickListener != null) {
            mOnMarkOfflineClickListener.onClick(mFile, offline, mPosition);
        }
    }

    private void shareToPerson() {
        if (mOnShareItemClickListener != null) {
            mOnShareItemClickListener.onShareToPersonItemClick(mFile, mPosition);
        }
    }

    private void showDeleteDialog() {
        NxlItemHelper.showDeleteDialog(mCtx, mFile,
                new NxlItemHelper.OnDeleteButtonClickListener() {
                    @Override
                    public void onClick(INxlFile f) {
                        if (mListener != null) {
                            mListener.onClick(f);
                        }
                    }
                });
    }

    public interface OnMarkOfflineClickListener {
        void onClick(INxlFile f, boolean offline, int pos);
    }

    public interface OnShareItemClickListener {
        void onAddToProjectItemClick(INxlFile f, int pos);

        void onShareToPersonItemClick(INxlFile f, int pos);
    }

    public interface OnModifyRightsClickListener {
        void onModifyRights(INxlFile f, int pos);
    }
}
