package com.skydrm.rmc.ui.common;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.base.NxlFileBase;
import com.skydrm.rmc.datalayer.repo.base.SharedWithBase;
import com.skydrm.rmc.datalayer.repo.library.LibraryFile;
import com.skydrm.rmc.datalayer.repo.library.LibraryNode;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.datalayer.repo.project.ProjectFile;
import com.skydrm.rmc.datalayer.repo.project.ProjectNode;
import com.skydrm.rmc.datalayer.repo.project.SharedWithProjectFile;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeFile;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceFile;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceNode;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuLayout;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.ViewUtils;
import com.skydrm.rmc.utils.commonUtils.IconHelper;
import com.skydrm.rmc.utils.commonUtils.StringUtils;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NxlAdapter extends RecyclerView.Adapter<NxlAdapter.ViewHolder> implements IDestroyable {
    public static final int TYPE_DELETE = 0;
    public static final int TYPE_SHARE = 1;
    public static final int TYPE_MANAGE = 2;
    public static final int TYPE_INFO = 3;
    public static final int TYPE_VIEW_ACTIVITY = 4;

    private static final int TYPE_NORMAL_ITEM = 1;
    private static final int TYPE_GROUP_ITEM = 2;

    private Context mCtx;
    private List<NxlFileItem> mData = new ArrayList<>();

    private boolean isCreatedByMe;
    private boolean isSelectFolderMode;
    private boolean disableLeftSwipeMenu;
    private boolean disableRightSwipeMenu;

    private OnItemClickListener mOnItemClickListener;
    private OnMenuToggleListener mOnMenuToggleListener;
    private OnLeftMenuItemClickListener mOnLeftMenuItemClickListener;
    private OnRightMenuItemClickListener mOnRightMenuItemClickListener;

    private int mLBt01Type = TYPE_DELETE;
    private int mRBt01Type = TYPE_SHARE;
    private int mRBt02Type = TYPE_VIEW_ACTIVITY;

    private INxlFile mWorkingFile;
    private INxlFile mClickedFolder;
    private String mWorkingFileId;
    private String mWorkingPathDisplay;
    private String mPathId = "/";
    private String mPathDisplay = "/";

    public NxlAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public INxlFile getFolder() {
        return mClickedFolder;
    }

    public INxlFile getWorkingFile() {
        return mWorkingFile;
    }

    public String getWorkingFileId() {
        return mWorkingFileId;
    }

    public String getWorkingPathDisplay() {
        return mWorkingPathDisplay;
    }

    public String getPathId() {
        return mPathId;
    }

    public String getPathDisplay() {
        return mPathDisplay;
    }

    public void setPathId(String pathId) {
        this.mPathId = pathId;
    }

    public void setWorkingFileId(String workingFileId) {
        this.mWorkingFileId = workingFileId;
    }

    public void setPathDisplay(String pathDisplay) {
        this.mPathDisplay = pathDisplay;
    }

    public void setWorkingPathDisplay(String pathDisplay) {
        this.mWorkingPathDisplay = pathDisplay;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnMenuToggleListener(OnMenuToggleListener listener) {
        this.mOnMenuToggleListener = listener;
    }

    public void setOnLeftMenuItemClickListener(OnLeftMenuItemClickListener listener) {
        this.mOnLeftMenuItemClickListener = listener;
    }

    public void setOnRightMenuItemClickListener(OnRightMenuItemClickListener listener) {
        this.mOnRightMenuItemClickListener = listener;
    }

    public void removeOnItemClickListener() {
        this.mOnItemClickListener = null;
    }

    public void removeOnMenuToggleListener() {
        this.mOnMenuToggleListener = null;
    }

    public void removeOnLeftMenuItemClickListener() {
        this.mOnLeftMenuItemClickListener = null;
    }

    public void removeOnRightMenuItemClickListener() {
        this.mOnRightMenuItemClickListener = null;
    }

    public void setData(List<NxlFileItem> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setCreatedByMe(boolean createdByMe) {
        isCreatedByMe = createdByMe;
    }

    public void setSelectFolderMode(boolean selectFolderMode) {
        isSelectFolderMode = selectFolderMode;
    }

    public void setDisableLeftSwipeMenu(boolean disable) {
        this.disableLeftSwipeMenu = disable;
    }

    public void setDisableRightSwipeMenu(boolean disable) {
        this.disableRightSwipeMenu = disable;
    }

    public void setFavoriteStatus(int pos, boolean favorite) {
        if (pos < 0 || pos >= mData.size()) {
            return;
        }
        for (int i = 0; i < mData.size(); i++) {
            if (i == pos) {
                NxlFileItem item = mData.get(i);
                NxlDoc doc = (NxlDoc) item.getNxlFile();
                doc.setFavorite(favorite);
                notifyItemChanged(pos);
                break;
            }
        }
    }

    public void setOfflineStatus(int pos, boolean offline) {
        if (pos < 0 || pos >= mData.size()) {
            return;
        }
        for (int i = 0; i < mData.size(); i++) {
            if (i == pos) {
                NxlFileItem item = mData.get(i);
                NxlDoc doc = (NxlDoc) item.getNxlFile();
                doc.setOffline(offline);
                notifyItemChanged(pos);
                break;
            }
        }
    }

    public void setOfflineStatus(INxlFile f, boolean offline) {
        if (mData == null || mData.size() == 0) {
            return;
        }
        if (f == null) {
            return;
        }
        int pos = -1;
        for (int i = 0; i < mData.size(); i++) {
            INxlFile nxlFile = mData.get(i).getNxlFile();
            if (nxlFile.getPathId().equals(f.getPathId())) {
                NxlDoc doc = (NxlDoc) nxlFile;
                doc.setOffline(offline);
                pos = i;
                break;
            }
        }
        if (pos != -1) {
            notifyItemChanged(pos);
        }
    }

    public void setOperationStatus(int pos, int status) {
        if (pos < 0 || pos >= mData.size()) {
            return;
        }
        for (int i = 0; i < mData.size(); i++) {
            if (i == pos) {
                NxlFileItem item = mData.get(i);
                NxlDoc f = (NxlDoc) item.getNxlFile();
                f.setOperationStatus(status);
                notifyItemChanged(pos);
                break;
            }
        }
    }

    public void setOperationStatus(INxlFile f, int status) {
        if (mData == null || mData.size() == 0) {
            return;
        }
        if (f == null) {
            return;
        }
        int pos = -1;
        for (int i = 0; i < mData.size(); i++) {
            INxlFile nxlFile = mData.get(i).getNxlFile();
            if (f.getPathId().equals(nxlFile.getPathId())) {
                NxlDoc doc = (NxlDoc) nxlFile;
                doc.setOperationStatus(status);
                pos = i;
                break;
            }
        }
        if (pos != -1) {
            notifyItemChanged(pos);
        }
    }

    public void setDeleted(int pos) {
        if (pos < 0 || pos >= mData.size()) {
            return;
        }
        for (int i = 0; i < mData.size(); i++) {
            if (i == pos) {
                NxlFileItem item = mData.get(i);
                if (item == null) {
                    continue;
                }
                INxlFile nxlFile = item.getNxlFile();
                if (nxlFile == null) {
                    continue;
                }
                if (nxlFile instanceof MyVaultFile) {
                    MyVaultFile doc = (MyVaultFile) nxlFile;
                    doc.setDeleted(true);
                    doc.setRevoked(true);
                    notifyItemChanged(pos);
                    break;
                }
            }
        }
    }

    public void removeItem(int pos) {
        if (pos < 0 || pos >= mData.size()) {
            return;
        }
        mData.remove(pos);
        notifyItemRemoved(pos);
    }

    public void removeItem(INxlFile file) {
        if (file == null) {
            return;
        }
        NxlFileItem removed = null;
        for (NxlFileItem item : mData) {
            if (item == null) {
                continue;
            }
            if (item.getNxlFile().getPathId().equals(file.getPathId())) {
                removed = item;
                break;
            }
        }
        if (removed != null) {
            mData.remove(removed);
        }

        notifyDataSetChanged();
    }

    public void removeItem(String pathId) {
        if (pathId == null || pathId.isEmpty()) {
            return;
        }
        NxlFileItem removed = null;
        for (NxlFileItem item : mData) {
            if (item == null) {
                continue;
            }
            if (item.getNxlFile().getPathId().equals(pathId)) {
                removed = item;
                break;
            }
        }
        if (removed != null) {
            mData.remove(removed);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_GROUP_ITEM;
        } else {
            String currentTitle = mData.get(position).getTitle();
            boolean isDifferent = !mData.get(position - 1).getTitle().equals(currentTitle);
            return isDifferent ? TYPE_GROUP_ITEM : TYPE_NORMAL_ITEM;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_NORMAL_ITEM:
                return new NormalItemViewHolder(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_nxl_normal, parent, false));
            case TYPE_GROUP_ITEM:
                return new GroupItemViewHolder(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_nxl_group, parent, false));
        }
        throw new IllegalArgumentException("Unrecognized view type " + viewType + "found.");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.bandData(mData.get(position));

        NormalItemViewHolder itemViewHolder = (NormalItemViewHolder) holder;
        boolean isMenuOpen = itemViewHolder.mSML != null &&
                itemViewHolder.mSML.isMenuOpen();

        if (isMenuOpen) {
            itemViewHolder.mSML.smoothCloseMenu();
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onReleaseResource() {
        if (mOnItemClickListener != null) {
            mOnItemClickListener = null;
        }
        if (mOnMenuToggleListener != null) {
            mOnMenuToggleListener = null;
        }
        if (mOnLeftMenuItemClickListener != null) {
            mOnLeftMenuItemClickListener = null;
        }
        if (mOnRightMenuItemClickListener != null) {
            mOnRightMenuItemClickListener = null;
        }
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bandData(NxlFileItem item);
    }

    class NormalItemViewHolder extends ViewHolder {
        private final SwipeMenuLayout mSML;
        //left menu
        private final ImageButton mIbLeft_01;

        //right menu
        private final Button mBtRight_01;
        private final Button mBtRight_02;

        //content menu
        private final ImageView mIvIcon;
        private final ImageView mIvFavoriteFlag;
        private final ImageView mIvOfflineFlag;
        private final TextView mTvName;
        private final TextView mTvSize;
        private final TextView mTvDate;
        private final TextView mTvDrivePath;
        private final TextView mTvSharedWith;
        private final ImageView mIvMenu;

        private final ImageView mIvOfflineStatus;
        private final TextView mTvOfflineStatus;

        NormalItemViewHolder(View itemView) {
            super(itemView);
            mSML = itemView.findViewById(R.id.swipe_menu_layout);
            //left
            mIbLeft_01 = itemView.findViewById(R.id.ib_left_01);
            //right
            mBtRight_01 = itemView.findViewById(R.id.bt_right_01);
            mBtRight_02 = itemView.findViewById(R.id.bt_right_02);
            //content
            mIvIcon = itemView.findViewById(R.id.iv_icon);
            mIvFavoriteFlag = itemView.findViewById(R.id.iv_favorite_flag);
            mIvOfflineFlag = itemView.findViewById(R.id.iv_offline_flag);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvSize = itemView.findViewById(R.id.tv_size);
            mTvDate = itemView.findViewById(R.id.tv_date);
            mTvDrivePath = itemView.findViewById(R.id.tv_drive_path);
            mTvSharedWith = itemView.findViewById(R.id.tv_shared_with);
            mIvMenu = itemView.findViewById(R.id.iv_menu);

            mIvOfflineStatus = itemView.findViewById(R.id.iv_offline_status);
            mTvOfflineStatus = itemView.findViewById(R.id.tv_offline_status);
            initListener();
        }

        private void initListener() {
            mSML.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getLayoutPosition();
                    if (pos == -1) {
                        return;
                    }
                    INxlFile f = mData.get(pos).getNxlFile();
                    if (f == null) {
                        return;
                    }
                    mWorkingFile = f;
                    mWorkingFileId = f.getPathId();
                    if (f.isFolder()) {
                        mClickedFolder = f;
                        mPathId = f.getPathId();
                        mPathDisplay = f.getPathDisplay();
                    }
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(f, pos);
                    }
                }
            });
            mIvMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getLayoutPosition();
                    if (pos == -1) {
                        return;
                    }
                    INxlFile f = mData.get(pos).getNxlFile();
                    if (f == null) {
                        return;
                    }
                    mWorkingFile = f;
                    mWorkingFileId = f.getPathId();
                    if (mOnMenuToggleListener != null) {
                        mOnMenuToggleListener.onMenuToggle(f, pos);
                    }
                }
            });
            if (!disableLeftSwipeMenu) {
                mIbLeft_01.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeMenuIfNecessary();
                        final int pos = getLayoutPosition();
                        if (pos == -1) {
                            return;
                        }
                        final INxlFile f = mData.get(pos).getNxlFile();
                        if (f == null) {
                            return;
                        }
                        mWorkingFile = f;
                        mWorkingFileId = f.getPathId();
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mOnLeftMenuItemClickListener != null) {
                                    mOnLeftMenuItemClickListener.onButton01Click(f, pos, mLBt01Type);
                                }
                            }
                        }, 200);
                    }
                });
            }
            if (!disableRightSwipeMenu) {
                mBtRight_01.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeMenuIfNecessary();
                        final int pos = getLayoutPosition();
                        if (pos == -1) {
                            return;
                        }
                        String text = mBtRight_01.getText().toString();
                        if (text.equals(mCtx.getString(R.string.manage))) {
                            mRBt01Type = TYPE_MANAGE;
                        } else if (text.equals(mCtx.getString(R.string.share))) {
                            mRBt01Type = TYPE_SHARE;
                        } else if (text.equals(mCtx.getString(R.string.info))) {
                            mRBt01Type = TYPE_INFO;
                        }
                        final INxlFile f = mData.get(pos).getNxlFile();
                        if (f == null) {
                            return;
                        }
                        mWorkingFile = f;
                        mWorkingFileId = f.getPathId();
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mOnRightMenuItemClickListener != null) {
                                    mOnRightMenuItemClickListener.onButton01Click(f, pos, mRBt01Type);
                                }
                            }
                        }, 200);
                    }
                });
                mBtRight_02.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeMenuIfNecessary();
                        final int pos = getLayoutPosition();
                        if (pos == -1) {
                            return;
                        }
                        final INxlFile f = mData.get(pos).getNxlFile();
                        if (f == null) {
                            return;
                        }
                        mWorkingFile = f;
                        mWorkingFileId = f.getPathId();
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mOnRightMenuItemClickListener != null) {
                                    mOnRightMenuItemClickListener.onButton02Click(f, pos, mRBt02Type);
                                }
                            }
                        }, 200);
                    }
                });
            }
        }

        @Override
        void bandData(NxlFileItem item) {
            INxlFile base = item.getNxlFile();

            String name = base.getName();
            if (base.isFolder()) {
                mIvIcon.setColorFilter(mCtx.getResources().getColor(R.color.main_drak_light));
                mIvIcon.setImageResource(R.drawable.icon_folder_black);
            } else {
                mIvIcon.setImageResource(IconHelper.getNxlIconResourceIdByExtension(name.toLowerCase()));
            }
            mTvName.setText(name);

            if (base instanceof ProjectFile) {
                bandProjectFileData((ProjectFile) base);
            } else if (base instanceof MyVaultFile) {
                bandMyVaultFileData((MyVaultFile) base);
            } else if (base instanceof SharedWithMeFile) {
                bandSharedWitheMeFileData((SharedWithMeFile) base);
            } else if (base instanceof ProjectNode) {
                bandProjectFileData((NxlFileBase) base);
            } else if (base instanceof WorkSpaceFile) {
                bandProjectFileData((WorkSpaceFile) base);
            } else if (base instanceof WorkSpaceNode) {
                bandProjectFileData((WorkSpaceNode) base);
            } else if (base instanceof SharedWithProjectFile) {
                bandSharedWithProjectFileData((SharedWithBase) base);
            } else if (base instanceof LibraryFile) {
                bandLibraryFileData(base);
            } else if (base instanceof LibraryNode) {
                bandLibraryFolderData(base);
            }
        }

        private void bandLibraryFolderData(INxlFile f) {
            mSML.setSwipeLeftEnable(!disableLeftSwipeMenu);
            mSML.setSwipeRightEnable(!disableRightSwipeMenu);
            mIvMenu.setVisibility(View.GONE);
            mTvSize.setVisibility(View.GONE);
            mTvDate.setText(TimeUtil.formatData(f.getLastModifiedTime()));
        }

        private void bandLibraryFileData(INxlFile f) {
            mIvIcon.setColorFilter(mCtx.getResources().getColor(android.R.color.transparent));
            mSML.setSwipeLeftEnable(!disableLeftSwipeMenu);
            mSML.setSwipeRightEnable(!disableRightSwipeMenu);
            mIvMenu.setVisibility(View.GONE);
            mTvSize.setVisibility(View.VISIBLE);
            mTvDate.setText(TimeUtil.formatData(f.getLastModifiedTime()));
            mTvSize.setText(FileUtils.transparentFileSize(((NxlDoc) f).getFileSize()));
        }

        private void bandProjectFileData(NxlFileBase f) {
            mTvDate.setText(TimeUtil.formatData(f.getLastModifiedTime()));
            mSML.setSwipeLeftEnable(isCreatedByMe & !disableLeftSwipeMenu & !isSelectFolderMode);
            mIvMenu.setVisibility(!isSelectFolderMode ? View.VISIBLE : View.INVISIBLE);
            mBtRight_01.setText(mCtx.getString(R.string.info));
            if (f.isFolder()) {
                resetBackgroundColor();
                mIvIcon.setColorFilter(mCtx.getResources().getColor(R.color.main_drak_light));
                mSML.setEnabled(true);
                mIvFavoriteFlag.setVisibility(View.GONE);
                mIvOfflineFlag.setVisibility(View.GONE);
                mIvOfflineStatus.setVisibility(View.GONE);
                mTvOfflineStatus.setVisibility(View.GONE);
                mSML.setSwipeRightEnable(false);
                mTvSize.setVisibility(View.GONE);
            } else {
                mSML.setSwipeRightEnable(!disableRightSwipeMenu & !isSelectFolderMode);
                mTvSize.setVisibility(View.VISIBLE);
                mTvSize.setText(FileUtils.transparentFileSize(((NxlDoc) f).getFileSize()));

                initOfflineStatus((NxlDoc) f);
            }
        }

        private void bandSharedWithProjectFileData(SharedWithBase f) {
            mSML.setSwipeLeftEnable(false);
            mSML.setSwipeRightEnable(false);
            String shareByProjectName = ((SharedWithProjectFile) f).getShareByProjectName();
            if (shareByProjectName == null || shareByProjectName.isEmpty()) {
                mTvDrivePath.setVisibility(View.GONE);
            } else {
                mTvDrivePath.setVisibility(View.VISIBLE);
                mTvDrivePath.setText(shareByProjectName);
            }
            mTvSize.setText(FileUtils.transparentFileSize(f.getFileSize()));
            mTvDate.setText(TimeUtil.formatData(f.getSharedDate()));
            mTvSharedWith.setVisibility(View.VISIBLE);
            mTvSharedWith.setText(f.getSharedBy());
            initOfflineStatus(f);
        }

        private void bandMyVaultFileData(MyVaultFile f) {
            mSML.setSwipeLeftEnable(!disableLeftSwipeMenu);
            mSML.setSwipeRightEnable(!disableRightSwipeMenu);
            mTvSize.setText(FileUtils.transparentFileSize(f.getFileSize()));
            mTvDate.setText(TimeUtil.getProtectTime(f.getSharedOn(), System.currentTimeMillis()));
            mTvDrivePath.setVisibility(View.VISIBLE);
            mTvDrivePath.setText(String.format("%s:%s", f.getSourceRepoName(), f.getSourceFilePathDisplay()));
            mIvFavoriteFlag.setVisibility(f.isFavorite() & !f.isDeleted() ? View.VISIBLE : View.GONE);
            String sharedWithStr = getSharedWithStr(f.getSharedWith());
            if (sharedWithStr.isEmpty()) {
                mTvSharedWith.setVisibility(View.GONE);
            } else {
                mTvSharedWith.setVisibility(View.VISIBLE);
                mTvSharedWith.setText(sharedWithStr);
            }
            if (f.isRevoked()) {
                //only in all protected files mode hide the manage button.
                if (f.isShared()) {
                    mBtRight_01.setText(mCtx.getString(R.string.manage));
                    mBtRight_01.setVisibility(View.VISIBLE);
                } else {
                    mBtRight_01.setVisibility(View.GONE);
                }
                if (f.isDeleted()) {
                    mIvOfflineFlag.setVisibility(View.GONE);
                    if (mBtRight_01.getVisibility() == View.VISIBLE) {
                        mBtRight_01.setText(mCtx.getString(R.string.manage));
                    }
                    mTvName.setText(StringUtils.getStrikelineSpan(f.getName()));
                    mSML.setSwipeLeftEnable(false);
                } else {
                    initOfflineStatus(f);
                }
                //Set the file icon to gray
                setupBackgroundColor(true, "#BFBFBF");
            } else {
                mBtRight_01.setVisibility(View.VISIBLE);
                if (f.isShared()) {
                    mBtRight_01.setText(mCtx.getString(R.string.manage));
                } else {
                    mBtRight_01.setText(mCtx.getString(R.string.share));
                }
                initOfflineStatus(f);
            }
        }

        private void bandSharedWitheMeFileData(SharedWithMeFile f) {
            mSML.setSwipeLeftEnable(false);
            mSML.setSwipeRightEnable(false);
            mTvDrivePath.setVisibility(View.GONE);
            mTvSize.setText(FileUtils.transparentFileSize(f.getFileSize()));
            mTvDate.setText(TimeUtil.formatData(f.getSharedDate()));
            mTvSharedWith.setVisibility(View.VISIBLE);
            mTvSharedWith.setText(f.getSharedBy());
            initOfflineStatus(f);
        }

        private void initOfflineStatus(NxlDoc doc) {
            if (doc.isOffline()) {
                resetBackgroundColor();
                mSML.setEnabled(true);
                mIvMenu.setEnabled(true);
                mIvOfflineStatus.setVisibility(View.GONE);
                mTvOfflineStatus.setVisibility(View.GONE);
                mIvOfflineFlag.setVisibility(View.VISIBLE);
                mIvOfflineFlag.setImageResource(R.drawable.available_offline_icon3);
            } else {
                int status = doc.getOperationStatus();
                if (status == INxlFile.PROCESS) {
                    setupBackgroundColor(false, "#BDBDBD");
                    mSML.setEnabled(false);
                    mIvMenu.setEnabled(false);

                    mIvOfflineFlag.setVisibility(View.VISIBLE);
                    mIvOfflineFlag.setImageResource(R.drawable.icon_offline_rotate);
                    mIvOfflineStatus.setVisibility(View.VISIBLE);
                    mTvOfflineStatus.setVisibility(View.VISIBLE);
                    mIvOfflineStatus.setImageResource(R.drawable.icon_offline_processing);
                    mTvOfflineStatus.setText(R.string.updating);
                    mTvOfflineStatus.setTextColor(Color.parseColor("#BDBDBD"));
                } else if (status == INxlFile.MARK_ERROR) {
                    //need update.
                    resetBackgroundColor();
                    mSML.setEnabled(true);
                    mIvMenu.setEnabled(true);

                    mIvOfflineFlag.setVisibility(View.GONE);
                    mIvOfflineStatus.setVisibility(View.GONE);
                    mTvOfflineStatus.setVisibility(View.GONE);
                } else {
                    resetBackgroundColor();
                    mSML.setEnabled(true);
                    mIvMenu.setEnabled(true);

                    mIvOfflineFlag.setVisibility(View.GONE);
                    mIvOfflineStatus.setVisibility(View.GONE);
                    mTvOfflineStatus.setVisibility(View.GONE);
                }
            }
        }

        private String getSharedWithStr(List<String> sharedWith) {
            if (sharedWith == null || sharedWith.size() == 0) {
                return "";
            }
            StringBuilder ret = new StringBuilder();
            int size = sharedWith.size();
            int count = 0;
            for (int i = 0; i < size; i++) {
                count++;
                if (count > 2) {
                    break;
                }
                String s = sharedWith.get(i);
                if (s.isEmpty()) {
                    continue;
                }
                ret.append(s);
                if (size > 2) {
                    if (i != 1) {
                        ret.append(", ");
                    }
                } else {
                    if (i != size - 1) {
                        ret.append(", ");
                    }
                }
            }

            if (size > 2) {
                ret.append(String.format(Locale.getDefault(), " + %d more", (size - 2)));
            }

            return ret.toString();
        }

        private void setupBackgroundColor(boolean revoke, String color) {
            mIvIcon.setColorFilter(Color.parseColor(color));
            mTvName.setTextColor(Color.parseColor(color));
            mTvSize.setTextColor(Color.parseColor(color));
            mTvDate.setTextColor(Color.parseColor(color));
            mIvMenu.setColorFilter(Color.parseColor(color));
            mTvDrivePath.setTextColor(Color.parseColor(color));
            mTvSharedWith.setTextColor(Color.parseColor(color));
            if (!revoke) {
                mIvMenu.setColorFilter(Color.parseColor(color));
            }
        }

        private void resetBackgroundColor() {
            mIvIcon.setColorFilter(mCtx.getResources().getColor(android.R.color.transparent));
            mTvName.setTextColor(mCtx.getResources().getColor(R.color.normal_text_color));
            mTvSize.setTextColor(mCtx.getResources().getColor(R.color.today_text_color));
            mTvDate.setTextColor(mCtx.getResources().getColor(R.color.sub_text_color));
            mTvDrivePath.setTextColor(mCtx.getResources().getColor(R.color.sub_text_color));
            mTvSharedWith.setTextColor(mCtx.getResources().getColor(R.color.sub_text_color));
            mIvMenu.setColorFilter(mCtx.getResources().getColor(android.R.color.transparent));
        }

        private void closeMenuIfNecessary() {
            if (mSML == null) {
                return;
            }
            if (!mSML.isMenuOpen()) {
                return;
            }
            mSML.smoothCloseMenu();
        }
    }


    class GroupItemViewHolder extends NormalItemViewHolder {
        private final TextView mTvTitle;

        GroupItemViewHolder(View itemView) {
            super(itemView);
            mTvTitle = itemView.findViewById(R.id.tv_title);
        }

        @Override
        void bandData(NxlFileItem item) {
            super.bandData(item);
            String title = item.getTitle();
            if (title == null || title.isEmpty()) {
                if (ViewUtils.isVisible(mTvTitle)) {
                    mTvTitle.setVisibility(View.GONE);
                }
            } else {
                if (ViewUtils.isGone(mTvTitle)) {
                    mTvTitle.setVisibility(View.VISIBLE);
                }
                mTvTitle.setText(title);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(INxlFile f, int pos);
    }

    public interface OnMenuToggleListener {
        void onMenuToggle(INxlFile f, int pos);
    }

    public interface OnLeftMenuItemClickListener {
        void onButton01Click(INxlFile f, int pos, @ButtonType int type);
    }

    public interface OnRightMenuItemClickListener {
        void onButton01Click(INxlFile f, int pos, @ButtonType int type);

        void onButton02Click(INxlFile f, int pos, @ButtonType int type);
    }

    @IntDef({TYPE_SHARE, TYPE_MANAGE, TYPE_INFO, TYPE_VIEW_ACTIVITY, TYPE_DELETE})
    @interface ButtonType {

    }
}
